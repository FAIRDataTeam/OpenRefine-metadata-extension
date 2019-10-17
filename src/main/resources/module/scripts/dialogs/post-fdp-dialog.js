/* global $, DOM, DialogSystem, MetadataHelpers, MetadataFormDialog, MetadataSpecs */
var PostFdpDialog = {};

PostFdpDialog.launch = function() {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-dialog.html"));
    this._elmts = DOM.bind(this.frame);
    this.metadata = {
        "fdp": null,
        "catalogs": new Map(),
        "datasets": new Map(),
        "distributions": new Map()
    };
    this.newlyCreatedIRIs = new Set();

    this._level = DialogSystem.showDialog(this.frame);

    let dialog = this;
    let elmts = this._elmts;

    PostFdpDialog.initBasicTexts(dialog);
    PostFdpDialog.resetDefault(dialog);
    PostFdpDialog.resetCatalogLayer(dialog);
    PostFdpDialog.resetDatasetLayer(dialog);
    PostFdpDialog.resetDistributionLayer(dialog);

    // Focus value of FDP URI input for easy overwrite
    elmts.baseURI.focus();
    elmts.baseURI[0].setSelectionRange(0, elmts.baseURI.val().length);

    // Bind actions
    elmts.closeButton.click(PostFdpDialog.dismissFunc(dialog));

    elmts.connectButton.click(() => {
        const fdpUri = dialog._elmts.baseURI.val();

        PostFdpDialog.resetDefault(dialog);
        PostFdpDialog.ajaxFDPMetadata(dialog, fdpUri);
    });

    elmts.catalogSelect.on("change", () => {
        const catalogUri = elmts.catalogSelect.val();

        PostFdpDialog.resetDatasetLayer(dialog);

        PostFdpDialog.ajaxDatasets(dialog, catalogUri);
    });

    elmts.datasetSelect.on("change", () => {
        const datasetUri = elmts.datasetSelect.val();

        PostFdpDialog.resetDistributionLayer(dialog);

        PostFdpDialog.ajaxDistributions(dialog, datasetUri);
    });

    elmts.catalogAddButton.click(() => {
        MetadataFormDialog.launch("catalog", MetadataSpecs.catalog,
            (newCatalog) => {
                PostFdpDialog.resetCatalogLayer(dialog);
                PostFdpDialog.ajaxCatalogs(dialog, fdpUri);
            }
        );
    });

    elmts.datasetAddButton.click(() => {
        MetadataFormDialog.launch("dataset", MetadataSpecs.dataset,
            (newDataset) => {
                PostFdpDialog.resetDatasetLayer(dialog);
                PostFdpDialog.ajaxDatasets(dialog, catalogUri);
            }
        );
    });

    elmts.distributionAddButton.click(() => {
        MetadataFormDialog.launch("distribution", MetadataSpecs.distribution,
            (newDistribution) => {
                PostFdpDialog.resetDistributionLayer(dialog);
                PostFdpDialog.ajaxDistributions(dialog, catalogUri);
            }
        );
    });
};

PostFdpDialog.initBasicTexts = (dialog) => {
    dialog.frame.i18n();
    dialog._elmts.baseURI.attr("title", $.i18n("post-fdp-dialog/description"));
};

PostFdpDialog.dismissFunc = (dialog) => {
    return () => { DialogSystem.dismissUntil(dialog._level - 1); };
};

PostFdpDialog.resetDefault = (dialog) => {
    dialog._elmts.dialogBody.find(".default-clear").empty();
    dialog._elmts.dialogBody.find(".default-hidden").addClass("hidden");
};

PostFdpDialog.createSelectOption = (name) => {
    return $("<option>")
        .prop("disabled", true)
        .prop("selected", true)
        .text(`-- select a ${name} --`);
};

PostFdpDialog.resetSelect = (select, name) => {
    select.empty();
    select.append(PostFdpDialog.createSelectOption(name));
};

PostFdpDialog.resetCatalogLayer = (dialog) => {
    dialog.metadata.catalogs.clear();
    PostFdpDialog.resetSelect(dialog._elmts.catalogSelect, "catalog");
    dialog._elmts.catalogLayer.addClass("hidden");

    PostFdpDialog.resetDatasetLayer(dialog);
    PostFdpDialog.resetDistributionLayer(dialog);
};

PostFdpDialog.resetDatasetLayer = (dialog) => {
    dialog.metadata.datasets.clear();
    PostFdpDialog.resetSelect(dialog._elmts.datasetSelect, "dataset");
    dialog._elmts.datasetLayer.addClass("hidden");

    PostFdpDialog.resetDistributionLayer(dialog);
};

PostFdpDialog.resetDistributionLayer = (dialog) => {
    dialog.metadata.distributions.clear();
    dialog._elmts.distributionsList.empty();
    dialog._elmts.distributionLayer.addClass("hidden");
};

PostFdpDialog.ajaxGeneric = (dialog, command, method, data, callback) => {
    MetadataHelpers.ajax(command, method, data,
        (result) => {
            if (result.status === "ok") {
                callback(result);
            } else {
                dialog._elmts.fdpConnected.addClass("hidden");
                dialog._elmts.fdpConnectionError.removeClass("hidden");
                dialog._elmts.warningsArea.text($.i18n(result.message));
            }
        },
        () => {
            dialog._elmts.fdpConnected.addClass("hidden");
            dialog._elmts.fdpConnectionError.removeClass("hidden");
            dialog._elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
        }
    );
};

PostFdpDialog.ajaxFDPMetadata = (dialog, fdpUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "fdp-metadata", "GET", { fdpUri },
        (result) => {
            dialog._elmts.fdpConnected.removeClass("hidden");
            dialog.metadata.fdp = result.fdpMetadata;
            PostFdpDialog.showFDPMetadata(dialog);

            PostFdpDialog.resetCatalogLayer(dialog);
            PostFdpDialog.ajaxCatalogs(dialog, fdpUri);
        }
    );
};

PostFdpDialog.ajaxCatalogs = (dialog, fdpUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "catalogs-metadata", "GET", { fdpUri },
        (result) => {
            dialog.metadata.catalogs.clear();
            result.catalogs.forEach((catalog) => {
                dialog.metadata.catalogs.set(catalog.iri, catalog);
            });
            PostFdpDialog.showCatalogs(dialog);
        }
    );
};

PostFdpDialog.ajaxDatasets = (dialog, catalogUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "datasets-metadata", "GET", { catalogUri },
        (result) => {
            dialog.metadata.datasets.clear();
            result.datasets.forEach((dataset) => {
                dialog.metadata.datasets.set(dataset.iri, dataset);
            });
            PostFdpDialog.showDatasets(dialog);
        }
    );
};

PostFdpDialog.ajaxDistributions = (dialog, datasetUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "distributions-metadata", "GET", { datasetUri },
        (result) => {
            dialog.metadata.distributions.clear();
            result.distributions.forEach((distribution) => {
                dialog.metadata.distributions.set(distribution.iri, distribution);
            });
            PostFdpDialog.showDistributions(dialog);
        }
    );
};

PostFdpDialog.showFDPMetadata = (dialog) => {
    const fdpMetadata = dialog.metadata.fdp;
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

    dialog._elmts.fdpMetadata.append($("<p>")
        .append($.i18n("post-fdp-dialog/connected-to-fdp"))
        .append(" \"" + title + "\" ")
        .append($.i18n("post-fdp-dialog/published-by"))
        .append(" " + publisher + ".")
    );
};

PostFdpDialog.showMetadataSelect = (dialog, select, metadatas) => {
    metadatas.forEach((metadata) => {
        const isNew = dialog.newlyCreatedIRIs.has(metadata.iri);
        select.append(
            $("<option>")
                .addClass("from-fdp")
                .addClass(isNew ? "new" : "original")
                .attr("value", metadata.iri)
                .text(metadata.title)
        );
    });
};

PostFdpDialog.showCatalogs = (dialog) => {
    PostFdpDialog.resetSelect(dialog._elmts.catalogSelect, "catalog");
    PostFdpDialog.showMetadataSelect(
        dialog,
        dialog._elmts.catalogSelect,
        dialog.metadata.catalogs
    );
    dialog._elmts.catalogLayer.removeClass("hidden");
};

PostFdpDialog.showDatasets = (dialog) => {
    PostFdpDialog.resetSelect(dialog._elmts.datasetSelect, "dataset");
    const actCatalog = dialog._elmts.catalogSelect.val();
    PostFdpDialog.showMetadataSelect(
        dialog,
        dialog._elmts.datasetSelect,
        dialog.metadata.datasets
    );
    dialog._elmts.datasetLayer.removeClass("hidden");
};

PostFdpDialog.showDistributions = (dialog) => {
    dialog._elmts.distributionsList.empty();
    dialog.metadata.distributions.forEach((distribution) => {
        const item = $("<li>")
            .addClass("distribution-item")
            .attr("id", distribution.id)
            .text(`${distribution.title} (version: ${distribution.version})`);
        dialog._elmts.distributionsList.append(item);
    });
    dialog._elmts.distributionLayer.removeClass("hidden");
};
