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

    elmts.catalogSelect.on("change", () => {
        const catalogUri = elmts.catalogSelect.val();

        PostFdpDialog.resetDatasetLayer(dialog);
        PostFdpDialog.resetDistributionLayer(dialog);
        PostFdpDialog.ajaxDatasets(dialog, catalogUri);
    });

    elmts.datasetSelect.on("change", () => {
        PostFdpDialog.resetDistributionLayer(dialog);
        dialog._elmts.distributionLayer.removeClass("hidden");
    });

    // TODO: handle "add" buttons
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
            PostFdpDialog.showFDPMetadata(dialog, result.fdpMetadata);

            PostFdpDialog.resetCatalogLayer(dialog);
            PostFdpDialog.resetDatasetLayer(dialog);
            PostFdpDialog.resetDistributionLayer(dialog);
            PostFdpDialog.ajaxCatalogs(dialog, fdpUri);
        }
    );
};

PostFdpDialog.ajaxCatalogs = (dialog, fdpUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "catalogs-metadata", "GET", { fdpUri },
        (result) => {
            dialog._elmts.catalogLayer.removeClass("hidden");
            PostFdpDialog.showCatalogs(dialog, result.catalogsMetadata);
        }
    );
};

PostFdpDialog.ajaxDatasets = (dialog, catalogUri) => {
    PostFdpDialog.ajaxGeneric(dialog, "datasets-metadata", "GET", { catalogUri },
        (result) => {
            dialog._elmts.fdpConnected.removeClass("hidden");
            PostFdpDialog.showDatasets(dialog, result.datasetsMetadata);
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

PostFdpDialog.showMetadataSelect = (select, metadatas) => {
    metadatas.forEach((metadata) => {
        select.append(
            $("<option>")
                .attr("value", MetadataHelpers.fdpMakeURL(metadata.uri))
                .text(metadata.title.label)
        );
    });
}

PostFdpDialog.showCatalogs = (dialog, catalogsMetadata) => {
    PostFdpDialog.showMetadataSelect(dialog._elmts.catalogSelect, catalogsMetadata);
    dialog._elmts.catalogLayer.removeClass("hidden");
};

PostFdpDialog.showDatasets = (dialog, datasetsMetadata) => {
    PostFdpDialog.showMetadataSelect(dialog._elmts.datasetSelect, datasetsMetadata);
    dialog._elmts.datasetLayer.removeClass("hidden");
};
