package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record MaterialKey(String itemId, String distinguishingTag) {
    public MaterialKey {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(distinguishingTag, "distinguishingTag");
    }

    @SuppressWarnings("deprecation")
    public static MaterialKey from(ItemStack stack) {
        ItemStack copy = stack.copy();
        CompoundTag tag = copy.getTag();
        String normalizedTag = tag == null ? "" : tag.copy().toString();
        return new MaterialKey(BuiltInRegistries.ITEM.getKey(copy.getItem()).toString(), normalizedTag);
    }
}
