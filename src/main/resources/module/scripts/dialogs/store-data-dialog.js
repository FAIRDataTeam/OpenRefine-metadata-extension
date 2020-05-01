/* global DOM, DialogSystem, MetadataHelpers, MetadataStorageSpecs, MetadataAuditDialog */

class StoreDataDialog {

    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/store-data-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.storeCallback = this.defaultCallback();
        this.metadataFields = new Map();
        this.apiClient = new MetadataApiClient();
        this.allowCustomStorage = false;

        this.initBasicTexts();
        this.bindActions();

        this.apiClient.getSettings([
            (result) => {
                this.loadSettings(result);
            }
        ]);
        this.apiClient.getStorageInfo([(data) => {
            console.log(data);
            this.formats = data.formats;
            this.storages = data.storages;
            this.storages.forEach((storage) => {
                storage.details = new Map(Object.entries(storage.details));
            });
            console.log(this.storages);
            this.defaults = new Map(Object.entries(data.defaults));

            this.showStorages();
            this.showFormats();
        }]);
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

    loadSettings(settings) {
        this.allowCustomStorage = settings.settings.allowCustomStorage === true;
        if (settings.settings.auditShow === true) {
            this.elements.auditButton.removeClass('hidden');
        }
    }

    initBasicTexts() {
        this.frame.i18n();
    }

    prepareStoreDataRequest(mode) {
        const elmts = this.elements;
        let metadata = new Map();

        this.metadataFields.forEach((value, key, map) => {
            metadata.set(key, value.val());
        });

        return {
            mode,
            format: elmts.fileFormatSelect.val(),
            storage: elmts.storageSelect.val(),
            metadata: Object.fromEntries(metadata.entries())
        };
    }

    validateStoreDataRequest(request) {
        if (request.format === null || request.storage === null) {
            this.elements.errorMessage.text($.i18n("store-data-dialog/error/select-storage-format"));
            return false;
        }
        return true;
    }

    sendStoreDataRequest(request, okCallback) {
        this.elements.errorMessage.empty();
        if (this.validateStoreDataRequest(request)) {
            MetadataHelpers.ajax("store-data", "POST", JSON.stringify(request), (data) => {
                if (!data) {
                    this.elements.errorMessage.text($.i18n("store-data-dialog/error/cannot-export"));
                } else if (data.status === "ok") {
                    okCallback(data);
                } else {
                    this.elements.errorMessage.text($.i18n(data.message, data.exception));
                }
            });
        }
    }

    bindActions() {
        const elmts = this.elements;

        elmts.closeButton.click(() => { this.dismiss(); });
        elmts.auditButton.click(() => { MetadataAuditDialog.createAndLaunch() });

        elmts.previewButton.click((event) => {
            // TODO handle custom storage
            event.preventDefault();
            this.sendStoreDataRequest(
                this.prepareStoreDataRequest("preview"),
                (data) => {
                    MetadataHelpers.download(data.data, data.filename, data.contentType);
                }
            );
        });

        elmts.storeButton.click((event) => {
            // TODO handle custom storage
            event.preventDefault();
            this.sendStoreDataRequest(
                this.prepareStoreDataRequest("store"),
                (data) => {
                    this.storeCallback(data.url, elmts.fileFormatSelect.val(), data.contentType, data.byteSize);
                }
            );
        });

        elmts.storageSelect.on("change", () => {
            const selectedStorage = elmts.storageSelect.val();
            if (selectedStorage == "_custom" && this.allowCustomStorage) {
                this.showCustomStorage();
            } else {
                this.showDefinedStorage(selectedStorage);
            }
        });
    }

    showCustomStorage() {
        this.elements.customStorageFields.removeClass("hidden");
        // TODO details fields
    }

    showDefinedStorage(selectedStorage) {
        this.elements.customStorageFields.addClass("hidden");
        const storage = this.storages.find((s) => { return s.name === selectedStorage; });
        this.showStorageLimits(storage);
        if (storage) {
            this.showFormats(storage.contentTypes);
            this.showStorageFields(storage);
        }
    }

    formatByteSize(byteSize) {
        if (byteSize <= 0) {
            return {
                size: 0,
                unit: "B",
            };
        }
        const units = ["B", "kB", "MB", "GB", "TB"];
        let x = units.length-1;
        while (byteSize < Math.pow(1024, x)) {
            x -= 1;
        }
        return {
            size: Math.ceil(100 * byteSize / Math.pow(1024, x)) / 100,
            unit: units[parseInt(x)],
        };
    }

    showStorageLimits(storage) {
        this.elements.storageLimits.empty();
        if (storage && storage.maxByteSize > 0) {
            const {size, unit} = this.formatByteSize(storage.maxByteSize);
            this.elements.storageLimits.text(
                $.i18n("store-data-dialog/max-bytesize", size, unit)
            );
        }
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
        this.elements.fileFormatSelect.append($("<option>")
            .prop("disabled", true)
            .prop("selected", true)
            .text($.i18n("common/select-option/storage"))
        );
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
        this.elements.storageSelect.append($("<option>")
            .prop("disabled", true)
            .prop("selected", true)
            .text($.i18n("common/select-option/storage"))
        );
        if (this.allowCustomStorage) {
            this.elements.storageSelect.append($("<option>")
                .val("_custom")
                .text($.i18n("store-data-dialog/custom"))
            );
        }
        this.storages.forEach((storage) => {
            const host = storage.details.get("host");
            const loc = storage.details.has("directory") ? storage.details.get("directory") : storage.details.get("repository");
            const label = $.i18n(`store-data-dialog/storages/${storage.type}`, storage.name, host, loc);
            this.elements.storageSelect.append(
                $("<option>").val(storage.name).text(label)
            );
        });
    }

    showStorageFields(storage) {
        this.elements.storageFields.empty();
        this.metadataFields.clear();

        const storageSpec = MetadataStorageSpecs.types.get(storage.type);
        if (storageSpec) {
            storageSpec.metadata.forEach((fieldId) => {
                if (MetadataStorageSpecs.metadata.has(fieldId)) {
                    this.addStorageField(storage, MetadataStorageSpecs.metadata.get(fieldId));
                }
            });
        }
    }

    addStorageField(storage, fieldSpec) {
        const label = $("<label>")
            .attr("for", fieldSpec.id)
            .text($.i18n(`store-data-dialog/form/${fieldSpec.id}`));
        const field = $("<input>")
            .attr("name", fieldSpec.id)
            .attr("id", fieldSpec.id)
            .prop("required", fieldSpec.required)
            .attr("type", fieldSpec.type);
        const note = $("<div>")
            .addClass("input-note")
            .text($.i18n(`store-data-dialog/form/${fieldSpec.id}/note`));

        if (this.defaults.has(fieldSpec.id)) {
            field.val(this.defaults.get(fieldSpec.id));
        }

        this.metadataFields.set(fieldSpec.id, field);
        let formGroup = $("<div>").addClass("form-group").append(label).append(field).append(note);
        if (fieldSpec.id === "filename" && storage.filenamePatterns && storage.filenamePatterns.length > 0) {
            formGroup.append(
                $("<div>")
                    .addClass("input-note")
                    .text($.i18n("store-data-dialog/filename-patterns", storage.filenamePatterns.join(", ")))
            );
        }
        this.elements.storageFields.append(formGroup);
    }

    defaultCallback() {
        return (url, format, contentType, byteSize) => {
            const {size, unit} = this.formatByteSize(byteSize);
            this.elements.storeDataResult.empty();
            this.elements.storeDataResult.append(
                $("<span>").addClass("intro").text($.i18n("store-data-dialog/result", contentType, size, unit))
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
