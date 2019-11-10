/* global $, theProject, Refine, window, DialogSystem */

let MetadataHelpers = {
    moduleName: "metadata"
};

MetadataHelpers.ajax = (command, method, body, success, error, params) => {
    body = body || {};

    params = params || {};
    params.project = theProject.id;

    const commandUrl =  "command/" + MetadataHelpers.moduleName + "/" + command + "?" + $.param(params);

    let done = false;
    let dismissBusy = null;

    const makeDone = () => {
        done = true;
        if (dismissBusy) {
            dismissBusy();
        }

        Refine.clearAjaxInProgress();
    };

    Refine.setAjaxInProgress();

    $.ajax({
        url: commandUrl,
        type: method,
        data: body,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success(data) {
            makeDone();
            success(data);
        },
        error(data) {
            makeDone();
            error(data);
        },
    });

    window.setTimeout(function() {
        if (!done) {
            dismissBusy = DialogSystem.showBusy();
        }
    }, 500);
};

MetadataHelpers.download = (contenBase64, filename, contentType) => {
    if(!contentType) {
        contentType = "application/octet-stream";
    }
    let link = document.createElement("a");
    link.download = filename;
    link.href = `data:${contentType};base64,${contenBase64}`;
    link.click();
};

MetadataHelpers.copyToClipboard = (text) => {
    let $temp = $("<input>");
    $("body").append($temp);
    $temp.val(text).select();
    document.execCommand("copy");
    $temp.remove();
};

