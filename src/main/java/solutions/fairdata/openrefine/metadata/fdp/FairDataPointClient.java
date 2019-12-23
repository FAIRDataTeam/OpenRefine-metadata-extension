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


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.dtl.fairmetadata4j.io.FDPMetadataParser;
import nl.dtl.fairmetadata4j.io.CatalogMetadataParser;
import nl.dtl.fairmetadata4j.io.DatasetMetadataParser;
import nl.dtl.fairmetadata4j.io.DistributionMetadataParser;
import nl.dtl.fairmetadata4j.io.MetadataException;
import nl.dtl.fairmetadata4j.model.CatalogMetadata;
import nl.dtl.fairmetadata4j.model.DatasetMetadata;
import nl.dtl.fairmetadata4j.model.DistributionMetadata;
import nl.dtl.fairmetadata4j.model.FDPMetadata;
import nl.dtl.fairmetadata4j.utils.MetadataUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.slf4j.Logger;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.dto.auth.AuthDTO;
import solutions.fairdata.openrefine.metadata.dto.auth.TokenDTO;
import solutions.fairdata.openrefine.metadata.dto.dashboard.DashboardCatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.FDPMetadataDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;
import solutions.fairdata.openrefine.metadata.fdp.transformers.CatalogTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.DatasetTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.DistributionTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.FDPMetadataTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.MetadataTransformerUtils;

/**
 * Simple client for communication with FAIR Data Point API
 *
 * It transforms various types returned by FDP API into DTOs (POJOs) to
 * provide simple data manipulation in the extension.
 */
public class FairDataPointClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final HashSet<Integer> REDIRECTS = new HashSet<>(Arrays.asList(
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_SEE_OTHER,
            307,  // HTTP Temporary Redirect
            308   // HTTP Permanent Redirect
    ));

    private static final String CATALOG_PART = "/catalog";
    private static final String DATASET_PART = "/dataset";
    private static final String DISTRIBUTION_PART = "/distribution";
    private static final String SPEC_PART = "/spec";
    private static final String AUTH_PART = "/tokens";;
    private static final String DASHBOARD_PART = "/dashboard";

    private static final String USER_AGENT = MetadataModuleImpl.USER_AGENT;

    private final Logger logger;
    private final String token;
    private final String fdpBaseURI;

    public FairDataPointClient(String fdpBaseURI, Logger logger) {
        this(fdpBaseURI, null, logger);
    }

    public FairDataPointClient(String fdpBaseURI, String token, Logger logger) {
        this.fdpBaseURI = fdpBaseURI;
        this.token = token;
        this.logger = logger;
    }

    /**
     * Authenticate with FDP using username and password
     *
     * @param auth DTO with username and password
     * @return token in DTO
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public TokenDTO postAuthentication(AuthDTO auth) throws IOException, FairDataPointException {
        HttpURLConnection conn = createConnection(fdpBaseURI + AUTH_PART, "POST", "application/json");
        conn.addRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        objectMapper.writeValue(conn.getOutputStream(), auth);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return objectMapper.readValue(conn.getInputStream(), TokenDTO.class);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get dashboard for user authenticated by the token
     *
     * @return list of dashboard catalogs
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public List<DashboardCatalogDTO> getDashboard() throws IOException, FairDataPointException {
        HttpURLConnection conn = request(fdpBaseURI + DASHBOARD_PART, "GET", "application/json", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return objectMapper.readValue(conn.getInputStream(), new TypeReference<List<DashboardCatalogDTO>>(){});
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get metadata about FAIR Data Point (Repository)
     *
     * @return FAIR Data Point (Repository) metadata
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public FDPMetadataDTO getFairDataPointMetadata() throws IOException, FairDataPointException {
        HttpURLConnection conn = request(fdpBaseURI, "GET", "text/turtle", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String actualURI = conn.getURL().toString();

            FDPMetadataParser metadataParser = new FDPMetadataParser();
            FDPMetadata fdpMetadata = metadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return FDPMetadataTransformerUtils.metadata2DTO(fdpMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get metadata about Catalog
     *
     * @param catalogURI URI of the catalog to be retrieved
     * @return Catalog metadata
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public CatalogDTO getCatalogMetadata(String catalogURI) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(catalogURI, "GET", "text/turtle", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String actualURI = conn.getURL().toString();

            CatalogMetadataParser catalogMetadataParser = new CatalogMetadataParser();
            CatalogMetadata catalogMetadata = catalogMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return CatalogTransformerUtils.metadata2DTO(catalogMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get metadata about Dataset
     *
     * @param datasetURI URI of the dataset to be retrieved
     * @return Dataset metadata
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public DatasetDTO getDatasetMetadata(String datasetURI) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(datasetURI, "GET", "text/turtle", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String actualURI = conn.getURL().toString();

            DatasetMetadataParser datasetMetadataParser = new DatasetMetadataParser();
            DatasetMetadata datasetMetadata = datasetMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return DatasetTransformerUtils.metadata2DTO(datasetMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get metadata about Distribution
     *
     * @param distributionURI URI of the distribution to be retrieved
     * @return Distribution metadata
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public DistributionDTO getDistributionMetadata(String distributionURI) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(distributionURI, "GET", "text/turtle", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String actualURI = conn.getURL().toString();

            DistributionMetadataParser distributionMetadataParser = new DistributionMetadataParser();
            DistributionMetadata distributionMetadata = distributionMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return DistributionTransformerUtils.metadata2DTO(distributionMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get specs of catalog layer (form specs)
     *
     * @return form specs as generic object
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public Object getCatalogSpec() throws IOException, FairDataPointException {
        return getMetadataSpec(CATALOG_PART);
    }


    /**
     * Get specs of dataset layer (form specs)
     *
     * @return form specs as generic object
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public Object getDatasetSpec() throws IOException, FairDataPointException {
        return getMetadataSpec(DATASET_PART);
    }


    /**
     * Get specs of distribution layer (form specs)
     *
     * @return form specs as generic object
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public Object getDistributionSpec() throws IOException, FairDataPointException {
        return getMetadataSpec(DISTRIBUTION_PART);
    }


    /**
     * Get form specs
     *
     * @param metadataPart specification of layer (catalog, dataset, or distribution)
     * @return form specs as generic object
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    private Object getMetadataSpec(String metadataPart) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(fdpBaseURI + metadataPart + SPEC_PART, "GET", "application/json", true);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return objectMapper.readValue(conn.getInputStream(), Object.class);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Create a new catalog in FDP
     *
     * @param catalogDTO catalog to be created
     * @return created catalog
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public CatalogDTO postCatalog(CatalogDTO catalogDTO) throws IOException, MetadataException, FairDataPointException {
        HttpURLConnection conn = createConnection(fdpBaseURI + CATALOG_PART, "POST", "text/turtle");
        conn.addRequestProperty("Content-Type", "text/turtle");
        conn.setDoOutput(true);

        // Generate random IRI, FDP will replace it with really unique
        String uri = fdpBaseURI + "/fdp/catalog/" + UUID.randomUUID();
        catalogDTO.setIri(uri);

        CatalogMetadata catalogMetadata = CatalogTransformerUtils.dto2Metadata(catalogDTO);
        catalogMetadata.setIdentifier(MetadataTransformerUtils.createIdentifier(uri));
        MetadataTransformerUtils.setTimestamps(catalogMetadata);
        String metadata = MetadataUtils.getString(catalogMetadata, RDFFormat.TURTLE);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            String actualURI = conn.getHeaderField("Location");
            CatalogMetadataParser catalogMetadataParser = new CatalogMetadataParser();

            CatalogMetadata returnCatalogMetadata = catalogMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return CatalogTransformerUtils.metadata2DTO(returnCatalogMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Create a new dataset in FDP
     *
     * @param datasetDTO dataset to be created
     * @return created dataset
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public DatasetDTO postDataset(DatasetDTO datasetDTO) throws IOException, MetadataException, FairDataPointException {
        HttpURLConnection conn = createConnection(fdpBaseURI + DATASET_PART, "POST", "text/turtle");
        conn.addRequestProperty("Content-Type", "text/turtle");
        conn.setDoOutput(true);

        // Generate random IRI, FDP will replace it with really unique
        String uri = fdpBaseURI + "/fdp/dataset/" + UUID.randomUUID();
        datasetDTO.setIri(uri);

        DatasetMetadata datasetMetadata = DatasetTransformerUtils.dto2Metadata(datasetDTO);
        datasetMetadata.setIdentifier(MetadataTransformerUtils.createIdentifier(uri));
        MetadataTransformerUtils.setTimestamps(datasetMetadata);
        String metadata = MetadataUtils.getString(datasetMetadata, RDFFormat.TURTLE);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            String actualURI = conn.getHeaderField("Location");
            DatasetMetadataParser datasetMetadataParser = new DatasetMetadataParser();

            DatasetMetadata returnDatasetMetadata = datasetMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return DatasetTransformerUtils.metadata2DTO(returnDatasetMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Create a new distribution in FDP
     *
     * @param distributionDTO distribution to be created
     * @return created distribution
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public DistributionDTO postDistribution(DistributionDTO distributionDTO) throws IOException, MetadataException, FairDataPointException {
        HttpURLConnection conn = createConnection(fdpBaseURI + DISTRIBUTION_PART, "POST", "text/turtle");
        conn.addRequestProperty("Content-Type", "text/turtle");
        conn.setDoOutput(true);

        // Generate random IRI, FDP will replace it with really unique
        String uri = fdpBaseURI + "/fdp/distribution/" + UUID.randomUUID();
        distributionDTO.setIri(uri);

        DistributionMetadata distributionMetadata = DistributionTransformerUtils.dto2Metadata(distributionDTO);
        distributionMetadata.setIdentifier(MetadataTransformerUtils.createIdentifier(uri));
        MetadataTransformerUtils.setTimestamps(distributionMetadata);
        String metadata = MetadataUtils.getString(distributionMetadata, RDFFormat.TURTLE);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            String actualURI = conn.getHeaderField("Location");
            DistributionMetadataParser distributionMetadataParser = new DistributionMetadataParser();

            DistributionMetadata returnDistributionMetadata = distributionMetadataParser.parse(
                    parseStatements(conn, actualURI),
                    SimpleValueFactory.getInstance().createIRI(actualURI)
            );
            return DistributionTransformerUtils.metadata2DTO(returnDistributionMetadata);
        } else {
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Helper for parsing RDF statements from connection
     *
     * @param conn connection with FAIR Data Point
     * @param uri base URI to be retrieved
     * @return list of statements for given URI
     * @throws IOException in case of communication error with FDP
     */
    private ArrayList<Statement> parseStatements(HttpURLConnection conn, String uri) throws IOException {
        TurtleParser parser = new TurtleParser();
        StatementCollector collector = new StatementCollector();
        parser.setRDFHandler(collector);

        parser.parse(new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)), uri);
        return new ArrayList<>(collector.getStatements());
    }

    /**
     * Helper for creating a connection to FAIR Data Point with prepared headers
     *
     * @param url target requested URL
     * @param method HTTP method (e.g. GET, POST, PUT)
     * @param accept accepted response media type
     * @return connection
     * @throws IOException in case of communication error
     */
    private HttpURLConnection createConnection(String url, String method, String accept) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.addRequestProperty("Accept", accept);
        conn.addRequestProperty("User-Agent", USER_AGENT);
        if (token != null) {
            conn.addRequestProperty("Authorization", "Bearer " + token);
        }
        return conn;
    }

    /**
     * Helper for creating a single HTTP request to FDP
     *
     * @param url target requested URL
     * @param method HTTP method (e.g. GET, POST, PUT)
     * @param accept accepted response media type
     * @param followRedirects flag if should follow redirects (defined REDIRECTS codes)
     * @return connection
     * @throws IOException in case of communication error
     */
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
