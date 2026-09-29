package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Objects;

public record TetraSlotSnapshot(String slotKey, String moduleKey, String variantKey, int ordinal) {
    public TetraSlotSnapshot {
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(moduleKey, "moduleKey");
        Objects.requireNonNull(variantKey, "variantKey");
    }
}
