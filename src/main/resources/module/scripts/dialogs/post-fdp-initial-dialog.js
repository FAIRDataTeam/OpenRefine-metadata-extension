var PostFDPInitialDialog = {};

PostFDPInitialDialog.launch = function() {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/post-fdp-initial-dialog.html"));
    this._elmts = DOM.bind(this.frame);

    this._level = DialogSystem.showDialog(this.frame);

    var self = this;
    var elmts = this._elmts;

    elmts.dialogHeader.text($.i18n("post-fdp-initial-dialog/title"));
    elmts.descriptionText.text($.i18n("post-fdp-initial-dialog/description"));
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
        alert("Connect to FDP @ " + fdpURI);
        // TODO: Call command to contact FDP and show result or error
        dismiss();
    });
};
