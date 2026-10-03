package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record VirtualToolBuildPlan(
        List<SchematicPreviewSnapshot> previews,
        Map<MaterialKey, Integer> materials) {
    public VirtualToolBuildPlan {
        Objects.requireNonNull(previews, "previews");
        Objects.requireNonNull(materials, "materials");
        previews = List.copyOf(previews);
        materials = Map.copyOf(materials);
    }
}
