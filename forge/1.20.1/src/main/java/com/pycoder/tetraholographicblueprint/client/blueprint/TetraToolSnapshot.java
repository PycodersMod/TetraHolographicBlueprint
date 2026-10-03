package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TetraToolSnapshot {
    private static final Comparator<TetraSlotSnapshot> CANONICAL_ORDER =
            Comparator.comparingInt(TetraSlotSnapshot::ordinal)
                    .thenComparing(TetraSlotSnapshot::slotKey);

    private final List<TetraSlotSnapshot> slots;

    public TetraToolSnapshot(List<TetraSlotSnapshot> slots) {
        Objects.requireNonNull(slots, "slots");
        this.slots = slots.stream()
                .sorted(CANONICAL_ORDER)
                .toList();
    }

    public List<TetraSlotSnapshot> slots() {
        return slots;
    }

    public Optional<TetraSlotSnapshot> findSlot(String slotKey) {
        if (slotKey == null) {
            return Optional.empty();
        }
        return slots.stream()
                .filter(slot -> slot.slotKey().equals(slotKey))
                .findFirst();
    }
}
