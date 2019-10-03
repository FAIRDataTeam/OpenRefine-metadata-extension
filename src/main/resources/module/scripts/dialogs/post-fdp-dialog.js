/* global $, DOM, DialogSystem, Refine, MetadataHelpers */
var PostFdpDialog = {};

PostFdpDialog.launch = function() {
    // TODO: refactor this long spaghetti
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-dialog.html"));
    this._elmts = DOM.bind(this.frame);

    this._level = DialogSystem.showDialog(this.frame);

    var self = this;
    var elmts = this._elmts;

    elmts.dialogTitle.text($.i18n("post-fdp-dialog/title"));
    elmts.closeButton.text($.i18n("post-fdp-dialog/button-close"));
    elmts.baseURI.attr("title", $.i18n("post-fdp-dialog/description"));
    elmts.connectButton.text($.i18n("post-fdp-dialog/button-connect"));
    elmts.baseURILabel.text($.i18n("post-fdp-dialog/label-uri"));

    elmts.baseURI.focus();
    elmts.baseURI[0].setSelectionRange(0, elmts.baseURI.val().length);

    const dismiss = () => {
        DialogSystem.dismissUntil(self._level - 1);
    };

    elmts.closeButton.click(function() {
        dismiss();
    });

    elmts.connectButton.click(function() {
        elmts.warningsArea.text("");
        elmts.fdpConnected.addClass("hidden");
        elmts.fdpConnectionError.addClass("hidden");

        MetadataHelpers.ajax(
            "fdp-metadata",
            "GET",
            { fdpUri: elmts.baseURI.val() },
            (o) => {
                if (o.status === "ok") {
                    elmts.fdpConnected.removeClass("hidden");
                    MetadataHelpers.showFDPMetadata(elmts.fdpMetadata, o.fdpMetadata);
                } else {
                    elmts.fdpConnectionError.removeClass("hidden");
                    elmts.warningsArea.text($.i18n(o.message));
                }
            },
            (o) => {
                elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
            }
        );

        MetadataHelpers.ajax(
            "catalogs-metadata",
            "GET",
            { fdpUri: elmts.baseURI.val() },
            (o) => {
                if (o.status === "ok") {
                    elmts.fdpConnected.removeClass("hidden");
                    // TODO: show possible catalogs
                } else {
                    elmts.fdpConnectionError.removeClass("hidden");
                    elmts.warningsArea.text($.i18n(o.message));
                }
            },
            (o) => {
                elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
            }
        );

    });

};
