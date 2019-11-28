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

import nl.dtl.fairmetadata4j.model.DatasetMetadata;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;


public class DatasetTransformerUtils extends MetadataTransformerUtils {

    public static DatasetDTO metadata2DTO(DatasetMetadata datasetMetadata) {
        DatasetDTO datasetDTO = new DatasetDTO();

        genericDtoFromMetadata(datasetDTO, datasetMetadata);

        datasetDTO.setPublisher(iriToString(datasetMetadata.getPublisher().getUri()));
        datasetDTO.setPublisherName(literalToString(datasetMetadata.getPublisher().getName()));

        datasetDTO.setLanguage(iriToString(datasetMetadata.getLanguage()));
        datasetDTO.setThemes(irisToStrings(datasetMetadata.getThemes()));
        datasetDTO.setContactPoint(iriToString(datasetMetadata.getContactPoint()));
        datasetDTO.setKeywords(literalsToStrings(datasetMetadata.getKeywords()));
        datasetDTO.setLandingPage(iriToString(datasetMetadata.getLandingPage()));

        datasetDTO.setDistributions(irisToStrings(datasetMetadata.getDistributions()));
        datasetDTO.setParentCatalog(iriToString(datasetMetadata.getParentURI()));

        return datasetDTO;
    }

    public static DatasetMetadata dto2Metadata(DatasetDTO datasetDTO) {
        DatasetMetadata datasetMetadata = new DatasetMetadata();

        genericMetadataFromDto(datasetMetadata, datasetDTO);

        datasetMetadata.setPublisher(createAgent(datasetDTO.getPublisher(), datasetDTO.getPublisherName()));

        datasetMetadata.setLanguage(stringToIri(datasetDTO.getLanguage()));
        datasetMetadata.setThemes(stringsToIris(datasetDTO.getThemes()));
        datasetMetadata.setContactPoint(stringToIri(datasetDTO.getContactPoint()));
        datasetMetadata.setKeywords(stringsToLiterals(datasetDTO.getKeywords()));
        datasetMetadata.setLandingPage(stringToIri(datasetDTO.getLandingPage()));

        datasetMetadata.setDistributions(stringsToIris(datasetDTO.getDistributions()));
        datasetMetadata.setParentURI(stringToIri(datasetDTO.getParentCatalog()));

        return datasetMetadata;
    }
}
