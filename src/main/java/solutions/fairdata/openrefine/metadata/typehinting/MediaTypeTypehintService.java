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

import org.w3c.dom.*;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.dto.TypehintDTO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaTypeTypehintService implements TypehintService {

    private static final String TARGET_URL = "https://www.iana.org/assignments/media-types/media-types.xml";
    private static final String USER_AGENT =  MetadataModuleImpl.USER_AGENT;

    @Override
    public List<TypehintDTO> getTypehints(String query) throws IOException {
        List<TypehintDTO> result = new ArrayList<>();
        HashMap<String, String> mediaTypes = getMediaTypes();
        for (Map.Entry<String, String> mediaType : mediaTypes.entrySet()) {
            if (mediaType.getValue().toLowerCase().contains(query.toLowerCase())) {
                result.add(new TypehintDTO(mediaType.getValue(), mediaType.getKey()));
            }
        }
        return result;
    }

    @Override
    public String getSource() {
        return TARGET_URL;
    }

    private HashMap<String, String> getMediaTypes() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(TARGET_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml;charset=utf-8");
        conn.addRequestProperty("User-Agent", USER_AGENT);

        HashMap<String, String> result = new HashMap<>();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(conn.getInputStream());
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("record");
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        boolean valid = eElement.getElementsByTagName("name").getLength() == 1 && eElement.getElementsByTagName("file").getLength() == 1;
                        if (valid) {
                            String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                            String type = eElement.getElementsByTagName("file").item(0).getTextContent();
                            result.put(type, name);
                        }
                    }
                }
            } catch (Exception e) {
                MetadataModuleImpl.getLogger().warn("Unable to get Media Type typehints: " + e.getMessage());
            }
        }
        return result;
    }
}
