/* global $, DOM, DialogSystem, Refine, MetadataHelpers */
let MetadataFormDialog = {};

MetadataFormDialog.launch = function(type, specs, callbackFn) {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/metadata-form-dialog.html"));
    this._elmts = DOM.bind(this.frame);

    this._level = DialogSystem.showDialog(this.frame);

    let dialog = this;
    let elmts = this._elmts;

    MetadataFormDialog.initBasicTexts(dialog, type);
    MetadataFormDialog.createForm(dialog, specs, callbackFn);

    // Bind actions
    elmts.closeButton.click(MetadataFormDialog.dismissFunc(dialog));
    elmts.optionalShowButton.click(() => {
        dialog.frame.find(".optional").each(function(){
            $(this).removeClass("hidden");
        });
        elmts.optionalShowButton.addClass("hidden");
        elmts.optionalHideButton.removeClass("hidden");
    });
    elmts.optionalHideButton.click(() => {
        dialog.frame.find(".optional").each(function(){
            $(this).addClass("hidden");
        });
        elmts.optionalShowButton.removeClass("hidden");
        elmts.optionalHideButton.addClass("hidden");
    });
};

MetadataFormDialog.initBasicTexts = (dialog, type) => {
    dialog.frame.i18n();
    dialog._elmts.dialogTitle.text($.i18n(`metadata/${type}/dialog-title`));
};

MetadataFormDialog.dismissFunc = (dialog) => {
    return () => { DialogSystem.dismissUntil(dialog._level - 1); };
};

MetadataFormDialog.createForm = (dialog, specs, callbackFn) => {

    const makeLabel = (field) => {
        return $("<label>")
            .attr("for", field.id)
            .text($.i18n(`metadata/${specs.id}/${field.id}/name`));
    };

    const makeInput = (field) => {
        const input = $(field.type === "text" ? "<textarea>" : "<input>")
            .attr("id", field.id)
            .attr("name", field.id)
            .attr("type", field.type === "iri" ? "url" : "text")
            .attr("placeholder", field.type === "iri" ? "http://" : "")
            .attr("title", $.i18n(`metadata/${specs.id}/${field.id}/description`))
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
    };

    const makeFormGroup = (field) => {
        let fieldDiv = $("<div>")
            .attr("id", `form-group-${field.id}`)
            .addClass("metadata-form-group")
            .addClass(field.required ? "required" : "optional");

        if (!field.required) { fieldDiv.addClass("hidden"); }

        const input = makeInput(field);

        fieldDiv.append(makeLabel(field));
        fieldDiv.append(input);

        if ("nested" in field) {
            let nestedDiv = $("<div>")
                .addClass("nested-fields");
            field.nested.fields.forEach((innerField) => {
                nestedDiv.append(makeFormGroup(innerField));
            });
            fieldDiv.append(nestedDiv);
        }

        return fieldDiv;
    };

    specs.fields.forEach((field) => {
        dialog._elmts.metadataForm.append(makeFormGroup(field));
    });

    const submitBtn = $("<button>")
        .attr("type", "submit")
        .addClass("button")
        .text($.i18n(`metadata/${specs.id}/submit`));
    dialog._elmts.metadataForm.append(submitBtn);

    dialog._elmts.metadataForm.submit((e) => {
        e.preventDefault();
        let result = {};
        specs.fields.forEach((field) => {
            if (field.multiple) {
                result[field.id] = dialog._elmts.metadataForm
                    .find(`.multiple-${field.id}`).map(function() {
                        return $(this).val();
                    }).get();
            } else {
                result[field.id] = dialog._elmts.metadataForm
                    .find(`#${field.id}`).val();
            }
        });

        // TODO: POST to FDP
        // TODO: if valid, retrieve the new one (including the URI and retrieve is as result)
        // TODO: if not valid, show problems
        callbackFn(result);
        MetadataFormDialog.dismissFunc(dialog)();
    });
};
