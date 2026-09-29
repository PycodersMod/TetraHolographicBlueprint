package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record BlueprintPartSelection(
        String slotKey,
        String schematicKey,
        String moduleKey,
        String moduleVariant,
        List<ItemStack> materialStacks,
        int canonicalOrder
) {
    public BlueprintPartSelection {
        materialStacks = copyStacks(materialStacks);
    }

    @Override
    public List<ItemStack> materialStacks() {
        return copyStacks(materialStacks);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copies.add(stack.copy());
        }
        return Collections.unmodifiableList(copies);
    }
}
