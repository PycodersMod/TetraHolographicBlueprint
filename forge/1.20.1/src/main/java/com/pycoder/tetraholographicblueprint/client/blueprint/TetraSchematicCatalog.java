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

/** Runtime catalog of Tetra schematics and their target-specific preview data. */
public final class TetraSchematicCatalog {
    private TetraSchematicCatalog() {
    }

    /**
     * Reads the registry at call time, so later-loaded addon/data-pack schematics
     * are included without a hard-coded mod list.
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
     * Evaluates every registered schematic against every occupied module slot
     * of the supplied target. Each preview keeps its own material slots and
     * processing conditions; entries are never merged across slots.
     */
    public static List<TetraSchematicPreviewEntry> evaluateForTool(ItemStack target) {
        return ToolSchematicCatalog.evaluate(target, false);
    }

    /**
     * Evaluates only the schematics Tetra considers previewable in the supplied
     * context. This is the entry point for GUI display and keeps hidden or
     * unavailable schematics out of the visible list.
     */
    public static List<TetraSchematicPreviewEntry> evaluateForContext(
            CraftingContext context,
            boolean ignoreRequirements) {
        return ToolSchematicCatalog.evaluate(context, ignoreRequirements);
    }
}
