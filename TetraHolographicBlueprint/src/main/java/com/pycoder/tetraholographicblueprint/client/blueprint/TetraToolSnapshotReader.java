package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.List;

public final class TetraToolSnapshotReader {
    private TetraToolSnapshotReader() {
    }

    public static TetraToolSnapshot fromModuleKeys(String[] majorKeys, String[] minorKeys) {
        var slots = new ArrayList<TetraSlotSnapshot>();
        appendSlots(slots, "major", majorKeys);
        appendSlots(slots, "minor", minorKeys, majorKeys == null ? 0 : majorKeys.length);
        return new TetraToolSnapshot(slots);
    }

    public static TetraToolSnapshot fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IModularItem modularItem)) {
            return new TetraToolSnapshot(List.of());
        }
        return fromModularItem(modularItem, stack);
    }

    static TetraToolSnapshot fromModularItem(IModularItem modularItem, ItemStack stack) {
        if (modularItem == null || stack == null || stack.isEmpty()) {
            return new TetraToolSnapshot(List.of());
        }
        return fromModuleKeys(
                modularItem.getMajorModuleKeys(stack),
                modularItem.getMinorModuleKeys(stack));
    }

    private static void appendSlots(List<TetraSlotSnapshot> slots, String kind, String[] moduleKeys) {
        appendSlots(slots, kind, moduleKeys, 0);
    }

    private static void appendSlots(List<TetraSlotSnapshot> slots, String kind, String[] moduleKeys, int ordinalOffset) {
        if (moduleKeys == null) {
            return;
        }
        for (int index = 0; index < moduleKeys.length; index++) {
            String moduleKey = moduleKeys[index] == null ? "" : moduleKeys[index];
            slots.add(new TetraSlotSnapshot(kind + "/" + index, moduleKey, "", ordinalOffset + index));
        }
    }
}
