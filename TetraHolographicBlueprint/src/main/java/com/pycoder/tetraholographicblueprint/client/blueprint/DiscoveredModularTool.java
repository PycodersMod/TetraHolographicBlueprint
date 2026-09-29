package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Objects;

public record DiscoveredModularTool(
        Item item,
        ResourceLocation id,
        List<TetraSlotSnapshot> slots,
        boolean blueprintSupported,
        String failureReason) {

    public DiscoveredModularTool {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(slots, "slots");
        slots = List.copyOf(slots);
        failureReason = failureReason == null ? "" : failureReason;
    }
}
