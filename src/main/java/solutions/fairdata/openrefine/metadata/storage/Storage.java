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

import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

abstract public class Storage {

    protected StorageDTO storageDTO;

    public Storage(StorageDTO storageDTO) {
        this.storageDTO = storageDTO;
    }

    public StorageDTO getStorageDTO() {
        return storageDTO;
    }

    public String getName() {
        return getStorageDTO().getName();
    }

    /**
     * @return storage type name
     */
    public abstract String getType();

    /**
     * @return list of details for storage connection
     */
    public abstract List<String> getDetailNames();

    /**
     * Get file path (in the storage) for reference stored daty
     *
     * @param metadata metadata about the data to be stored
     * @return file path of data (if stored)
     */
    public abstract String getFilePath(HashMap<String, String> metadata);

    /**
     * Get URL for reference stored data
     *
     * @param metadata metadata about the data to be stored
     * @return URL of data (if stored)
     */
    public abstract String getURL(HashMap<String, String> metadata);

    /**
     * Store given data described by metadata and in given content type to the storage
     *
     * @param data data to be stored
     * @param metadata metadata about the given data
     * @param contentType content type of the given data
     * @throws IOException in case of communication error
     */
    public abstract void storeData(byte[] data, HashMap<String, String> metadata, String contentType) throws IOException;

    /**
     * Check whether the storage forbids (or allows) the content type
     *
     * @param contentType content type of data
     * @return if is forbidden
     */
    public boolean forbidsContentType(String contentType) {
        if (storageDTO == null || storageDTO.getContentTypes() == null) {
            return false;
        }
        return !storageDTO.getContentTypes().contains(contentType);
    }

    public boolean forbidsName(String filename) {
        if (storageDTO == null || storageDTO.getFilenamePatterns() == null || storageDTO.getFilenamePatterns().isEmpty()) {
            return false;
        }
        for (String pattern : storageDTO.getFilenamePatterns()) {
            if (FileSystems.getDefault().getPathMatcher("glob:"+pattern).matches(Paths.get(filename))) {
                return false;
            }
        }
        return true;
    }

    public boolean forbidsByteSize(int byteSize) {
        if (storageDTO == null || storageDTO.getMaxByteSize() == null) {
            return false;
        }
        return byteSize > storageDTO.getMaxByteSize();
    }
}
