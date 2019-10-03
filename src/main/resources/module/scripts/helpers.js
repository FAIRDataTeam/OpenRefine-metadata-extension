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

MetadataHelpers.showFDPMetadata = (target, fdpMetadata) => {
    let title = $("<a>")
        .attr("href", MetadataHelpers.fdpMakeURL(fdpMetadata.uri))
        .attr("target", "_blank")
        .text(fdpMetadata.title.label)
        .get(0).outerHTML;
    let publisher = $("<a>")
        .attr("href", MetadataHelpers.fdpMakeURL(fdpMetadata.publisher.uri))
        .attr("target", "_blank")
        .text(fdpMetadata.publisher.name.label)
        .get(0).outerHTML;
    let description = fdpMetadata.description.label;

    let table = $("<table>")
        .append("<tr><th>Title</th><td>" + title + "</td></tr>")
        .append("<tr><th>Publisher</th><td>" + publisher + "</td></tr>")
        .append("<tr><th>Description</th><td>" + description + "</td></tr>");

    target
        .append("<p>" + $.i18n("post-fdp-dialog/connected-to-fdp") + "<p>")
        .append(table);
};
