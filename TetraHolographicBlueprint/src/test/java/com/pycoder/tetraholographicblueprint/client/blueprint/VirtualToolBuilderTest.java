package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VirtualToolBuilderTest {
    @Test
    void createsCanonicalPlanAndAggregatesMaterialsWithoutMergingPreviews() {
        var iron = new MaterialKey("minecraft:iron_ingot", "{}");
        var first = SchematicPreviewMapper.fromFields(
                "tetra:second", "minor/0", "module_b", "variant_b", 2, List.of(iron));
        var second = SchematicPreviewMapper.fromFields(
                "tetra:first", "major/0", "module_a", "variant_a", 1, List.of(iron));

        var plan = VirtualToolBuilder.plan(List.of(first, second));

        assertEquals(List.of("major/0", "minor/0"),
                plan.previews().stream().map(SchematicPreviewSnapshot::slotKey).toList());
        assertEquals(2, plan.materials().get(iron));
        assertThrows(UnsupportedOperationException.class,
                () -> plan.previews().add(first));
        assertThrows(UnsupportedOperationException.class,
                () -> plan.materials().put(iron, 9));
    }
}
