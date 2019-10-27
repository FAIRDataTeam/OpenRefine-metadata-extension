/* global $, DOM, DialogSystem, MetadataHelpers, MetadataFormDialog, MetadataSpecs */

class PostFdpDialog {
    // TODO: refactor - separate Model and APIClient

    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.token = null;

        this.metadata = {
            fdp: null,
            catalogs: new Map(),
            datasets: new Map(),
            distributions: new Map()
        };
        this.newlyCreatedIRIs = new Set();

        this.initBasicTexts();
        this.resetDefault();

        this.focusBaseURI();
        this.bindActions();
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    focusBaseURI() {
        // Focus value of FDP URI input for easy overwrite
        this.elements.baseURI.focus();
        this.elements.baseURI[0].setSelectionRange(0, this.elements.baseURI.val().length);
    }

    bindActions() {
        const self = this;
        const elmts = this.elements;

        elmts.closeButton.click(() => { self.dismiss(); });

        elmts.connectButton.click(() => {
            const fdpUri = elmts.baseURI.val();
            const username = elmts.username.val();
            const password = elmts.password.val();

            self.resetDefault();
            self.ajaxConnectFDP(fdpUri, username, password);
        });

        elmts.catalogSelect.on("change", () => {
            const fdpUri = elmts.baseURI.val();
            const catalogUri = elmts.catalogSelect.val();

            self.resetDatasetLayer();
            self.ajaxDatasets(fdpUri, catalogUri);
        });

        elmts.datasetSelect.on("change", () => {
            const fdpUri = elmts.baseURI.val();
            const datasetUri = elmts.datasetSelect.val();

            self.resetDistributionLayer();
            self.ajaxDistributions(fdpUri, datasetUri);
        });

        elmts.catalogAddButton.click(() => {
            const fdpUri = elmts.baseURI.val();
            const token = this.token;
            MetadataFormDialog.createAndLaunch("catalog", MetadataSpecs.catalog,
                (catalog, formDialog) => {
                    const self = this;
                    const catalogPostRequest = JSON.stringify({
                        fdpUri, token, catalog
                    });
                    MetadataHelpers.ajax("catalogs-metadata", "POST", catalogPostRequest,
                        (result) => {
                            if (result.status === "ok") {
                                this.newlyCreatedIRIs.add(result.catalog.iri);
                                self.resetCatalogLayer();
                                self.ajaxCatalogs(fdpUri, [
                                    () => { elmts.catalogSelect.val(result.catalog.iri).trigger("change"); }
                                ]);
                                formDialog.dismiss();
                            } else {
                                formDialog.displayError(result.exception);
                            }
                        },
                        () => {
                            formDialog.displayError($.i18n("metadata-post/error-general"));
                        }
                    );
                },
                {
                    parentFDP: fdpUri
                }
            );
        });

        elmts.datasetAddButton.click(() => {
            const fdpUri = elmts.baseURI.val();
            const catalogUri = elmts.catalogSelect.val();
            const token = this.token;
            MetadataFormDialog.createAndLaunch("dataset", MetadataSpecs.dataset,
                (dataset, formDialog) => {
                    const self = this;
                    const datasetPostRequest = JSON.stringify({
                        fdpUri, token, dataset
                    });
                    this.ajaxGeneric("datasets-metadata", "POST", datasetPostRequest,
                        (result) => {
                            if (result.status === "ok") {
                                this.newlyCreatedIRIs.add(result.dataset.iri);
                                self.resetDatasetLayer();
                                self.ajaxDatasets(fdpUri, catalogUri, [
                                    () => { elmts.datasetSelect.val(result.dataset.iri).trigger("change"); }
                                ]);
                                formDialog.dismiss();
                            } else {
                                console.log("Error occured while POSTing dataset");
                            }
                        }
                    );
                },
                {
                    parentCatalog: catalogUri
                }
            );
        });

        elmts.distributionAddButton.click(() => {
            const fdpUri = elmts.baseURI.val();
            const datasetUri = elmts.datasetSelect.val();
            const token = this.token;
            MetadataFormDialog.createAndLaunch("distribution", MetadataSpecs.distribution,
                (distribution, formDialog) => {
                    const self = this;
                    const distributionPostRequest = JSON.stringify({
                        fdpUri, token, distribution
                    });
                    this.ajaxGeneric("distributions-metadata", "POST", distributionPostRequest,
                        (result) => {
                            if (result.status === "ok") {
                                this.newlyCreatedIRIs.add(result.distribution.iri);
                                self.resetDistributionLayer();
                                self.ajaxDistributions(fdpUri, datasetUri);
                                formDialog.dismiss();
                            } else {
                                console.log("Error occured while POSTing dataset");
                            }
                        }
                    );
                },
                {
                    parentDataset: datasetUri
                }
            );
        });
    }

    initBasicTexts() {
        this.frame.i18n();
        this.elements.baseURI.attr("title", $.i18n("post-fdp-dialog/description"));
    }

    // resetting
    resetDefault() {
        this.elements.dialogBody.find(".default-clear").empty();
        this.elements.dialogBody.find(".default-hidden").addClass("hidden");
        this.resetCatalogLayer();
    }

    resetCatalogLayer() {
        this.metadata.catalogs.clear();
        this.constructor.resetSelect(this.elements.catalogSelect, "catalog");
        this.elements.catalogLayer.addClass("hidden");

        this.resetDatasetLayer();
        this.resetDistributionLayer();
    }

    resetDatasetLayer() {
        this.metadata.datasets.clear();
        this.constructor.resetSelect(this.elements.datasetSelect, "dataset");
        this.elements.datasetLayer.addClass("hidden");

        this.resetDistributionLayer();
    }

    resetDistributionLayer() {
        this.metadata.distributions.clear();
        this.elements.distributionsList.empty();
        this.elements.distributionLayer.addClass("hidden");
    }

    // ajax
    ajaxConnectFDP(fdpUri, username, password, callbacks) {
        const self = this;
        callbacks = callbacks || [];
        const authRequest = JSON.stringify({
            fdpUri,
            authDTO: {
                username,
                password
            }
        });
        this.ajaxGeneric("fdp-auth", "POST", authRequest,
            (result) => {
                self.token = result.token;
                self.ajaxFDPMetadata(fdpUri);
                callbacks.forEach((callback) => { callback(result); });
            }
        );
    }

    ajaxGeneric(command, method, data, callback) {
        const self = this;
        MetadataHelpers.ajax(command, method, data,
            (result) => {
                if (result.status === "ok") {
                    callback(result);
                } else {
                    self.elements.fdpConnected.addClass("hidden");
                    self.elements.fdpConnectionError.removeClass("hidden");
                    self.elements.warningsArea.text($.i18n(result.message));
                }
            },
            () => {
                self.elements.fdpConnected.addClass("hidden");
                self.elements.fdpConnectionError.removeClass("hidden");
                self.elements.warningsArea.text($.i18n("connect-fdp-command/error"));
            }
        );
    }

    ajaxFDPMetadata(fdpUri) {
        const self = this;
        this.ajaxGeneric("fdp-metadata", "GET", { fdpUri },
            (result) => {
                self.elements.fdpConnected.removeClass("hidden");
                self.metadata.fdp = result.fdpMetadata;
                self.showFDPMetadata();

                self.resetCatalogLayer();
                self.ajaxCatalogs(fdpUri);
            }
        );
    }

    ajaxCatalogs(fdpUri, callbacks) {
        const self = this;
        callbacks = callbacks || [];
        this.ajaxGeneric("catalogs-metadata", "GET", { fdpUri },
            (result) => {
                self.metadata.catalogs.clear();
                result.catalogs.forEach((catalog) => {
                    self.metadata.catalogs.set(catalog.iri, catalog);
                });
                self.showCatalogs();
                callbacks.forEach((callback) => { callback(result); });
            }
        );
    }

    ajaxDatasets(fdpUri, catalogUri, callbacks) {
        const self = this;
        callbacks = callbacks || [];
        this.ajaxGeneric("datasets-metadata", "GET", { fdpUri, catalogUri },
            (result) => {
                self.metadata.datasets.clear();
                result.datasets.forEach((dataset) => {
                    self.metadata.datasets.set(dataset.iri, dataset);
                });
                self.showDatasets();
                callbacks.forEach((callback) => { callback(result); });
            }
        );
    }

    ajaxDistributions(fdpUri, datasetUri, callbacks) {
        const self = this;
        callbacks = callbacks || [];
        this.ajaxGeneric("distributions-metadata", "GET", { fdpUri, datasetUri },
            (result) => {
                self.metadata.distributions.clear();
                result.distributions.forEach((distribution) => {
                    self.metadata.distributions.set(distribution.iri, distribution);
                });
                self.showDistributions();
                callbacks.forEach((callback) => { callback(result); });
            }
        );
    }

    // show parts
    showFDPMetadata() {
        const fdpMetadata = this.metadata.fdp;
        this.elements.fdpMetadata.empty();
        let title = $("<a>")
            .attr("href", fdpMetadata.iri)
            .attr("target", "_blank")
            .text(fdpMetadata.title)
            .get(0).outerHTML;
        let publisher = $("<a>")
            .attr("href", fdpMetadata.publisher)
            .attr("target", "_blank")
            .text(fdpMetadata.publisherName)
            .get(0).outerHTML;

        this.elements.fdpMetadata.append($("<p>")
            .append($.i18n("post-fdp-dialog/connected-to-fdp"))
            .append(" \"" + title + "\" ")
            .append($.i18n("post-fdp-dialog/published-by"))
            .append(" " + publisher + ".")
        );
    }

    showMetadataSelect(select, metadatas) {
        metadatas.forEach((metadata) => {
            const isNew = this.newlyCreatedIRIs.has(metadata.iri);
            select.append(
                $("<option>")
                    .addClass("from-fdp")
                    .addClass(isNew ? "new" : "original")
                    .attr("value", metadata.iri)
                    .text(isNew ? `${metadata.title} [new]` : metadata.title)
            );
        });
    }

    showCatalogs() {
        this.constructor.resetSelect(this.elements.catalogSelect, "catalog");
        this.showMetadataSelect(
            this.elements.catalogSelect,
            this.metadata.catalogs
        );
        this.elements.catalogLayer.removeClass("hidden");
    }

    showDatasets() {
        this.constructor.resetSelect(this.elements.datasetSelect, "dataset");
        this.showMetadataSelect(
            this.elements.datasetSelect,
            this.metadata.datasets
        );
        this.elements.datasetLayer.removeClass("hidden");
    }

    showDistributions() {
        this.elements.distributionsList.empty();
        this.metadata.distributions.forEach((distribution) => {
            const isNew = this.newlyCreatedIRIs.has(distribution.iri);
            let text = `${distribution.title} (version: ${distribution.version})`;
            if (isNew) {
                text = `${text} [new]`;
            }
            const item = $("<li>")
                .addClass("distribution-item")
                .attr("id", distribution.id)
                .text(text);
            this.elements.distributionsList.append(item);
        });
        this.elements.distributionLayer.removeClass("hidden");
    }

    // generic helpers
    static createSelectOption(name) {
        return $("<option>")
            .prop("disabled", true)
            .prop("selected", true)
            .text(`-- select a ${name} --`);
    }

    static resetSelect(select, name) {
        select.empty();
        select.append(this.createSelectOption(name));
    }

    // launcher
    static createAndLaunch() {
        const dialog = new PostFdpDialog();
        dialog.launch();
    }
}
