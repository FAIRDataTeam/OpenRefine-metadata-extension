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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeTypehintService implements TypehintService {

    private static final String TARGET_URL = "https://www.wikidata.org/";
    private static final String USER_AGENT =  MetadataModuleImpl.USER_AGENT;
    private static final String LIST_PROP = "search";
    private static final String TITLE_PROP = "label";
    private static final String URI_PROP = "concepturi";

    @Override
    public List<TypehintDTO> getTypehints(String query) throws IOException {
        List<TypehintDTO> result = new ArrayList<>();
        if (query.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, String> entry : getThemes(query).entrySet()) {
            result.add(new TypehintDTO(entry.getValue(), entry.getKey()));
        }
        return result;
    }

    @Override
    public String getSource() {
        return TARGET_URL;
    }

    private HashMap<String, String> getThemes(String query) throws IOException {
        String url = TARGET_URL + "w/api.php?action=wbsearchentities&limit=50&language=en&format=json&origin=*&search=" + query;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/turtle;charset=utf-8");
        conn.addRequestProperty("User-Agent", USER_AGENT);

        HashMap<String, String> result = new HashMap<>();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            JsonNode root = MetadataModuleImpl.objectMapper.readTree(conn.getInputStream());
            if (root.isObject() ) {
                JsonNode themeList = root.get(LIST_PROP);
                if (themeList.isArray()) {
                    for (final JsonNode theme : themeList) {
                        result.put(theme.get(URI_PROP).asText(), theme.get(TITLE_PROP).asText());
                    }
                }
            }
        }
        return result;
    }
}
