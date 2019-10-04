/* global $, DOM, DialogSystem, Refine, MetadataHelpers */
var PostFdpDialog = {};

PostFdpDialog.launch = () => {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-dialog.html"));
    this._elmts = DOM.bind(this.frame);

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

    elmts.catalogSelect.on('change', () => {
        const catalogUri = elmts.catalogSelect.val();

        PostFdpDialog.ajaxDatasets(dialog, catalogUri);
    });

    elmts.datasetSelect.on('change', () => {
        dialog._elmts.distributionLayer.removeClass('hidden');
    });

    // TODO: handle "add" buttons
};

PostFdpDialog.initBasicTexts = (dialog) => {
    dialog._elmts.dialogTitle.text($.i18n("post-fdp-dialog/title"));
    dialog._elmts.closeButton.text($.i18n("post-fdp-dialog/button-close"));
    dialog._elmts.baseURI.attr("title", $.i18n("post-fdp-dialog/description"));
    dialog._elmts.connectButton.text($.i18n("post-fdp-dialog/button-connect"));
    dialog._elmts.baseURILabel.text($.i18n("post-fdp-dialog/label-uri"));
    dialog._elmts.catalogLabel.text($.i18n("post-fdp-dialog/layers/catalog"));
    dialog._elmts.datasetLabel.text($.i18n("post-fdp-dialog/layers/dataset"));
    dialog._elmts.distributionLabel.text($.i18n("post-fdp-dialog/layers/distribution"));
    dialog._elmts.catalogAddButton.text($.i18n("post-fdp-dialog/layers/catalog-add"));
    dialog._elmts.datasetAddButton.text($.i18n("post-fdp-dialog/layers/dataset-add"));
    dialog._elmts.distributionAddButton.text($.i18n("post-fdp-dialog/layers/distribution-add"));
};

PostFdpDialog.dismissFunc = (dialog) => {
    return () => { DialogSystem.dismissUntil(dialog._level - 1); };
};

PostFdpDialog.resetDefault = (dialog) => {
    dialog._elmts.dialogBody.find(".default-clear").empty();
    dialog._elmts.dialogBody.find(".default-hidden").addClass("hidden");
};

PostFdpDialog.createSelectOption = (name) => {
    return $('<option>')
        .attr("disabled", true)
        .attr("selected", true)
        .text(`-- select a ${name} --`);
};

PostFdpDialog.resetSelect = (select, name) => {
    select.empty();
    select.append(PostFdpDialog.createSelectOption(name));
};

PostFdpDialog.resetCatalogLayer = (dialog) => {
    PostFdpDialog.resetSelect(dialog._elmts.catalogSelect, "catalog");
    dialog._elmts.catalogLayer.addClass("hidden");
};

PostFdpDialog.resetDatasetLayer = (dialog) => {
    PostFdpDialog.resetSelect(dialog._elmts.datasetSelect, "dataset");
    dialog._elmts.datasetLayer.addClass("hidden");
};

PostFdpDialog.resetDistributionLayer = (dialog) => {
    dialog._elmts.distributionLayer.addClass("hidden");
};

PostFdpDialog.ajaxFDPMetadata = (dialog, fdpUri) => {
    MetadataHelpers.ajax(
        "fdp-metadata",
        "GET",
        { fdpUri: fdpUri },
        (o) => {
            if (o.status === "ok") {
                dialog._elmts.fdpConnected.removeClass("hidden");
                PostFdpDialog.showFDPMetadata(dialog, o.fdpMetadata);
                PostFdpDialog.ajaxCatalogs(dialog, fdpUri); // load catalogs
            } else {
                dialog._elmts.fdpConnectionError.removeClass("hidden");
                dialog._elmts.warningsArea.text($.i18n(o.message));
            }
        },
        (o) => {
            dialog._elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
        }
    );
};

PostFdpDialog.ajaxCatalogs = (dialog, fdpUri) => {
    PostFdpDialog.resetCatalogLayer(dialog);
    PostFdpDialog.resetDatasetLayer(dialog);
    PostFdpDialog.resetDistributionLayer(dialog);

    MetadataHelpers.ajax(
        "catalogs-metadata",
        "GET",
        { fdpUri: fdpUri },
        (o) => {
            if (o.status === "ok") {
                dialog._elmts.catalogLayer.removeClass("hidden");
                PostFdpDialog.showCatalogs(dialog, o.catalogsMetadata);
            } else {
                dialog._elmts.fdpConnected.addClass("hidden");
                dialog._elmts.fdpConnectionError.removeClass("hidden");
                dialog._elmts.warningsArea.text($.i18n(o.message));
            }
        },
        (o) => {
            dialog._elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
        }
    );
};

PostFdpDialog.ajaxDatasets = (dialog, catalogUri) => {
    PostFdpDialog.resetDatasetLayer(dialog);
    PostFdpDialog.resetDistributionLayer(dialog);

    MetadataHelpers.ajax(
        "datasets-metadata",
        "GET",
        { catalogUri: catalogUri },
        (o) => {
            if (o.status === "ok") {
                dialog._elmts.fdpConnected.removeClass("hidden");
                PostFdpDialog.showDatasets(dialog, o.datasetsMetadata);
            } else {
                dialog._elmts.fdpConnected.addClass("hidden");
                dialog._elmts.fdpConnectionError.removeClass("hidden");
                dialog._elmts.warningsArea.text($.i18n(o.message));
            }
        },
        (o) => {
            dialog._elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
        }
    );
};

PostFdpDialog.showFDPMetadata = (dialog, fdpMetadata) => {
    let title = $("<a>")
        .attr("href", MetadataHelpers.fdpMakeURL(fdpMetadata.uri))
        .attr("target", "_blank")
        .text(fdpMetadata.title.label)
        .get(0).outerHTML;
    let publisher = $("<a>")
        .attr("href", MetadataHelpers.fdpMakeURL(fdpMetadata.publisher.uri))
        .attr("target", "_blank")
        .text(fdpMetadata.publisher.name.label)
        .get(0).outerHTML;

    dialog._elmts.fdpMetadata.append($("<p>")
        .append($.i18n("post-fdp-dialog/connected-to-fdp"))
        .append(" \"" + title + "\" ")
        .append($.i18n("post-fdp-dialog/published-by"))
        .append(" " + publisher + ".")
    );
};

PostFdpDialog.showCatalogs = (dialog, catalogsMetadata) => {
    catalogsMetadata.forEach((catalogMetadata) => {
        dialog._elmts.catalogSelect.append(
            $("<option>")
                .attr("value", MetadataHelpers.fdpMakeURL(catalogMetadata.uri))
                .text(catalogMetadata.title.label)
        );
    });
    dialog._elmts.catalogLayer.removeClass('hidden');
};

PostFdpDialog.showDatasets = (dialog, datasetsMetadata) => {
    datasetsMetadata.forEach((datasetMetadata) => {
        dialog._elmts.datasetSelect.append(
            $("<option>")
                .attr("value", MetadataHelpers.fdpMakeURL(datasetMetadata.uri))
                .text(datasetMetadata.title.label)
        );
    });
    dialog._elmts.datasetLayer.removeClass('hidden');
};
