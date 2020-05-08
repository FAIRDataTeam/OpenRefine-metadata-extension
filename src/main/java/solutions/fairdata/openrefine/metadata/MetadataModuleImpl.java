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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.refine.io.ProjectUtilities;
import com.google.refine.model.Project;
import edu.mit.simile.butterfly.ButterflyModuleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solutions.fairdata.openrefine.metadata.dto.ProjectInfoDTO;
import solutions.fairdata.openrefine.metadata.dto.project.ProjectHistoryDTO;
import solutions.fairdata.openrefine.metadata.dto.config.SettingsConfigDTO;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;
import solutions.fairdata.openrefine.metadata.model.MetadataOverlayModel;
import solutions.fairdata.openrefine.metadata.storage.StorageRegistryUtil;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MetadataModuleImpl extends ButterflyModuleImpl {

    private static final Logger logger = LoggerFactory.getLogger("metadata");

    public static final String USER_AGENT = "OpenRefine/metadata";
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final String OVERLAY_MODEL = "metadataOverlayModel";

    private SettingsConfigDTO settings = SettingsConfigDTO.getDefaultSettings();
    private SettingsConfigDTO settingsDetails = settings.copyDetails();
    private ProjectInfoDTO projectInfo = new ProjectInfoDTO();;

    private static MetadataModuleImpl instance;

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

        loadProjectInfo();
        loadConfig();

        logger.trace("Metadata Extension module has been initialized");
    }

    public SettingsConfigDTO getSettings() {
        return settings;
    }

    public SettingsConfigDTO getSettingsDetails() {
        return settingsDetails;
    }

    public ProjectInfoDTO getProjectInfo() {
        return projectInfo;
    }

    public void loadProjectInfo() {
        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
            this.projectInfo.setName(properties.getProperty("artifactId", null));
            this.projectInfo.setVersion(properties.getProperty("version", null));
            this.projectInfo.setOpenrefineVersion(properties.getProperty("openrefineVersion", null));
            this.projectInfo.setOpenrefineSupported(
                    Arrays.asList(properties.getProperty("openrefineSupported").split("\\s+"))
            );
        } catch (IOException e) {
            logger.warn("Could not load metadata about the metadata extension: " + e.getMessage());
        }
    }

    public void clearConfig() {
        settings = SettingsConfigDTO.getDefaultSettings();
        settingsDetails = settings.copyDetails();
    }

    public void loadConfig() {
        clearConfig();
        File configFolderFile = new File(getPath(),"config");
        logger.info("Loading configuration from " + configFolderFile.toString());

        readSettingsConfig(new File(configFolderFile, "settings.yaml"));
        readStorageConfig(new File(configFolderFile, "storages.yaml"));
    }

    private void readSettingsConfig(File file) {
        try {
            settings = yamlObjectMapper.readValue(file, SettingsConfigDTO.class);
            settingsDetails = settings.copyDetails();
            logger.trace("Loaded Settings configuration from file");
        } catch (IOException e) {
            logger.warn("Could not load FDP connections configuration - skipping");
        }
    }

    private void readStorageConfig(File file) {
        try {
            StorageRegistryUtil.clear();
            List<StorageDTO> configuredStorages = yamlObjectMapper.readValue(file, new TypeReference<List<StorageDTO>>(){});
            for (StorageDTO storageDTO : configuredStorages) {
                try {
                    StorageRegistryUtil.createAndRegisterStorageFor(storageDTO);
                } catch (IllegalArgumentException e) {
                    logger.warn("Skipped storage " + storageDTO.getName() + ": " + e.getMessage());
                }
            }
            logger.trace("Loaded storage configuration with " + configuredStorages.size() + " items");
        } catch (IOException e) {
            logger.warn("Could not load storages configuration - skipping");
        }
    }

    public static MetadataOverlayModel getProjectModel(Project project) {
        MetadataOverlayModel metadataOverlayModel = (MetadataOverlayModel) project.overlayModels.get(OVERLAY_MODEL);
        if (metadataOverlayModel == null) {
            metadataOverlayModel = new MetadataOverlayModel();
            metadataOverlayModel.setProjectData(new ProjectHistoryDTO());
            project.overlayModels.put(OVERLAY_MODEL, metadataOverlayModel);
        }
        return metadataOverlayModel;
    }

    public static ProjectHistoryDTO getProjectHistoryDTO(Project project) {
        return getProjectModel(project).getProjectData();
    }

    public static void forceSaveProject(Project project) throws IOException {
        ProjectUtilities.save(project);
    }

    public static Logger getLogger() {
        return logger;
    }

}
