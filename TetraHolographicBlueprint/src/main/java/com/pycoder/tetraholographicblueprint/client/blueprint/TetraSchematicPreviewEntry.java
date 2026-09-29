package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Objects;

public record TetraSchematicPreviewEntry(
        String displayName,
        String schematicKey,
        String slotKey,
        String moduleKey,
        String variantKey,
        MaterialManifest fullMaterialManifest,
        ProcessingConditionSummary processingConditions) {
    public TetraSchematicPreviewEntry {
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(schematicKey, "schematicKey");
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(moduleKey, "moduleKey");
        Objects.requireNonNull(variantKey, "variantKey");
        Objects.requireNonNull(fullMaterialManifest, "fullMaterialManifest");
        Objects.requireNonNull(processingConditions, "processingConditions");
        fullMaterialManifest = fullMaterialManifest.copy();
        processingConditions = processingConditions.copy();
    }

    public TetraSchematicPreviewEntry(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            MaterialManifest fullMaterialManifest,
            ProcessingConditionSummary processingConditions) {
        this(schematicKey, schematicKey, slotKey, moduleKey, variantKey, fullMaterialManifest, processingConditions);
    }
}
