/* global $, DOM, DialogSystem, MetadataHelpers */

class StoreDataDialog {

    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/store-data-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.storeCallback = this.defaultCallback();

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

    setCallback(callback) {
        this.storeCallback = callback;
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
            self.elements.errorMessage.empty();
            const storeDataRequest = JSON.stringify({
                mode: "preview",
                format: elmts.fileFormatSelect.val(),
                storage: elmts.storageSelect.val(),
                filename: elmts.filenameInput.val(),
            });

            MetadataHelpers.ajax("store-data", "POST", storeDataRequest, (data) => {
                if (data.status === "ok") {
                    MetadataHelpers.download(data.data, data.filename, data.contentType);
                } else {
                    self.elements.errorMessage.text($.i18n(data.message, data.exception));
                }
            });
        });

        elmts.storeButton.click((event) => {
            self.elements.errorMessage.empty();
            event.preventDefault();
            const storeDataRequest = JSON.stringify({
                mode: "store",
                format: elmts.fileFormatSelect.val(),
                storage: elmts.storageSelect.val(),
                filename: elmts.filenameInput.val(),
            });
            MetadataHelpers.ajax("store-data", "POST", storeDataRequest, (data) => {
                if (data.status === "ok") {
                    self.storeCallback(data.url);
                } else {
                    self.elements.errorMessage.text($.i18n(data.message, data.exception));
                }

            });
        });

        elmts.storageSelect.on("change", () => {
            const selectedStorage = elmts.storageSelect.val();
            const storage =  this.storages.find((s) => { return s.name === selectedStorage; });
            if (storage) {
                this.showFormats(storage.contentTypes);
            }
        });
    }

    showFormats(contentTypes) {
        const allowedTypes = new Set(contentTypes);
        const unusables = new Map();
        const addFormat = (format) => {
            if (format.usable) {
                const label = $.i18n(`store-data-dialog/formats/${format.identifier}`);
                this.elements.fileFormatSelect.append(
                    $("<option>").val(format.identifier).text(`${label} (*.${format.extension})`)
                );
            } else {
                unusables.set(format.source, unusables.get(format.source) || []);
                unusables.get(format.source).push(format);
            }
        };

        this.elements.unusableFormats.empty();
        this.elements.fileFormatSelect.empty();
        this.formats.forEach((format) => {
            if (!contentTypes || allowedTypes.has(format.contentType)) {
                addFormat(format);
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
            const label = $.i18n(`store-data-dialog/storages/${storage.type}`, storage.name, storage.host, storage.directory);
            this.elements.storageSelect.append(
                $("<option>").val(storage.name).text(label)
            );
        });
    }

    defaultCallback() {
        return (url) => {
            this.elements.storeDataResult.empty();
            this.elements.storeDataResult.append(
                $("<span>").addClass("intro").text($.i18n("store-data-dialog/result"))
            );
            this.elements.storeDataResult.append(
                $("<a>").addClass("link").attr("href", url).attr("target", "_blank").text(url)
            );
            this.elements.storeDataResult.append(
                $("<button>").addClass("copy-clipboard").text($.i18n("store-data-dialog/copy-clipboard")).click(() => {
                    MetadataHelpers.copyToClipboard(url);
                })
            );
        };
    }

    // launcher
    static createAndLaunch(callback) {
        const dialog = new StoreDataDialog();
        dialog.launch(callback);
    }
}
