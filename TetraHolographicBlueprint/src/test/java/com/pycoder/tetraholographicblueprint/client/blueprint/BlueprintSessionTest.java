package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintSessionTest {
    @Test
    void newSessionIsActiveAndCloseClearsTransientState() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.HOLOSPHERE_NEW_BUILD);

        assertTrue(session.isActive());
        assertTrue(session.selectionsBySlot().isEmpty());

        session.processingConditions().addToolRequirement("hammer", 4);
        session.processingConditions().addCondition(
                ProcessingConditionKind.STRUCTURE, "minecraft:smithing", "锻造台附近");

        session.close();

        assertFalse(session.isActive());
        assertTrue(session.selectionsBySlot().isEmpty());
        assertTrue(session.fullMaterialManifest().entries().isEmpty());
        assertTrue(session.trackingMaterialManifest().entries().isEmpty());
        assertTrue(session.processingConditions().minimumToolLevels().isEmpty());
        assertTrue(session.processingConditions().conditions().isEmpty());
    }

    @Test
    void selectionIsClampedAndEntriesAreCopied() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
        TetraSchematicPreviewEntry first = entry("first");
        TetraSchematicPreviewEntry second = entry("second");

        session.setSchematicEntries(List.of(first, second));
        assertEquals(2, session.schematicEntries().size());
        assertEquals("first", session.selectedSchematicEntry().orElseThrow().schematicKey());

        session.selectSchematicIndex(99);
        assertEquals("second", session.selectedSchematicEntry().orElseThrow().schematicKey());
        session.selectSchematicIndex(-1);
        assertEquals("first", session.selectedSchematicEntry().orElseThrow().schematicKey());
    }

    @Test
    void refreshingEntriesPreservesTheSelectedPreviewAndSupportsPaging() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
        TetraSchematicPreviewEntry first = entry("first");
        TetraSchematicPreviewEntry second = entry("second");
        TetraSchematicPreviewEntry refreshedFirst = entry("first");
        TetraSchematicPreviewEntry refreshedSecond = entry("second");

        session.setSchematicEntries(List.of(first, second));
        session.selectNextSchematic();
        assertEquals("second", session.selectedSchematicEntry().orElseThrow().schematicKey());

        session.setSchematicEntries(List.of(refreshedFirst, refreshedSecond));
        assertEquals("second", session.selectedSchematicEntry().orElseThrow().schematicKey());

        session.selectNextSchematic();
        assertEquals("second", session.selectedSchematicEntry().orElseThrow().schematicKey());
        session.selectPreviousSchematic();
        assertEquals("first", session.selectedSchematicEntry().orElseThrow().schematicKey());
    }

    private static TetraSchematicPreviewEntry entry(String key) {
        return new TetraSchematicPreviewEntry(
                key,
                "blade",
                "module",
                "variant",
                new MaterialManifest(),
                new ProcessingConditionSummary());
    }
}
