/*
 * Main JS file for FAIR Metadata extension of OpenRefine
 */
/* global importPackage, module, org, Packages, ConnectFDPCommand, ClientSideResourceManager */
/* eslint-disable no-unused-vars */
importPackage(org.fair.openrefine.metadata.commands);

function init() {
    var RefineServlet = Packages.com.google.refine.RefineServlet;

    // Commands
    RefineServlet.registerCommand(module, "connect-fdp", new ConnectFDPCommand());

    // Resources
    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/menu-bar-extension.js",
            "scripts/dialogs/post-fdp-initial-dialog.js",
        ]);
    ClientSideResourceManager.addPaths(
        "project/styles",
        module,
        [
            "styles/dialogs/post-fdp-initial-dialog.less",
        ]);
}



