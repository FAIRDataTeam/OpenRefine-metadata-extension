/* global $, DOM, DialogSystem, MetadataHelpers, MetadataFormDialog, MetadataSpecs, MetadataApiClient */

class PostFdpDialog {

    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.apiClient = new MetadataApiClient();

        // Model
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
            const email = elmts.email.val();
            const password = elmts.password.val();

            self.resetDefault();
            self.apiClient.connectFDP(
                fdpUri, email, password,
                [this.callbackFDPConnected(), this.callbackErrorResponse()],
                [this.callbackGeneralError()]
            );
        });

        elmts.catalogSelect.on("change", () => {
            self.resetDatasetLayer();
            self.apiClient.getDatasets(
                elmts.catalogSelect.val(),
                [this.callbackDatasets(), this.callbackErrorResponse()],
                [this.callbackGeneralError()]
            );
        });

        elmts.datasetSelect.on("change", () => {
            self.resetDistributionLayer();
            self.apiClient.getDistributions(
                elmts.datasetSelect.val(),
                [this.callbackDistributions(), this.callbackErrorResponse()],
                [this.callbackGeneralError()]
            );
        });

        elmts.catalogAddButton.click(() => {
            MetadataFormDialog.createAndLaunch("catalog", MetadataSpecs.catalog,
                (catalog, formDialog) => {
                    this.apiClient.postCatalog(
                        catalog,
                        [this.callbackPostCatalog(formDialog)],
                        [this.callbackPostError(formDialog)]
                    );
                },
                {
                    parentFDP: elmts.baseURI.val()
                }
            );
        });

        elmts.datasetAddButton.click(() => {
            const catalogUri = this.elements.catalogSelect.val();
            MetadataFormDialog.createAndLaunch("dataset", MetadataSpecs.dataset,
                (dataset, formDialog) => {
                    this.apiClient.postDataset(
                        dataset,
                        [this.callbackPostDataset(formDialog, catalogUri)],
                        [this.callbackPostError(formDialog)]
                    );
                },
                {
                    parentCatalog: catalogUri
                }
            );
        });

        elmts.distributionAddButton.click(() => {
            const datasetUri = elmts.datasetSelect.val();
            MetadataFormDialog.createAndLaunch("distribution", MetadataSpecs.distribution,
                (distribution, formDialog) => {
                    this.apiClient.postDistribution(
                        distribution,
                        [this.callbackPostDistribution(formDialog, datasetUri)],
                        [this.callbackPostError(formDialog)]
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

    // callbacks (factories)
    callbackGeneralError() {
        return () => {
            this.elements.fdpConnected.addClass("hidden");
            this.elements.fdpConnectionError.removeClass("hidden");
            this.elements.warningsArea.text($.i18n("connect-fdp-command/error"));
        };
    }

    callbackErrorResponse(result) {
        return (result) => {
            if (result.status !== "ok") {
                this.elements.fdpConnected.addClass("hidden");
                this.elements.fdpConnectionError.removeClass("hidden");
                this.elements.warningsArea.text($.i18n(result.message));
            }
        };
    }

    callbackFDPConnected() {
        return (result) => {
            if (result.status === "ok") {
                this.elements.fdpConnected.removeClass("hidden");
                this.metadata.fdp = result.fdpMetadata;
                this.showFDPMetadata();

                this.resetCatalogLayer();
                this.apiClient.getCatalogs(
                    [this.callbackCatalogs(), this.callbackErrorResponse()],
                    [this.callbackGeneralError()]
                );
            }
        };
    }

    callbackCatalogs() {
        return (result) => {
            if (result.status === "ok") {
                this.metadata.catalogs.clear();
                result.catalogs.forEach((catalog) => {
                    this.metadata.catalogs.set(catalog.iri, catalog);
                });
                this.showCatalogs();
            }
        };
    }

    callbackDatasets() {
        return (result) => {
            if (result.status === "ok") {
                this.metadata.datasets.clear();
                result.datasets.forEach((dataset) => {
                    this.metadata.datasets.set(dataset.iri, dataset);
                });
                this.showDatasets();
            }
        };
    }

    callbackDistributions() {
        return (result) => {
            if (result.status === "ok") {
                this.metadata.distributions.clear();
                result.distributions.forEach((distribution) => {
                    this.metadata.distributions.set(distribution.iri, distribution);
                });
                this.showDistributions();
            }
        };
    }

    callbackPostError(formDialog) {
        return () => {
            formDialog.displayError($.i18n("metadata-post/error-general"));
        };
    }

    callbackPostCatalog(formDialog) {
        return (result) => {
            if (result.status === "ok") {
                this.newlyCreatedIRIs.add(result.catalog.iri);
                this.resetCatalogLayer();
                this.apiClient.getCatalogs(
                    [
                        this.callbackCatalogs(),
                        () => { this.elements.catalogSelect.val(result.catalog.iri).trigger("change"); }
                    ],
                    [ this.callbackGeneralError() ]
                );
                formDialog.dismiss();
            } else {
                formDialog.displayError(result.exceptionName, result.exception);
            }
        };
    }

    callbackPostDataset(formDialog, catalogUri) {
        return (result) => {
            if (result.status === "ok") {
                this.newlyCreatedIRIs.add(result.dataset.iri);
                this.resetDatasetLayer();
                this.apiClient.getDatasets(
                    catalogUri,
                    [
                        this.callbackDatasets(),
                        () => { this.elements.datasetSelect.val(result.dataset.iri).trigger("change"); }
                    ],
                    [ this.callbackGeneralError() ]
                );
                formDialog.dismiss();
            } else {
                formDialog.displayError(result.exceptionName, result.exception);
            }
        };
    }

    callbackPostDistribution(formDialog, datasetUri) {
        return (result) => {
            if (result.status === "ok") {
                this.newlyCreatedIRIs.add(result.distribution.iri);
                this.resetDistributionLayer();
                this.apiClient.getDistributions(
                    datasetUri,
                    [ this.callbackDistributions() ],
                    [ this.callbackGeneralError() ]
                );
                formDialog.dismiss();
            } else {
                formDialog.displayError(result.exceptionName, result.exception);
            }
        }
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
