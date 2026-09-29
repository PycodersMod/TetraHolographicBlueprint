package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TetraProcessingConditionReaderTest {

    @Test
    void nullRequirementDescriptionsDoNotAddAnything() {
        ProcessingConditionSummary summary = new ProcessingConditionSummary();

        TetraProcessingConditionReader.appendRequirementDescriptions(summary, null);

        assertEquals(0, summary.minimumToolLevels().size());
        assertEquals(0, summary.conditions().size());
    }
}
