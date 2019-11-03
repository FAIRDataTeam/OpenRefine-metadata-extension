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

import com.fasterxml.jackson.databind.JsonNode;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.dto.TypehintDTO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguageTypehintService implements TypehintService {

    private static final String TARGET_URL = "http://id.loc.gov/vocabulary/iso639-1.madsrdf.json";
    private static final String LIST_PROP = "http://www.loc.gov/mads/rdf/v1#hasTopMemberOfMADSScheme";
    private static final String URI_PROP = "@id";
    private static final String USER_AGENT =  MetadataModuleImpl.USER_AGENT;

    @Override
    public List<TypehintDTO> getTypehints(String query) throws IOException {
        List<TypehintDTO> result = new ArrayList<>();
        for (String languageURI : getLanguageURIs()) {
            String code = languageURI.substring(languageURI.lastIndexOf('/') + 1).toLowerCase();
            if (code.contains(query.toLowerCase())) {
                result.add(new TypehintDTO(code, languageURI));
            }
        }
        return result;
    }

    @Override
    public String getSource() {
        return TARGET_URL;
    }

    private static Set<String> getLanguageURIs() throws IOException {
        Set<String> result = new HashSet<>();
        HttpURLConnection conn = (HttpURLConnection) new URL(TARGET_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("User-Agent", USER_AGENT);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            JsonNode root = MetadataModuleImpl.objectMapper.readTree(conn.getInputStream());
            if (root.isArray()) {
                for (final JsonNode obj : root) {
                    JsonNode langList = obj.get(LIST_PROP);
                    if (langList.isArray()) {
                        for (final JsonNode lang : langList) {
                            result.add(lang.get(URI_PROP).asText());
                        }
                    }
                }
            }
        }
        return result;
    }
}
