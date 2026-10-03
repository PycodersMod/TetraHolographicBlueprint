package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialTotalsTest {
    @Test
    void mergesEqualMaterialKeysAndPreservesIndependentQuantities() {
        MaterialKey stick = new MaterialKey("minecraft:stick", "");
        MaterialTotals totals = new MaterialTotals();

        totals.add(stick, 2);
        totals.add(stick, 3);

        assertEquals(1, totals.entries().size());
        assertEquals(5, totals.entries().get(stick));
    }

    @Test
    void remainingAfterSubtractsSatisfiedQuantitiesWithoutGoingNegative() {
        MaterialKey stick = new MaterialKey("minecraft:stick", "");
        MaterialKey iron = new MaterialKey("minecraft:iron_ingot", "");
        MaterialTotals required = new MaterialTotals();
        required.add(stick, 5);
        required.add(iron, 2);

        MaterialTotals satisfied = new MaterialTotals();
        satisfied.add(stick, 2);
        satisfied.add(iron, 4);

        MaterialTotals remaining = required.remainingAfter(satisfied);

        assertEquals(3, remaining.entries().get(stick));
        assertEquals(0, remaining.entries().getOrDefault(iron, 0));
        assertEquals(3, remaining.total());
        assertEquals(7, required.total());
    }
}
