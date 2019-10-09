/* global $, DOM, DialogSystem, Refine, MetadataHelpers */
let MetadataFormDialog = {};

MetadataFormDialog.launch = function(type, specs, callback) {
    this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/metadata-form-dialog.html"));
    this._elmts = DOM.bind(this.frame);

    this._level = DialogSystem.showDialog(this.frame);

    let dialog = this;
    let elmts = this._elmts;

    MetadataFormDialog.initBasicTexts(dialog, type);
    MetadataFormDialog.createForm(dialog, specs, callback);

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

MetadataFormDialog.createForm = (dialog, specs, callback) => {

    const makeLabel = (field) => {
        return $("<label>")
            .attr("for", field.id)
            .text($.i18n(`metadata/${specs.id}/${field.id}/name`));
    };

    const makeInput = (field) => {
        return $(field.type === "text" ? "<textarea>" : "<input>")
            .attr("id", field.id)
            .attr("name", field.id)
            .attr("type", field.type === "iri" ? "url" : "text")
            .prop("required", field.required);
        // TODO: multiple? (need more info)
    };

    const makeTooltip = (field, input, parent) => {
        const tooltipId = `tooltip-${field.id}`;
        input.focus(() => {
            parent.find(`#${tooltipId}`).removeClass("hidden");
        });
        input.focusout(() => {
            parent.find(`#${tooltipId}`).addClass("hidden");
        });
        return $("<div>")
            .attr("id", tooltipId)
            .addClass("tooltip").addClass("hidden")
            .text($.i18n(`metadata/${specs.id}/${field.id}/description`));

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
        fieldDiv.append(makeTooltip(field, input, fieldDiv));

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
            result[field.id] = dialog._elmts.metadataForm.find(`#${field.id}`).val();
        });
        callback(result);
        MetadataFormDialog.dismissFunc(dialog)();
    });
};
