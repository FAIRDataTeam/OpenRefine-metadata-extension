
let MetadataSpecs = {};

MetadataSpecs.catalog = {
    "id": "catalog",
    "fields": [
        {
            "id": "title",
            "type": "string",
            "required": true,
        },
        {
            "id": "version",
            "type": "string",
            "required": true,
        },
        {
            "id": "publisher",
            "type": "iri",
            "required": true,
            "nested": {
                "fields": [
                    {
                        "id": "publisherName",
                        "type": "string",
                        "required": true,
                    },
                ]
            }
        },
        {
            "id": "description",
            "type": "text",
            "required": false,
        },
        {
            "id": "language",
            "type": "iri",
            "required": false,
            "typehints": {
                "name": "language",
                "type": "static"
            },
            //"multiple": true,
        },
        {
            "id": "license",
            "type": "iri",
            "required": false,
            "typehints": {
                "name": "license",
                "type": "static"
            },
        },
        {
            "id": "rights",
            "type": "iri",
            "required": false,
        },
        {
            "id": "homepage",
            "type": "iri",
            "required": false,
        },
        {
            "id": "themeTaxonomies",
            "type": "iri",
            "required": true,
            "multiple": true,
        },
        {
            "id": "parent",
            "type": "iri",
            "required": true,
            "hidden": true
        },
    ]
};

MetadataSpecs.dataset = {
    "id": "dataset",
    "fields": [
        {
            "id": "title",
            "type": "string",
            "required": true,
        },
        {
            "id": "version",
            "type": "string",
            "required": true,
        },
        {
            "id": "publisher",
            "type": "iri",
            "required": true,
            "nested": {
                "fields": [
                    {
                        "id": "publisherName",
                        "type": "string",
                        "required": true,
                    },
                ]
            }
        },
        {
            "id": "description",
            "type": "text",
            "required": false,
        },
        {
            "id": "language",
            "type": "iri",
            "required": false,
            "typehints": {
                "name": "language",
                "type": "static"
            },
            //"multiple": true,
        },
        {
            "id": "license",
            "type": "iri",
            "required": false,
            "typehints": {
                "name": "license",
                "type": "static"
            },
        },
        {
            "id": "rights",
            "type": "iri",
            "required": false,
        },
        {
            "id": "themes",
            "type": "iri",
            "required": true,
            "multiple": true,
            "typehints": {
                "name": "theme",
                "type": "dynamic"
            },
        },
        {
            "id": "contactPoint",
            "type": "iri",
            "required": false,
        },
        {
            "id": "keywords",
            "type": "string",
            "required": false,
            "multiple": true,
        },
        {
            "id": "landingPage",
            "type": "iri",
            "required": false,
        },
        {
            "id": "parent",
            "type": "iri",
            "required": true,
            "hidden": true
        },
    ]
};

MetadataSpecs.distribution = {
    "id": "distribution",
    "storeData": {
        "inline": "targetUrl",
        "target":  "downloadUrl",
        "others": {
            "targetUrl-downloadUrl": true
        }
    },
    "fields": [
        {
            "id": "title",
            "type": "string",
            "required": true,
        },
        {
            "id": "license",
            "type": "iri",
            "required": true,
            "typehints": {
                "name": "license",
                "type": "static"
            },
        },
        {
            "id": "version",
            "type": "string",
            "required": true,
        },
        {
            "id": "publisher",
            "type": "iri",
            "required": true,
            "nested": {
                "fields": [
                    {
                        "id": "publisherName",
                        "type": "string",
                        "required": true,
                    },
                ]
            }
        },
        {
            "id": "rights",
            "type": "iri",
            "required": false,
        },
        {
            "id": "description",
            "type": "text",
            "required": false,
        },
        {
            "id": "format",
            "type": "string",
            "required": false
        },
        {
            "id": "bytesize",
            "type": "string",
            "required": false,
        },
        {
            "id": "mediaType",
            "type": "string",
            "required": true,
            "typehints": {
                "name": "mediaType",
                "type": "static"
            }
        },
        {
            "id": "targetUrl",
            "type": "xor",
            "required": true,
            "options": [
                {
                    "id": "downloadUrl",
                    "type": "iri"
                },
                {
                    "id": "accessUrl",
                    "type": "iri"
                },
            ]
        },
        {
            "id": "parent",
            "type": "iri",
            "required": true,
            "hidden": true
        },
    ]
};
