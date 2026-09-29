package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolSchematicCatalogTest {
    @Test
    void resolveCandidatesFiltersUnpreviewableEntriesAndSortsByKey() {
        var resolved = ToolSchematicCatalog.resolveCandidates(List.of(
                candidate("tetra:hammer", "major/0", "module_hammer_head", "", false,
                        material("minecraft:iron_ingot", "", 2), summary("ignore")),
                candidate("tetra:axe", "major/0", "module_axe_head", "", true,
                        material("minecraft:stick", "", 1), summary("axe")),
                candidate("tetra:axe", "minor/0", "module_handle", "", true,
                        material("minecraft:planks", "", 3), summary("handle")),
                candidate("tetra:bow", "minor/0", "module_string", "", true,
                        material("minecraft:string", "", 2), summary("string"))));

        assertEquals(List.of("tetra:axe", "tetra:axe", "tetra:bow"),
                resolved.stream().map(ToolSchematicCatalog.ResolvedPreview::schematicKey).toList());
        assertEquals(List.of("major/0", "minor/0", "minor/0"),
                resolved.stream().map(ToolSchematicCatalog.ResolvedPreview::slotKey).toList());
    }

    @Test
    void resolveCandidatesKeepsSlotMaterialsSeparatedAndPreservesQuantities() {
        var resolved = ToolSchematicCatalog.resolveCandidates(List.of(
                candidate("tetra:bow", "string", "module_string", "", true,
                        material("minecraft:string", "", 2),
                        material("minecraft:slime_ball", "", 1),
                        summary("string")),
                candidate("tetra:bow", "limb", "module_limb", "", true,
                        material("minecraft:planks", "", 4),
                        summary("limb"))));

        assertEquals(2, resolved.size());
        assertEquals("limb", resolved.get(0).slotKey());
        assertEquals(List.of(4),
                resolved.get(0).materials().stream().map(ToolSchematicCatalog.ResolvedMaterial::quantity).toList());
        assertEquals("string", resolved.get(1).slotKey());
        assertEquals(List.of(2, 1),
                resolved.get(1).materials().stream().map(ToolSchematicCatalog.ResolvedMaterial::quantity).toList());
    }

    @Test
    void resolveCandidatesCopiesNestedStateAndRejectsMutableAliasChanges() {
        ProcessingConditionSummary originalSummary = summary("forge");
        List<ToolSchematicCatalog.ResolvedMaterial> originalMaterials = new ArrayList<>(List.of(
                material("minecraft:iron_ingot", "", 3),
                material("minecraft:stick", "", 1)));

        var resolved = ToolSchematicCatalog.resolveCandidates(List.of(
                new ToolSchematicCatalog.PreviewCandidate(
                        "tetra:tool",
                        "major/0",
                        "module_head",
                        "",
                        true,
                        originalMaterials,
                        originalSummary)));

        originalSummary.addToolRequirement("axe", 9);
        originalMaterials.add(material("minecraft:planks", "", 8));

        assertEquals(1, resolved.size());
        assertEquals(1, resolved.get(0).processingConditions().conditions().size());
        assertEquals(1, resolved.get(0).processingConditions().minimumToolLevels().size());
        assertEquals(List.of(3, 1),
                resolved.get(0).materials().stream().map(ToolSchematicCatalog.ResolvedMaterial::quantity).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> resolved.get(0).materials().add(material("minecraft:coal", "", 1)));
        assertTrue(resolved.get(0).processingConditions().minimumToolLevels().containsKey("forge"));
    }

    private static ToolSchematicCatalog.PreviewCandidate candidate(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            boolean previewable,
            ToolSchematicCatalog.ResolvedMaterial firstMaterial,
            ProcessingConditionSummary conditions) {
        return candidate(schematicKey, slotKey, moduleKey, variantKey, previewable, List.of(firstMaterial), conditions);
    }

    private static ToolSchematicCatalog.PreviewCandidate candidate(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            boolean previewable,
            ToolSchematicCatalog.ResolvedMaterial firstMaterial,
            ToolSchematicCatalog.ResolvedMaterial secondMaterial,
            ProcessingConditionSummary conditions) {
        return candidate(schematicKey, slotKey, moduleKey, variantKey, previewable,
                List.of(firstMaterial, secondMaterial), conditions);
    }

    private static ToolSchematicCatalog.PreviewCandidate candidate(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            boolean previewable,
            List<ToolSchematicCatalog.ResolvedMaterial> materials,
            ProcessingConditionSummary conditions) {
        return new ToolSchematicCatalog.PreviewCandidate(
                schematicKey,
                slotKey,
                moduleKey,
                variantKey,
                previewable,
                materials,
                conditions);
    }

    private static ToolSchematicCatalog.ResolvedMaterial material(String itemId, String tag, int quantity) {
        return new ToolSchematicCatalog.ResolvedMaterial(itemId, tag, quantity);
    }

    private static ProcessingConditionSummary summary(String label) {
        ProcessingConditionSummary summary = new ProcessingConditionSummary();
        summary.addToolRequirement(label, 2);
        summary.addCondition(ProcessingConditionKind.OTHER, "cond:" + label, "needs " + label);
        return summary;
    }
}
