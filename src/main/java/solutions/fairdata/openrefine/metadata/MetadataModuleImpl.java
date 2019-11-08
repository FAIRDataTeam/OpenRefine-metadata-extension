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
package solutions.fairdata.openrefine.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.simile.butterfly.ButterflyModuleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.fairdata.openrefine.metadata.dto.StorageDTO;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetadataModuleImpl extends ButterflyModuleImpl {

    private static final Logger logger = LoggerFactory.getLogger("MetadataModule");

    public static final String USER_AGENT = "OpenRefine/metadata";
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static MetadataModuleImpl instance;

    private Map<String, StorageDTO> storages = new HashMap<>();

    private static void setInstance(MetadataModuleImpl metadataModule) {
        instance = metadataModule;
    }

    public static MetadataModuleImpl getInstance() {
        return instance;
    }

    @Override
    public void init(ServletConfig config) throws Exception {
        super.init(config);

        setInstance(this);

        readConfig();

        logger.trace("Metadata Extension module has been initialized");
    }

    private void readConfig() {
        File configFolderFile = new File(getPath(),"config");

        readStorageConfig(new File(configFolderFile, "storages.json"));
    }

    private void readStorageConfig(File file) {
        try {
            List<StorageDTO> configuredStorages = objectMapper.readValue(file, new TypeReference<>(){});
            storages = configuredStorages.stream().filter(s -> StorageDTO.ALLOWED_TYPES.contains(s.getType())).collect(Collectors.toMap(StorageDTO::getName, Function.identity()));
            logger.trace("Loaded storage configuration with " + storages.size() + " items");
        } catch (IOException e) {
            logger.warn("Could not load storages configuration - skipping");
        }
    }

    public Map<String, StorageDTO> getStorages() {
        return storages;
    }
}
