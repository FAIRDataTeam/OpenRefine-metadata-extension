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
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.commands.request.ServiceRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ServiceResponse;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Command for service tasks
 */
public class ServiceCommand extends Command {

    private static final String ENVVAR = "OPENREFINE_SERVICE_TASKS";
    private static final String ENABLED = "enabled";

    public enum ServiceTask {
        RELOAD_CONFIG
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServiceRequest serviceRequest = CommandUtils.objectMapper.readValue(request.getReader(), ServiceRequest.class);
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(); // no project for service tasks

        String command = System.getenv(ServiceCommand.ENVVAR);
        ServiceResponse serviceResponse = new ServiceResponse();
        serviceResponse.setStatus(ServiceResponse.STATUS_ERROR);
        try {
            if (ENABLED.equalsIgnoreCase(command)) {
                ServiceTask task = ServiceTask.valueOf(serviceRequest.getTask().toUpperCase());
                pa.reportInfo(EventSource.SERVICE, "Requested to execute service task " + task.toString());
                if (task == ServiceTask.RELOAD_CONFIG) {
                    MetadataModuleImpl.getInstance().loadConfig();
                    serviceResponse.setStatus(ServiceResponse.STATUS_OK);
                    serviceResponse.setMessage("Configuration reloaded");
                    pa.reportDebug(EventSource.SERVICE, "Config has been reloaded");
                }
            } else {
                pa.reportWarning(EventSource.SERVICE, "Requested to execute service task but it is disabled");
                serviceResponse.setMessage("Service tasks are not enabled");
            }
            CommandUtils.objectMapper.writeValue(w, serviceResponse);
        } catch (IllegalArgumentException e) {
            pa.reportWarning(EventSource.SERVICE, "Requested unknown service task: " + serviceRequest.getTask());
            serviceResponse.setMessage("Unknown service task: " + serviceRequest.getTask());
            CommandUtils.objectMapper.writeValue(w, serviceResponse);
        } catch (Exception e) {
            pa.reportError(EventSource.SERVICE,"Error while performing service task: " + serviceRequest.getTask());
            pa.reportTrace(EventSource.SERVICE, e);
            serviceResponse.setMessage("Error while performing service task: " +  serviceRequest.getTask() );
            CommandUtils.objectMapper.writeValue(w, serviceResponse);
        } finally {
            w.flush();
            w.close();
        }
    }
}
