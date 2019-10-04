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

MetadataHelpers.fdpMakeURL = (uriObject) => {
    return uriObject.namespace + uriObject.localName;
};