/* global $, DOM, DialogSystem, MetadataHelpers */

class StoreDataDialog {

    constructor(callback) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/store-data-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.callback = callback || this.defaultCallback;

        this.initBasicTexts();
        this.bindActions();

        MetadataHelpers.ajax("store-data", "GET", null, (data) => {
            this.formats = data.formats;
            this.storages = data.storages;
            this.showStorages();
            this.showFormats();

            this.elements.filenameInput.val(data.defaultFilename);
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

        elmts.previewButton.click(() => {
            const storeDataRequest = JSON.stringify({
                mode: "preview",
                format: elmts.fileFormatSelect.val(),
                storage: elmts.storageSelect.val(),
                filename: elmts.filenameInput.val(),
            });

            MetadataHelpers.ajax("store-data", "POST", storeDataRequest, (data) => {
                MetadataHelpers.download(data.data, data.filename, data.contentType);
            });
        });

        elmts.storeButton.click((event) => {
            event.preventDefault();
            const storeDataRequest = JSON.stringify({
                mode: "store",
                format: elmts.fileFormatSelect.val(),
                storage: elmts.storageSelect.val(),
                filename: elmts.filenameInput.val(),
            });

            MetadataHelpers.ajax("store-data", "POST", storeDataRequest, (data) => {
                this.callback(data.link);
            });
        });

        elmts.storageSelect.on("change", () => {
            const selectedStorage = elmts.storageSelect.val();
            const storage =  this.storages.find((s) => { s.name === selectedStorage });
            if (storage) {
                this.showFormats(storage.contentTypes);
            }
        });
    }

    showFormats(contentTypes) {
        const allowedTypes = new Set(contentTypes);
        const unusables = new Map();
        this.elements.unusableFormats.empty();
        this.elements.fileFormatSelect.empty();
        this.formats.forEach((format) => {
            if (contentTypes && !allowedTypes.has(format.contentType)) {
                return;
            }
            if (format.usable) {
                const label = $.i18n(`store-data-dialog/formats/${format.identifier}`);
                this.elements.fileFormatSelect.append(
                    $("<option>").val(format.identifier).text(`${label} (*.${format.extension})`)
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

    showStorages() {
        this.elements.storageSelect.empty();
        this.storages.forEach((storage) => {
            if (storage.enabled) {
                const label = $.i18n(`store-data-dialog/storages/${storage.type}`, storage.name, storage.host, storage.directory);
                this.elements.storageSelect.append(
                    $("<option>").val(storage.name).text(label)
                );
            }
        });
    }

    defaultCallback(link) {
        // TODO: nicer
        this.elements.storeDataResult.text(link);
    }

    // launcher
    static createAndLaunch() {
        const dialog = new StoreDataDialog();
        dialog.launch();
    }
}
