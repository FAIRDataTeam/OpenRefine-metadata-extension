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
package org.fair.openrefine.metadata.fdp;


import nl.dtl.fairmetadata4j.model.FDPMetadata;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import nl.dtl.fairmetadata4j.io.FDPMetadataParser;

public class FairDataPointClient {

    private String baseURI;

    public FairDataPointClient(String baseURI) {
        this.baseURI = baseURI;
    }

    public FDPMetadata getFairDataPointMetadata() throws IOException {
        URL url = new URL(baseURI);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/turtle");

        if(conn.getResponseCode() == 200) {
            TurtleParser parser = new TurtleParser();
            StatementCollector collector = new StatementCollector();
            parser.setRDFHandler(collector);

            parser.parse(new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)), baseURI);

            FDPMetadataParser metadataParser = new FDPMetadataParser();
            return metadataParser.parse(new ArrayList<>(collector.getStatements()), SimpleValueFactory.getInstance().createIRI(baseURI));
        }
        // TODO: handle communication errors
        // TODO: handle not-FDP case (not returning turtle, not returning valid FDPMetadata)
        return null;
    }
}
