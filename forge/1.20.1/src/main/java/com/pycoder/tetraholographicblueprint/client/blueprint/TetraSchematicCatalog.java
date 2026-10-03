package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Tetra 蓝图的运行时目录及其针对目标物品的预览数据。 */
public final class TetraSchematicCatalog {
    private TetraSchematicCatalog() {
    }

    /**
     * 在调用时读取注册表，因此后续加载的附属模组或数据包蓝图
     * 也会自动包含在内，无需硬编码模组列表。
     */
    public static List<UpgradeSchematic> allRegisteredSchematics() {
        Collection<UpgradeSchematic> registered = SchematicRegistry.getAllSchematics();
        if (registered == null) {
            return List.of();
        }
        return registered.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(UpgradeSchematic::getKey))
                .toList();
    }

    /**
     * 将每个已注册蓝图与目标物品中所有已占用的模块槽位逐一匹配。
     * 每条预览保留各自的材料槽位和加工条件，不会合并不同槽位的条目。
     */
    public static List<TetraSchematicPreviewEntry> evaluateForTool(ItemStack target) {
        return ToolSchematicCatalog.evaluate(target, false);
    }

    /**
     * 仅计算 Tetra 在给定上下文中判定为可预览的蓝图。
     * 此方法供图形界面显示使用，隐藏或不可用的蓝图不会出现在列表中。
     */
    public static List<TetraSchematicPreviewEntry> evaluateForContext(
            CraftingContext context,
            boolean ignoreRequirements) {
        return ToolSchematicCatalog.evaluate(context, ignoreRequirements);
    }
}
