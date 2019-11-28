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
package solutions.fairdata.openrefine.metadata.commands.response.storage;

import solutions.fairdata.openrefine.metadata.dto.storage.ExportFormatDTO;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageInfoDTO;

import java.util.List;

public class StoreDataInfoResponse {

    private String status;
    private String defaultFilename;
    private List<ExportFormatDTO> formats;
    private List<StorageInfoDTO> storages;

    public StoreDataInfoResponse() {
    }

    public StoreDataInfoResponse(String defaultFilename, List<ExportFormatDTO> formats, List<StorageInfoDTO> storages) {
        this.status = "ok";
        this.defaultFilename = defaultFilename;
        this.formats = formats;
        this.storages = storages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDefaultFilename() {
        return defaultFilename;
    }

    public void setDefaultFilename(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }

    public List<ExportFormatDTO> getFormats() {
        return formats;
    }

    public void setFormats(List<ExportFormatDTO> formats) {
        this.formats = formats;
    }

    public List<StorageInfoDTO> getStorages() {
        return storages;
    }

    public void setStorages(List<StorageInfoDTO> storages) {
        this.storages = storages;
    }
}
