package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TetraReverseResolverTest {
    @Test
    void ranksCandidatesDeterministically() {
        var candidates = List.of(
                new TetraReverseMappingCandidate("tetra:z", "major/0", "module", "variant_b", 1),
                new TetraReverseMappingCandidate("tetra:a", "major/0", "module", "variant_a", 1),
                new TetraReverseMappingCandidate("tetra:first", "minor/0", "module", "variant", 2));

        var ranked = TetraReverseResolver.rankCandidates(candidates);

        assertEquals("tetra:a", ranked.get(0).schematicKey());
        assertEquals("tetra:z", ranked.get(1).schematicKey());
        assertEquals("minor/0", ranked.get(2).slotKey());
        assertTrue(TetraReverseResolver.rankCandidates(List.of()).isEmpty());
    }
}
