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
import org.apache.http.HttpHeaders;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.auth.AuthDTO;
import solutions.fairdata.openrefine.metadata.dto.auth.TokenDTO;
import solutions.fairdata.openrefine.metadata.dto.config.FDPConfigDTO;
import solutions.fairdata.openrefine.metadata.dto.config.FDPInfoDTO;
import solutions.fairdata.openrefine.metadata.dto.dashboard.DashboardItemDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.CatalogDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DatasetDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;
import solutions.fairdata.openrefine.metadata.dto.metadata.FDPMetadataDTO;
import solutions.fairdata.openrefine.metadata.fdp.transformers.CatalogTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.DatasetTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.DistributionTransformerUtils;
import solutions.fairdata.openrefine.metadata.fdp.transformers.FDPMetadataTransformerUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private static final String INFO_PART = "actuator/info";
    private static final String CONFIG_PART = "configs/bootstrap";
    private static final String CATALOG_PART = "catalog";
    private static final String DATASET_PART = "dataset";
    private static final String DISTRIBUTION_PART = "distribution";
    private static final String SPEC_PART = "spec";
    private static final String AUTH_PART = "tokens";
    private static final String DASHBOARD_PART = "dashboard";

    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String MEDIA_TYPE_TURTLE = "text/turtle";

    private static final String USER_AGENT = MetadataModuleImpl.USER_AGENT;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final ProjectAudit projectAudit;
    private final String token;
    private final String fdpBaseURI;

    public FairDataPointClient(String fdpBaseURI, ProjectAudit projectAudit) {
        this(fdpBaseURI, null, projectAudit);
    }

    public FairDataPointClient(String fdpBaseURI, String token, ProjectAudit projectAudit) {
        while (fdpBaseURI.endsWith("/")) {
            fdpBaseURI = fdpBaseURI.substring(0, fdpBaseURI.length() - 1);
        }
        this.fdpBaseURI = fdpBaseURI;
        this.token = token;
        this.projectAudit = projectAudit;
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
        HttpURLConnection conn = createConnection(url(fdpBaseURI, AUTH_PART), "POST", MEDIA_TYPE_JSON);
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_JSON);
        conn.setDoOutput(true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending POST authentication request to FDP");
        objectMapper.writeValue(conn.getOutputStream(), auth);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received authentication response");
            return objectMapper.readValue(conn.getInputStream(), TokenDTO.class);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to authenticate: " + conn.getResponseCode());
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
    public List<DashboardItemDTO> getDashboard() throws IOException, FairDataPointException {
        HttpURLConnection conn = request(url(fdpBaseURI,DASHBOARD_PART), "GET", MEDIA_TYPE_JSON, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET dashboard request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received dashboard response");
            return objectMapper.readValue(conn.getInputStream(), new TypeReference<List<DashboardItemDTO>>(){});
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get dashboard: " + conn.getResponseCode());
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
    public FDPMetadataDTO getFairDataPointMetadata(String repositoryUri) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(fdpBaseURI, "GET", MEDIA_TYPE_TURTLE, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET repository metadata request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received repository metadata");
            String actualURI = repositoryUri != null ? repositoryUri : conn.getURL().toString();
            ArrayList<Statement> statements = parseStatements(conn, actualURI);
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Parsed and processed repository metadata");
            return FDPMetadataTransformerUtils.metadata2DTO(statements, actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get repository metadata: " + conn.getResponseCode());
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get information about FAIR Data Point (API server)
     *
     * @return FAIR Data Point (API server) info or null if endpoint not operable
     * @throws IOException in case of a communication error
     */
    public FDPInfoDTO getFairDataPointInfo() throws IOException {
        HttpURLConnection conn = request(url(fdpBaseURI, INFO_PART), "GET", MEDIA_TYPE_JSON, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET info request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received FDP info");
            return objectMapper.readValue(conn.getInputStream(), FDPInfoDTO.class);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get FDP info: " + conn.getResponseCode());
            return null;
        }
    }

    /**
     * Get configuration details of FAIR Data Point (API server)
     *
     * @return FAIR Data Point (API server) config or null if endpoint not operable
     * @throws IOException in case of a communication error
     */
    public FDPConfigDTO getFairDataPointConfig() throws IOException {
        HttpURLConnection conn = request(url(fdpBaseURI, CONFIG_PART), "GET", MEDIA_TYPE_JSON, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET config request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received FDP config");
            return objectMapper.readValue(conn.getInputStream(), FDPConfigDTO.class);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get FDP config: " + conn.getResponseCode());
            return null;
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
        HttpURLConnection conn = request(catalogURI, "GET", MEDIA_TYPE_TURTLE, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET catalog request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received catalog from FDP");
            String actualURI = conn.getURL().toString();
            return CatalogTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get catalog: " + conn.getResponseCode());
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
        HttpURLConnection conn = request(datasetURI, "GET", MEDIA_TYPE_TURTLE, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET dataset request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received dataset from FDP");
            String actualURI = conn.getURL().toString();
            return DatasetTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get dataset: " + conn.getResponseCode());
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
        HttpURLConnection conn = request(distributionURI, "GET", MEDIA_TYPE_TURTLE, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET distribution request to FDP");

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received distribution from FDP");
            String actualURI = conn.getURL().toString();
            return DistributionTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get distribution: " + conn.getResponseCode());
            throw new FairDataPointException(conn.getResponseCode(), conn.getResponseMessage());
        }
    }

    /**
     * Get shape specs
     *
     * @param shapeName specification of layer (catalog, dataset, or distribution)
     * @return SHACL definition as string
     * @throws IOException in case of a communication error
     * @throws FairDataPointException in case that FDP responds with an unexpected code
     */
    public String getMetadataSpec(String shapeName) throws IOException, FairDataPointException {
        HttpURLConnection conn = request(url(fdpBaseURI, shapeName, SPEC_PART), "GET", MEDIA_TYPE_TURTLE, true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending GET metadata spec request to FDP - " + shapeName);

        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Received metadata specs from FDP");
            return readInputStream(conn.getInputStream());
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to get metadata specs: " + conn.getResponseCode());
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
    public CatalogDTO postCatalog(CatalogDTO catalogDTO) throws IOException, FairDataPointException {
        HttpURLConnection conn = createConnection(url(fdpBaseURI, CATALOG_PART), "POST", MEDIA_TYPE_TURTLE);
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_TURTLE);
        conn.setDoOutput(true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending POST catalog request to FDP");

        // Generate random IRI, FDP will replace it with really unique
        String uri = url(fdpBaseURI, CATALOG_PART, UUID.randomUUID().toString());
        catalogDTO.setIri(uri);

        ArrayList<Statement> statements = CatalogTransformerUtils.dto2Statements(catalogDTO);
        String metadata = serializeStatements(statements);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), CHARSET));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Created new catalog and received it as response");
            String actualURI = conn.getHeaderField(HttpHeaders.LOCATION);
            return CatalogTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to create new catalog: " + conn.getResponseCode());
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
    public DatasetDTO postDataset(DatasetDTO datasetDTO) throws IOException, FairDataPointException {
        HttpURLConnection conn = createConnection(url(fdpBaseURI, DATASET_PART), "POST", MEDIA_TYPE_TURTLE);
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_TURTLE);
        conn.setDoOutput(true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending POST dataset request to FDP");

        // Generate random IRI, FDP will replace it with really unique
        String uri = url(fdpBaseURI, DATASET_PART, UUID.randomUUID().toString());
        datasetDTO.setIri(uri);

        ArrayList<Statement> statements = DatasetTransformerUtils.dto2Statements(datasetDTO);
        String metadata = serializeStatements(statements);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), CHARSET));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Created new dataset and received it as response");
            String actualURI = conn.getHeaderField(HttpHeaders.LOCATION);
            return DatasetTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to create new dataset: " + conn.getResponseCode());
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
    public DistributionDTO postDistribution(DistributionDTO distributionDTO) throws IOException, FairDataPointException {
        HttpURLConnection conn = createConnection(url(fdpBaseURI, DISTRIBUTION_PART), "POST", MEDIA_TYPE_TURTLE);
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_TURTLE);
        conn.setDoOutput(true);
        projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Sending POST distribution request to FDP");

        // Generate random IRI, FDP will replace it with really unique
        String uri = url(fdpBaseURI, DISTRIBUTION_PART, UUID.randomUUID().toString());
        distributionDTO.setIri(uri);

        ArrayList<Statement> statements = DistributionTransformerUtils.dto2Statements(distributionDTO);
        String metadata = serializeStatements(statements);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), CHARSET));
        bw.write(metadata);
        bw.flush();
        bw.close();

        if(conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Created new distribution and received it as response");
            String actualURI = conn.getHeaderField(HttpHeaders.LOCATION);
            return DistributionTransformerUtils.statements2DTO(parseStatements(conn, actualURI), actualURI);
        } else {
            projectAudit.reportDebug(EventSource.FDP_CONNECTION, "Failed to create new distribution: " + conn.getResponseCode());
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
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        StatementCollector collector = new StatementCollector();
        parser.setRDFHandler(collector);

        parser.parse(new BufferedReader(new InputStreamReader(conn.getInputStream(), CHARSET)), uri);
        return new ArrayList<>(collector.getStatements());
    }

    /**
     * Helper for creating string from RDF statements
     *
     * @param statements list of statements
     * @return string (Turtle)
     */
    private String serializeStatements(ArrayList<Statement> statements) {
        StringWriter sw = new StringWriter();
        Rio.write(statements, sw, RDFFormat.TURTLE);
        return sw.toString();
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
        conn.addRequestProperty(HttpHeaders.ACCEPT, accept);
        conn.addRequestProperty(HttpHeaders.USER_AGENT, USER_AGENT);
        conn.addRequestProperty(HttpHeaders.CONTENT_ENCODING, CHARSET.displayName());

        if (token != null) {
            conn.addRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
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

        projectAudit.reportDebug(EventSource.FDP_CONNECTION, String.format("HTTP %s request to %s (response: %d)", method, url, conn.getResponseCode()));
        conn.connect();

        // HttpUrlConnection redirection does not work with HTTPS and has others flaws
        if (followRedirects && REDIRECTS.contains(conn.getResponseCode())) {
            HashSet<String> visited = new HashSet<>();
            visited.add(url);

            while (REDIRECTS.contains(conn.getResponseCode())) {
                String nextUrl = conn.getHeaderField(HttpHeaders.LOCATION);
                String cookies = conn.getHeaderField("Set-Cookie");

                projectAudit.reportDebug(EventSource.FDP_CONNECTION, "HTTP request (caused by redirect) to " + nextUrl);

                if (visited.contains(nextUrl)) {
                    throw new IOException("HTTP redirection loop detected");
                }
                visited.add(nextUrl);

                conn = createConnection(nextUrl, method, accept);
                conn.setRequestProperty("Cookie", cookies);

                projectAudit.reportDebug(EventSource.FDP_CONNECTION, String.format("HTTP %s request to %s (response: %d)", method, url, conn.getResponseCode()));
                conn.connect();
            }
        }
        return conn;
    }

    public static String url(String base, String ... fragment) {
        StringBuilder sb = new StringBuilder(base);
        for (String f: fragment) {
            sb.append("/").append(f);
        }
        return sb.toString();
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(CHARSET.name());
    }

    public String getBaseURI() {
        return fdpBaseURI;
    }
}
