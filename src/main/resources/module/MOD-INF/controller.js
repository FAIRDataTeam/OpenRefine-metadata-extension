/*
 * Main JS file for FAIR Metadata extension of OpenRefine
 */

function init() {

    // Resources
    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/menu-bar-extension.js",
            "scripts/dialogs/post-fdp-initial-dialog.js",
        ]);
}



