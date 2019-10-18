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
package solutions.fairdata.openrefine.metadata.fdp.transformers;

import nl.dtl.fairmetadata4j.model.DistributionMetadata;
import solutions.fairdata.openrefine.metadata.dto.DistributionDTO;


public class DistributionTransformerUtils extends MetadataTransformerUtils {

    public static DistributionDTO metadata2DTO(DistributionMetadata distributionMetadata) {
        DistributionDTO distributionDTO = new DistributionDTO();

        genericDtoFromMetadata(distributionDTO, distributionMetadata);

        distributionDTO.setFormat(literalToString(distributionMetadata.getFormat()));
        distributionDTO.setBytesize(literalToString(distributionMetadata.getByteSize()));

        return distributionDTO;
    }

    public static DistributionMetadata dto2Metadata(DistributionDTO distributionDTO) {
        DistributionMetadata distributionMetadata = new DistributionMetadata();

        genericMetadataFromDto(distributionMetadata, distributionDTO);

        distributionMetadata.setFormat(stringToLiteral(distributionDTO.getFormat()));
        distributionMetadata.setByteSize(stringToLiteral(distributionDTO.getBytesize()));

        return distributionMetadata;
    }
}
