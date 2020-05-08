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
package solutions.fairdata.openrefine.metadata.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solutions.fairdata.openrefine.metadata.Utils;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.MetadataDTO;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProjectMetadataRecordDTO {
    public static final String DETAIL_TYPE = "type";
    public static final String DETAIL_PARENT = "parent";
    public static final String DETAIL_TITLE = "title";

    private String timestamp = Utils.currentTimestamp();
    private String uri;
    private HashMap<String, String> details = new HashMap<>();

    public void setDetail(String name, String value) {
        getDetails().put(name, value);
    }

    public static ProjectMetadataRecordDTO createFor(CatalogDTO catalogDTO) {
        ProjectMetadataRecordDTO record = createForGeneric(catalogDTO);
        record.setDetail(DETAIL_TYPE, "catalog");
        record.setDetail(DETAIL_PARENT, catalogDTO.getParent());
        return record;
    }

    public static ProjectMetadataRecordDTO createFor(DatasetDTO datasetDTO) {
        ProjectMetadataRecordDTO record = createForGeneric(datasetDTO);
        record.setDetail(DETAIL_TYPE, "dataset");
        record.setDetail(DETAIL_PARENT, datasetDTO.getParent());
        return record;
    }

    public static ProjectMetadataRecordDTO createFor(DistributionDTO distributionDTO) {
        ProjectMetadataRecordDTO record = createForGeneric(distributionDTO);
        record.setDetail(DETAIL_TYPE, "distribution");
        record.setDetail(DETAIL_PARENT, distributionDTO.getParent());
        return record;
    }

    private static ProjectMetadataRecordDTO createForGeneric(MetadataDTO metadataDTO) {
        ProjectMetadataRecordDTO record = new ProjectMetadataRecordDTO();
        record.setUri(metadataDTO.getIri());
        record.setDetail(DETAIL_TITLE, metadataDTO.getTitle());
        return record;
    }
}
