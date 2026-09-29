package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchematicPreviewSnapshotTest {
    @Test
    void keepsPreviewMaterialsIndependentAndValuesImmutable() {
        var first = SchematicPreviewMapper.fromFields(
                "tetra:socket", "major/0", "module_head", "variant_a", 2,
                List.of(new MaterialKey("minecraft:iron_ingot", "{}")));
        var second = SchematicPreviewMapper.fromFields(
                "tetra:socket", "major/0", "module_head", "variant_b", 1,
                List.of(new MaterialKey("minecraft:diamond", "{}")));

        var ordered = SchematicPreviewMapper.canonicalOrder(List.of(first, second));

        assertEquals("variant_b", ordered.get(0).variantKey());
        assertEquals("minecraft:iron_ingot", ordered.get(1).materials().get(0).itemId());
        assertThrows(UnsupportedOperationException.class,
                () -> ordered.get(0).materials().add(new MaterialKey("minecraft:stick", "{}")));
    }
}
