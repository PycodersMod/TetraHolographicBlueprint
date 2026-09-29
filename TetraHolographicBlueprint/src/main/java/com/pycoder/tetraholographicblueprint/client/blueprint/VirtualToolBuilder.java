package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.LinkedHashMap;
import java.util.List;

public final class VirtualToolBuilder {
    private VirtualToolBuilder() {
    }

    public static VirtualToolBuildPlan plan(List<SchematicPreviewSnapshot> previews) {
        var orderedPreviews = SchematicPreviewMapper.canonicalOrder(previews);
        var materials = new LinkedHashMap<MaterialKey, Integer>();
        for (var preview : orderedPreviews) {
            for (var material : preview.materials()) {
                materials.merge(material, 1, Integer::sum);
            }
        }
        return new VirtualToolBuildPlan(orderedPreviews, materials);
    }
}
