/* global $, DOM, DialogSystem, Refine, MetadataHelpers */

class MetadataFormDialog {
    constructor(type, specs, callbackFn, prefill) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/metadata-form-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.type = type;
        this.specs = specs;
        this.callbackFn = callbackFn;

        const prefillObj = prefill || {};

        this.initBasicTexts();
        this.createForm();
        this.fillForm(prefillObj);
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

    getValue(field) {
        const value = this.elements.metadataForm.find(`#${field.id}`).val();
        return value === "" ? null : value;
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
                            result[option.id] = this.getValue(option);
                        });
                    } else {
                        result[field.id] = this.getValue(field);
                    }
                    if (field.nested) {
                        gatherResults(field.nested.fields);
                    }
                });
            };
            gatherResults(this.specs.fields);
            this.callbackFn(result, this);
        });
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

    fillForm(obj) {
        const elmts = this.elements;
        Object.entries(obj).forEach(([fieldId, value]) => {
            const field = elmts.metadataForm.find(`#${fieldId}`);
            if (field) {
                field.val(value);
            }
        });
    }

    // helpers
    makeInputField(field) {
        const input = $(field.type === "text" ? "<textarea>" : "<input>")
            .attr("id", field.id)
            .attr("name", field.id)
            .attr("type", "text")
            .attr("title", $.i18n(`metadata/${this.specs.id}/${field.id}/description`))
            .prop("required", field.required);

        if (field.hidden) {
            input.attr("type", "hidden");
        } else if (field.type === "iri") {
            input
                .attr("type", "uri")
                .attr("placeholder", "http://");
        }
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
            const inputCopy = input.clone();
            inputCopy.attr("name", `${field.id}[${counter}]`);
            counter++;

            const deleteButton = $("<button>")
                .attr("type", "button ")
                .addClass("button button-multiple-delete")
                .text("-")
                .click(function(e){
                    e.preventDefault();
                    $(this).parent().remove();
                });
            const addButton = $("<button>")
                .attr("type", "button ")
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

    // launcher
    static createAndLaunch(type, specs, callbackFn, prefill) {
        const dialog = new MetadataFormDialog(type, specs, callbackFn, prefill);
        dialog.launch();
    }
}