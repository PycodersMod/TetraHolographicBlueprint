package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TetraToolSnapshotTest {
    @Test
    void normalizesSlotOrderAndExposesImmutableValues() {
        var snapshot = new TetraToolSnapshot(List.of(
                new TetraSlotSnapshot("minor/second", "module_b", "variant_b", 2),
                new TetraSlotSnapshot("major/primary", "module_a", "variant_a", 1)));

        assertEquals(List.of("major/primary", "minor/second"),
                snapshot.slots().stream().map(TetraSlotSnapshot::slotKey).toList());
        assertEquals("module_a", snapshot.findSlot("major/primary").orElseThrow().moduleKey());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.slots().add(new TetraSlotSnapshot("x", "y", "z", 3)));
    }
}
