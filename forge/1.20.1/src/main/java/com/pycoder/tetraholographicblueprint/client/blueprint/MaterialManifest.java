package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MaterialManifest {
    private final Map<MaterialKey, MaterialEntry> entries = new LinkedHashMap<>();

    public void add(ItemStack stack, int quantity) {
        if (stack == null || stack.isEmpty() || quantity <= 0) {
            return;
        }

        MaterialKey key = MaterialKey.from(stack);
        add(key, stack, quantity);
    }

    public void add(MaterialKey key, int quantity) {
        if (key == null || quantity <= 0) {
            return;
        }
        MaterialEntry existing = entries.get(key);
        if (existing == null) {
            entries.put(key, new MaterialEntry(key, null, quantity));
        } else {
            entries.put(key, new MaterialEntry(key, existing.stack(), existing.quantity() + quantity));
        }
    }

    private void add(MaterialKey key, ItemStack stack, int quantity) {
        MaterialEntry existing = entries.get(key);
        if (existing == null) {
            entries.put(key, new MaterialEntry(key, stack.copyWithCount(1), quantity));
        } else {
            entries.put(key, new MaterialEntry(key, existing.stack(), existing.quantity() + quantity));
        }
    }

    public List<MaterialEntry> entries() {
        List<MaterialEntry> result = new ArrayList<>(entries.size());
        for (MaterialEntry entry : entries.values()) {
            result.add(new MaterialEntry(entry.key(), entry.stack(), entry.quantity()));
        }
        return Collections.unmodifiableList(result);
    }

    public int total() {
        return entries.values().stream().mapToInt(MaterialEntry::quantity).sum();
    }

    /**
     * Returns the part of this manifest that is not already satisfied by another manifest.
     * Quantities never become negative, and neither input manifest is mutated.
     */
    public MaterialManifest remainingAfter(MaterialManifest satisfied) {
        Objects.requireNonNull(satisfied, "satisfied");

        Map<MaterialKey, Integer> satisfiedQuantities = new LinkedHashMap<>();
        for (Map.Entry<MaterialKey, MaterialEntry> entry : satisfied.entries.entrySet()) {
            satisfiedQuantities.merge(entry.getKey(), entry.getValue().quantity(), Integer::sum);
        }

        MaterialManifest remaining = new MaterialManifest();
        for (Map.Entry<MaterialKey, MaterialEntry> entry : entries.entrySet()) {
            int alreadySatisfied = satisfiedQuantities.getOrDefault(entry.getKey(), 0);
            int quantity = Math.max(0, entry.getValue().quantity() - alreadySatisfied);
            remaining.add(entry.getKey(), quantity);
        }
        return remaining;
    }

    public void clear() {
        entries.clear();
    }

    public MaterialManifest copy() {
        MaterialManifest copy = new MaterialManifest();
        for (MaterialEntry entry : entries()) {
            copy.add(entry.key(), entry.quantity());
        }
        return copy;
    }

    public record MaterialEntry(MaterialKey key, ItemStack stack, int quantity) {
        public MaterialEntry {
            key = Objects.requireNonNull(key, "key");
            if (stack != null) {
                stack = stack.copy();
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }

        @Override
        public ItemStack stack() {
            return stack == null ? null : stack.copy();
        }
    }
}
