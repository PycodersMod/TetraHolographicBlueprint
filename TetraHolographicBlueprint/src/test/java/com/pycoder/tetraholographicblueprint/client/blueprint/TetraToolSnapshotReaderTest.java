package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TetraToolSnapshotReaderTest {
    @Test
    void convertsMajorAndMinorKeysToCanonicalSlots() {
        var snapshot = TetraToolSnapshotReader.fromModuleKeys(
                new String[]{"module_head", "module_handle"},
                new String[]{"module_socket"});

        assertEquals(3, snapshot.slots().size());
        assertEquals("major/0", snapshot.slots().get(0).slotKey());
        assertEquals("module_head", snapshot.slots().get(0).moduleKey());
        assertEquals("major/1", snapshot.slots().get(1).slotKey());
        assertEquals("minor/0", snapshot.slots().get(2).slotKey());
    }

    @Test
    void treatsNullModuleArraysAsEmpty() {
        assertEquals(0, TetraToolSnapshotReader.fromModuleKeys(null, null).slots().size());
    }
}
