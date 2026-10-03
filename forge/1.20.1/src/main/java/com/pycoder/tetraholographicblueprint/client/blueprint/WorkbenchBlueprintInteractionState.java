package com.pycoder.tetraholographicblueprint.client.blueprint;

public final class WorkbenchBlueprintInteractionState {
    private final boolean modeActive;
    private final boolean showPagingControls;
    private final boolean hasSelectedSchematic;

    private WorkbenchBlueprintInteractionState(
            boolean modeActive,
            boolean showPagingControls,
            boolean hasSelectedSchematic) {
        this.modeActive = modeActive;
        this.showPagingControls = showPagingControls;
        this.hasSelectedSchematic = hasSelectedSchematic;
    }

    public static WorkbenchBlueprintInteractionState of(
            boolean modeActive,
            boolean showPagingControls,
            boolean hasSelectedSchematic) {
        return new WorkbenchBlueprintInteractionState(modeActive, showPagingControls, hasSelectedSchematic);
    }

    public boolean isModeButtonClick(double mouseX, double mouseY) {
        return mouseX >= 6 && mouseX < 96 && mouseY >= 4 && mouseY < 24;
    }

    public boolean isModeButtonClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    public boolean isPreviousButtonClick(double mouseX, double mouseY) {
        return modeActive && showPagingControls && hasSelectedSchematic
                && mouseX >= 6 && mouseX < 42 && mouseY >= 28 && mouseY < 48;
    }

    public boolean isNextButtonClick(double mouseX, double mouseY) {
        return modeActive && showPagingControls && hasSelectedSchematic
                && mouseX >= 46 && mouseX < 82 && mouseY >= 28 && mouseY < 48;
    }

    public boolean isPreviousPageClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return modeActive && showPagingControls && hasSelectedSchematic
                && mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    public boolean isNextPageClick(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return modeActive && showPagingControls && hasSelectedSchematic
                && mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    public boolean isEntryClick(double mouseX, double mouseY, int left, int top, int cellWidth, int cellHeight, int columns, int visibleCount) {
        if (!modeActive || !hasSelectedSchematic) {
            return false;
        }
        if (mouseX < left || mouseY < top) {
            return false;
        }
        int relativeX = (int) mouseX - left;
        int relativeY = (int) mouseY - top;
        int column = relativeX / cellWidth;
        int row = relativeY / cellHeight;
        if (column < 0 || column >= columns || row < 0 || row >= 5) {
            return false;
        }
        int index = row * columns + column;
        return index >= 0 && index < visibleCount
                && relativeX % cellWidth < cellWidth
                && relativeY % cellHeight < cellHeight;
    }
}
