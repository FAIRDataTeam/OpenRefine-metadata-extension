
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
            "multiple": true,
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
            "id": "themeTaxonomy",
            "type": "iri",
            "required": true,
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
            "multiple": true,
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
            "type": "url",
            "required": false,
        },
        {
            "id": "keyword",
            "type": "url",
            "required": false,
            "multiple": true,
        },
        {
            "id": "landingPage",
            "type": "url",
            "required": false,
        },
    ]
};
