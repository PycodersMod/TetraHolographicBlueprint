package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Objects;

public record TetraReverseMappingCandidate(
        String schematicKey,
        String slotKey,
        String moduleKey,
        String variantKey,
        int canonicalOrder) {
    public TetraReverseMappingCandidate {
        Objects.requireNonNull(schematicKey, "schematicKey");
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(moduleKey, "moduleKey");
        Objects.requireNonNull(variantKey, "variantKey");
    }
}
