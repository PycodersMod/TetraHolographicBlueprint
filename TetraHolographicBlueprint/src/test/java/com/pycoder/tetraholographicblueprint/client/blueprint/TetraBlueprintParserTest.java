package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TetraBlueprintParserTest {
    @Test
    void resolvesSupportedBlueprintFromRankedCandidates() {
        BlueprintParseResult result = TetraReverseResolver.resolveFromCandidates(
                List.of(slot("major/0",
                        candidate("tetra:z", "major/0", "module_b", "variant_b", 2,
                                List.of()),
                        candidate("tetra:a", "major/0", "module_a", "variant_a", 1,
                                List.of()))),
                true);

        assertEquals(BlueprintParseResult.Status.SUPPORTED, result.status());
        assertEquals(1, result.selections().size());
        assertEquals("tetra:a", result.selections().get(0).schematicKey());
        assertEquals("major/0", result.selections().get(0).slotKey());
        assertEquals(List.of("tetra:a"), result.normalizedFingerprintInputs().stream()
                .map(TetraReverseMappingCandidate::schematicKey)
                .toList());
        assertThrows(UnsupportedOperationException.class, () ->
                result.selections().add(result.selections().get(0)));
    }

    @Test
    void resolvesUnsupportedWhenARequiredSlotHasNoCandidate() {
        BlueprintParseResult result = TetraReverseResolver.resolveFromCandidates(
                List.of(slot("major/0")), true);

        assertEquals(BlueprintParseResult.Status.UNSUPPORTED, result.status());
        assertTrue(result.failureReason().contains("major/0"));
        assertTrue(result.selections().isEmpty());
    }

    @Test
    void resolvesNotModularForNonModularTargets() {
        BlueprintParseResult result = TetraReverseResolver.resolveFromCandidates(
                List.of(slot("major/0",
                        candidate("tetra:a", "major/0", "module_a", "variant", 0,
                                List.of()))), false);

        assertEquals(BlueprintParseResult.Status.NOT_MODULAR, result.status());
        assertFalse(result.supported());
        assertTrue(result.selections().isEmpty());
    }

    @Test
    void resolveCompleteNullReturnsNotModular() {
        BlueprintParseResult result = TetraReverseResolver.resolveComplete(null);

        assertEquals(BlueprintParseResult.Status.NOT_MODULAR, result.status());
    }

    @Test
    void parseResultCopiesNestedCollections() {
        List<BlueprintPartSelection> selections = List.of(new BlueprintPartSelection(
                "major/0",
                "tetra:a",
                "module_a",
                "variant",
                List.of(),
                1));
        List<TetraReverseMappingCandidate> fingerprint = List.of(
                new TetraReverseMappingCandidate("tetra:a", "major/0", "module_a", "variant", 1));

        BlueprintParseResult result = new BlueprintParseResult(
                BlueprintParseResult.Status.SUPPORTED,
                selections,
                null,
                fingerprint);

        assertEquals("", result.failureReason());
        assertEquals(1, result.selections().size());
        assertEquals(1, result.normalizedFingerprintInputs().size());
        assertThrows(UnsupportedOperationException.class, () ->
                result.normalizedFingerprintInputs().add(fingerprint.get(0)));
    }

    private static TetraReverseResolver.SlotResolutionCandidate slot(
            String slotKey,
            TetraReverseResolver.ResolvedReverseCandidate... candidates) {
        return new TetraReverseResolver.SlotResolutionCandidate(slotKey, List.of(candidates));
    }

    private static TetraReverseResolver.ResolvedReverseCandidate candidate(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            int canonicalOrder,
            List<?> materials) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        List rawMaterials = (List) materials;
        return new TetraReverseResolver.ResolvedReverseCandidate(
                new TetraReverseMappingCandidate(
                        schematicKey,
                        slotKey,
                        moduleKey,
                        variantKey,
                        canonicalOrder),
                rawMaterials);
    }
}
