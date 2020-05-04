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
import solutions.fairdata.openrefine.metadata.commands.response.TypehintsResponse;
import solutions.fairdata.openrefine.metadata.dto.TypehintDTO;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.typehinting.LanguageTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.LicenseTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.MediaTypeTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.ThemeTypehintService;
import solutions.fairdata.openrefine.metadata.typehinting.TypehintService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Command for getting typehints of certain type
 *
 * It holds information about defined typehint services. Then can return a list
 * of typehints based on the name specifying the type and user query for filtering
 * the typehints.
 */
public class TypehintsCommand extends Command {

    private static HashMap<String, TypehintService> typehintServices = new HashMap<>();

    static {
        typehintServices.put("language", new LanguageTypehintService());
        typehintServices.put("license", new LicenseTypehintService());
        typehintServices.put("theme", new ThemeTypehintService());
        typehintServices.put("mediaType", new MediaTypeTypehintService());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String query = request.getParameter("query");
        if (query == null) {
            query = "";
        }
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        pa.reportInfo(EventSource.TYPEHINTS, "Retrieving typehints \"" + name + "\" with query \"" + query + "\"");
        try {
            List<TypehintDTO> typehints = null;
            String source = "none";
            TypehintService typehintService = typehintServices.get(name);
            if (typehintService != null) {
                typehints = typehintService.getTypehints(query);
                source = typehintService.getSource();
                typehints.sort(Comparator.comparing(TypehintDTO::getTitle));
                pa.reportInfo(EventSource.TYPEHINTS, "Retrieved " + typehints.size() + " typehint items");
            } else {
                pa.reportWarning(EventSource.TYPEHINTS, "Unknown typehints requested: " + name);
            }

            CommandUtils.objectMapper.writeValue(w, new TypehintsResponse(source, typehints));
        } catch (Exception e) {
            pa.reportError(EventSource.TYPEHINTS,"Error while retrieving typehints: " + name);
            pa.reportTrace(EventSource.TYPEHINTS, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }

    }
}
