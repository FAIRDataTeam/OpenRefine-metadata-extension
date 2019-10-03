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

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.refine.commands.Command;
import com.google.refine.util.ParsingUtilities;

import nl.dtl.fairmetadata4j.model.FDPMetadata;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class FDPMetadataCommand extends Command {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fdpUri = request.getParameter("fdpUri");
        Writer w = response.getWriter();
        JsonGenerator writer = ParsingUtilities.mapper.getFactory().createGenerator(w);

        logger.info("Retrieving FAIR Data Point metadata from URI: " + fdpUri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(logger);
            FDPMetadata fairDataPointMetadata = fdpClient.getFairDataPointMetadata(fdpUri);

            logger.info("FAIR Data Point metadata retrieved: " + fdpUri);
            writer.writeStartObject();
            writer.writeStringField("status", "ok");
            writer.writeStringField("message", "connect-fdp-command/success");
            writer.writeObjectField("fdpMetadata", fairDataPointMetadata);
            writer.writeEndObject();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error("Error while contacting FAIR Data Point: " + fdpUri + " (" + e.getMessage() + ")");
            writer.writeStartObject();
            writer.writeStringField("status", "error");
            writer.writeStringField("message", "connect-fdp-command/error");
            writer.writeStringField("exception", e.getMessage());
            writer.writeEndObject();
            writer.flush();
            writer.close();
        } finally {
            w.flush();
            w.close();
        }
    }
}
