package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class MaterialTotals {
    private final Map<MaterialKey, Integer> entries = new LinkedHashMap<>();

    public void add(MaterialKey key, int quantity) {
        if (quantity <= 0) {
            return;
        }
        entries.merge(key, quantity, Integer::sum);
    }

    public Map<MaterialKey, Integer> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public int total() {
        return entries.values().stream().mapToInt(Integer::intValue).sum();
    }

    public MaterialTotals remainingAfter(MaterialTotals satisfied) {
        Objects.requireNonNull(satisfied, "satisfied");
        MaterialTotals remaining = new MaterialTotals();
        for (Map.Entry<MaterialKey, Integer> entry : entries.entrySet()) {
            int quantity = Math.max(0, entry.getValue() - satisfied.entries.getOrDefault(entry.getKey(), 0));
            remaining.add(entry.getKey(), quantity);
        }
        return remaining;
    }
}
