/* global DOM, DialogSystem, MetadataApiClient, MetadataAuditDialog */

class MetadataAboutDialog {
    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/about-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.apiClient = new MetadataApiClient();

        this.frame.i18n();
        this.bindActions();

        this.apiClient.getSettings([
            (result) => {
                this.loadSettings(result);
                if (result.projectInfo) {
                    this.showProjectInfo(result.projectInfo);
                }
            }
        ]);
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    loadSettings(settings) {
        if (settings.settings.auditShow === true) {
            this.elements.auditButton.removeClass("hidden");
        }
    }

    bindActions() {
        this.elements.closeButton.click(() => {
            this.dismiss();
        });
        this.elements.auditButton.click(() => {
            MetadataAuditDialog.createAndLaunch();
        });
    }

    showProjectInfo(info) {
        if (info.version) {
            this.elements.infoVersion.empty();
            const link = `https://github.com/FAIRDataTeam/OpenRefine-metadata-extension/releases/tag/v${info.version}`;
            this.elements.infoVersion.append(
                $("<div>").addClass("version").append(
                    $("<a>").attr("href", link).attr("target", "_blank").text(info.version)
                )
            );
        }
        if (info.name) {
            this.elements.infoName.empty();
            this.elements.infoName.text(info.name);
        }
        if (info.openrefineVersion && info.openrefineSupported) {
            this.elements.infoOpenRefine.empty();
            let list = $("<ul>").addClass("versions");
            info.openrefineSupported.forEach((version) => {
                const link = `https://github.com/OpenRefine/OpenRefine/releases/tag/${version}`;
                list.append($("<li>")
                    .addClass(version === info.openrefineVersion ? "version supported" : "version")
                    .append(
                        $("<a>").attr("href", link).attr("target", "_blank").text(version)
                    )
                );
            });
            this.elements.infoOpenRefine.append(list);
        }
    }

    // launcher
    static createAndLaunch() {
        const dialog = new MetadataAboutDialog();
        dialog.launch();
    }
}
