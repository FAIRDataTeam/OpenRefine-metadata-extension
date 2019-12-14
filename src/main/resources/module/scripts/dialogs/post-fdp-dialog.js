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

        this.bindActions();
        this.prepareConnections();
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    bindActions() {
        const self = this;
        const elmts = this.elements;
        const isCreatePermission = (permission) => { return permission.code === "C"; };

        elmts.closeButton.click(() => { self.dismiss(); });

        elmts.connectButton.click(() => {
            const fdpConnection = elmts.fdpConnectionSelect.val();

            self.resetDefault();

            if (fdpConnection === "custom") {
                const fdpUri = elmts.baseURI.val();
                const email = elmts.email.val();
                const password = elmts.password.val();

                self.apiClient.connectCustomFDP(
                    fdpUri, email, password,
                    [this.callbackFDPConnected(), this.callbackErrorResponse()],
                    [this.callbackGeneralError()]
                );
            } else if (Number.isInteger(Number.parseInt(fdpConnection))) {
                self.apiClient.connectPreConfiguredFDP(
                    Number.parseInt(fdpConnection),
                    [this.callbackFDPConnected(), this.callbackErrorResponse()],
                    [this.callbackGeneralError()]
                );
            }
        });

        elmts.fdpConnectionSelect.on("change", () => {
            this.resetDefault();
            if (elmts.fdpConnectionSelect.val() === "custom") {
                elmts.fdpCustomForm.removeClass("hidden");
            } else {
                elmts.fdpCustomForm.addClass("hidden");
            }
        });

        elmts.catalogSelect.on("change", () => {
            self.resetDatasetLayer();
            const catalogUri = elmts.catalogSelect.val();
            const catalog = this.metadata.catalogs.get(catalogUri);
            if (catalog) {
                this.metadata.datasets.clear();
                catalog.datasets.forEach((dataset) => {
                    this.metadata.datasets.set(dataset.uri, dataset);
                });
                const canCreate = catalog.membership && catalog.membership.permissions.some(isCreatePermission);
                this.showDatasets(canCreate);
            }
        });

        elmts.datasetSelect.on("change", () => {
            self.resetDistributionLayer();
            const datasetUri = elmts.datasetSelect.val();
            const dataset = this.metadata.datasets.get(datasetUri);

            if (dataset) {
                this.metadata.distributions.clear();
                dataset.distributions.forEach((distribution) => {
                    this.metadata.distributions.set(distribution.uri, distribution);
                });
                const canCreate = dataset.membership.permissions && dataset.membership.permissions.some(isCreatePermission);
                this.showDistributions(canCreate);
            }
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

    // pre-configured connections
    prepareConnections() {
        this.apiClient.getFDPConnections(
            [
                (result) => {
                    if (result.status === "ok" && result.fdpConnections.length > 0) {
                        this.showFDPConnections(result.fdpConnections);
                    } else {
                        this.customFDPOnly();
                    }
                }
            ],
            [ () => { this.customFDPOnly(); } ]
        );
    }

    prepareFDPConnectionSelect() {
        const defaultOption = $("<option>")
            .prop("selected", true)
            .prop("disabled", true)
            .text($.i18n("post-fdp-dialog/connections/default-option"));
        const customOption = $("<option>")
            .attr("value", "custom")
            .text($.i18n("post-fdp-dialog/connections/custom-option"));
        this.elements.fdpConnectionSelect.empty();
        this.elements.fdpConnectionSelect.append(defaultOption);
        this.elements.fdpConnectionSelect.append(customOption);
    }

    customFDPOnly() {
        this.prepareFDPConnectionSelect();
        this.elements.fdpCustomSelectForm.addClass("hidden");
        this.elements.fdpCustomForm.removeClass("hidden");
        this.elements.fdpConnectionSelect.val("custom");
    }

    showFDPConnections(fdpConnections) {
        this.prepareFDPConnectionSelect();
        this.elements.fdpCustomSelectForm.removeClass("hidden");
        this.elements.fdpCustomForm.addClass("hidden");

        fdpConnections.forEach((connection, index) => {
            this.elements.fdpConnectionSelect.append(
                $("<option>").val(index).text(`${connection.name} @ ${connection.baseURI}`)
            );
        });
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
        this.elements.datasetAddButton.addClass("hidden");

        this.resetDistributionLayer();
    }

    resetDistributionLayer() {
        this.metadata.distributions.clear();
        this.elements.distributionsList.empty();
        this.elements.distributionLayer.addClass("hidden");
        this.elements.distributionAddButton.addClass("hidden");
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
                this.apiClient.getDashboard(
                    [this.callbackDashboard(), this.callbackErrorResponse()],
                    [this.callbackGeneralError()]
                );
            }
        };
    }

    callbackDashboard() {
        return (result) => {
            if (result.status === "ok") {
                this.dashboard = result.catalogs;

                this.metadata.catalogs.clear();
                result.catalogs.forEach((catalog) => {
                    this.metadata.catalogs.set(catalog.uri, catalog);
                });
                this.showCatalogs();
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
                const catalogUri = result.catalog.iri;
                this.newlyCreatedIRIs.add(catalogUri);
                this.resetCatalogLayer();

                this.apiClient.getDashboard(
                    [
                        this.callbackDashboard(),
                        () => { this.elements.catalogSelect.val(catalogUri).trigger("change"); }
                    ],
                    [this.callbackGeneralError()]
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
                const datasetUri = result.dataset.iri;
                this.newlyCreatedIRIs.add(datasetUri);
                this.resetDatasetLayer();

                this.apiClient.getDashboard(
                    [
                        this.callbackDashboard(),
                        () => {
                            this.elements.catalogSelect.val(catalogUri).trigger("change");
                            this.elements.datasetSelect.val(datasetUri).trigger("change");
                        }
                    ],
                    [this.callbackGeneralError()]
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
                const catalogUri = this.elements.catalogSelect.val();
                const distributionUri = result.distribution.iri;
                this.newlyCreatedIRIs.add(distributionUri);
                this.resetDistributionLayer();

                this.apiClient.getDashboard(
                    [
                        this.callbackDashboard(),
                        () => {
                            this.elements.catalogSelect.val(catalogUri).trigger("change");
                            this.elements.datasetSelect.val(datasetUri).trigger("change");
                        }
                    ],
                    [this.callbackGeneralError()]
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
            const isNew = this.newlyCreatedIRIs.has(metadata.uri);
            select.append(
                $("<option>")
                    .addClass("from-fdp")
                    .addClass(isNew ? "new" : "original")
                    .attr("value", metadata.uri)
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

    showDatasets(canCreate) {
        this.constructor.resetSelect(this.elements.datasetSelect, "dataset");
        this.showMetadataSelect(
            this.elements.datasetSelect,
            this.metadata.datasets
        );
        this.elements.datasetLayer.removeClass("hidden");
        if (canCreate) {
            this.elements.datasetAddButton.removeClass("hidden");
        }
    }

    showDistributions(canCreate) {
        this.elements.distributionsList.empty();
        this.metadata.distributions.forEach((distribution) => {
            const isNew = this.newlyCreatedIRIs.has(distribution.uri);
            let text = distribution.title;
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
        if (canCreate) {
            this.elements.distributionAddButton.removeClass("hidden");
        }
    }

    // generic helpers
    static createSelectOption(name) {
        return $("<option>")
            .prop("disabled", true)
            .prop("selected", true)
            .text($.i18n("common/select-option", name));
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
