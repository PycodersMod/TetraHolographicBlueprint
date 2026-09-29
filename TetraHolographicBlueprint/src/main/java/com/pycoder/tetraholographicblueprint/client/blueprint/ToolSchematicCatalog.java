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

public final class ToolSchematicCatalog {
    private static final Comparator<ResolvedPreview> BY_KEY = Comparator
            .comparing(ResolvedPreview::schematicKey)
            .thenComparing(ResolvedPreview::slotKey)
            .thenComparing(ResolvedPreview::moduleKey)
            .thenComparing(ResolvedPreview::variantKey);

    private ToolSchematicCatalog() {
    }

    public static List<TetraSchematicPreviewEntry> evaluate(ItemStack target, boolean ignoreRequirements) {
        if (target == null || target.isEmpty() || !(target.getItem() instanceof IModularItem modularItem)) {
            return List.of();
        }

        Set<String> slots = new LinkedHashSet<>();
        addModuleSlots(slots, modularItem.getMajorModules(target));
        addModuleSlots(slots, modularItem.getMinorModules(target));

        return evaluate(allRegisteredSchematics(), target, slots);
    }

    public static List<TetraSchematicPreviewEntry> evaluate(CraftingContext context, boolean ignoreRequirements) {
        if (context == null || context.targetStack == null || context.targetStack.isEmpty()
                || context.slot == null || context.slot.isBlank()) {
            return List.of();
        }
        UpgradeSchematic[] schematics = SchematicRegistry.getPreviewSchematics(context, ignoreRequirements);
        if (schematics == null) {
            return List.of();
        }
        return evaluate(Arrays.asList(schematics), context.targetStack, Set.of(context.slot));
    }

    static List<ResolvedPreview> resolveCandidates(Iterable<PreviewCandidate> candidates) {
        if (candidates == null) {
            return List.of();
        }

        List<ResolvedPreview> resolved = new ArrayList<>();
        for (PreviewCandidate candidate : candidates) {
            if (candidate == null || !candidate.previewable()) {
                continue;
            }
            resolved.add(new ResolvedPreview(
                    candidate.schematicKey(),
                    candidate.slotKey(),
                    candidate.moduleKey(),
                    candidate.variantKey(),
                    candidate.materials(),
                    candidate.processingConditions()));
        }
        return resolved.stream()
                .sorted(BY_KEY)
                .toList();
    }

    private static List<TetraSchematicPreviewEntry> evaluate(
            Collection<UpgradeSchematic> schematics,
            ItemStack target,
            Set<String> slots) {
        List<TetraSchematicPreviewEntry> entries = new ArrayList<>();
        for (UpgradeSchematic schematic : schematics) {
            if (schematic == null) {
                continue;
            }
            for (String slot : slots) {
                if (!schematic.isApplicableForSlot(slot, target)) {
                    continue;
                }

                OutcomePreview[] previews = schematic.getPreviews(target, slot);
                if (previews == null) {
                    continue;
                }
                for (OutcomePreview preview : previews) {
                    if (preview == null) {
                        continue;
                    }
                    ProcessingConditionSummary conditions = TetraProcessingConditionReader.read(
                            schematic,
                            target,
                            preview.materials);
                    entries.add(new TetraSchematicPreviewEntry(
                            schematic.getName(),
                            schematic.getKey(),
                            slot,
                            Objects.requireNonNullElse(preview.moduleKey, ""),
                            Objects.requireNonNullElse(preview.variantKey, ""),
                            TetraMaterialManifestReader.read(schematic, target, preview),
                            conditions));
                }
            }
        }
        return entries.stream()
                .sorted(Comparator
                        .comparing(TetraSchematicPreviewEntry::schematicKey)
                        .thenComparing(TetraSchematicPreviewEntry::slotKey)
                        .thenComparing(TetraSchematicPreviewEntry::moduleKey)
                        .thenComparing(TetraSchematicPreviewEntry::variantKey))
                .toList();
    }

    private static Collection<UpgradeSchematic> allRegisteredSchematics() {
        return TetraSchematicCatalog.allRegisteredSchematics();
    }

    private static void addModuleSlots(Set<String> slots, ItemModule[] modules) {
        if (modules == null) {
            return;
        }
        for (ItemModule module : modules) {
            if (module != null && module.getSlot() != null && !module.getSlot().isBlank()) {
                slots.add(module.getSlot());
            }
        }
    }

    public record PreviewCandidate(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            boolean previewable,
            List<ResolvedMaterial> materials,
            ProcessingConditionSummary processingConditions) {
        public PreviewCandidate {
            Objects.requireNonNull(schematicKey, "schematicKey");
            Objects.requireNonNull(slotKey, "slotKey");
            Objects.requireNonNull(moduleKey, "moduleKey");
            Objects.requireNonNull(variantKey, "variantKey");
            Objects.requireNonNull(materials, "materials");
            Objects.requireNonNull(processingConditions, "processingConditions");
            materials = List.copyOf(materials);
            processingConditions = processingConditions.copy();
        }
    }

    public record ResolvedPreview(
            String schematicKey,
            String slotKey,
            String moduleKey,
            String variantKey,
            List<ResolvedMaterial> materials,
            ProcessingConditionSummary processingConditions) {
        public ResolvedPreview {
            Objects.requireNonNull(schematicKey, "schematicKey");
            Objects.requireNonNull(slotKey, "slotKey");
            Objects.requireNonNull(moduleKey, "moduleKey");
            Objects.requireNonNull(variantKey, "variantKey");
            Objects.requireNonNull(materials, "materials");
            Objects.requireNonNull(processingConditions, "processingConditions");
            materials = List.copyOf(materials);
            processingConditions = processingConditions.copy();
        }
    }

    public record ResolvedMaterial(String itemId, String distinguishingTag, int quantity) {
        public ResolvedMaterial {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(distinguishingTag, "distinguishingTag");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }
}
