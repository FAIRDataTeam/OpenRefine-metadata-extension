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
package org.fair.openrefine.metadata.commands;

import com.google.refine.commands.Command;
import org.fair.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectFDPCommand extends Command {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getParameter("uri");

        logger.info("Contacting FAIR Data Point on URI: " + uri);
        try {
            FairDataPointClient fdpClient = new FairDataPointClient(uri);
            respond(response, "ok", fdpClient.getFairDataPointMetadata().getTitle().toString());
            // TODO: return as JSON object and log
        } catch (Exception e) {
            logger.error("Error while contacting FAIR Data Point: " + uri + " (" + e.getMessage() + ")");
            respond(response, "error", "Failed to contact FAIR Data Point: " + uri);
        }
    }
}
