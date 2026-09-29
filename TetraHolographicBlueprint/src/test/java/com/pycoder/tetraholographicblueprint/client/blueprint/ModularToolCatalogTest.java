package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModularToolCatalogTest {
    @Test
    void refreshEntriesReplacesPreviousSnapshotAndFindsCurrentValue() {
        ModularToolCatalog.refreshEntries(List.of(
                entry("tetra:hammer", true, "", "major/0"),
                entry("tetra:axe", true, "", "major/0", "minor/0")));

        assertTrue(ModularToolCatalog.find(id("tetra:hammer")).isPresent());
        assertTrue(ModularToolCatalog.find(id("tetra:axe")).isPresent());

        ModularToolCatalog.refreshEntries(List.of(
                entry("tetra:hammer", false, "No occupied modules discovered"),
                entry("tetra:pickaxe", true, "", "major/0")));

        assertFalse(ModularToolCatalog.find(id("tetra:axe")).isPresent());
        assertEquals("No occupied modules discovered",
                ModularToolCatalog.find(id("tetra:hammer")).orElseThrow().failureReason());
        assertTrue(ModularToolCatalog.find(id("tetra:pickaxe")).isPresent());
    }

    @Test
    void refreshEntriesKeepsFailureEntriesAndLetsLaterDuplicateIdsWin() {
        ModularToolCatalog.refreshEntries(List.of(
                entry("tetra:slot_tool", true, "", "major/0"),
                entry("tetra:slot_tool", false, "broken slot"),
                entry("tetra:shield", true, "", "major/0", "minor/0")));

        var snapshot = ModularToolCatalog.snapshot();
        assertEquals(3, snapshot.size());

        ModularToolCatalog.CatalogEntry current = ModularToolCatalog.find(id("tetra:slot_tool")).orElseThrow();
        assertFalse(current.blueprintSupported());
        assertEquals("broken slot", current.failureReason());
        assertEquals(List.of(), current.slots());
    }

    @Test
    void clearRemovesAllEntries() {
        ModularToolCatalog.refreshEntries(List.of(
                entry("tetra:hammer", true, "", "major/0")));

        ModularToolCatalog.clear();

        assertTrue(ModularToolCatalog.snapshot().isEmpty());
        assertTrue(ModularToolCatalog.find(id("tetra:hammer")).isEmpty());
    }

    private static ModularToolCatalog.CatalogEntry entry(
            String id,
            boolean blueprintSupported,
            String failureReason,
            String... slots) {
        return new ModularToolCatalog.CatalogEntry(
                id(id),
                List.of(slots).stream().map(slot -> new TetraSlotSnapshot(slot, "", "", 0)).toList(),
                blueprintSupported,
                failureReason);
    }

    private static ResourceLocation id(String id) {
        return ResourceLocation.parse(id);
    }
}
