package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record BlueprintFingerprint(
        MaterialKey baseKey,
        List<BlueprintPartSelection> selections,
        String signature) {
    private static final Comparator<BlueprintPartSelection> BY_CANONICAL_ORDER = Comparator
            .comparingInt(BlueprintPartSelection::canonicalOrder)
            .thenComparing(BlueprintPartSelection::slotKey)
            .thenComparing(BlueprintPartSelection::schematicKey)
            .thenComparing(BlueprintPartSelection::moduleKey)
            .thenComparing(BlueprintPartSelection::moduleVariant);

    public BlueprintFingerprint {
        Objects.requireNonNull(baseKey, "baseKey");
        Objects.requireNonNull(selections, "selections");
        Objects.requireNonNull(signature, "signature");
        selections = List.copyOf(new ArrayList<>(selections.stream().sorted(BY_CANONICAL_ORDER).toList()));
    }

    public static BlueprintFingerprint from(ItemStack cleanStack, List<BlueprintPartSelection> selections) {
        Objects.requireNonNull(cleanStack, "cleanStack");
        return fromNormalizedData(normalizedBaseKey(cleanStack), selections);
    }

    static BlueprintFingerprint fromNormalizedData(MaterialKey baseKey, List<BlueprintPartSelection> selections) {
        Objects.requireNonNull(baseKey, "baseKey");
        Objects.requireNonNull(selections, "selections");
        List<BlueprintPartSelection> canonicalSelections = selections.stream()
                .sorted(BY_CANONICAL_ORDER)
                .toList();
        return new BlueprintFingerprint(baseKey, canonicalSelections, buildSignature(baseKey, canonicalSelections));
    }

    static String normalizedTag(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }

        CompoundTag normalized = tag.copy();
        normalized.remove("Damage");
        normalized.remove("RepairCost");
        normalized.remove("Enchantments");
        normalized.remove("StoredEnchantments");
        normalized.remove("CustomModelData");
        normalized.remove("Unbreakable");
        normalized.remove("AttributeModifiers");

        if (normalized.contains("display", Tag.TAG_COMPOUND)) {
            CompoundTag display = normalized.getCompound("display").copy();
            display.remove("Name");
            display.remove("Lore");
            display.remove("color");
            if (display.isEmpty()) {
                normalized.remove("display");
            } else {
                normalized.put("display", display);
            }
        }

        return normalized.isEmpty() ? "" : normalized.toString();
    }

    private static MaterialKey normalizedBaseKey(ItemStack stack) {
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return new MaterialKey(itemId, normalizedTag(stack.getTag()));
    }

    private static String buildSignature(MaterialKey baseKey, List<BlueprintPartSelection> selections) {
        StringBuilder builder = new StringBuilder();
        builder.append(baseKey.itemId()).append('|').append(baseKey.distinguishingTag()).append('|');
        for (BlueprintPartSelection selection : selections) {
            builder.append(selection.slotKey()).append('|')
                    .append(selection.schematicKey()).append('|')
                    .append(selection.moduleKey()).append('|')
                    .append(selection.moduleVariant()).append('|')
                    .append(selection.canonicalOrder()).append('|');
            for (ItemStack stack : selection.materialStacks()) {
                if (stack != null && !stack.isEmpty()) {
                    String materialId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                    builder.append(materialId).append('|');
                    CompoundTag tag = stack.getTag();
                    builder.append(normalizedTag(tag)).append('|');
                }
            }
            builder.append(';');
        }
        return builder.toString();
    }
}
