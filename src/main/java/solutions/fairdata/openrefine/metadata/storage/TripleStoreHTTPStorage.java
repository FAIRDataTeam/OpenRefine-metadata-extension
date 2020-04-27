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

import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TripleStoreHTTPStorage extends Storage {

    public static final String TYPE = "tripleStoreHTTP";

    private static final String DETAIL_HOST = "host";
    private static final String DETAIL_USERNAME = "username";
    private static final String DETAIL_PASSWORD = "password";
    private static final String DETAIL_REPOSITORY = "repository";
    public static final List<String> DETAILS = ImmutableList.of(
            DETAIL_HOST,
            DETAIL_USERNAME,
            DETAIL_PASSWORD,
            DETAIL_REPOSITORY
    );

    private String host;
    private String username;
    private String password;
    private String repository;

    private static final HashMap<String, RDFFormat> formats = new HashMap<>();
    private final HTTPRepository repo;

    static {
        List<RDFFormat> rdfFormats = Arrays.asList(RDFFormat.TURTLE, RDFFormat.RDFXML);
        for (RDFFormat rdfFormat: rdfFormats) {
            for (String mimeType: rdfFormat.getMIMETypes()) {
                formats.put(mimeType, rdfFormat);
            }
        }
    }

    private void loadDetails(StorageDTO storageDTO) {
        host = storageDTO.getDetails().get(DETAIL_HOST);
        username = storageDTO.getDetails().get(DETAIL_USERNAME);
        password = storageDTO.getDetails().get(DETAIL_PASSWORD);
        repository = storageDTO.getDetails().get(DETAIL_REPOSITORY);

        if (host == null) {
            throw new IllegalArgumentException("Missing host for FTP storage");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Missing repository for FTP storage");
        }
    }

    public TripleStoreHTTPStorage(StorageDTO storageDTO) {
        super(storageDTO);
        loadDetails(storageDTO);
        String uri = host;
        if (!uri.startsWith("http")) {  // For HTTPRepository it needs the protocol
            uri = "http://" + uri;
        }
        repo = new HTTPRepository(uri, repository);
        if (username != null && password != null) {
            repo.setUsernameAndPassword(username, password);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getDetailNames() {
        return DETAILS;
    }

    @Override
    public String getFilePath(HashMap<String, String> metadata) {
        return repo.getRepositoryURL();
    }

    private RDFFormat getFormat(String contentType) {
        return formats.getOrDefault(contentType, RDFFormat.RDFXML);
    }

    @Override
    public void storeData(byte[] data, HashMap<String, String> metadata, String contentType) throws IOException {
        String baseURI = metadata.get("baseURI");
        if (baseURI == null) {
            throw new IOException("Base URI not given");
        }
        InputStream inputStream =  new ByteArrayInputStream(data);
        RepositoryConnection repositoryConnection = repo.getConnection();
        try {
            repositoryConnection.add(inputStream, baseURI, getFormat(contentType));
        } finally {
            repositoryConnection.close();
        }
    }

    @Override
    public String getURL(HashMap<String, String> metadata) {
        return repo.getRepositoryURL();
    }
}
