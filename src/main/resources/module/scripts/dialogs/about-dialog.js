/* global $, DOM, DialogSystem */

class MetadataAboutDialog {
    constructor(type, specs, callbackFn, prefill) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/about-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.frame.i18n();
        this.bindActions();
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    bindActions() {
        this.elements.closeButton.click(() => {
            this.dismiss();
        });
    }

    // launcher
    static createAndLaunch() {
        const dialog = new MetadataAboutDialog();
        dialog.launch();
    }
}
