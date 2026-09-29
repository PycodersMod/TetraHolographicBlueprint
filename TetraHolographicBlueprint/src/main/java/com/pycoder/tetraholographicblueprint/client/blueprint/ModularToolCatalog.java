package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ModularToolCatalog {
    private static volatile List<CatalogEntry> snapshot = List.of();
    private static volatile Map<ResourceLocation, CatalogEntry> byId = Map.of();

    private ModularToolCatalog() {
    }

    public static void refresh(Iterable<Item> items) {
        if (items == null) {
            clear();
            return;
        }

        List<CatalogEntry> refreshed = new ArrayList<>();
        for (DiscoveredModularTool discovered : ModularToolDiscovery.scan(items)) {
            refreshed.add(new CatalogEntry(
                    discovered.id(),
                    discovered.slots(),
                    discovered.blueprintSupported(),
                    discovered.failureReason()));
        }
        replaceAll(refreshed);
    }

    static void refreshEntries(Iterable<CatalogEntry> entries) {
        if (entries == null) {
            clear();
            return;
        }

        List<CatalogEntry> refreshed = new ArrayList<>();
        for (CatalogEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            refreshed.add(entry);
        }
        replaceAll(refreshed);
    }

    public static Optional<CatalogEntry> find(ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }

    public static List<CatalogEntry> snapshot() {
        return snapshot;
    }

    public static void clear() {
        snapshot = List.of();
        byId = Map.of();
    }

    private static void replaceAll(List<CatalogEntry> refreshed) {
        Map<ResourceLocation, CatalogEntry> index = new LinkedHashMap<>();
        for (CatalogEntry entry : refreshed) {
            index.put(entry.id(), entry);
        }
        snapshot = List.copyOf(refreshed);
        byId = Map.copyOf(index);
    }

    public record CatalogEntry(
            ResourceLocation id,
            List<TetraSlotSnapshot> slots,
            boolean blueprintSupported,
            String failureReason) {
        public CatalogEntry {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(slots, "slots");
            slots = List.copyOf(slots);
            failureReason = failureReason == null ? "" : failureReason;
        }
    }
}
