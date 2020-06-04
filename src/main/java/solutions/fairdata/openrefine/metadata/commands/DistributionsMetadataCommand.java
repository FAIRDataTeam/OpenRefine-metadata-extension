/**
 * The MIT License
 * Copyright © 2019 FAIR Data Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.fairdata.openrefine.metadata.commands;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.commands.request.metadata.DistributionPostRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.DistributionPostResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.DistributionsMetadataResponse;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;
import solutions.fairdata.openrefine.metadata.dto.project.ProjectMetadataRecordDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Command for handling Distribution metadata layer
 *
 * It can list all visible distribution (GET) or create a new distribution (POST)
 * in Dataset (of some Catalog) specified by its URI.
 */
public class DistributionsMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fdpUri = request.getParameter("fdpUri");
        String datasetUri = request.getParameter("datasetUri");
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));
        FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, pa);

        pa.reportInfo(EventSource.FDP_METADATA, "Retrieving distributions from dataset: " + datasetUri);
        try {
            DatasetDTO datasetDTO = fdpClient.getDatasetMetadata(datasetUri);
            ArrayList<DistributionDTO> distributionDTOs = new ArrayList<>();
            for (String distributionURI : datasetDTO.getChildren()) {
                distributionDTOs.add(fdpClient.getDistributionMetadata(distributionURI));
            }

            pa.reportDebug(EventSource.FDP_METADATA, "Distributions retrieved from dataset: " + datasetUri);
            CommandUtils.objectMapper.writeValue(w, new DistributionsMetadataResponse(distributionDTOs));
        } catch (Exception e) {
            pa.reportError(EventSource.FDP_METADATA, "Error while getting distributions from dataset: " + datasetUri);
            pa.reportTrace(EventSource.FDP_METADATA, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DistributionPostRequest distributionPostRequest = CommandUtils.objectMapper.readValue(request.getReader(), DistributionPostRequest.class);
        Writer w = CommandUtils.prepareWriter(response);
        Project project = getProject(request);
        ProjectAudit pa = new ProjectAudit(project);
        FairDataPointClient fdpClient = new FairDataPointClient(distributionPostRequest.getFdpUri(), distributionPostRequest.getToken(), pa);

        try {
            pa.reportDebug(EventSource.FDP_METADATA, "Creating distribution in dataset: " + distributionPostRequest.getDistribution().getParent());
            DistributionDTO distributionDTO = fdpClient.postDistribution(distributionPostRequest.getDistribution());
            MetadataModuleImpl.getProjectModel(project).addProjectMetadata(ProjectMetadataRecordDTO.createFor(distributionDTO));
            MetadataModuleImpl.forceSaveProject(project);
            pa.reportDebug(EventSource.FDP_METADATA, "Distribution created: " + distributionDTO.getIri());
            CommandUtils.objectMapper.writeValue(w, new DistributionPostResponse(distributionDTO));
        } catch (Exception e) {
            pa.reportError(EventSource.FDP_METADATA, "Error while creating distribution in dataset: " + distributionPostRequest.getDistribution().getParent());
            pa.reportTrace(EventSource.FDP_METADATA, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
