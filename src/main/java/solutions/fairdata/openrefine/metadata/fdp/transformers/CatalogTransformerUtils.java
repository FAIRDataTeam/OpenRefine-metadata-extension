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
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.fdp.Vocabulary;

import java.util.ArrayList;


public class CatalogTransformerUtils extends MetadataTransformerUtils {

    public static CatalogDTO statements2DTO(ArrayList<Statement> statements, String actualURI) {
        CatalogDTO dto = new CatalogDTO();
        MetadataTransformerUtils.statements2DTO(statements, actualURI, dto);

        IRI subject = stringToIri(actualURI);
        for (Statement st: statements) {
            if (st.getSubject().equals(subject)) {
                IRI predicate = st.getPredicate();
                if (predicate.equals(Vocabulary.DATASET)) {
                    dto.getChildren().add(st.getObject().stringValue());
                } else if (predicate.equals(Vocabulary.LANGUAGE)) {
                    dto.setLanguage(st.getObject().stringValue());
                } else if (predicate.equals(Vocabulary.HOMEPAGE)) {
                    dto.setHomepage(st.getObject().stringValue());
                } else if (predicate.equals(Vocabulary.PARENT)) {
                    dto.setParent(st.getObject().stringValue());
                } else if (predicate.equals(Vocabulary.THEME_TAXONOMY)) {
                    dto.getThemeTaxonomies().add(st.getObject().stringValue());
                }
            }
        }
        return dto;
    }

    public static ArrayList<Statement> dto2Statements(CatalogDTO catalogDTO) {
        ArrayList<Statement> statements = MetadataTransformerUtils.dto2Statements(catalogDTO);
        IRI subject = stringToIri(catalogDTO.getIri());
        statements.add(valueFactory.createStatement(subject, Vocabulary.TYPE, Vocabulary.TYPE_CATALOG));
        statements.add(valueFactory.createStatement(subject, Vocabulary.PARENT, stringToIri(catalogDTO.getParent())));
        for (String datasetUri: catalogDTO.getChildren()) {
            statements.add(valueFactory.createStatement(subject, Vocabulary.DATASET, stringToIri(datasetUri)));
        }
        for (String taxonomyUri: catalogDTO.getThemeTaxonomies()) {
            statements.add(valueFactory.createStatement(subject, Vocabulary.THEME_TAXONOMY, stringToIri(taxonomyUri)));
        }
        if (catalogDTO.getHomepage() != null) {
            statements.add(valueFactory.createStatement(subject, Vocabulary.HOMEPAGE, stringToIri(catalogDTO.getHomepage())));
        }
        if (catalogDTO.getLanguage() != null) {
            statements.add(valueFactory.createStatement(subject, Vocabulary.LANGUAGE, stringToIri(catalogDTO.getLanguage())));
        }
        return statements;
    }
}
