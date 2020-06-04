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
package solutions.fairdata.openrefine.metadata.dto.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class StorageInfoDTO {
    private String name;
    private String type;
    private Map<String, String> details;
    private Set<String> contentTypes;
    private Integer maxByteSize;
    private Set<String> filenamePatterns;

    public StorageInfoDTO(StorageDTO storageDTO) {
        this(
                storageDTO.getName(),
                storageDTO.getType(),
                storageDTO.getDetails(),
                storageDTO.getContentTypes(),
                storageDTO.getMaxByteSize(),
                storageDTO.getFilenamePatterns()
        );
    }

    public StorageInfoDTO(String name, String type, Map<String, String> details, Set<String> contentTypes, Integer maxByteSize, Set<String> filenamePatterns) {
        this.name = name;
        this.type = type;
        this.details = details;
        this.contentTypes = contentTypes;
        this.maxByteSize = maxByteSize;
        this.filenamePatterns = filenamePatterns;

        if (details.containsKey("password")) {
            details.put("password", "<hidden>");
        }
    }
}
