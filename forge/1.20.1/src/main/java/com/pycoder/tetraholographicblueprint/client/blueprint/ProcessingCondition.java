package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Objects;

public record ProcessingCondition(
        ProcessingConditionKind kind,
        String identity,
        String displayText) {
    public ProcessingCondition {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(displayText, "displayText");
    }
}
