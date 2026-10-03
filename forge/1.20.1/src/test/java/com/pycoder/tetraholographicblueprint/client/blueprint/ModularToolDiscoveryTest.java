package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModularToolDiscoveryTest {
    @Test
    void scanCandidatesIgnoresOrdinaryEntriesAndSortsById() {
        var discovered = ModularToolDiscovery.scanCandidates(List.of(
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "ordinary",
                        "minecraft:stick",
                        false,
                        new MutableSource("ignored"),
                        MutableSource::copy,
                        source -> new TetraToolSnapshot(List.of())),
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "hammer",
                        "tetra:hammer",
                        true,
                        new MutableSource("hammer"),
                        MutableSource::copy,
                        source -> TetraToolSnapshotReader.fromModuleKeys(
                                new String[]{"module_hammer_head"},
                                new String[]{"module_socket"})),
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "axe",
                        "tetra:axe",
                        true,
                        new MutableSource("axe"),
                        MutableSource::copy,
                        source -> TetraToolSnapshotReader.fromModuleKeys(
                                new String[]{"module_axe_head", "module_handle"},
                                new String[0]))));

        assertEquals(List.of("tetra:axe", "tetra:hammer"),
                discovered.stream().map(ModularToolDiscovery.DiscoveredModularSnapshot::id).toList());
        assertTrue(discovered.stream().allMatch(ModularToolDiscovery.DiscoveredModularSnapshot::blueprintSupported));
    }

    @Test
    void scanCandidatesPreservesCanonicalSlotOrderAndDoesNotMutateInputSource() {
        MutableSource original = new MutableSource("keep");

        var discovered = ModularToolDiscovery.scanCandidates(List.of(
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "tool",
                        "tetra:tool",
                        true,
                        original,
                        MutableSource::copy,
                        source -> {
                            source.markMutated("changed");
                            return new TetraToolSnapshot(List.of(
                                    new TetraSlotSnapshot("minor/0", "module_socket", "", 2),
                                    new TetraSlotSnapshot("major/0", "module_head", "", 1)));
                        })));

        assertEquals(List.of("major/0", "minor/0"),
                discovered.get(0).slots().stream().map(TetraSlotSnapshot::slotKey).toList());
        assertFalse(original.mutated());
        assertEquals("keep", original.payload());
    }

    @Test
    void scanCandidatesKeepsScanningWhenOneEntryThrowsExceptionAndRecordsFailureReason() {
        var discovered = ModularToolDiscovery.scanCandidates(List.of(
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "working",
                        "tetra:working_tool",
                        true,
                        new MutableSource("working"),
                        MutableSource::copy,
                        source -> TetraToolSnapshotReader.fromModuleKeys(
                                new String[]{"module_pickaxe_head"},
                                new String[]{"module_binding"})),
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "broken",
                        "tetra:broken_tool",
                        true,
                        new MutableSource("broken"),
                        MutableSource::copy,
                        source -> {
                            throw new IllegalStateException("boom");
                        })));

        assertEquals(2, discovered.size());

        ModularToolDiscovery.DiscoveredModularSnapshot<String> broken = discovered.get(0);
        assertEquals("broken", broken.reference());
        assertEquals("tetra:broken_tool", broken.id());
        assertFalse(broken.blueprintSupported());
        assertTrue(broken.failureReason().contains("IllegalStateException"));
        assertTrue(broken.failureReason().contains("boom"));
        assertEquals(List.of(), broken.slots());

        ModularToolDiscovery.DiscoveredModularSnapshot<String> working = discovered.get(1);
        assertEquals("working", working.reference());
        assertEquals("tetra:working_tool", working.id());
        assertTrue(working.blueprintSupported());
        assertEquals(List.of("major/0", "minor/0"),
                working.slots().stream().map(TetraSlotSnapshot::slotKey).toList());
    }

    @Test
    void scanCandidatesKeepsScanningWhenOneEntryThrowsNonFatalErrorAndRecordsFailureReason() {
        var discovered = ModularToolDiscovery.scanCandidates(List.of(
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "working",
                        "tetra:working_tool",
                        true,
                        new MutableSource("working"),
                        MutableSource::copy,
                        source -> TetraToolSnapshotReader.fromModuleKeys(
                                new String[]{"module_pickaxe_head"},
                                new String[]{"module_binding"})),
                new ModularToolDiscovery.DiscoveryCandidate<>(
                        "broken",
                        "tetra:broken_tool",
                        true,
                        new MutableSource("broken"),
                        MutableSource::copy,
                        source -> {
                            throw new NoClassDefFoundError("missing.compat.ToolShim");
                        })));

        assertEquals(2, discovered.size());

        ModularToolDiscovery.DiscoveredModularSnapshot<String> broken = discovered.get(0);
        assertEquals("broken", broken.reference());
        assertEquals("tetra:broken_tool", broken.id());
        assertFalse(broken.blueprintSupported());
        assertTrue(broken.failureReason().contains("NoClassDefFoundError"));
        assertTrue(broken.failureReason().contains("missing.compat.ToolShim"));
        assertEquals(List.of(), broken.slots());

        ModularToolDiscovery.DiscoveredModularSnapshot<String> working = discovered.get(1);
        assertEquals("working", working.reference());
        assertEquals("tetra:working_tool", working.id());
        assertTrue(working.blueprintSupported());
    }

    @Test
    void scanCandidatesRethrowsJvmFatalErrors() {
        FatalDiscoveryError fatalError = new FatalDiscoveryError("fatal");

        FatalDiscoveryError thrown = assertThrows(FatalDiscoveryError.class, () ->
                ModularToolDiscovery.scanCandidates(List.of(
                        new ModularToolDiscovery.DiscoveryCandidate<>(
                                "broken",
                                "tetra:broken_tool",
                                true,
                                new MutableSource("broken"),
                                MutableSource::copy,
                                source -> {
                                    throw fatalError;
                                }))));

        assertEquals("fatal", thrown.getMessage());
    }

    private static final class MutableSource {
        private String payload;
        private boolean mutated;

        private MutableSource(String payload) {
            this(payload, false);
        }

        private MutableSource(String payload, boolean mutated) {
            this.payload = payload;
            this.mutated = mutated;
        }

        private MutableSource copy() {
            return new MutableSource(payload, mutated);
        }

        private void markMutated(String nextPayload) {
            payload = nextPayload;
            mutated = true;
        }

        private String payload() {
            return payload;
        }

        private boolean mutated() {
            return mutated;
        }
    }

    private static final class FatalDiscoveryError extends VirtualMachineError {
        private FatalDiscoveryError(String message) {
            super(message);
        }
    }
}
