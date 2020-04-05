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
import solutions.fairdata.openrefine.metadata.commands.request.metadata.CatalogPostRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.CatalogPostResponse;
import solutions.fairdata.openrefine.metadata.commands.response.metadata.CatalogsMetadataResponse;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.FDPMetadataDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Command for handling Catalog metadata layer
 *
 * It can list all visible catalogs (GET) or create a new catalog (POST)
 * in FAIR Data Point specified by its URI.
 */
public class CatalogsMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fdpUri = request.getParameter("fdpUri");
        Writer w = CommandUtils.prepareWriter(response);

        logger.info("Retrieving Catalogs metadata from FDP URI: " + fdpUri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, logger);
            FDPMetadataDTO fdpMetadataDTO = fdpClient.getFairDataPointMetadata();
            ArrayList<CatalogDTO> catalogDTOs = new ArrayList<>();
            for (String catalogURI : fdpMetadataDTO.getChildren()) {
                catalogDTOs.add(fdpClient.getCatalogMetadata(catalogURI));
            }

            logger.info("Catalogs metadata retrieved from FDP: " + fdpUri);
            CommandUtils.objectMapper.writeValue(w, new CatalogsMetadataResponse(catalogDTOs));
        } catch (Exception e) {
            logger.error("Error while contacting FAIR Data Point: " + fdpUri + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CatalogPostRequest catalogPostRequest = CommandUtils.objectMapper.readValue(request.getReader(), CatalogPostRequest.class);
        Writer w = CommandUtils.prepareWriter(response);

        try {
            FairDataPointClient fdpClient = new FairDataPointClient(catalogPostRequest.getFdpUri(), catalogPostRequest.getToken(), logger);
            CatalogDTO catalogDTO = fdpClient.postCatalog(catalogPostRequest.getCatalog());

            CommandUtils.objectMapper.writeValue(w, new CatalogPostResponse(catalogDTO));
        } catch (Exception e) {
            logger.error("Error while creating catalog in FAIR Data Point: " + catalogPostRequest.getFdpUri() + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
