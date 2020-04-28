/* global DOM, DialogSystem, Refine, MetadataApiClient, StoreDataDialog */

class MetadataFormDialog {
    constructor(apiClient, specs, callbackFn, prefill) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/metadata-form-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.type = specs.id;
        this.specs = specs;
        this.callbackFn = callbackFn;
        this.apiClient = apiClient;
        this.shaclSpec = "";

        this.datalists = new Set();

        this.prefill = prefill || new Map();

        this.initBasicTexts();
        this.getSpec();
        this.bindActions();
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
        this.elements.dialogTitle.text($.i18n(`metadata/${this.type}/dialog-title`));
    }

    bindActions() {
        const self = this;
        const elmts = this.elements;

        elmts.closeButton.click(() => { self.dismiss(); });

        elmts.optionalShowButton.click(() => {
            this.frame.find(".optional").each(function() {
                $(this).removeClass("hidden");
            });
            elmts.optionalShowButton.addClass("hidden");
            elmts.optionalHideButton.removeClass("hidden");
        });

        elmts.optionalHideButton.click(() => {
            this.frame.find(".optional").each(function() {
                $(this).addClass("hidden");
            });
            elmts.optionalShowButton.removeClass("hidden");
            elmts.optionalHideButton.addClass("hidden");
        });
    }

    getValue(fieldId) {
        const value = this.elements.metadataForm.find(`#${fieldId}`).val();
        return value === "" ? null : value;
    }

    setValue(fieldId, value) {
        const input = this.elements.metadataForm.find(`#${fieldId}`);
        if (input !== null) {
            if (input.is(":radio") && value === true) {
                input.prop("checked", true);
            } else {
                input.val(value);
            }
            input.trigger("change");
        }
    }

    getSpec() {
        this.apiClient.getMetadataSpec(this.type, [
            (result) => {
                this.shaclSpec = result.spec;
                this.createForm();
            }
        ], []);
    }

    createForm() {
        const elmts = this.elements;

        this.specs.fields.forEach((field) => {
            elmts.metadataForm.append(this.makeFormGroup(field));
        });

        const submitBtn = $("<button>")
            .attr("type", "submit")
            .addClass("button")
            .text($.i18n(`metadata/${this.specs.id}/submit`));
        elmts.metadataForm.append(submitBtn);

        elmts.metadataForm.submit((e) => {
            e.preventDefault();
            elmts.errorMessage.empty();
            let result = {};
            const gatherResults = (fields) => {
                fields.forEach((field) => {
                    if (field.multiple) {
                        result[field.id] = elmts.metadataForm
                            .find(`.multiple-${field.id}`).map(function() {
                                return $(this).val();
                            }).get().filter((e) => e !== "");
                    } else if(field.type === "xor") {
                        field.options.forEach((option) => {
                            result[option.id] = this.getValue(option.id);
                        });
                    } else {
                        result[field.id] = this.getValue(field.id);
                    }
                    if (field.nested) {
                        gatherResults(field.nested.fields);
                    }
                });
            };
            gatherResults(this.specs.fields);
            this.callbackFn(result, this);
        });

        if (this.specs.storeData) {
            const formGroupId = `#form-group-${this.specs.storeData.inline}`;
            const target = this.specs.storeData.target;
            const others = this.specs.storeData.others;

            this.makeStoreDataButton(
                (url, format, contentType, byteSize) => {
                    Object.entries(others).forEach(([fieldId, value]) => {
                        this.setValue(fieldId, value);
                    });
                    this.setValue(target, url);
                    this.setValue("format", format);
                    this.setValue("mediaType", contentType);
                    this.setValue("bytesize", byteSize);
                },
                formGroupId
            );
        }

        this.fillForm(this.prefill);
    }

    displayError(errorName, errorMessage) {
        this.elements.errorMessage.empty();
        let basicMsg = "";
        let mainMsg = $.i18n("metadata-post/error-general");
        if (errorName.endsWith("MetadataException")) {
            basicMsg = $.i18n("metadata-post/error-fdp-template", $.i18n(`metadata/${this.specs.id}/name`));
            mainMsg = errorMessage;
        } else if (errorName.endsWith("FairDataPointException")) {
            mainMsg = $.i18n("metadata-post/error-api",  $.i18n(`metadata/${this.specs.id}/name`));
        } else if (errorName.endsWith("ConnectException")) {
            mainMsg = $.i18n("metadata-post/error-communication",  $.i18n(`metadata/${this.specs.id}/name`));
        }
        this.elements.errorMessage.text(basicMsg);
        this.elements.errorMessage.append($("<span>").addClass("fdp-message").text(mainMsg));
    }

    fillForm(prefill) {
        const elmts = this.elements;
        prefill.forEach((value, key) => {
            this.setValue(key, value);
        });
    }

    // helpers
    callbackTypehints(field) {
        return (result) => {
            if (result.status !== "ok") {
                // ignore error for typehints
                return;
            }
            const datalist = this.frame.find(`#list-${field.typehints.name}`);
            datalist.empty();
            result.datalist.forEach((entry) => {
                const text = `${entry.title} (${entry.value})${entry.description ? ": " + entry.description : ""}`;
                datalist.append(
                    $("<option>").attr("data-value", entry.value).text(text));
            });
        };
    }

    makeDataList(field, input) {
        const listId = `list-${field.typehints.name}`;
        input.attr("list", listId);

        if (!this.datalists.has(field.typehints.name)) {
            this.datalists.add(field.typehints.name);
            this.elements.datalists.append(
                $("<datalist>").attr("id", listId).addClass(field.typehints.type)
            );

            if (field.typehints.type === "static") {
                this.apiClient.getTypehints(
                    field.typehints.name,
                    null,
                    [this.callbackTypehints(field)],
                    []
                );
            }
        }

        const self = this;
        if (field.typehints.type === "dynamic") {
            let ajaxTimer;
            input.keyup(function() {
                if ($(this).val() !== "") {
                    clearTimeout(ajaxTimer);
                    ajaxTimer = setTimeout(() => {
                        self.apiClient.getTypehints(
                            field.typehints.name,
                            $(this).val(),
                            [self.callbackTypehints(field)],
                            []
                        );
                    }, 500);
                }
            });
            input.keydown(function() {
                clearTimeout(ajaxTimer);
            });
        }

        input.on("change", function() {
            const shownValue = $(this).val();
            self.frame.find(`#${listId}`).children().each((index, element) => {
                if (shownValue === $(element).text()) {
                    $(this).val($(element).data("value"));
                }
            });
        });
    }

    handleInputType(field, input) {
        if (field.hidden) {
            input.attr("type", "hidden");
        } else if (field.type === "iri") {
            input
                .attr("type", "uri")
                .attr("placeholder", "http://");
        }
    }

    handleTypehints(field, input) {
        if (field.typehints) {
            this.makeDataList(field, input);
        }
    }

    makeInputField(field) {
        let input = $(field.type === "text" ? "<textarea>" : "<input>")
            .attr("id", field.id)
            .attr("name", field.id)
            .attr("type", "text")
            .attr("title", $.i18n(`metadata/${this.specs.id}/${field.id}/description`))
            .prop("required", field.required);
        this.handleInputType(field, input);
        this.handleTypehints(field, input);
        return input;
    }

    makeLabel(field) {
        return $("<label>")
            .attr("for", field.id)
            .text($.i18n(`metadata/${this.specs.id}/${field.id}/name`));
    }

    makeMultipleInput(input, field) {
        input.removeAttr("id");
        input.addClass(`multiple-${field.id}`);
        const wrapper = $("<div>")
            .addClass("multiple-field")
            .attr("id", field.id);

        let counter = 0;
        const createRow = function(input) {
            const inputCopy = input.clone(true, true);
            inputCopy.attr("name", `${field.id}[${counter}]`);
            counter++;

            const deleteButton = $("<button>")
                .attr("type", "button")
                .addClass("button button-multiple-delete")
                .text("-")
                .click(function(e){
                    e.preventDefault();
                    $(this).parent().remove();
                });
            const addButton = $("<button>")
                .attr("type", "button")
                .addClass("button button-multiple-add")
                .text("+")
                .click(function(e){
                    e.preventDefault();
                    $(this).parent().parent().append(createRow(input));
                });
            return $("<div>")
                .addClass("multiple-row")
                .append(inputCopy)
                .append(deleteButton)
                .append(addButton);
        };
        wrapper.append(createRow(input));
        return wrapper;
    }

    makeInput(field) {
        const input = this.makeInputField(field);
        return field.multiple ? this.makeMultipleInput(input, field) : input;
    }

    makeInitFieldDiv(field) {
        return $("<div>")
            .attr("id", `form-group-${field.id}`)
            .addClass("metadata-form-group");
    }

    makeOptionalFieldDiv(field) {
        return this.makeInitFieldDiv(field)
            .addClass("optional")
            .addClass("hidden")
            .append(this.makeLabel(field))
            .append(this.makeInput(field));
    }

    makeRequiredFieldDiv(field) {
        return this.makeInitFieldDiv(field)
            .addClass("required")
            .append(this.makeLabel(field))
            .append(this.makeInput(field));
    }

    makeHiddenFieldDiv(field) {
        return this.makeInitFieldDiv(field)
            .append(this.makeInput(field));
    }

    makeFieldDiv(field) {
        if (field.hidden) {
            return this.makeHiddenFieldDiv(field);
        } else if (field.required) {
            return this.makeRequiredFieldDiv(field);
        } else {
            return this.makeOptionalFieldDiv(field);
        }
    }

    makeXorFormGroup(field) {
        let fieldDiv = this.makeInitFieldDiv(field);
        if (field.required) {
            fieldDiv.addClass("required");
        } else {
            fieldDiv.addClass("optional").addClass("hidden");
        }
        const xorFields = $("<div>").addClass("xor-fields");
        const xorSwitches = $("<div>").addClass("xor-switches");
        field.options.forEach((option) => {
            const switchId = `${field.id}-${option.id}`;
            const inputField = this.makeInputField(option)
                .addClass("hidden")
                .addClass(`field-${field.id}`);
            const xorSwitch = $("<div>")
                .addClass("xor-switch")
                .append(
                    $("<input>")
                        .attr("type", "radio")
                        .attr("name", `${field.id}`)
                        .attr("id", switchId)
                        .val(option.id)
                        .prop("required", field.required)
                ).append(
                    $("<label>")
                        .attr("for", switchId)
                        .text($.i18n(`metadata/${this.specs.id}/${option.id}/name`))
                );
            xorSwitch.on("change", () => {
                if(!$(`#${switchId}`).is(":checked")) {
                    return;
                }
                $(`.field-${field.id}`).each(function() {
                    $(this)
                        .addClass("hidden")
                        .val("")
                        .prop("required", false);

                });
                $(`#${option.id}`)
                    .removeClass("hidden")
                    .prop("required", field.required);
            });
            xorSwitches.append(xorSwitch);
            xorFields.append(inputField);
        });
        fieldDiv.append(xorSwitches);
        fieldDiv.append(xorFields);
        return fieldDiv;
    }

    makeFormGroup(field) {
        if (field.type === "xor") {
            return this.makeXorFormGroup(field);
        }

        let fieldDiv = this.makeFieldDiv(field);

        if ("nested" in field) {
            let nestedDiv = $("<div>")
                .addClass("nested-fields");
            field.nested.fields.forEach((innerField) => {
                nestedDiv.append(this.makeFormGroup(innerField));
            });
            fieldDiv.append(nestedDiv);
        }

        return fieldDiv;
    }

    makeStoreDataButton(callback, formGroupId) {
        this.frame.find(formGroupId).append($("<div>").addClass("store-button-row").append(
            $("<button>")
                .addClass("button")
                .addClass("store-data")
                .attr("type", "button")
                .text($.i18n("metadata/storeData"))
                .click(() => {
                    const dialog = new StoreDataDialog();
                    dialog.setCallback((...args) => {
                        callback(...args);
                        dialog.dismiss();
                    });
                    dialog.launch();
                })
        ));
    }

    // launcher
    static createAndLaunch(apiClient, specs, callbackFn, prefill) {
        const dialog = new MetadataFormDialog(apiClient, specs, callbackFn, prefill);
        dialog.launch();
    }
}
