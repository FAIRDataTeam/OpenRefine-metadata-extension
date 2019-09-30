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
package solutions.fairdata.openrefine.metadata.fdp;


import nl.dtl.fairmetadata4j.model.FDPMetadata;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import nl.dtl.fairmetadata4j.io.FDPMetadataParser;
import org.slf4j.Logger;

public class FairDataPointClient {

    private static final HashSet<Integer> REDIRECTS = new HashSet<>(Arrays.asList(
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_SEE_OTHER,
            307,  // HTTP Temporary Redirect
            308   // HTTP Permanent Redirect
    ));

    private static final String USER_AGENT = "OpenRefine/metadata";

    private final String baseURI;
    private final Logger logger;

    public FairDataPointClient(String baseURI, Logger logger) {
        this.baseURI = baseURI;
        this.logger = logger;
    }

    public FDPMetadata getFairDataPointMetadata() throws IOException, FairDataPointException {
        HttpURLConnection conn = request(baseURI, "GET", "text/turtle", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String actualURI = conn.getURL().toString();

            TurtleParser parser = new TurtleParser();
            StatementCollector collector = new StatementCollector();
            parser.setRDFHandler(collector);

            parser.parse(new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)), actualURI);

            FDPMetadataParser metadataParser = new FDPMetadataParser();
            return metadataParser.parse(new ArrayList<>(collector.getStatements()), SimpleValueFactory.getInstance().createIRI(actualURI));
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    private HttpURLConnection createConnection(String url, String method, String accept) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.addRequestProperty("Accept", accept);
        conn.addRequestProperty("User-Agent", USER_AGENT);
        return conn;
    }

    private HttpURLConnection request(String url, String method, String accept, boolean followRedirects) throws IOException {
        HttpURLConnection conn = createConnection(url, method, accept);

        logger.info("FDP HTTP {} request to {} (response: {})", method, url, conn.getResponseCode());
        conn.connect();

        // HttpUrlConnection redirection does not work with HTTPS and has others flaws
        if (followRedirects && REDIRECTS.contains(conn.getResponseCode())) {
            HashSet<String> visited = new HashSet<>();
            visited.add(url);

            while (REDIRECTS.contains(conn.getResponseCode())) {
                String nextUrl = conn.getHeaderField("Location");
                String cookies = conn.getHeaderField("Set-Cookie");

                logger.info("HTTP request (caused by redirect) to " + nextUrl);

                if (visited.contains(nextUrl)) {
                    throw new IOException("HTTP redirection loop detected");
                }
                visited.add(nextUrl);

                conn = createConnection(nextUrl, method, accept);
                conn.setRequestProperty("Cookie", cookies);

                logger.info("FDP HTTP {} request to {} after redirect (response: {})", method, url, conn.getResponseCode());
                conn.connect();
            }
        }
        return conn;
    }
}
