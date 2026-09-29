package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TetraVirtualToolBuilder {
    private TetraVirtualToolBuilder() {
    }

    public static ItemStack build(ItemStack baseStack, List<UpgradeApplication> applications, Player player) {
        Objects.requireNonNull(baseStack, "baseStack");
        Objects.requireNonNull(applications, "applications");
        Objects.requireNonNull(player, "player");

        ItemStack virtualStack = baseStack.copy();
        for (UpgradeApplication application : applications) {
            UpgradeSchematic schematic = SchematicRegistry.getSchematic(application.schematicKey());
            if (schematic == null) {
                throw new IllegalArgumentException("Unknown Tetra schematic: " + application.schematicKey());
            }
            ItemStack result = schematic.applyUpgrade(
                    virtualStack,
                    application.materials().toArray(ItemStack[]::new),
                    false,
                    application.slotKey(),
                    player);
            if (result == null || result.isEmpty()) {
                throw new IllegalStateException(
                        "Tetra schematic produced an empty virtual stack: " + application.schematicKey());
            }
            virtualStack = result.copy();
        }
        return virtualStack;
    }

    public record UpgradeApplication(String schematicKey, String slotKey, List<ItemStack> materials) {
        public UpgradeApplication {
            Objects.requireNonNull(schematicKey, "schematicKey");
            Objects.requireNonNull(slotKey, "slotKey");
            Objects.requireNonNull(materials, "materials");
            materials = copyMaterials(materials);
        }

        @Override
        public List<ItemStack> materials() {
            return copyMaterials(materials);
        }

        private static List<ItemStack> copyMaterials(List<ItemStack> materials) {
            var copies = new ArrayList<ItemStack>(materials.size());
            for (ItemStack material : materials) {
                copies.add(Objects.requireNonNull(material, "material").copy());
            }
            return List.copyOf(copies);
        }
    }
}
