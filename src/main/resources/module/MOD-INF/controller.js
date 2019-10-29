/*
 * Main JS file for FAIR Metadata extension of OpenRefine
 */
/* global importPackage, module, Packages, ConnectFDPCommand, ClientSideResourceManager */
/* eslint-disable no-unused-vars */

function init() {
    var RefineServlet = Packages.com.google.refine.RefineServlet;

    // Commands
    RefineServlet.registerCommand(module, "fdp-auth", new Packages.solutions.fairdata.openrefine.metadata.commands.AuthCommand());
    RefineServlet.registerCommand(module, "fdp-metadata", new Packages.solutions.fairdata.openrefine.metadata.commands.FDPMetadataCommand());
    RefineServlet.registerCommand(module, "catalogs-metadata", new Packages.solutions.fairdata.openrefine.metadata.commands.CatalogsMetadataCommand());
    RefineServlet.registerCommand(module, "datasets-metadata", new Packages.solutions.fairdata.openrefine.metadata.commands.DatasetsMetadataCommand());
    RefineServlet.registerCommand(module, "distributions-metadata", new Packages.solutions.fairdata.openrefine.metadata.commands.DistributionsMetadataCommand());

    // Resources
    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/api-client.js",
            "scripts/helpers.js",
            "scripts/menu-bar-extension.js",
            "scripts/metadata-specs.js",
            "scripts/dialogs/metadata-form-dialog.js",
            "scripts/dialogs/post-fdp-dialog.js",
        ]);
    ClientSideResourceManager.addPaths(
        "project/styles",
        module,
        [
            "styles/dialogs/metadata-form-dialog.less",
            "styles/dialogs/post-fdp-dialog.less",
        ]);
}



