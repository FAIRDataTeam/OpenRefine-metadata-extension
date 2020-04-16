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
import com.google.refine.model.Project;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.commands.request.config.SettingsRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.config.SettingsResponse;
import solutions.fairdata.openrefine.metadata.dto.ProjectInfoDTO;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.config.ProjectConfigDTO;
import solutions.fairdata.openrefine.metadata.dto.config.SettingsConfigDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Command for getting the extension settings
 */
public class SettingsCommand extends Command {

    private SettingsResponse createSettingsResponse(Project project) {
        SettingsConfigDTO settings = MetadataModuleImpl.getInstance().getSettingsDetails();
        ProjectConfigDTO projectData = MetadataModuleImpl.getProjectConfigDTO(project);
        ProjectInfoDTO projectInfo = MetadataModuleImpl.getInstance().getProjectInfo();
        return new SettingsResponse(settings, projectData, projectInfo);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Writer w = CommandUtils.prepareWriter(response);
        Project project = getProject(request);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        try {
            pa.reportDebug(EventSource.SETTINGS,"Retrieving current configuration bundle");
            CommandUtils.objectMapper.writeValue(w, createSettingsResponse(project));
        } catch (Exception e) {
            pa.reportError(EventSource.SETTINGS,"Error occurred while getting Settings");
            pa.reportTrace(EventSource.SETTINGS, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("connect-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        SettingsRequest settingsRequest = CommandUtils.objectMapper.readValue(request.getReader(), SettingsRequest.class);
        Project project = getProject(request);
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        try {
            pa.reportDebug(EventSource.SETTINGS,"Persisting project configuration");
            MetadataModuleImpl.setProjectConfigDTO(project, settingsRequest.getProjectData());
            CommandUtils.objectMapper.writeValue(w, createSettingsResponse(project));
        } catch (Exception e) {
            pa.reportError(EventSource.SETTINGS,"Error occurred while persisting Settings");
            pa.reportTrace(EventSource.SETTINGS, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("auth-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
