package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkbenchBlueprintUiStateTest {
    @Test
    void modeButtonLabelMatchesActivationState() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);

        WorkbenchBlueprintUiState inactive = WorkbenchBlueprintUiState.from(session, false);
        assertEquals("打开蓝图模式", inactive.modeButtonLabel());

        session.setSchematicEntries(List.of(entry("first")));
        WorkbenchBlueprintUiState active = WorkbenchBlueprintUiState.from(session, true);
        assertEquals("关闭蓝图模式", active.modeButtonLabel());
    }

    @Test
    void pagingControlsRequireAnActiveModeAndMultipleEntries() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
        session.setSchematicEntries(List.of(entry("first")));

        assertFalse(WorkbenchBlueprintUiState.from(session, false).showPagingControls());
        assertFalse(WorkbenchBlueprintUiState.from(session, true).showPagingControls());

        session.setSchematicEntries(List.of(entry("first"), entry("second")));
        assertTrue(WorkbenchBlueprintUiState.from(session, true).showPagingControls());
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
