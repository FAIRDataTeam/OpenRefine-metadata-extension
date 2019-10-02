/* global $, DOM, DialogSystem, Refine */
var PostFdpDialog = {};

PostFdpDialog.launch = function() {
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

    const fdpMakeURL = (uriObject) => {
        return uriObject.namespace + uriObject.localName;
    };

    const showFDPMetadata = (fdpMetadata) => {
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

        let table = $("<table>")
            .append("<tr><th>Title</th><td>" + title + "</td></tr>")
            .append("<tr><th>Publisher</th><td>" + publisher + "</td></tr>")
            .append("<tr><th>Description</th><td>" + description + "</td></tr>");

        elmts.fdpMetadata
            .append("<p>" + $.i18n("post-fdp-dialog/connected-to-fdp") + "<p>")
            .append(table);
    };

    elmts.closeButton.click(function() {
        dismiss();
    });

    elmts.connectButton.click(function() {
        var fdpURI = elmts.baseURI.val();
        elmts.warningsArea.text("");
        elmts.fdpConnected.addClass("hidden");
        elmts.fdpConnectionError.addClass("hidden");

        MetadataHelpers.ajax(
            "connect-fdp",
            "GET",
            { uri: fdpURI },
            (o) => {
                if (o.status === "ok") {
                    elmts.fdpConnected.removeClass("hidden");
                    showFDPMetadata(o.fdpMetadata);
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
