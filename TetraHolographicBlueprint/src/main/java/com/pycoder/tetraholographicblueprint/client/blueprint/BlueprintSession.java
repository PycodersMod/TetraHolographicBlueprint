package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BlueprintSession {
    private static final int SCHEMATIC_PAGE_SIZE = 10;
    private final BlueprintSourceMode sourceMode;
    private final ItemStack originalStack;
    private final ItemStack committedStack;
    private ItemStack hoverPreviewStack;
    private final Map<String, BlueprintPartSelection> selectionsBySlot = new LinkedHashMap<>();
    private final List<TetraSchematicPreviewEntry> schematicEntries = new ArrayList<>();
    private int selectedSchematicIndex = -1;
    private BlueprintFingerprint blueprintFingerprint;
    private final MaterialManifest fullMaterialManifest = new MaterialManifest();
    private final MaterialManifest trackingMaterialManifest = new MaterialManifest();
    private final ProcessingConditionSummary processingConditions = new ProcessingConditionSummary();
    private boolean active = true;

    private BlueprintSession(BlueprintSourceMode sourceMode) {
        this.sourceMode = sourceMode;
        this.originalStack = null;
        this.committedStack = null;
    }

    public static BlueprintSession empty(BlueprintSourceMode sourceMode) {
        return new BlueprintSession(sourceMode);
    }

    public BlueprintSourceMode sourceMode() {
        return sourceMode;
    }

    public boolean isActive() {
        return active;
    }

    public ItemStack originalStack() {
        return originalStack == null ? null : originalStack.copy();
    }

    public ItemStack committedStack() {
        return committedStack == null ? null : committedStack.copy();
    }

    public ItemStack hoverPreviewStack() {
        return hoverPreviewStack == null ? ItemStack.EMPTY : hoverPreviewStack.copy();
    }

    public Map<String, BlueprintPartSelection> selectionsBySlot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(selectionsBySlot));
    }

    public Optional<BlueprintFingerprint> blueprintFingerprint() {
        return Optional.ofNullable(blueprintFingerprint);
    }

    public void setBlueprintFingerprint(BlueprintFingerprint fingerprint) {
        this.blueprintFingerprint = fingerprint;
    }

    public void setSchematicEntries(List<TetraSchematicPreviewEntry> entries) {
        TetraSchematicPreviewEntry previousSelection = selectedSchematicEntry().orElse(null);
        schematicEntries.clear();
        if (entries != null) {
            schematicEntries.addAll(entries);
        }
        selectedSchematicIndex = -1;
        if (previousSelection != null) {
            for (int index = 0; index < schematicEntries.size(); index++) {
                if (samePreview(previousSelection, schematicEntries.get(index))) {
                    selectedSchematicIndex = index;
                    break;
                }
            }
        }
        if (selectedSchematicIndex < 0 && !schematicEntries.isEmpty()) {
            selectedSchematicIndex = 0;
        }
    }

    public List<TetraSchematicPreviewEntry> schematicEntries() {
        return Collections.unmodifiableList(new ArrayList<>(schematicEntries));
    }

    public Optional<TetraSchematicPreviewEntry> selectedSchematicEntry() {
        if (selectedSchematicIndex < 0 || selectedSchematicIndex >= schematicEntries.size()) {
            return Optional.empty();
        }
        return Optional.of(schematicEntries.get(selectedSchematicIndex));
    }

    public void selectSchematicIndex(int index) {
        if (schematicEntries.isEmpty()) {
            selectedSchematicIndex = -1;
            return;
        }
        selectedSchematicIndex = Math.max(0, Math.min(index, schematicEntries.size() - 1));
    }

    public void selectNextSchematic() {
        if (!schematicEntries.isEmpty()) {
            selectSchematicIndex(selectedSchematicIndex + 1);
        }
    }

    public void selectPreviousSchematic() {
        if (!schematicEntries.isEmpty()) {
            selectSchematicIndex(selectedSchematicIndex - 1);
        }
    }

    public void selectNextSchematicPage() {
        if (!schematicEntries.isEmpty()) {
            selectSchematicPage(schematicPageIndex() + 1);
        }
    }

    public void selectPreviousSchematicPage() {
        if (!schematicEntries.isEmpty()) {
            selectSchematicPage(schematicPageIndex() - 1);
        }
    }

    public void selectSchematicPage(int pageIndex) {
        if (schematicEntries.isEmpty()) {
            selectedSchematicIndex = -1;
            return;
        }
        int clampedPage = Math.max(0, Math.min(pageIndex, schematicPageCount() - 1));
        selectedSchematicIndex = Math.min(clampedPage * SCHEMATIC_PAGE_SIZE, schematicEntries.size() - 1);
    }

    public int schematicPageCount() {
        if (schematicEntries.isEmpty()) {
            return 0;
        }
        return ((schematicEntries.size() - 1) / SCHEMATIC_PAGE_SIZE) + 1;
    }

    public int schematicPageIndex() {
        if (selectedSchematicIndex < 0 || schematicEntries.isEmpty()) {
            return 0;
        }
        return Math.min(selectedSchematicIndex / SCHEMATIC_PAGE_SIZE, schematicPageCount() - 1);
    }

    public int schematicPageStartIndex() {
        return schematicPageIndex() * SCHEMATIC_PAGE_SIZE;
    }

    public int schematicPageSize() {
        return SCHEMATIC_PAGE_SIZE;
    }

    private static boolean samePreview(TetraSchematicPreviewEntry first, TetraSchematicPreviewEntry second) {
        return first.schematicKey().equals(second.schematicKey())
                && first.slotKey().equals(second.slotKey())
                && first.moduleKey().equals(second.moduleKey())
                && first.variantKey().equals(second.variantKey());
    }

    public MaterialManifest fullMaterialManifest() {
        return fullMaterialManifest;
    }

    public MaterialManifest trackingMaterialManifest() {
        return trackingMaterialManifest;
    }

    public ProcessingConditionSummary processingConditions() {
        return processingConditions;
    }

    public void close() {
        active = false;
        hoverPreviewStack = null;
        selectionsBySlot.clear();
        schematicEntries.clear();
        selectedSchematicIndex = -1;
        blueprintFingerprint = null;
        fullMaterialManifest.clear();
        trackingMaterialManifest.clear();
        processingConditions.clear();
    }
}
