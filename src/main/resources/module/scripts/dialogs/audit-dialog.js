/* global DOM, DialogSystem, MetadataApiClient */

class MetadataAuditDialog {
    constructor() {
        this.frame = $(DOM.loadHTML("metadata", "scripts/dialogs/audit-dialog.html"));
        this.elements = DOM.bind(this.frame);
        this.level = null;
        this.apiClient = new MetadataApiClient();

        this.frame.i18n();

        this.filter = {
            type: "DEBUG",
            source: "ANY",
            format: "JSON",
        };
        this.eventTypes = new Map([
            ["ERROR", 200],
            ["WARNING", 300],
            ["INFO", 400],
            ["DEBUG", 500],
            ["TRACE", 900],
        ]);
        this.filters = {
            type: Array.from(this.eventTypes.keys()),
            source: ["ANY", "FDP_CONNECTION", "FDP_METADATA", "SERVICE", "STORAGE", "AUDIT", "TYPEHINTS", "FRONTEND"],
            format: ["JSON", "PLAIN", "TABLE"],
        };
        this.auditLog = null;

        this.bindActions();
        this.showFilters();
        this.refreshLog();
    }

    refreshLog() {
        this.apiClient.getAuditLog([
            (result) => {
                this.auditLog = result.auditLog;
                this.showLog();
            }
        ]);
    }

    clearLog() {
        this.apiClient.clearAuditLog([
            () => {
                this.refreshLog();
            }
        ]);
    }

    setSelectOptions(select, options, what, toSelect) {
        select.empty();
        options.forEach((option) => {
            select.append(
                $("<option>")
                    .attr("value", option)
                    .text($.i18n(`audit/${what}/${option}`))
            );
        });
        select.val(toSelect)
    }

    showFilters() {
        this.setSelectOptions(this.elements.typeSelect, this.filters.type, "type", this.filter.type);
        this.setSelectOptions(this.elements.sourceSelect, this.filters.source, "source", this.filter.source);
        this.setSelectOptions(this.elements.formatSelect, this.filters.format, "format", this.filter.format);
    }

    showLogTable(entries) {
        entries.forEach((entry) => {
            console.log(entry.timestamp.split("Z", 1)[0]);
            const timestamp = Date.parse(entry.timestamp.split("Z", 1)[0]);
            this.elements.auditTableBody.append(
                $("<tr>")
                    .append($("<td>")
                        .addClass("date")
                        .text(timestamp.toLocaleDateString()))
                    .append($("<td>")
                        .addClass("time")
                        .text(timestamp.toLocaleTimeString()))
                    .append($("<td>")
                        .addClass(`type-${entry.eventType.toLowerCase()}`)
                        .text($.i18n(`audit/type/${entry.eventType}`)))
                    .append($("<td>")
                        .addClass(`source-${entry.eventSource.toLowerCase()}`)
                        .text($.i18n(`audit/source/${entry.eventSource}`)))
                    .append($("<td>")
                        .addClass("message")
                        .text(entry.message))
            );
        });
    }

    showLogJSON(entries) {
        this.elements.auditTextArea.text(
            JSON.stringify(entries, null, "  ")
        );
    }

    showLogPlain(entries) {
        let str = "";
        entries.forEach((entry) => {
            str += `${entry.timestamp} ${entry.eventType} ${entry.eventSource} | ${entry.message}\n`;
        });
        this.elements.auditTextArea.text(str);
    }

    filterEntries(entries) {
        const type = this.elements.typeSelect.val();
        const typeVal = this.eventTypes.get(type);
        const typeFiltered = entries.filter(e => this.eventTypes.get(e.eventType) <= typeVal);
        const source = this.elements.sourceSelect.val();
        if (source !== "ANY") {
            return typeFiltered.filter(e => e.eventSource === source);
        }
        return typeFiltered;
    }

    showLog() {
        this.elements.auditTextArea.empty();
        this.elements.auditRaw.addClass("hidden");
        this.elements.auditTableBody.empty();
        this.elements.auditTable.addClass("hidden");
        const entries = this.filterEntries(this.auditLog.entries);

        const format = this.elements.formatSelect.val();
        if (format === "TABLE") {
            this.elements.auditTable.removeClass("hidden");
            this.showLogTable(entries);
        } else {
            this.elements.auditRaw.removeClass("hidden");

            if (format === "JSON") {
                this.showLogJSON(entries);
            } else if (format === "PLAIN") {
                this.showLogPlain(entries);
            }
        }

        this.elements.auditItems.empty();
        this.elements.auditItems.text(entries.length);
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
        this.elements.typeSelect.on("change", () => {
            this.showLog();
        });
        this.elements.sourceSelect.on("change", () => {
            this.showLog();
        });
        this.elements.formatSelect.on("change", () => {
            this.showLog();
        });
        this.elements.refreshButton.click(() => {
            this.refreshLog();
        });
        this.elements.clearButton.click(() => {
            this.clearLog();
        });
    }

    // launcher
    static createAndLaunch() {
        const dialog = new MetadataAuditDialog();
        dialog.launch();
    }
}
