/* global DOM, DialogSystem, MetadataHelpers, MetadataFormDialog, FDPInfoDialog, MetadataSpecs, MetadataApiClient */

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
        this.settings = new Map();
        this.projectData = null;
        this.prefill = new Map();
        this.fdpConnections = [];

        this.initBasicTexts();
        this.resetDefault();

        this.elements.dialogBody.addClass("hidden");
        this.apiClient.getSettings([
            (result) => {
                this.settings = new Map(Object.entries(result.settings));
                this.loadProjectData(result.projectData);
                this.preparePrefill();
                this.bindActions();
                this.prepareConnections();
                this.recallCredentials();
                this.elements.dialogBody.removeClass("hidden");
            }
        ]);
    }

    getCurrentRepositoryUri() {
        return this.apiClient.fdpUri;
    }

    loadProjectData(projectData) {
        this.projectData = projectData;
        this.projectData.lastCatalog = new Map(Object.entries(projectData.lastCatalog));
        this.projectData.lastDataset = new Map(Object.entries(projectData.lastDataset));
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    persistProjectData() {
        const settings = { projectData: {
                lastCatalog: Object.fromEntries(this.projectData.lastCatalog.entries()),
                projectData: Object.fromEntries(this.projectData.lastDataset.entries())
            }};
        this.apiClient.postSettings(settings, [
            (result) => {
                this.loadProjectData(result.projectData);
            }
        ]);
    }

    recallCredentials() {
        if (MetadataHelpers.tempStorage.has("fdpConnection")) {
            this.elements.fdpConnectionSelect.val(MetadataHelpers.tempStorage.get("fdpConnection"));
        }
        if (this.elements.fdpConnectionSelect.val() === "custom") {
            this.elements.baseURI.val(MetadataHelpers.tempStorage.get("fdpUri") || "");
            this.elements.email.val(MetadataHelpers.tempStorage.get("email") || "");
            this.elements.password.val(MetadataHelpers.tempStorage.get("password") || "");
        }
    }

    bindActions() {
        const self = this;
        const elmts = this.elements;
        const isCreatePermission = (permission) => { return permission.code === "C"; };

        elmts.closeButton.click(() => { self.dismiss(); });

        elmts.connectButton.click(() => {
            const fdpConnection = elmts.fdpConnectionSelect.val();

            self.resetDefault();

            MetadataHelpers.tempStorage.set("fdpConnection", fdpConnection);
            if (fdpConnection === "custom") {
                const fdpUri = MetadataHelpers.handleFdpUrl(elmts.baseURI.val());
                elmts.baseURI.val(fdpUri);
                const email = elmts.email.val();
                const password = elmts.password.val();

                MetadataHelpers.tempStorage.set("fdpUri", fdpUri);
                MetadataHelpers.tempStorage.set("email", email);
                MetadataHelpers.tempStorage.set("password", password);

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
            const fdpConnectionId = elmts.fdpConnectionSelect.val();
            if (fdpConnectionId === "custom") {
                elmts.fdpCustomForm.removeClass("hidden");
            } else {
                this.preparePrefill();
                const prefillConnection = this.fdpConnections[Number.parseInt(fdpConnectionId)].metadata;
                if (prefillConnection) {
                    const xmap = new Map(Object.entries(prefillConnection));
                    xmap.forEach((value, key) => {
                        this.prefill.set(key, value);
                    });
                }

                elmts.fdpCustomForm.addClass("hidden");
            }
        });

        elmts.catalogSelect.on("change", () => {
            this.resetDatasetLayer();
            const catalogUri = elmts.catalogSelect.val();
            const catalog = this.metadata.catalogs.get(catalogUri);
            if (catalog) {
                this.metadata.datasets.clear();
                catalog.children.forEach((dataset) => {
                    this.metadata.datasets.set(dataset.uri, dataset);
                });
                const canCreate = catalog.membership && catalog.membership.permissions.some(isCreatePermission);
                this.showDatasets(canCreate);

                const repositoryUri = this.getCurrentRepositoryUri();
                if (!this.projectData.lastCatalog.get(repositoryUri) !== catalogUri) {
                    this.projectData.lastCatalog.set(repositoryUri, catalogUri);
                    this.persistProjectData();
                }
            }
        });

        elmts.datasetSelect.on("change", () => {
            this.resetDistributionLayer();
            const datasetUri = elmts.datasetSelect.val();
            const dataset = this.metadata.datasets.get(datasetUri);
            if (dataset) {
                this.metadata.distributions.clear();
                dataset.children.forEach((distribution) => {
                    this.metadata.distributions.set(distribution.uri, distribution);
                });
                const canCreate = dataset.membership.permissions && dataset.membership.permissions.some(isCreatePermission);
                this.showDistributions(canCreate);

                const repositoryUri = this.getCurrentRepositoryUri();
                if (this.projectData.lastDataset.get(repositoryUri) !== datasetUri) {
                    this.projectData.lastDataset.set(repositoryUri, datasetUri);
                    this.persistProjectData();
                }
            }
        });

        elmts.catalogAddButton.click(() => {
            let prefill = new Map(this.prefill);
            prefill.set("parent", self.apiClient.fdpUri);
            MetadataFormDialog.createAndLaunch(MetadataSpecs.catalog,
                (catalog, formDialog) => {
                    this.apiClient.postCatalog(
                        catalog,
                        [this.callbackPostCatalog(formDialog)],
                        [this.callbackPostError(formDialog)]
                    );
                },
                prefill
            );
        });

        elmts.datasetAddButton.click(() => {
            const catalogUri = this.elements.catalogSelect.val();
            let prefill = new Map(this.prefill);
            prefill.set("parent", catalogUri);
            MetadataFormDialog.createAndLaunch(MetadataSpecs.dataset,
                (dataset, formDialog) => {
                    this.apiClient.postDataset(
                        dataset,
                        [this.callbackPostDataset(formDialog, catalogUri)],
                        [this.callbackPostError(formDialog)]
                    );
                },
                prefill
            );
        });

        elmts.distributionAddButton.click(() => {
            const datasetUri = elmts.datasetSelect.val();
            let prefill = new Map(this.prefill);
            prefill.set("parent", datasetUri);
            MetadataFormDialog.createAndLaunch(MetadataSpecs.distribution,
                (distribution, formDialog) => {
                    this.apiClient.postDistribution(
                        distribution,
                        [this.callbackPostDistribution(formDialog, datasetUri)],
                        [this.callbackPostError(formDialog)]
                    );
                },
                prefill
            );
        });
    }

    initBasicTexts() {
        this.frame.i18n();
        this.elements.baseURI.attr("title", $.i18n("post-fdp-dialog/description"));
    }

    preparePrefill() {
        if (this.settings.has("metadata") && this.settings.get("metadata") !== null) {
            this.prefill = new Map(Object.entries(this.settings.get("metadata")));
        }
    }

    // pre-configured connections
    prepareConnections() {
        if (this.settings.has("fdpConnections")) {
            this.fdpConnections = this.settings.get("fdpConnections") || [];
        }
        this.showFDPConnections();
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
        if (!this.settings.has("allowCustomFDP") || this.settings.get("allowCustomFDP") === true) {
            this.elements.fdpConnectionSelect.append(customOption);
        }
    }

    customFDPOnly() {
        this.prepareFDPConnectionSelect();
        this.elements.fdpCustomSelectForm.addClass("hidden");
        this.elements.fdpCustomForm.removeClass("hidden");
        this.elements.fdpConnectionSelect.val("custom");
    }

    showFDPConnections() {
        this.prepareFDPConnectionSelect();
        this.elements.fdpCustomSelectForm.removeClass("hidden");
        this.elements.fdpCustomForm.addClass("hidden");

        let preselect = null;
        this.fdpConnections.forEach((connection, index) => {
            this.elements.fdpConnectionSelect.append(
                $("<option>")
                    .val(index)
                    .text(`${connection.name} @ ${connection.baseURI}`)
            );
            if (preselect === null && connection.preselected === true) {
                preselect = index;
                this.elements.fdpConnectionSelect.val(index);
                this.elements.fdpConnectionSelect.trigger("change");
            }
        });
        if (this.fdpConnections.length === 0) {
            this.customFDPOnly();
        }
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
        };
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
        let infop = $("<p>")
            .append($.i18n("post-fdp-dialog/connected-to-fdp"))
            .append(` "${title}" `)
            .append($.i18n("post-fdp-dialog/published-by"))
            .append(` ${publisher}.`);
        if (this.apiClient.hasFDPInfo()) {
            infop.append($("<button>")
                .addClass("fdp-info")
                .addClass("button button-primary")
                .text("?")
                .attr("title", $.i18n("post-fdp-dialog/fdp-info"))
                .click(() => {
                    FDPInfoDialog.createAndLaunch(fdpMetadata.title, this.apiClient);
                })
            );
        }
        this.elements.fdpMetadata.append(infop);
    }

    showMetadataSelect(select, metadatas, toSelect) {
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
        if (metadatas.has(toSelect)) {
            select.val(toSelect).trigger("change");
        }
    }

    showCatalogs() {
        this.constructor.resetSelect(this.elements.catalogSelect, "catalog");
        this.showMetadataSelect(
            this.elements.catalogSelect,
            this.metadata.catalogs,
            this.projectData.lastCatalog.get(this.getCurrentRepositoryUri())
        );
        this.elements.catalogLayer.removeClass("hidden");
    }

    showDatasets(canCreate) {
        this.constructor.resetSelect(this.elements.datasetSelect, "dataset");
        this.showMetadataSelect(
            this.elements.datasetSelect,
            this.metadata.datasets,
            this.projectData.lastDataset.get(this.getCurrentRepositoryUri())
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
