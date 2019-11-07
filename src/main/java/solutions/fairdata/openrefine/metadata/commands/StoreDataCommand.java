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
import com.google.refine.exporters.Exporter;
import com.google.refine.exporters.ExporterRegistry;
import solutions.fairdata.openrefine.metadata.commands.response.StoreDataInfoResponse;
import solutions.fairdata.openrefine.metadata.dto.ExportFormatDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreDataCommand extends Command {

    private static HashMap<String, ExportFormatDTO> formats = new HashMap<>();

    static {
        formats.put("csv", new ExportFormatDTO("csv", "OpenRefine", "csv"));
        formats.put("tsv", new ExportFormatDTO("tsv", "OpenRefine", "tsv"));
        formats.put("xls", new ExportFormatDTO("xls", "OpenRefine", "xls"));
        formats.put("xlsx", new ExportFormatDTO("xlsx", "OpenRefine", "xlsx"));
        formats.put("ods", new ExportFormatDTO("ods", "OpenRefine", "ods"));
        formats.put("html", new ExportFormatDTO("html", "OpenRefine", "html"));
        formats.put("sql", new ExportFormatDTO("sql", "OpenRefine", "sql"));
        formats.put("rdf", new ExportFormatDTO("rdf", "rdf-extension", "rdf"));
        formats.put("Turtle", new ExportFormatDTO("Turtle", "rdf-extension", "ttl"));
    }

    private static void updateFormats() {
        for (Map.Entry<String, ExportFormatDTO> format : formats.entrySet()) {
            final Exporter e = ExporterRegistry.getExporter(format.getKey());
            if (e != null) {
                format.getValue().setContentType(e.getContentType());
                format.getValue().setUsable(true);
            } else {
                format.getValue().setUsable(false);
            }
        }
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Writer w = CommandUtils.prepareWriter(response);
        updateFormats();

        CommandUtils.objectMapper.writeValue(w, new StoreDataInfoResponse(new ArrayList<>(formats.values())));

        w.flush();
        w.close();
    }
}
