package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkbenchBlueprintInteractionStateTest {
    @Test
    void inactiveStateOnlyAllowsModeToggle() {
        WorkbenchBlueprintInteractionState state = WorkbenchBlueprintInteractionState.of(false, false, false);

        assertTrue(state.isModeButtonClick(6, 4));
        assertTrue(state.isModeButtonClick(95, 23));
        assertFalse(state.isPreviousButtonClick(6, 28));
        assertFalse(state.isNextButtonClick(46, 28));
    }

    @Test
    void activeStateAllowsPagingOnlyWithinPanelBounds() {
        WorkbenchBlueprintInteractionState state = WorkbenchBlueprintInteractionState.of(true, true, true);

        assertTrue(state.isModeButtonClick(10, 10));
        assertTrue(state.isPreviousButtonClick(6, 28));
        assertTrue(state.isPreviousButtonClick(41, 47));
        assertTrue(state.isNextButtonClick(46, 28));
        assertTrue(state.isNextButtonClick(81, 47));
        assertFalse(state.isPreviousButtonClick(5, 28));
        assertFalse(state.isNextButtonClick(82, 47));
        assertFalse(state.isPreviousButtonClick(10, 27));
        assertFalse(state.isNextButtonClick(10, 48));
    }
}
