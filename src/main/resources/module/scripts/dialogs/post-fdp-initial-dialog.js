/* global $, DOM, DialogSystem, Refine */
var PostFDPInitialDialog = {};

PostFDPInitialDialog.launch = function() {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-initial-dialog.html"));
    this._elmts = DOM.bind(this.frame);

    this._level = DialogSystem.showDialog(this.frame);

    var self = this;
    var elmts = this._elmts;

    elmts.dialogHeader.text($.i18n("post-fdp-initial-dialog/title"));
    elmts.connectButton.text($.i18n("post-fdp-initial-dialog/button-connect"));
    elmts.cancelButton.text($.i18n("post-fdp-initial-dialog/button-cancel"));
    elmts.baseURILabel.text($.i18n("post-fdp-initial-dialog/label-uri"));

    var dismiss = function() {
        DialogSystem.dismissUntil(self._level - 1);
    };

    elmts.cancelButton.click(function() {
        dismiss();
    });

    elmts.connectButton.click(function() {
        var fdpURI = elmts.baseURI.val();
        elmts.warningsArea.text("");
        $("#fdp-connected").addClass("hidden");
        $("#fdp-connection-error").addClass("hidden");

        Refine.postProcess(
            "metadata",
            "connect-fdp",
            {},
            { uri: fdpURI },
            {},
            {
                onDone(o) {
                    console.log(o);
                    if (o.status === "ok") {
                        $("#fdp-connected").removeClass("hidden");
                        showFDPMetadata(o.fdpMetadata);
                    } else {
                        $("#fdp-connection-error").removeClass("hidden");
                        elmts.warningsArea.text($.i18n(o.message));
                    }
                },
                onError() {
                    elmts.warningsArea.text($.i18n("connect-fdp-command/error"));
                }
            }
        );
    });



    function fdpMakeURL(uriObject) {
        return uriObject.namespace + uriObject.localName;
    }


    function showFDPMetadata(fdpMetadata) {
        let title = $("<a>")
            .attr("href", fdpMakeURL(fdpMetadata.uri))
            .attr("target", "_blank")
            .text(fdpMetadata.title.label)
            .get(0).outerHTML;
        let publisher = $("<a>")
            .attr("href", fdpMakeURL(fdpMetadata.publisher.uri))
            .attr("target", "_blank")
            .text(fdpMetadata.publisher.name.label)
            .get(0).outerHTML;
        let description = fdpMetadata.description.label;

        let table = $('<table>')
            .append('<tr><th>Title</th><td>' + title + '</td></tr>')
            .append('<tr><th>Publisher</th><td>' + publisher + '</td></tr>')
            .append('<tr><th>Description</th><td>' + description + '</td></tr>');

        elmts.fdpMetadata
            .append("<p>" + $.i18n("post-fdp-initial-dialog/connected-to-fdp") + "<p>")
            .append(table);
    }
};
