package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Objects;

/**
 * 按乘法原理将蓝图总材料成本与玩家仍需收集的材料分开计算。
 */
public final class BlueprintMaterialCalculator {
    private BlueprintMaterialCalculator() {
    }

    public static MaterialTotals calculateFull(MaterialTotals fullMaterialTotals) {
        return copyOf(fullMaterialTotals);
    }

    public static MaterialTotals calculateTracking(
            BlueprintSourceMode sourceMode,
            MaterialTotals fullMaterialTotals,
            MaterialTotals alreadyPresentTotals) {
        Objects.requireNonNull(sourceMode, "sourceMode");
        Objects.requireNonNull(fullMaterialTotals, "fullMaterialTotals");
        Objects.requireNonNull(alreadyPresentTotals, "alreadyPresentTotals");

        if (sourceMode == BlueprintSourceMode.WORKBENCH_MODIFICATION) {
            return fullMaterialTotals.remainingAfter(alreadyPresentTotals);
        }
        return copyOf(fullMaterialTotals);
    }

    public static MaterialTotals trackingFor(
            BlueprintSourceMode sourceMode,
            MaterialTotals fullMaterialTotals,
            MaterialTotals alreadyPresentTotals) {
        return calculateTracking(sourceMode, fullMaterialTotals, alreadyPresentTotals);
    }

    public static MaterialManifest calculateFull(MaterialManifest fullMaterialManifest) {
        Objects.requireNonNull(fullMaterialManifest, "fullMaterialManifest");
        MaterialManifest copy = new MaterialManifest();
        for (MaterialManifest.MaterialEntry entry : fullMaterialManifest.entries()) {
            copy.add(entry.stack(), entry.quantity());
        }
        return copy;
    }

    public static MaterialManifest calculateTracking(
            BlueprintSourceMode sourceMode,
            MaterialManifest fullMaterialManifest,
            MaterialManifest alreadyPresentManifest) {
        Objects.requireNonNull(sourceMode, "sourceMode");
        Objects.requireNonNull(fullMaterialManifest, "fullMaterialManifest");
        Objects.requireNonNull(alreadyPresentManifest, "alreadyPresentManifest");

        if (sourceMode == BlueprintSourceMode.WORKBENCH_MODIFICATION) {
            return fullMaterialManifest.remainingAfter(alreadyPresentManifest);
        }
        return calculateFull(fullMaterialManifest);
    }

    public static MaterialManifest trackingFor(
            BlueprintSourceMode sourceMode,
            MaterialManifest fullMaterialManifest,
            MaterialManifest alreadyPresentManifest) {
        return calculateTracking(sourceMode, fullMaterialManifest, alreadyPresentManifest);
    }

    private static MaterialTotals copyOf(MaterialTotals source) {
        MaterialTotals copy = new MaterialTotals();
        for (var entry : source.entries().entrySet()) {
            copy.add(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
