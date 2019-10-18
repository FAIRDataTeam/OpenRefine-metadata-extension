/* global $, DOM, DialogSystem, Refine, MetadataHelpers */

class MetadataFormDialog {
    constructor(type, specs, callbackFn) {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/metadata-form-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;

        this.type = type;
        this.specs = specs;
        this.callbackFn = callbackFn;

        this.initBasicTexts();
        this.createForm();
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
            let result = {};
            this.specs.fields.forEach((field) => {
                if (field.multiple) {
                    result[field.id] = elmts.metadataForm
                        .find(`.multiple-${field.id}`).map(function() {
                            return $(this).val();
                        }).get();
                } else {
                    result[field.id] = elmts.metadataForm.find(`#${field.id}`).val();
                }
            });

            // TODO: POST to FDP
            // TODO: if valid, retrieve the new one (including the URI and retrieve is as result)
            // TODO: if not valid, show problems
            this.callbackFn(result);
            this.dismiss();
        });
    }

    // helpers
    makeLabel(field) {
        return $("<label>")
            .attr("for", field.id)
            .text($.i18n(`metadata/${this.specs.id}/${field.id}/name`));
    }

    makeInput(field) {
        const input = $(field.type === "text" ? "<textarea>" : "<input>")
            .attr("id", field.id)
            .attr("name", field.id)
            .attr("type", field.type === "iri" ? "url" : "text")
            .attr("placeholder", field.type === "iri" ? "http://" : "")
            .attr("title", $.i18n(`metadata/${this.specs.id}/${field.id}/description`))
            .prop("required", field.required);

        if (field.multiple) {
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
        } else {
            return input;
        }
    }

    makeFormGroup(field) {
        let fieldDiv = $("<div>")
            .attr("id", `form-group-${field.id}`)
            .addClass("metadata-form-group")
            .addClass(field.required ? "required" : "optional");

        if (!field.required) { fieldDiv.addClass("hidden"); }

        fieldDiv.append(this.makeLabel(field));
        fieldDiv.append(this.makeInput(field));

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
    static createAndLaunch(type, specs, callbackFn) {
        const dialog = new MetadataFormDialog(type, specs, callbackFn);
        dialog.launch();
    }
}
