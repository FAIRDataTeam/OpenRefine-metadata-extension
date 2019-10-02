/*
 * Main JS file for FAIR Metadata extension of OpenRefine
 */
/* global importPackage, module, Packages, ConnectFDPCommand, ClientSideResourceManager */
/* eslint-disable no-unused-vars */

function init() {
    var RefineServlet = Packages.com.google.refine.RefineServlet;

    // Commands
    RefineServlet.registerCommand(module, "connect-fdp", new Packages.solutions.fairdata.openrefine.metadata.commands.ConnectFDPCommand());

    // Resources
    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/helpers.js",
            "scripts/menu-bar-extension.js",
            "scripts/dialogs/post-fdp-dialog.js",
        ]);
    ClientSideResourceManager.addPaths(
        "project/styles",
        module,
        [
            "styles/dialogs/post-fdp-dialog.less",
        ]);
}



