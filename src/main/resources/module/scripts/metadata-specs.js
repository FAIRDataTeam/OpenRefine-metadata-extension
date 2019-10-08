
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
} ;

