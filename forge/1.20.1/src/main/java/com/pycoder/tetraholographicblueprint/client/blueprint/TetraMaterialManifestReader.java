package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

/** 通过 Tetra 蓝图 API 读取材料成本，不推测或补造数量。 */
public final class TetraMaterialManifestReader {
    private TetraMaterialManifestReader() {
    }

    public static MaterialManifest read(
            UpgradeSchematic schematic,
            ItemStack target,
            ItemStack[] materials) {
        MaterialManifest manifest = new MaterialManifest();
        if (schematic == null || target == null || target.isEmpty() || materials == null) {
            return manifest;
        }

        int slotCount = Math.min(schematic.getNumMaterialSlots(), materials.length);
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack material = materials[slot];
            if (material == null || material.isEmpty()) {
                continue;
            }

            String slotName = schematic.getSlotName(target, slot);
            if (!schematic.acceptsMaterial(target, slotName, slot, material)) {
                continue;
            }

            int quantity = schematic.getRequiredQuantity(target, slot, material);
            if (quantity > 0) {
                manifest.add(material, quantity);
            }
        }
        return manifest;
    }

    public static MaterialManifest read(
            UpgradeSchematic schematic,
            ItemStack target,
            OutcomePreview preview) {
        if (preview == null) {
            return new MaterialManifest();
        }
        return read(schematic, target, preview.materials);
    }
}
