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
package solutions.fairdata.openrefine.metadata.storage;

import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class VirtuosoStorage extends Storage {

    public static final String TYPE = "virtuoso";

    public VirtuosoStorage(StorageDTO storageDTO) {
        super(storageDTO);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getFilePath(HashMap<String, String> metadata) {
        return storageDTO.getDirectory() + metadata.getOrDefault("filename", "");
    }

    @Override
    public String getURL(HashMap<String, String> metadata) {
        return "http://" + storageDTO.getHost() + getFilePath(metadata);
    }

    @Override
    public void storeData(byte[] data, HashMap<String, String> metadata, String contentType) throws IOException {
        String filename = metadata.get("filename");
        if (filename == null) {
            throw new IOException("Filename not given");
        }
        if (!allowsContentType(contentType)) {
            throw new IOException("Bad content type for selected storage");
        }
        makeVirtuosoPut(getURL(metadata), new String(data, StandardCharsets.UTF_8), contentType + "; charset=\"UTF-8\"");
    }

    /**
     * @see http://vos.openlinksw.com/owiki/wiki/VOS/VirtRDFInsert#HTTP%20PUT
     */
    private void makeVirtuosoPut(String uri, String data, String contentType) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        out.write(data);
        out.close();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Error occured while creating in Virtuoso: " + conn.getResponseMessage());
        }
    }
}
