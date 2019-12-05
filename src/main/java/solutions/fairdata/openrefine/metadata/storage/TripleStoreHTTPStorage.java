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

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class TripleStoreHTTPStorage extends Storage {

    public static final String TYPE = "tripleStoreHTTP";
    private static final HashMap<String, RDFFormat> formats = new HashMap<>();
    private final HTTPRepository repository;

    static {
        formats.put("application/x-turtle", RDFFormat.TURTLE);
        formats.put("text/turtle", RDFFormat.TURTLE);
        formats.put("application/rdf+xml", RDFFormat.RDFXML);
    }

    public TripleStoreHTTPStorage(StorageDTO storageDTO) {
        super(storageDTO);
        String uri = storageDTO.getHost();
        if (!uri.startsWith("http")) {  // For HTTPRepository it needs the protocol
            uri = "http://" + uri;
        }
        repository = new HTTPRepository(uri, storageDTO.getDirectory());
        repository.setUsernameAndPassword(storageDTO.getUsername(), storageDTO.getPassword());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getFilePath(String filename) {
        return repository.getRepositoryURL();
    }

    private RDFFormat getFormat(String contentType) {
        return formats.getOrDefault(contentType, RDFFormat.RDFXML);
    }

    @Override
    public void storeData(byte[] data, String baseURI, String contentType) throws IOException {
        InputStream inputStream =  new ByteArrayInputStream(data);
        RepositoryConnection repositoryConnection = repository.getConnection();
        try {
            repositoryConnection.add(inputStream, baseURI, getFormat(contentType));
        } finally {
            repositoryConnection.close();
        }
    }

    @Override
    public String getURL(String filename) {
        return repository.getRepositoryURL();
    }
}
