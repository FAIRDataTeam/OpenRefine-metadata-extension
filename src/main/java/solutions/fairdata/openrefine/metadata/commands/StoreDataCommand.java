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
package solutions.fairdata.openrefine.metadata.commands;

import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.exporters.Exporter;
import com.google.refine.exporters.ExporterRegistry;
import com.google.refine.exporters.StreamExporter;
import com.google.refine.exporters.WriterExporter;
import solutions.fairdata.openrefine.metadata.ProjectAudit;
import solutions.fairdata.openrefine.metadata.commands.exceptions.MetadataCommandException;
import solutions.fairdata.openrefine.metadata.commands.request.storage.StoreDataRequest;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.commands.response.storage.StoreDataInfoResponse;
import solutions.fairdata.openrefine.metadata.commands.response.storage.StoreDataPreviewResponse;
import solutions.fairdata.openrefine.metadata.commands.response.storage.StoreDataResponse;
import solutions.fairdata.openrefine.metadata.dto.audit.EventSource;
import solutions.fairdata.openrefine.metadata.dto.storage.ExportFormatDTO;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;
import solutions.fairdata.openrefine.metadata.storage.FTPStorage;
import solutions.fairdata.openrefine.metadata.storage.Storage;
import solutions.fairdata.openrefine.metadata.storage.StorageRegistryUtil;
import solutions.fairdata.openrefine.metadata.storage.TripleStoreHTTPStorage;
import solutions.fairdata.openrefine.metadata.storage.VirtuosoStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.refine.commands.project.ExportRowsCommand.getRequestParameters;

/**
 * Command handling storing the FAIR data
 *
 * It holds the information about supported export formats. This information
 * together with possible configured storages can be retrieved (GET). Then
 * data/project can be stored using selected format to selected storage or
 * returned in response for preview (POST).
 */
public class StoreDataCommand extends Command {

    private static HashMap<String, ExportFormatDTO> formats = new HashMap<>();

    static {
        formats.put("csv", new ExportFormatDTO("csv", "OpenRefine", "csv"));
        formats.put("tsv", new ExportFormatDTO("tsv", "OpenRefine", "tsv"));
        formats.put("xls", new ExportFormatDTO("xls", "OpenRefine", "xls"));
        formats.put("xlsx", new ExportFormatDTO("xlsx", "OpenRefine", "xlsx"));
        formats.put("ods", new ExportFormatDTO("ods", "OpenRefine", "ods"));
        formats.put("html", new ExportFormatDTO("html", "OpenRefine", "html"));
        formats.put("sql", new ExportFormatDTO("sql", "OpenRefine", "sql"));
        formats.put("rdf", new ExportFormatDTO("rdf", "rdf-extension", "rdf"));
        formats.put("Turtle", new ExportFormatDTO("Turtle", "rdf-extension", "ttl"));
    }

    private static void updateFormats() {
        for (Map.Entry<String, ExportFormatDTO> format : formats.entrySet()) {
            final Exporter e = ExporterRegistry.getExporter(format.getKey());
            if (e != null) {
                format.getValue().setContentType(e.getContentType());
                format.getValue().setUsable(true);
            } else {
                format.getValue().setUsable(false);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        pa.reportDebug(EventSource.STORAGE,"Updating possible and known export formats");
        updateFormats();

        String defaultFilename = getProject(request).getMetadata().getName().replaceAll("\\W+", "_");
        String defaultBaseURI = "http://" + request.getServerName() + "/" + defaultFilename;

        pa.reportDebug(EventSource.STORAGE,"Retrieving storage information");
        StoreDataInfoResponse storeDataInfoResponse = new StoreDataInfoResponse(
                new ArrayList<>(formats.values()),
                new ArrayList<>(StorageRegistryUtil.getStorages().stream().map(Storage::getStorageDTO).map(StorageDTO::toInfo).collect(Collectors.toList()))
        );
        storeDataInfoResponse.getDefaults().put("filename", defaultFilename);
        storeDataInfoResponse.getDefaults().put("baseURI", defaultBaseURI);
        storeDataInfoResponse.getStorageTypes().put(FTPStorage.TYPE, FTPStorage.DETAILS);
        storeDataInfoResponse.getStorageTypes().put(VirtuosoStorage.TYPE, VirtuosoStorage.DETAILS);
        storeDataInfoResponse.getStorageTypes().put(TripleStoreHTTPStorage.TYPE, TripleStoreHTTPStorage.DETAILS);
        CommandUtils.objectMapper.writeValue(w, storeDataInfoResponse);

        w.flush();
        w.close();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StoreDataRequest storeDataRequest = CommandUtils.objectMapper.readValue(request.getReader(), StoreDataRequest.class);
        Writer w = CommandUtils.prepareWriter(response);
        ProjectAudit pa = new ProjectAudit(getProject(request));

        try {
            pa.reportDebug(EventSource.STORAGE,"Preparing information for storing data");
            Engine engine = getEngine(request, getProject(request));
            Properties params = getRequestParameters(request);
            String defaultFilename = getProject(request).getMetadata().getName().replaceAll("\\W+", "_");

            ExportFormatDTO format = formats.get(storeDataRequest.getFormat());
            Exporter exporter = ExporterRegistry.getExporter(storeDataRequest.getFormat());
            Storage storage = StorageRegistryUtil.getStorage(storeDataRequest.getStorage(), storeDataRequest.getCustom());

            if (exporter == null) {
                pa.reportWarning(EventSource.STORAGE,"Unknown export format requested");
                throw new MetadataCommandException("store-data-dialog/error/unknown-export-format");
            }
            else if (storage == null) {
                pa.reportWarning(EventSource.STORAGE,"Unknown storage requested");
                throw new MetadataCommandException("store-data-dialog/error/unknown-storage");
            }
            else if (storage.forbidsContentType(exporter.getContentType())) {
                pa.reportWarning(EventSource.STORAGE,"Requested content-type is forbidden for selected storage");
                throw new MetadataCommandException("store-data-dialog/error/unsupported-type");
            }

            try (
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
            ) {
                if (exporter instanceof WriterExporter) {
                    WriterExporter writerExporter = (WriterExporter) exporter;
                    writerExporter.export(getProject(request), params, engine, writer);
                } else if (exporter instanceof StreamExporter) {
                    StreamExporter streamExporter = (StreamExporter) exporter;
                    streamExporter.export(getProject(request), params, engine, stream);
                } else {
                    pa.reportWarning(EventSource.STORAGE,"Failed to export to selected format");
                    throw new MetadataCommandException("store-data-dialog/error/unusable-exporter");
                }

                byte[] data = stream.toByteArray();

                storeDataRequest.getMetadata().put(
                        "filenameExt",
                        storeDataRequest.getMetadata().getOrDefault("filename", defaultFilename) + "." + format.getExtension()
                );
                String filename = storeDataRequest.getMetadata().get("filenameExt");

                if (storeDataRequest.getMode().equals("preview")) {
                    pa.reportInfo(EventSource.STORAGE,"Preparing preview of data: " + filename);
                    pa.reportDebug(EventSource.STORAGE,"File size is (bytes): " + data.length);
                    pa.reportDebug(EventSource.STORAGE,"File content-type is: " + exporter.getContentType());
                    String base64Data = Base64.getEncoder().encodeToString(data);
                    CommandUtils.objectMapper.writeValue(w,
                            new StoreDataPreviewResponse(filename, exporter.getContentType(), base64Data)
                    );
                } else {
                    if (storage.forbidsName(filename)) {
                        pa.reportWarning(EventSource.STORAGE,"Filename violates storage requirements");
                        throw new MetadataCommandException("store-data-dialog/error/naming-violation");
                    }
                    else if (storage.forbidsByteSize(data.length)) {
                        pa.reportWarning(EventSource.STORAGE,"File is too big for selected storage");
                        throw new MetadataCommandException("store-data-dialog/error/too-big");
                    }
                    pa.reportInfo(EventSource.STORAGE,"Storing data: " + filename);
                    pa.reportDebug(EventSource.STORAGE,"File size is (bytes): " + data.length);
                    pa.reportDebug(EventSource.STORAGE,"File content-type is: " + exporter.getContentType());

                    storage.storeData(data, storeDataRequest.getMetadata(), exporter.getContentType());
                    CommandUtils.objectMapper.writeValue(w,
                            new StoreDataResponse(storage.getURL(storeDataRequest.getMetadata()), exporter.getContentType(), data.length)
                    );
                }
            }
        } catch (MetadataCommandException e) {
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse(e.getMessage(), e));
        } catch (Exception e) {
            pa.reportError(EventSource.STORAGE,"Unable to store data");
            pa.reportTrace(EventSource.STORAGE, e);
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("store-data-dialog/error/exporting", e));
        } finally {
            w.flush();
            w.close();
        }
    }
}
