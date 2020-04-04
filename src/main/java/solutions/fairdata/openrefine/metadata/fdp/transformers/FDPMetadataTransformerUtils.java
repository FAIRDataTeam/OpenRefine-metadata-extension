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
import solutions.fairdata.openrefine.metadata.dto.metadata.FDPMetadataDTO;
import solutions.fairdata.openrefine.metadata.fdp.Vocabulary;

import java.util.ArrayList;

public class FDPMetadataTransformerUtils extends MetadataTransformerUtils {

    public static FDPMetadataDTO metadata2DTO(ArrayList<Statement> statements, String actualURI) {
        FDPMetadataDTO dto = new FDPMetadataDTO();
        MetadataTransformerUtils.statements2DTO(statements, actualURI, dto);

        IRI subject = stringToIri(actualURI);
        for (Statement st: statements) {
            if (st.getSubject().equals(subject)) {
                IRI predicate = st.getPredicate();
                if (predicate.equals(Vocabulary.CATALOG)) {
                    dto.getChildren().add(st.getObject().stringValue());
                }
            }
        }
        return dto;
    }
}
