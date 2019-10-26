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
import solutions.fairdata.openrefine.metadata.commands.response.DistributionsMetadataResponse;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.dto.DatasetDTO;
import solutions.fairdata.openrefine.metadata.dto.DistributionDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class DistributionsMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fdpUri = request.getParameter("fdpUri");
        String datasetUri = request.getParameter("datasetUri");
        Writer w = response.getWriter();

        logger.info("Retrieving Distributions metadata from dataset URI: " + datasetUri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, logger);
            DatasetDTO datasetDTO = fdpClient.getDatasetMetadata(datasetUri);
            ArrayList<DistributionDTO> distributionDTOs = new ArrayList<>();
            for (String distributionURI : datasetDTO.getDistributions()) {
                distributionDTOs.add(fdpClient.getDistributionMetadata(distributionURI));
            }

            logger.info("Distributions metadata retrieved from dataset: " + datasetUri);
            CommandUtils.objectMapper.writeValue(w, new DistributionsMetadataResponse(distributionDTOs));
        } catch (Exception e) {
            logger.error("Error while contacting FAIR Data Point: " + datasetUri + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e.getMessage()));
        } finally {
            w.flush();
            w.close();
        }
    }
}
