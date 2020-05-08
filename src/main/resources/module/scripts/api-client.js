/* global MetadataHelpers */

class MetadataApiClient {

    constructor() {
        this.fdpUri = null;
        this.repositoryUri = null;
        this.token = null;
        this.fdpConfig = null;
        this.fdpInfo = null;
    }

    connectCustomFDP(fdpUri, email, password, callbacks, errorCallbacks) {
        this.connectFDP("custom", null, fdpUri, email, password, callbacks, errorCallbacks);
    }

    connectPreConfiguredFDP(configId, callbacks, errorCallbacks) {
        this.connectFDP("configured", configId, null, null, null, callbacks, errorCallbacks);
    }

    connectFDP(mode, configId, fdpUri, email, password, callbacks, errorCallbacks) {
        callbacks = callbacks || [];
        this._ajaxGeneric("fdp-auth", "POST",
            JSON.stringify({
                mode,
                configId,
                fdpUri,
                authDTO: {
                    email,
                    password,
                }
            }),
            [(result) => {
                this.fdpUri = result.fdpUri;
                this.repositoryUri = result.fdpConfig.persistentUrl;
                this.token = result.token;
                this.fdpConfig = result.fdpConfig;
                this.fdpInfo = result.fdpInfo;
                this.getFDPMetadata(callbacks, errorCallbacks);
            }],
            errorCallbacks
        );
    }

    getSettings(callbacks) {
        this._ajaxGeneric("settings","GET", {}, callbacks, []);
    }

    getStorageInfo(callbacks) {
        this._ajaxGeneric("store-data", "GET", {}, callbacks, []);
    }

    postSettings(projectData, callbacks) {
        const settingsPostRequest = JSON.stringify(projectData);
        this._ajaxGeneric("settings", "POST", settingsPostRequest, callbacks, [], true);
    }

    clearProjectHistory(callbacks) {
        this._ajaxGeneric("settings", "DELETE", {}, callbacks, [], true);
    }

    getAuditLog(callbacks) {
        this._ajaxGeneric("audit", "GET", {}, callbacks, []);
    }

    clearAuditLog(callbacks) {
        this._ajaxGeneric("audit", "DELETE", {}, callbacks, [], true);
    }

    postAuditEntry(eventType, message, callbacks) {
        const entryData = JSON.stringify({ eventType, message });
        this._ajaxGeneric("audit","POST", entryData, callbacks, [], true);
    }

    getDashboard(callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, token: this.token };
        this._ajaxGeneric("fdp-dashboard", "GET", params, callbacks, errorCallbacks);
    }

    getFDPMetadata(callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, repositoryUri: this.repositoryUri, token: this.token };
        this._ajaxGeneric("fdp-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getCatalogs(callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, repositoryUri: this.repositoryUri, token: this.token };
        this._ajaxGeneric("catalogs-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getDatasets(catalogUri, callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, repositoryUri: this.repositoryUri, token: this.token, catalogUri };
        this._ajaxGeneric("datasets-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getDistributions(datasetUri, callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, repositoryUri: this.repositoryUri, token: this.token, datasetUri };
        this._ajaxGeneric("distributions-metadata", "GET", params, callbacks, errorCallbacks);
    }

    getMetadataSpec(type, callbacks, errorCallbacks) {
        const params = { fdpUri: this.fdpUri, token: this.token, type };
        this._ajaxGeneric("metadata-specs", "GET", params, callbacks, errorCallbacks);
    }

    getTypehints(name, query, callbacks, errorCallbacks) {
        this._ajaxGeneric("typehints", "GET", { name, query }, callbacks, errorCallbacks, true);
    }

    postCatalog(catalog, callbacks, errorCallbacks) {
        const catalogPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this.token, catalog
        });
        this._ajaxGeneric("catalogs-metadata", "POST", catalogPostRequest, callbacks, errorCallbacks);
    }

    postDataset(dataset, callbacks, errorCallbacks) {
        const datasetPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this.token, dataset
        });
        this._ajaxGeneric("datasets-metadata", "POST", datasetPostRequest, callbacks, errorCallbacks);
    }

    postDistribution(distribution, callbacks, errorCallbacks) {
        const distributionPostRequest = JSON.stringify({
            fdpUri: this.fdpUri, token: this.token, distribution
        });
        this._ajaxGeneric("distributions-metadata", "POST", distributionPostRequest, callbacks, errorCallbacks);
    }

    hasFDPInfo() {
        return this.fdpUri !== null && (this.fdpConfig !== null || this.fdpInfo !== null);
    }

    // helpers
    _ajaxGeneric(command, method, data, callbacks, errorCallbacks, hideProgress) {
        callbacks = callbacks || [];
        errorCallbacks = errorCallbacks || [];

        MetadataHelpers.ajax(command, method, data,
            (result) => {
                callbacks.forEach((callback) => { callback(result); });
            },
            () => {
                errorCallbacks.forEach((callback) => { callback(); });
            },
            {},
            hideProgress
        );
    }
}
