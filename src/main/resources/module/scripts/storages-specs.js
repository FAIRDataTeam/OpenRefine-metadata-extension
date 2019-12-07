
let MetadataStorageSpecs = {};

MetadataStorageSpecs.types = new Map(Object.entries({
    "ftp": {
        metadata: ["filename"]
    },
    "virtuoso": {
        metadata: ["filename"]
    },
    "tripleStoreHTTP": {
        metadata: ["baseURI"]
    }
}));

MetadataStorageSpecs.metadata = new Map(Object.entries({
    "filename": {
        id: "filename",
        type: "text",
        required: "true"
    },
    "baseURI": {
        id: "baseURI",
        type: "url",
        required: "true"
    }
}));
