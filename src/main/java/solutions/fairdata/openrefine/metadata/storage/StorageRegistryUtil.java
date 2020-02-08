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
import solutions.fairdata.openrefine.metadata.storage.factory.FTPStorageFactory;
import solutions.fairdata.openrefine.metadata.storage.factory.StorageFactory;
import solutions.fairdata.openrefine.metadata.storage.factory.TripleStoreHTTPStorageFactory;
import solutions.fairdata.openrefine.metadata.storage.factory.VirtuosoStorageFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class StorageRegistryUtil {

    private static final HashMap<String, Storage> storages = new HashMap<>();
    private static final HashMap<String, StorageFactory> factories = new HashMap<>();

    static {
        factories.put(FTPStorage.TYPE.toLowerCase(), new FTPStorageFactory());
        factories.put(VirtuosoStorage.TYPE.toLowerCase(), new VirtuosoStorageFactory());
        factories.put(TripleStoreHTTPStorage.TYPE.toLowerCase(), new TripleStoreHTTPStorageFactory());
    }

    public static void clear() { storages.clear(); }

    public static void registerStorage(String name, Storage storage) {
        storages.put(name, storage);
    }

    public static Storage getStorage(String name) {
        return storages.get(name);
    }

    public static Set<String> getStorageNames() {
        return storages.keySet();
    }

    public static Collection<Storage> getStorages() {
        return storages.values();
    }

    public static void createAndRegisterStorageFor(StorageDTO storageDTO) throws IllegalArgumentException {
        if (!storageDTO.getEnabled()) {
            return;
        }
        StorageFactory storageFactory = factories.get(storageDTO.getType().toLowerCase());
        if (storageFactory == null) {
            throw new IllegalArgumentException("Given storage type has no implementation: " + storageDTO.getType());
        }
        Storage storage = storageFactory.createStorage(storageDTO);
        StorageRegistryUtil.registerStorage(storage.getName(), storage);
    }
}
