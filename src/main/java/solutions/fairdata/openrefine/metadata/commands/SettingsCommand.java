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
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.config.SettingsResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class SettingsCommand extends Command {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Writer w = CommandUtils.prepareWriter(response);

        try {
            CommandUtils.objectMapper.writeValue(w, new SettingsResponse(MetadataModuleImpl.getInstance().getSettings()));
        } catch (Exception e) {
            logger.error("Error while getting Settings");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
