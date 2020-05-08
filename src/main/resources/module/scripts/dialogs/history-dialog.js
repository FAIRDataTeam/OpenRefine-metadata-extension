/* global DOM, DialogSystem, MetadataApiClient */

class MetadataHistoryDialog {
    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/history-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.apiClient = new MetadataApiClient();

        this.records = [];

        this.frame.i18n();

        this.bindActions();
        this.refreshHistory();
    }

    launch() {
        this.level = DialogSystem.showDialog(this.frame);
    }

    dismiss() {
        DialogSystem.dismissUntil(this.level - 1);
        this.level = null;
    }

    bindActions() {
        this.elements.closeButton.click(() => {
            this.dismiss();
        });
        this.elements.refreshButton.click(() => {
            this.refreshHistory();
        });
        this.elements.clearButton.click(() => {
            this.clearHistory();
        });
    }

    updateCallback() {
        return [(result) => {
            this.records = result?.projectData?.records ?? [];
            this.showHistory();
        }];
    }

    refreshHistory() {
        this.apiClient.getSettings(this.updateCallback());
    }

    clearHistory() {
        this.apiClient.clearProjectHistory(this.updateCallback());
    }

    showHistory() {
        this.elements.historyTableBody.empty();
        this.records.forEach((record) => {
            const timestamp = Date.parse(record.timestamp.split(".", 1)[0]);
            let date = "";
            let time = "";
            if (timestamp) {
                date = timestamp.toLocaleDateString();
                time = timestamp.toLocaleTimeString();
            }
            this.elements.historyTableBody.append(
                $("<tr>")
                    .append($("<td>").addClass("date").text(date))
                    .append($("<td>").addClass("time").text(time))
                    .append($("<td>")
                        .addClass(`type-${record.details.type.toLowerCase()}`)
                        .text($.i18n(`history/type/${record.details.type.toLowerCase()}`)))
                    .append($("<td>")
                        .append($("<a>")
                            .attr("href", record.uri)
                            .attr("target", "_blank")
                            .text(record.details.title)))
                    .append($("<td>")
                        .append($("<a>")
                            .attr("href", record.details.parent)
                            .attr("target", "_blank")
                            .text($.i18n(`history/parent`))))
            );
        });
        this.elements.historyItems.text(this.records.length);
    }

    // launcher
    static createAndLaunch() {
        const dialog = new MetadataHistoryDialog();
        dialog.launch();
    }
}
