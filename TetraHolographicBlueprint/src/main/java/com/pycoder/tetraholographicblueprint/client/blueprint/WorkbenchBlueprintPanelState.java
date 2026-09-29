package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class WorkbenchBlueprintPanelState {
    private static final int MAX_CONDITION_LINES = 4;

    private final boolean visible;
    private final String previewHeader;
    private final String selectedPreviewLine;
    private final String statusLine;
    private final String pageLabel;
    private final List<PreviewEntryView> pageEntries;
    private final List<String> materialLines;
    private final List<String> conditionLines;

    private WorkbenchBlueprintPanelState(
            boolean visible,
            String previewHeader,
            String selectedPreviewLine,
            String statusLine,
            String pageLabel,
            List<PreviewEntryView> pageEntries,
            List<String> materialLines,
            List<String> conditionLines) {
        this.visible = visible;
        this.previewHeader = Objects.requireNonNull(previewHeader, "previewHeader");
        this.selectedPreviewLine = selectedPreviewLine;
        this.statusLine = statusLine;
        this.pageLabel = Objects.requireNonNull(pageLabel, "pageLabel");
        this.pageEntries = List.copyOf(Objects.requireNonNull(pageEntries, "pageEntries"));
        this.materialLines = List.copyOf(Objects.requireNonNull(materialLines, "materialLines"));
        this.conditionLines = List.copyOf(Objects.requireNonNull(conditionLines, "conditionLines"));
    }

    public static WorkbenchBlueprintPanelState from(BlueprintSession session, boolean modeActive) {
        Objects.requireNonNull(session, "session");
        if (!modeActive) {
            return hidden();
        }

        WorkbenchBlueprintUiState uiState = WorkbenchBlueprintUiState.from(session, true);
        List<TetraSchematicPreviewEntry> entries = session.schematicEntries();
        TetraSchematicPreviewEntry selected = session.selectedSchematicEntry().orElse(null);
        String previewHeader = selected == null
                ? "蓝图预览: 0/" + entries.size()
                : "蓝图预览: " + (entries.indexOf(selected) + 1) + "/" + entries.size();
        String selectedPreviewLine = selected == null ? null : selected.displayName() + " [" + selected.slotKey() + "]";
        String statusLine = entries.isEmpty() ? "未找到可预览蓝图" : null;
        List<PreviewEntryView> pageEntries = new ArrayList<>();
        int startIndex = uiState.pageIndex() * uiState.visibleEntries().size();
        int globalIndex = uiState.pageIndex() * 10;
        for (TetraSchematicPreviewEntry entry : uiState.visibleEntries()) {
            pageEntries.add(new PreviewEntryView(
                    globalIndex++,
                    entry.displayName(),
                    entry.slotKey(),
                    selected != null && samePreview(selected, entry)));
        }

        List<String> materialLines = new ArrayList<>();
        for (MaterialManifest.MaterialEntry entry : session.fullMaterialManifest().entries()) {
            materialLines.add(entry.key().itemId() + " x" + entry.quantity());
        }

        List<String> conditionLines = new ArrayList<>();
        ProcessingConditionSummary summary = session.processingConditions();
        for (var toolRequirement : summary.minimumToolLevels().entrySet()) {
            if (conditionLines.size() >= MAX_CONDITION_LINES) {
                break;
            }
            conditionLines.add("Needs " + toolRequirement.getKey() + " " + toolRequirement.getValue());
        }
        for (ProcessingCondition condition : summary.conditions()) {
            if (conditionLines.size() >= MAX_CONDITION_LINES) {
                break;
            }
            conditionLines.add(condition.displayText());
        }

        return new WorkbenchBlueprintPanelState(
                true,
                previewHeader,
                selectedPreviewLine,
                statusLine,
                uiState.pageLabel(),
                pageEntries,
                materialLines,
                conditionLines);
    }

    public static WorkbenchBlueprintPanelState hidden() {
        return new WorkbenchBlueprintPanelState(false, "", null, null, "第 0/0 页", List.of(), List.of(), List.of());
    }

    public boolean visible() {
        return visible;
    }

    public String previewHeader() {
        return previewHeader;
    }

    public Optional<String> selectedPreviewLine() {
        return Optional.ofNullable(selectedPreviewLine);
    }

    public Optional<String> statusLine() {
        return Optional.ofNullable(statusLine);
    }

    public String pageLabel() {
        return pageLabel;
    }

    public List<PreviewEntryView> pageEntries() {
        return pageEntries;
    }

    public List<String> materialLines() {
        return materialLines;
    }

    public List<String> conditionLines() {
        return conditionLines;
    }

    public record PreviewEntryView(
            int globalIndex,
            String displayName,
            String slotKey,
            boolean selected) {
        public PreviewEntryView {
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(slotKey, "slotKey");
        }

        public String label() {
            return displayName + " [" + slotKey + "]";
        }
    }

    private static boolean samePreview(TetraSchematicPreviewEntry first, TetraSchematicPreviewEntry second) {
        return first.schematicKey().equals(second.schematicKey())
                && first.slotKey().equals(second.slotKey())
                && first.moduleKey().equals(second.moduleKey())
                && first.variantKey().equals(second.variantKey());
    }
}
