package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SchematicPreviewMapper {
    private static final Comparator<SchematicPreviewSnapshot> CANONICAL_ORDER =
            Comparator.comparingInt(SchematicPreviewSnapshot::canonicalOrder)
                    .thenComparing(SchematicPreviewSnapshot::schematicKey)
                    .thenComparing(SchematicPreviewSnapshot::slotKey)
                    .thenComparing(SchematicPreviewSnapshot::moduleKey)
                    .thenComparing(SchematicPreviewSnapshot::variantKey);

    private SchematicPreviewMapper() {
    }

    public static SchematicPreviewSnapshot fromFields(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            int canonicalOrder,
            List<MaterialKey> materials) {
        return new SchematicPreviewSnapshot(
                schematicKey, slotKey, moduleKey, variantKey, canonicalOrder, materials);
    }

    public static SchematicPreviewSnapshot fromOutcome(
            String schematicKey,
            String slotKey,
            int canonicalOrder,
            OutcomePreview preview) {
        var materials = new ArrayList<MaterialKey>();
        if (preview.materials != null) {
            for (ItemStack material : preview.materials) {
                if (material != null && !material.isEmpty()) {
                    materials.add(MaterialKey.from(material));
                }
            }
        }
        return fromFields(
                schematicKey,
                slotKey,
                preview.moduleKey == null ? "" : preview.moduleKey,
                preview.variantKey == null ? "" : preview.variantKey,
                canonicalOrder,
                materials);
    }

    public static List<SchematicPreviewSnapshot> canonicalOrder(
            List<SchematicPreviewSnapshot> previews) {
        return previews.stream().sorted(CANONICAL_ORDER).toList();
    }
}
