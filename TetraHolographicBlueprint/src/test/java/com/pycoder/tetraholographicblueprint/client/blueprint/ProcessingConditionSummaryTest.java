package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingConditionSummaryTest {
    @Test
    void keepsTheHighestMinimumLevelForEachToolAction() {
        ProcessingConditionSummary summary = new ProcessingConditionSummary();

        summary.addToolRequirement("hammer", 4);
        summary.addToolRequirement("hammer", 6);
        summary.addToolRequirement("cut", 3);

        assertEquals(6, summary.minimumToolLevels().get("hammer"));
        assertEquals(3, summary.minimumToolLevels().get("cut"));
    }

    @Test
    void deduplicatesStructuredConditionsWithoutInterpretingTheirKeys() {
        ProcessingConditionSummary summary = new ProcessingConditionSummary();

        summary.addCondition(ProcessingConditionKind.UNLOCK, "tetra:unknown_key", "原样显示的解锁条件");
        summary.addCondition(ProcessingConditionKind.UNLOCK, "tetra:unknown_key", "原样显示的解锁条件");
        summary.addCondition(ProcessingConditionKind.STRUCTURE, "minecraft:smithing", "锻造台附近");

        assertEquals(2, summary.conditions().size());
        assertEquals("tetra:unknown_key", summary.conditions().get(0).identity());
        assertEquals("原样显示的解锁条件", summary.conditions().get(0).displayText());
    }
}
