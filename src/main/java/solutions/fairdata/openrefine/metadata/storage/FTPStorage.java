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

import org.apache.commons.net.ftp.FTPClient;
import solutions.fairdata.openrefine.metadata.dto.storage.StorageDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

public class FTPStorage extends Storage {

    public static final String TYPE = "ftp";

    public FTPStorage(StorageDTO storageDTO) {
        super(storageDTO);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getFilePath(HashMap<String, String> metadata) {
        return storageDTO.getDirectory() + metadata.getOrDefault("filename", "");
    }

    @Override
    public String getURL(HashMap<String, String> metadata) {
        return "ftp://" + storageDTO.getHost() + getFilePath(metadata);
    }

    @Override
    public void storeData(byte[] data, HashMap<String, String> metadata, String contentType) throws IOException {
        String filename = metadata.get("filename");
        if (filename == null) {
            throw new IOException("Filename not given");
        }
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(storageDTO.getHost());
        ftpClient.login(storageDTO.getUsername(), storageDTO.getPassword());
        ftpClient.changeWorkingDirectory(storageDTO.getDirectory());
        ftpClient.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);

        try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            boolean retval = ftpClient.storeFile(filename, is);
            if (!retval) {
                throw new IOException("Storing file in FTP storage failed");
            }
        }
    }
}
