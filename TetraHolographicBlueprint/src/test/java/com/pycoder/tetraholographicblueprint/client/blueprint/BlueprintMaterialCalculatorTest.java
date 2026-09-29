package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlueprintMaterialCalculatorTest {
    @Test
    void nonWorkbenchSourcesTrackTheFullFromZeroManifest() {
        MaterialKey iron = new MaterialKey("minecraft:iron_ingot", "");
        MaterialTotals full = totals(iron, 6);
        MaterialTotals alreadyPresent = totals(iron, 4);

        MaterialTotals tracking = BlueprintMaterialCalculator.calculateTracking(
                BlueprintSourceMode.HOLOSPHERE_NEW_BUILD,
                full,
                alreadyPresent);

        assertEquals(6, tracking.entries().get(iron));
        assertEquals(6, full.entries().get(iron));
    }

    @Test
    void chatImportAlsoTracksTheFullFromZeroManifest() {
        MaterialKey iron = new MaterialKey("minecraft:iron_ingot", "");
        MaterialTotals full = totals(iron, 3);
        MaterialTotals alreadyPresent = totals(iron, 1);

        MaterialTotals tracking = BlueprintMaterialCalculator.calculateTracking(
                BlueprintSourceMode.CHAT_IMPORT,
                full,
                alreadyPresent);

        assertEquals(3, tracking.entries().get(iron));
        assertEquals(3, tracking.total());
    }

    @Test
    void workbenchSourceTracksOnlyNewMaterials() {
        MaterialKey iron = new MaterialKey("minecraft:iron_ingot", "");
        MaterialKey wood = new MaterialKey("minecraft:oak_planks", "");
        MaterialTotals full = new MaterialTotals();
        full.add(iron, 6);
        full.add(wood, 2);
        MaterialTotals alreadyPresent = new MaterialTotals();
        alreadyPresent.add(iron, 4);
        alreadyPresent.add(wood, 5);

        MaterialTotals fullCopy = BlueprintMaterialCalculator.calculateFull(full);
        MaterialTotals tracking = BlueprintMaterialCalculator.calculateTracking(
                BlueprintSourceMode.WORKBENCH_MODIFICATION,
                full,
                alreadyPresent);

        assertEquals(2, tracking.entries().get(iron));
        assertEquals(0, tracking.entries().getOrDefault(wood, 0));
        assertEquals(2, tracking.total());
        assertEquals(8, full.total());
        assertEquals(8, fullCopy.total());
        assertEquals(8, fullCopy.entries().get(iron) + fullCopy.entries().get(wood));
    }

    private static MaterialTotals totals(MaterialKey key, int quantity) {
        MaterialTotals totals = new MaterialTotals();
        totals.add(key, quantity);
        return totals;
    }
}
