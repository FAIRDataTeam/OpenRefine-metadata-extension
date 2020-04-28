/* global DOM, DialogSystem */

class FDPInfoDialog {
    constructor(name, apiClient) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/fdp-info-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.frame.i18n();
        this.bindActions();

        const fdpUri = apiClient.fdpUri;
        let fdpName = null;
        let fdpVersion = null;
        let fdpBuiltAt = null;
        let fdpPersistentUrl = null;
        if (apiClient.fdpInfo !== null) {
            fdpName = apiClient.fdpInfo.name;
            fdpVersion = apiClient.fdpInfo.version;
            fdpBuiltAt = apiClient.fdpInfo.builtAt;
        }
        if (apiClient.fdpConfig !== null) {
            fdpPersistentUrl = apiClient.fdpConfig.persistentUrl;
        }

        const handleNull = (text) => {
            return (text === null ? $.i18n("fdp-info-dialog/unknown") : text);
        };

        this.elements.dialogTitle.text(handleNull(name));
        this.elements.fdpInfoName.text(handleNull(fdpName));
        this.elements.fdpInfoVersion.text(handleNull(fdpVersion));
        this.elements.fdpInfoBuiltAt.text(handleNull(fdpBuiltAt));
        this.elements.fdpInfoBaseUri.text(handleNull(fdpUri));
        this.elements.fdpInfoPersistentUrl.text(handleNull(fdpPersistentUrl));
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
    static createAndLaunch(name, apiClient) {
        const dialog = new FDPInfoDialog(name, apiClient);
        dialog.launch();
    }
}
