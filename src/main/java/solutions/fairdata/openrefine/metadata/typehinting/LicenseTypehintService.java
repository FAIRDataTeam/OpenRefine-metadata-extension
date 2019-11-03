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
package solutions.fairdata.openrefine.metadata.typehinting;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.dto.TypehintDTO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenseTypehintService implements TypehintService {

    private static final String TARGET_URL = "http://rdflicense.appspot.com/rdflicense";
    private static final String USER_AGENT =  MetadataModuleImpl.USER_AGENT;

    @Override
    public List<TypehintDTO> getTypehints(String query) throws IOException {
        List<TypehintDTO> result = new ArrayList<>();
        HashMap<String, String> licenses = getLicenses();
        for (Map.Entry<String, String> license : licenses.entrySet()) {
            if (license.getValue().toLowerCase().contains(query.toLowerCase())) {
                result.add(new TypehintDTO(license.getValue(), license.getKey()));
            }
        }
        return result;
    }

    @Override
    public String getSource() {
        return TARGET_URL;
    }

    private HashMap<String, String> getLicenses() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(TARGET_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/turtle;charset=utf-8");
        conn.addRequestProperty("User-Agent", USER_AGENT);

        HashMap<String, String> result = new HashMap<>();
        GraphQueryResult query = QueryResults.parseGraphBackground(conn.getInputStream(), TARGET_URL, RDFFormat.TURTLE);
        while (query.hasNext()) {
            Statement st = query.next();
            if (st.getPredicate().getLocalName().equals("label")) {
                result.put(st.getSubject().stringValue(), st.getObject().stringValue());
                System.out.println(st.getObject().stringValue());
            }
        }
        return result;
    }
}
