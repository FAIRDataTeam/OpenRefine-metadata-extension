package solutions.fairdata.openrefine.metadata.dto;

import java.util.Set;

public class StorageInfoDTO {

    private String name;
    private String type;
    private String host;
    private String directory;
    private Set<String> contentTypes;

    public StorageInfoDTO(StorageDTO storageDTO) {
        this(
                storageDTO.getName(),
                storageDTO.getType(),
                storageDTO.getHost(),
                storageDTO.getDirectory(),
                storageDTO.getContentTypes()
        );
    }

    public StorageInfoDTO(String name, String type, String host, String directory, Set<String> contentTypes) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.directory = directory;
        this.contentTypes = contentTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Set<String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(Set<String> contentTypes) {
        this.contentTypes = contentTypes;
    }
}
