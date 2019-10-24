
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
                        "id": "publisher-name",
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
            //"multiple": true,
        },
        {
            "id": "license",
            "type": "iri",
            "required": false,
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
            "id": "parentFDP",
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
                        "id": "publisher-name",
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
            //"multiple": true,
        },
        {
            "id": "license",
            "type": "iri",
            "required": false,
        },
        {
            "id": "rights",
            "type": "iri",
            "required": false,
        },
        {
            "id": "theme",
            "type": "iri",
            "required": true,
            "multiple": true,
        },
        {
            "id": "contactPoint",
            "type": "iri",
            "required": false,
        },
        {
            "id": "keyword",
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
            "id": "parentCatalog",
            "type": "iri",
            "required": true,
            "hidden": true
        },
    ]
};

MetadataSpecs.distribution = {
    "id": "distribution",
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
        },
        {
            "id": "version",
            "type": "string",
            "required": true,
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
            "required": true
        },
        {
            "id": "targetUrl",
            "type": "xor",
            "required": true,
            "xor": [
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
            "id": "parentDataset",
            "type": "iri",
            "required": true,
            "hidden": true
        },
    ]
};
