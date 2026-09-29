package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.List;
import java.util.Objects;

public record BlueprintParseResult(
        Status status,
        List<BlueprintPartSelection> selections,
        String failureReason,
        List<TetraReverseMappingCandidate> normalizedFingerprintInputs) {
    public BlueprintParseResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(selections, "selections");
        Objects.requireNonNull(normalizedFingerprintInputs, "normalizedFingerprintInputs");
        selections = List.copyOf(selections);
        normalizedFingerprintInputs = List.copyOf(normalizedFingerprintInputs);
        failureReason = failureReason == null ? "" : failureReason;
    }

    public static BlueprintParseResult supported(
            List<BlueprintPartSelection> selections,
            List<TetraReverseMappingCandidate> normalizedFingerprintInputs) {
        return new BlueprintParseResult(
                Status.SUPPORTED,
                selections,
                "",
                normalizedFingerprintInputs);
    }

    public static BlueprintParseResult unsupported(
            String failureReason,
            List<TetraReverseMappingCandidate> normalizedFingerprintInputs) {
        return new BlueprintParseResult(
                Status.UNSUPPORTED,
                List.of(),
                failureReason,
                normalizedFingerprintInputs);
    }

    public static BlueprintParseResult notModular(String failureReason) {
        return new BlueprintParseResult(
                Status.NOT_MODULAR,
                List.of(),
                failureReason,
                List.of());
    }

    public boolean supported() {
        return status == Status.SUPPORTED;
    }

    public enum Status {
        SUPPORTED,
        UNSUPPORTED,
        NOT_MODULAR
    }
}
