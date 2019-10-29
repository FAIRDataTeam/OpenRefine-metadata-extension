/* global $, MetadataHelpers */

class MetadataApiClient {

    constructor(fdpUri, username, password) {
        this.fdpUri = fdpUri;
        this.username = username;
        this.password = password;
        this.token = null;
    }

    connectFDP(callbacks, errorCallbacks) {
        callbacks = callbacks || [];
        this._ajaxGeneric("fdp-auth", "POST",
            JSON.stringify({
                fdpUri: this.fdpUri,
                authDTO: {
                    username: this.username,
                    password: this.password
                }
            }),
            [(result) => {
                this.token = result.token;
                this.getFDPMetadata(callbacks, errorCallbacks);
            }],
            errorCallbacks
        );
    }

    getFDPMetadata(callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri};
        this._ajaxGeneric("fdp-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getCatalogs(callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri};
        this._ajaxGeneric("catalogs-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getDatasets(catalogUri, callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, catalogUri };
        this._ajaxGeneric("datasets-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getDistributions(datasetUri, callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, datasetUri };
        this._ajaxGeneric("distributions-metadata", "GET", params, callbacks, errorCallbacks);
    }

    postCatalog(catalog, callbacks, errorCallbacks) {
        const catalogPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this. token, catalog
        });
        this._ajaxGeneric("catalogs-metadata", "POST", catalogPostRequest, callbacks, errorCallbacks);
    }

    postDataset(dataset, callbacks, errorCallbacks) {
        const datasetPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this. token, dataset
        });
        this._ajaxGeneric("datasets-metadata", "POST", datasetPostRequest, callbacks, errorCallbacks);
    }

    postDistribution(distribution, callbacks, errorCallbacks) {
        const distributionPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this. token, distribution
        });
        this._ajaxGeneric("distributions-metadata", "POST", distributionPostRequest, callbacks, errorCallbacks);
    }

    // helpers
    _ajaxGeneric(command, method, data, callbacks, errorCallbacks) {
        callbacks = callbacks || [];
        errorCallbacks = errorCallbacks || [];

        MetadataHelpers.ajax(command, method, data,
            (result) => {
                callbacks.forEach((callback) => { callback(result); });
            },
            () => {
                errorCallbacks.forEach((callback) => { callback(); });
            }
        );
    }
}
