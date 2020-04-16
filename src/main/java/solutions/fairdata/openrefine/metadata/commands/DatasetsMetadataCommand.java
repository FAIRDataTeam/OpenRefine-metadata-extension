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
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.commands.request.metadata.DatasetPostRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.DatasetPostResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.DatasetsMetadataResponse;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Command for handling Dataset metadata layer
 *
 * It can list all visible dataset (GET) or create a new dataset (POST)
 * in Catalog (of some FDP) specified by its URI.
 */
public class DatasetsMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fdpUri = request.getParameter("fdpUri");
        String catalogUri = request.getParameter("catalogUri");
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        pa.reportInfo(EventSource.FDP_METADATA, "Retrieving datasets from catalog: " + catalogUri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, logger);
            CatalogDTO catalogDTO = fdpClient.getCatalogMetadata(catalogUri);
            ArrayList<DatasetDTO> datasetDTOs = new ArrayList<>();
            for (String datasetURI : catalogDTO.getChildren()) {
                datasetDTOs.add(fdpClient.getDatasetMetadata(datasetURI));
            }

            pa.reportDebug(EventSource.FDP_METADATA, "Datasets retrieved from catalog: " + catalogUri);
            CommandUtils.objectMapper.writeValue(w, new DatasetsMetadataResponse(datasetDTOs));
        } catch (Exception e) {
            pa.reportError(EventSource.FDP_METADATA, "Error while getting datasets from catalog: " + catalogUri);
            pa.reportTrace(EventSource.FDP_METADATA, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DatasetPostRequest datasetPostRequest = CommandUtils.objectMapper.readValue(request.getReader(), DatasetPostRequest.class);
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        try {
            pa.reportInfo(EventSource.FDP_METADATA, "Creating dataset in catalog: " + datasetPostRequest.getDataset().getParent());
            FairDataPointClient fdpClient = new FairDataPointClient(datasetPostRequest.getFdpUri(), datasetPostRequest.getToken(), logger);
            DatasetDTO datasetDTO = fdpClient.postDataset(datasetPostRequest.getDataset());

            pa.reportDebug(EventSource.FDP_METADATA, "Dataset created: " + datasetDTO.getIri());
            CommandUtils.objectMapper.writeValue(w, new DatasetPostResponse(datasetDTO));
        } catch (Exception e) {
            pa.reportError(EventSource.FDP_METADATA, "Error while creating dataset in catalog: " + datasetPostRequest.getDataset().getParent());
            pa.reportTrace(EventSource.FDP_METADATA, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
