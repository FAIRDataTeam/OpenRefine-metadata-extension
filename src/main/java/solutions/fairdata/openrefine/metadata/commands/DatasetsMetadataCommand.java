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
import solutions.fairdata.openrefine.metadata.commands.request.DatasetPostRequest;
import solutions.fairdata.openrefine.metadata.commands.response.DatasetPostResponse;
import solutions.fairdata.openrefine.metadata.commands.response.DatasetsMetadataResponse;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.dto.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.DatasetDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class DatasetsMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fdpUri = request.getParameter("fdpUri");
        String catalogUri = request.getParameter("catalogUri");
        Writer w = response.getWriter();

        logger.info("Retrieving Datasets metadata from catalog URI: " + catalogUri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, logger);
            CatalogDTO catalogDTO = fdpClient.getCatalogMetadata(catalogUri);
            ArrayList<DatasetDTO> datasetDTOs = new ArrayList<>();
            for (String datasetURI : catalogDTO.getDatasets()) {
                datasetDTOs.add(fdpClient.getDatasetMetadata(datasetURI));
            }

            logger.info("Datasets metadata retrieved from catalog: " + catalogUri);
            CommandUtils.objectMapper.writeValue(w, new DatasetsMetadataResponse(datasetDTOs));
        } catch (Exception e) {
            logger.error("Error while contacting FAIR Data Point: " + catalogUri + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e.getMessage()));
        } finally {
            w.flush();
            w.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatasetPostRequest datasetPostRequest = CommandUtils.objectMapper.readValue(request.getReader(), DatasetPostRequest.class);
        Writer w = response.getWriter();

        try {
            FairDataPointClient fdpClient = new FairDataPointClient(datasetPostRequest.getFdpUri(), datasetPostRequest.getToken(), logger);
            DatasetDTO datasetDTO = fdpClient.postDataset(datasetPostRequest.getDatasetDTO());

            CommandUtils.objectMapper.writeValue(w, new DatasetPostResponse(datasetDTO));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while creating catalog in FAIR Data Point: " + datasetPostRequest.getFdpUri() + " (" + e.getMessage() + ")");
            // TODO: post error
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e.getMessage()));
        } finally {
            w.flush();
            w.close();
        }
    }
}
