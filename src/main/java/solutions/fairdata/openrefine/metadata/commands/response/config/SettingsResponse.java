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
package solutions.fairdata.openrefine.metadata.commands.response.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solutions.fairdata.openrefine.metadata.dto.ProjectInfoDTO;
import solutions.fairdata.openrefine.metadata.dto.project.ProjectHistoryDTO;
import solutions.fairdata.openrefine.metadata.dto.config.SettingsConfigDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SettingsResponse {

    private String status;
    private SettingsConfigDTO settings;
    private ProjectHistoryDTO projectData;
    private ProjectInfoDTO projectInfo;

    public SettingsResponse(SettingsConfigDTO settings, ProjectHistoryDTO projectData, ProjectInfoDTO projectInfo) {
        this.status = "ok";
        this.settings = settings;
        this.projectData = projectData;
        this.projectInfo = projectInfo;
    }
}
