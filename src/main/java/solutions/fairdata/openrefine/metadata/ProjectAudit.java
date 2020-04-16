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
package solutions.fairdata.openrefine.metadata;

import com.google.refine.model.Project;
import solutions.fairdata.openrefine.metadata.dto.audit.AuditEntryDTO;
import solutions.fairdata.openrefine.metadata.dto.audit.AuditLogDTO;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.audit.EventType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ProjectAudit {

    private AuditLogDTO log;
    private int level;

    private static String currentTimestamp() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public ProjectAudit(Project project) {
        this.level = MetadataModuleImpl.getInstance().getSettings().getAudit().getLevel();
        if (MetadataModuleImpl.getProjectModel(project).getProjectLog() == null) {
            MetadataModuleImpl.getProjectModel(project).setProjectLog(new AuditLogDTO(project.id, new ArrayList<>()));
        }
        this.log = MetadataModuleImpl.getProjectModel(project).getProjectLog();
        System.out.println("Prepared ProjectAudit with level " + this.level);
    }

    public void report(EventType eventType, EventSource eventSource, String message) {
        if (eventType.getLevel() <= level) {
            this.log.getEntries().add(new AuditEntryDTO(eventType, eventSource, message, currentTimestamp()));
        }
    }

    public void reportError(EventSource eventSource, String message) {
        MetadataModuleImpl.getLogger().error(eventSource + ": " + message);
        report(EventType.ERROR, eventSource, message);
    }

    public void reportWarning(EventSource eventSource, String message) {
        MetadataModuleImpl.getLogger().warn(eventSource + ": " + message);
        report(EventType.WARNING, eventSource, message);
    }

    public void reportInfo(EventSource eventSource, String message) {
        MetadataModuleImpl.getLogger().info(eventSource + ": " + message);
        report(EventType.INFO, eventSource, message);
    }

    public void reportDebug(EventSource eventSource, String message) {
        MetadataModuleImpl.getLogger().debug(eventSource + ": " + message);
        report(EventType.DEBUG, eventSource, message);
    }

    public void reportTrace(EventSource eventSource, Exception e) {
        String message = e.getClass().getSimpleName() + " appeared:\n" + getStackTrace(e);
        MetadataModuleImpl.getLogger().trace(eventSource + ": " + message);
        report(EventType.TRACE, eventSource, message);
    }

    public AuditLogDTO getAuditLog() {
        return log;
    }

    public void clearLog() {
        this.log.getEntries().clear();
    }
}
