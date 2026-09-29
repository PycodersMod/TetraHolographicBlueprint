package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkbenchBlueprintPanelStateTest {
    @Test
    void hiddenPanelDoesNotExposeLines() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
        session.fullMaterialManifest().add(new MaterialKey("minecraft:stick", ""), 2);
        session.processingConditions().addToolRequirement("hammer", 4);

        WorkbenchBlueprintPanelState panel = WorkbenchBlueprintPanelState.from(session, false);

        assertFalse(panel.visible());
        assertTrue(panel.materialLines().isEmpty());
        assertTrue(panel.conditionLines().isEmpty());
    }

    @Test
    void visibleEmptyPanelShowsNoPreviewMessage() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);

        WorkbenchBlueprintPanelState panel = WorkbenchBlueprintPanelState.from(session, true);

        assertTrue(panel.visible());
        assertEquals("未找到可预览蓝图", panel.statusLine().orElseThrow());
        assertEquals("蓝图预览: 0/0", panel.previewHeader());
        assertTrue(panel.selectedPreviewLine().isEmpty());
    }

    @Test
    void visiblePanelShowsPreviewHeaderMaterialsAndBindsConditionCount() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
        session.setSchematicEntries(List.of(entry("first")));
        session.selectSchematicIndex(0);
        session.fullMaterialManifest().add(new MaterialKey("minecraft:stick", ""), 2);
        session.fullMaterialManifest().add(new MaterialKey("minecraft:string", ""), 3);
        session.processingConditions().addToolRequirement("hammer", 4);
        session.processingConditions().addCondition(
                ProcessingConditionKind.SCROLL, "tetra:scroll", "需要卷轴");
        session.processingConditions().addCondition(
                ProcessingConditionKind.SCHEMATIC, "tetra:schematic", "需要图纸");
        session.processingConditions().addCondition(
                ProcessingConditionKind.STRUCTURE, "minecraft:smithing", "锻造台附近");
        session.processingConditions().addCondition(
                ProcessingConditionKind.BLOCK, "minecraft:anvil", "铁砧附近");
        session.processingConditions().addCondition(
                ProcessingConditionKind.OTHER, "addon:extra", "额外条件");

        WorkbenchBlueprintPanelState panel = WorkbenchBlueprintPanelState.from(session, true);

        assertTrue(panel.visible());
        assertEquals("蓝图预览: 1/1", panel.previewHeader());
        assertEquals("first [blade]", panel.selectedPreviewLine().orElseThrow());
        assertEquals(List.of("minecraft:stick x2", "minecraft:string x3"), panel.materialLines());
        assertEquals(4, panel.conditionLines().size());
        assertEquals("Needs hammer 4", panel.conditionLines().get(0));
        assertEquals("需要卷轴", panel.conditionLines().get(1));
    }

    private static TetraSchematicPreviewEntry entry(String key) {
        return new TetraSchematicPreviewEntry(
                key,
                "blade",
                "module",
                "variant",
                new MaterialManifest(),
                new ProcessingConditionSummary());
    }
}
