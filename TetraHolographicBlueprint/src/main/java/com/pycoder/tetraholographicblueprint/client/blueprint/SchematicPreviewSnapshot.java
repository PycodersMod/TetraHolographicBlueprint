package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.List;
import java.util.Objects;

public record SchematicPreviewSnapshot(
        String schematicKey,
        String slotKey,
        String moduleKey,
        String variantKey,
        int canonicalOrder,
        List<MaterialKey> materials) {
    public SchematicPreviewSnapshot {
        Objects.requireNonNull(schematicKey, "schematicKey");
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(moduleKey, "moduleKey");
        Objects.requireNonNull(variantKey, "variantKey");
        Objects.requireNonNull(materials, "materials");
        materials = List.copyOf(materials);
    }
}
