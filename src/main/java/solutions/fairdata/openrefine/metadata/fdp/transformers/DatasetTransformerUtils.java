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
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.fdp.VocabularyHelper;

import java.util.ArrayList;


public class DatasetTransformerUtils extends MetadataTransformerUtils {

    public static DatasetDTO statements2DTO(ArrayList<Statement> statements, String actualURI) {
        DatasetDTO dto = new DatasetDTO();
        MetadataTransformerUtils.statements2DTO(statements, actualURI, dto);

        IRI subject = stringToIri(actualURI);
        for (Statement st: statements) {
            if (st.getSubject().equals(subject)) {
                IRI predicate = st.getPredicate();
                if (predicate.equals(VocabularyHelper.CONTACT_POINT)) {
                    dto.setContactPoint(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.DISTRIBUTION)) {
                    dto.getChildren().add(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.LANGUAGE)) {
                    dto.setLanguage(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.LANDING_PAGE)) {
                    dto.setLandingPage(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.PARENT)) {
                    dto.setParent(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.THEME)) {
                    dto.getThemes().add(st.getObject().stringValue());
                } else if (predicate.equals(VocabularyHelper.KEYWORD)) {
                    dto.getKeywords().add(st.getObject().stringValue());
                }
            }
        }
        return dto;
    }

    public static ArrayList<Statement> dto2Statements(DatasetDTO datasetDTO) {
        ArrayList<Statement> statements = new ArrayList<>();
        IRI subject = stringToIri(datasetDTO.getIri());
        statements.add(valueFactory.createStatement(subject, VocabularyHelper.TYPE, VocabularyHelper.TYPE_DATASET));
        statements.add(valueFactory.createStatement(subject, VocabularyHelper.PARENT, stringToIri(datasetDTO.getParent())));
        for (String datasetUri: datasetDTO.getChildren()) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.DISTRIBUTION, stringToIri(datasetUri)));
        }
        for (String themeUri: datasetDTO.getThemes()) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.THEME, stringToIri(themeUri)));
        }
        for (String keyword: datasetDTO.getKeywords()) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.KEYWORD, stringToLiteral(keyword)));
        }
        if (datasetDTO.getContactPoint() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.CONTACT_POINT, stringToIri(datasetDTO.getContactPoint())));
        }
        if (datasetDTO.getLanguage() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.LANGUAGE, stringToIri(datasetDTO.getLanguage())));
        }
        if (datasetDTO.getLandingPage() != null) {
            statements.add(valueFactory.createStatement(subject, VocabularyHelper.LANDING_PAGE, stringToIri(datasetDTO.getLandingPage())));
        }
        return statements;
    }
}
