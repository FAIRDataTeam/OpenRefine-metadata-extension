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

import nl.dtl.fairmetadata4j.model.CatalogMetadata;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;


public class CatalogTransformerUtils extends MetadataTransformerUtils {

    public static CatalogDTO metadata2DTO(CatalogMetadata catalogMetadata) {
        CatalogDTO catalogDTO = new CatalogDTO();

        genericDtoFromMetadata(catalogDTO, catalogMetadata);

        catalogDTO.setLanguage(iriToString(catalogMetadata.getLanguage()));
        catalogDTO.setHomepage(iriToString(catalogMetadata.getHomepage()));
        catalogDTO.setThemeTaxonomies(irisToStrings(catalogMetadata.getThemeTaxonomys()));

        catalogDTO.setDatasets(irisToStrings(catalogMetadata.getDatasets()));
        catalogDTO.setParentFDP(iriToString(catalogMetadata.getParentURI()));

        return catalogDTO;
    }

    public static CatalogMetadata dto2Metadata(CatalogDTO catalogDTO) {
        CatalogMetadata catalogMetadata = new CatalogMetadata();
        genericMetadataFromDto(catalogMetadata, catalogDTO);

        catalogMetadata.setLanguage(stringToIri(catalogDTO.getLanguage()));
        catalogMetadata.setHomepage(stringToIri(catalogDTO.getHomepage()));
        catalogMetadata.setThemeTaxonomys(stringsToIris(catalogDTO.getThemeTaxonomies()));

        catalogMetadata.setDatasets(stringsToIris(catalogDTO.getDatasets()));
        catalogMetadata.setParentURI(stringToIri(catalogDTO.getParentFDP()));

        return catalogMetadata;
    }
}
