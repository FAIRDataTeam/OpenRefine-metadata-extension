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
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.FDPMetadataResponse;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.metadata.FDPMetadataDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Command for handling Repository/FDP metadata layer
 *
 * It can return metadata (GET) of FAIR Data Point (Repository) itself specified
 * by its URI.
 */
public class FDPMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fdpUri = request.getParameter("fdpUri");
        String repositoryUri = request.getParameter("repositoryUri");
        String token = request.getParameter("token");
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));
        FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, token, pa);

        pa.reportInfo(EventSource.FDP_METADATA, "Retrieving FAIR Data Point (repository) metadata from URI: " + fdpUri);
        try {
            FDPMetadataDTO fdpMetadataDTO = fdpClient.getFairDataPointMetadata(repositoryUri);
            pa.reportInfo(EventSource.FDP_METADATA, "FAIR Data Point (repository) metadata retrieved from: " + fdpUri);
            CommandUtils.objectMapper.writeValue(w, new FDPMetadataResponse("connect-fdp-command/success", fdpMetadataDTO));
        } catch (Exception e) {
            pa.reportError(EventSource.FDP_METADATA, "Error while getting FAIR Data Point metadata from: " + fdpUri);
            pa.reportTrace(EventSource.FDP_METADATA, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
