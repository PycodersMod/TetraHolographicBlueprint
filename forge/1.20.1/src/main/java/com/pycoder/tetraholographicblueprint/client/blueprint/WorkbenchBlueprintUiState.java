package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.List;
import java.util.Objects;

public final class WorkbenchBlueprintUiState {
    private static final int PAGE_SIZE = 10;
    private final boolean modeActive;
    private final List<TetraSchematicPreviewEntry> schematicEntries;
    private final int selectedIndex;
    private final int pageIndex;
    private final int pageCount;
    private final List<TetraSchematicPreviewEntry> visibleEntries;

    private WorkbenchBlueprintUiState(
            boolean modeActive,
            List<TetraSchematicPreviewEntry> schematicEntries,
            int selectedIndex) {
        this.modeActive = modeActive;
        this.schematicEntries = List.copyOf(Objects.requireNonNull(schematicEntries, "schematicEntries"));
        this.selectedIndex = selectedIndex;
        this.pageCount = this.schematicEntries.isEmpty() ? 0 : ((this.schematicEntries.size() - 1) / PAGE_SIZE) + 1;
        this.pageIndex = this.schematicEntries.isEmpty()
                ? 0
                : Math.max(0, Math.min(selectedIndex < 0 ? 0 : selectedIndex / PAGE_SIZE, pageCount - 1));
        int startIndex = this.pageIndex * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, this.schematicEntries.size());
        this.visibleEntries = List.copyOf(this.schematicEntries.subList(startIndex, endIndex));
    }

    public static WorkbenchBlueprintUiState from(BlueprintSession session, boolean modeActive) {
        Objects.requireNonNull(session, "session");
        TetraSchematicPreviewEntry selected = session.selectedSchematicEntry().orElse(null);
        int selectedIndex = selected == null ? -1 : session.schematicEntries().indexOf(selected);
        return new WorkbenchBlueprintUiState(modeActive, session.schematicEntries(), selectedIndex);
    }

    public static WorkbenchBlueprintUiState empty(boolean modeActive) {
        return new WorkbenchBlueprintUiState(modeActive, List.of(), -1);
    }

    public String modeButtonLabel() {
        return modeActive ? "关闭蓝图模式" : "打开蓝图模式";
    }

    public boolean showPagingControls() {
        return modeActive && schematicEntries.size() > 1;
    }

    public boolean canSelectPrevious() {
        return showPagingControls() && selectedIndex > 0;
    }

    public boolean canSelectNext() {
        return showPagingControls() && selectedIndex >= 0 && selectedIndex < schematicEntries.size() - 1;
    }

    public boolean showPageNavigation() {
        return modeActive && pageCount > 1;
    }

    public boolean canSelectPreviousPage() {
        return showPageNavigation() && pageIndex > 0;
    }

    public boolean canSelectNextPage() {
        return showPageNavigation() && pageIndex < pageCount - 1;
    }

    public int pageIndex() {
        return pageIndex;
    }

    public int pageCount() {
        return pageCount;
    }

    public String pageLabel() {
        if (pageCount == 0) {
            return "第 0/0 页";
        }
        return "第 " + (pageIndex + 1) + "/" + pageCount + " 页";
    }

    public List<TetraSchematicPreviewEntry> visibleEntries() {
        return visibleEntries;
    }
}
