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
package solutions.fairdata.openrefine.metadata.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solutions.fairdata.openrefine.metadata.dto.audit.EventType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SettingsConfigDTO {
    private Boolean allowCustomFDP;
    private EventType audit;
    private HashMap<String, String> metadata = new HashMap<>();
    private List<FDPConnectionConfigDTO> fdpConnections = new LinkedList<>();

    SettingsConfigDTO(Boolean allowCustomFDP, EventType audit) {
        this.allowCustomFDP = allowCustomFDP;
        this.audit = audit;
    }

    public static SettingsConfigDTO getDefaultSettings() {
        return new SettingsConfigDTO(true, EventType.OFF);
    }

    public SettingsConfigDTO copyDetails() {
        SettingsConfigDTO details = new SettingsConfigDTO();
        details.setAllowCustomFDP(getAllowCustomFDP());
        details.setMetadata(getMetadata());
        details.setFdpConnections(getFdpConnections().stream().map(FDPConnectionConfigDTO::copyDetails).collect(Collectors.toList()));
        return details;
    }
}
