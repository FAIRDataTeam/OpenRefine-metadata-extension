/* global $, DOM, DialogSystem */

class StoreDataDialog {

    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/store-data-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.initBasicTexts();
        this.bindActions();

        MetadataHelpers.ajax("store-data", "GET", null, (data) => {
            this.formats = data.formats;
            this.showFormats();
        });
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    initBasicTexts() {
        this.frame.i18n();
    }

    bindActions() {
        const self = this;
        const elmts = this.elements;

        elmts.closeButton.click(() => { self.dismiss(); });
    }

    showFormats() {
        const unusables = new Map();
        this.elements.unusableFormats.empty();
        this.elements.fileFormatSelect.empty();
        this.formats.forEach((format) => {
            if (format.usable) {
                const label = $.i18n(`store-data-dialog/formats/${format.identifier}`);
                this.elements.fileFormatSelect.append(
                    $("<option>")
                        .val(format.identifier)
                        .text(`${label} (*.${format.extension})`)
                );
            } else {
                if (!unusables.has(format.source)) {
                    unusables.set(format.source, []);
                }
                unusables.get(format.source).push(format);
            }
        });
        if (unusables.size > 0) {
            const list = $("<ul>");
            unusables.forEach((formats, id, map) => {
                const strFormats = formats.map((f) => f.identifier).join(", ");
                list.append($("<li>").text(
                    $.i18n("store-data-dialog/unusableFormats", id, formats.length, strFormats)
                ));
            });
            this.elements.unusableFormats.append(list);
        }
    }

    // launcher
    static createAndLaunch() {
        const dialog = new StoreDataDialog();
        dialog.launch();
    }
}