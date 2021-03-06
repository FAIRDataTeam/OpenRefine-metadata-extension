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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;
import solutions.fairdata.openrefine.metadata.fdp.VocabularyHelper;

import java.util.ArrayList;


public class DistributionTransformerUtils extends MetadataTransformerUtils {

    public static DistributionDTO statements2DTO(ArrayList<Statement> statements, String actualURI) {
        DistributionDTO dto = new DistributionDTO();
        MetadataTransformerUtils.statements2DTO(statements, actualURI, dto);

        IRI subject = stringToIri(actualURI);
        for (Statement st: statements) {
            if (st.getSubject().equals(subject)) {
                IRI predicate = st.getPredicate();
                if (predicate.equals(VocabularyHelper.FORMAT)) {
                    dto.setFormat(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.BYTE_SIZE)) {
                    dto.setBytesize(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.MEDIA_TYPE)) {
                    dto.setMediaType(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.ACCESS_URL)) {
                    dto.setAccessUrl(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.PARENT)) {
                    dto.setParent(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.DOWNLOAD_URL)) {
                    dto.setDownloadUrl(st.getObject().stringValue());
                }
            }
        }
        return dto;
    }

    public static ArrayList<Statement> dto2Statements(DistributionDTO distributionDTO) {
        ArrayList<Statement> statements = MetadataTransformerUtils.dto2Statements(distributionDTO);
        IRI subject = stringToIri(distributionDTO.getIri());
        statements.add(valueFactory.createStatement(subject, VocabularyHelper.TYPE, VocabularyHelper.TYPE_DISTRIBUTION));
        statements.add(valueFactory.createStatement(subject, VocabularyHelper.PARENT, stringToIri(distributionDTO.getParent())));
        if (distributionDTO.getMediaType() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.MEDIA_TYPE, stringToLiteral(distributionDTO.getMediaType())));
        }
        if (distributionDTO.getBytesize() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.BYTE_SIZE, stringToLiteral(distributionDTO.getBytesize())));
        }
        if (distributionDTO.getFormat() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.FORMAT, stringToLiteral(distributionDTO.getFormat())));
        }
        if (distributionDTO.getAccessUrl() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.ACCESS_URL, stringToIri(distributionDTO.getAccessUrl())));
        }
        if (distributionDTO.getDownloadUrl() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.DOWNLOAD_URL, stringToIri(distributionDTO.getDownloadUrl())));
        }
        return statements;
    }
}
