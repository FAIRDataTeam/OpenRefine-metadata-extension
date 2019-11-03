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
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.TypehintsResponse;
import solutions.fairdata.openrefine.metadata.dto.TypehintDTO;
import solutions.fairdata.openrefine.metadata.typehinting.LanguageTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.LicenseTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.TypehintService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TypehintsCommand extends Command {

    private static HashMap<String, TypehintService> typehintServices = new HashMap<>();

    static {
        typehintServices.put("language", new LanguageTypehintService());
        typehintServices.put("license", new LicenseTypehintService());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String query = request.getParameter("query");
        if (query == null) {
            query = "";
        }
        Writer w = CommandUtils.prepareWriter(response);

        try {
            List<TypehintDTO> typehints = null;
            String source = "none";
            TypehintService typehintService = typehintServices.get(name);
            if (typehintService != null) {
                typehints = typehintService.getTypehints(query);
                source = typehintService.getSource();
                typehints.sort(Comparator.comparing(TypehintDTO::getTitle));
            }

            CommandUtils.objectMapper.writeValue(w, new TypehintsResponse(source, typehints));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while preparing typehints of name: " + name + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }

    }
}
