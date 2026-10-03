package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 通过 Tetra 自身的蓝图 API 读取加工要求。 */
public final class TetraProcessingConditionReader {
    private TetraProcessingConditionReader() {
    }

    public static ProcessingConditionSummary read(
            UpgradeSchematic schematic,
            ItemStack target,
            ItemStack[] materials) {
        Objects.requireNonNull(schematic, "schematic");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(materials, "materials");

        ProcessingConditionSummary summary = new ProcessingConditionSummary();
        for (Map.Entry<ToolAction, Integer> entry : schematic.getRequiredToolLevels(target, materials).entrySet()) {
            ToolAction action = entry.getKey();
            if (action != null) {
                summary.addToolRequirement(action.name(), entry.getValue());
            }
        }

        appendRequirementDescriptions(summary, schematic.getRequirementDescription());
        return summary;
    }

    static void appendRequirementDescriptions(ProcessingConditionSummary summary, List<Component> descriptions) {
        Objects.requireNonNull(summary, "summary");
        if (descriptions == null || descriptions.isEmpty()) {
            return;
        }
        for (int index = 0; index < descriptions.size(); index++) {
            Component description = descriptions.get(index);
            if (description == null) {
                continue;
            }
            String text = description.getString();
            if (!text.isBlank()) {
                summary.addCondition(ProcessingConditionKind.OTHER, "requirement:" + index + ":" + text, text);
            }
        }
    }
}
