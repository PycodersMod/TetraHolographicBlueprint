package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TetraReverseResolver {
    private TetraReverseResolver() {
    }

    public static List<TetraReverseMappingCandidate> rankCandidates(
            List<TetraReverseMappingCandidate> candidates) {
        return candidates.stream()
                .distinct()
                .sorted(java.util.Comparator
                        .comparingInt(TetraReverseMappingCandidate::canonicalOrder)
                        .thenComparing(TetraReverseMappingCandidate::schematicKey)
                        .thenComparing(TetraReverseMappingCandidate::slotKey)
                        .thenComparing(TetraReverseMappingCandidate::moduleKey)
                        .thenComparing(TetraReverseMappingCandidate::variantKey))
                .toList();
    }

    public static List<TetraReverseMappingCandidate> resolve(ItemStack tool, String slotKey) {
        if (tool == null || tool.isEmpty() || slotKey == null || slotKey.isBlank()) {
            return List.of();
        }
        var candidates = new ArrayList<TetraReverseMappingCandidate>();
        var schematics = SchematicRegistry.getSchematics(tool, slotKey);
        if (schematics == null) {
            return List.of();
        }
        int canonicalOrder = 0;
        for (var schematic : schematics) {
            if (schematic == null) {
                continue;
            }
            OutcomePreview[] previews = schematic.getPreviews(tool, slotKey);
            if (previews == null) {
                continue;
            }
            for (var preview : previews) {
                if (preview != null && preview.isApplied(tool, slotKey)) {
                    candidates.add(new TetraReverseMappingCandidate(
                            schematic.getKey(),
                            slotKey,
                            Objects.requireNonNullElse(preview.moduleKey, ""),
                            Objects.requireNonNullElse(preview.variantKey, ""),
                            canonicalOrder++));
                }
            }
        }
        return rankCandidates(candidates);
    }

    public static BlueprintParseResult resolveComplete(ItemStack target) {
        if (target == null || target.isEmpty()) {
            return BlueprintParseResult.notModular("Target is empty");
        }
        if (!(target.getItem() instanceof IModularItem modularItem)) {
            return BlueprintParseResult.notModular("Target is not modular");
        }

        Set<String> slots = new LinkedHashSet<>();
        addModuleSlots(slots, modularItem.getMajorModules(target));
        addModuleSlots(slots, modularItem.getMinorModules(target));
        if (slots.isEmpty()) {
            return BlueprintParseResult.unsupported("No modular slots discovered", List.of());
        }

        List<SlotResolutionCandidate> slotCandidates = new ArrayList<>();
        for (String slotKey : slots) {
            slotCandidates.add(resolveSlotCandidates(target, slotKey));
        }
        return resolveFromCandidates(slotCandidates, true);
    }

    static BlueprintParseResult resolveFromCandidates(
            List<SlotResolutionCandidate> slotCandidates,
            boolean modularTarget) {
        if (!modularTarget) {
            return BlueprintParseResult.notModular("Target is not modular");
        }
        if (slotCandidates == null || slotCandidates.isEmpty()) {
            return BlueprintParseResult.unsupported("No modular slots discovered", List.of());
        }

        List<BlueprintPartSelection> selections = new ArrayList<>();
        List<TetraReverseMappingCandidate> fingerprintInputs = new ArrayList<>();
        for (SlotResolutionCandidate slotCandidate : slotCandidates) {
            if (slotCandidate == null) {
                continue;
            }
            List<ResolvedReverseCandidate> ranked = rankResolvedCandidates(slotCandidate.candidates());
            if (ranked.isEmpty()) {
                return BlueprintParseResult.unsupported(
                        "No matching schematic found for slot " + slotCandidate.slotKey(),
                        fingerprintInputs);
            }

            ResolvedReverseCandidate selected = ranked.get(0);
            fingerprintInputs.add(selected.candidate());
            selections.add(new BlueprintPartSelection(
                    slotCandidate.slotKey(),
                    selected.candidate().schematicKey(),
                    selected.candidate().moduleKey(),
                    selected.candidate().variantKey(),
                    selected.materialStacks(),
                    selected.candidate().canonicalOrder()));
        }

        if (selections.isEmpty()) {
            return BlueprintParseResult.unsupported("No modular slots discovered", fingerprintInputs);
        }
        return BlueprintParseResult.supported(selections, fingerprintInputs);
    }

    static SlotResolutionCandidate resolveSlotCandidates(ItemStack tool, String slotKey) {
        List<ResolvedReverseCandidate> candidates = new ArrayList<>();
        var schematics = SchematicRegistry.getSchematics(tool, slotKey);
        if (schematics == null) {
            return new SlotResolutionCandidate(slotKey, List.of());
        }
        int canonicalOrder = 0;
        for (var schematic : schematics) {
            if (schematic == null) {
                continue;
            }
            OutcomePreview[] previews = schematic.getPreviews(tool, slotKey);
            if (previews == null) {
                continue;
            }
            for (var preview : previews) {
                if (preview != null && preview.isApplied(tool, slotKey)) {
                    candidates.add(new ResolvedReverseCandidate(
                            new TetraReverseMappingCandidate(
                                    schematic.getKey(),
                                    slotKey,
                                    Objects.requireNonNullElse(preview.moduleKey, ""),
                                    Objects.requireNonNullElse(preview.variantKey, ""),
                                    canonicalOrder++),
                            copyMaterials(preview.materials)));
                }
            }
        }
        return new SlotResolutionCandidate(slotKey, rankResolvedCandidates(candidates));
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

    private static List<ItemStack> copyMaterials(ItemStack[] materials) {
        if (materials == null || materials.length == 0) {
            return List.of();
        }
        List<ItemStack> copies = new ArrayList<>(materials.length);
        for (ItemStack material : materials) {
            if (material != null && !material.isEmpty()) {
                copies.add(material.copy());
            }
        }
        return List.copyOf(copies);
    }

    private static List<ResolvedReverseCandidate> rankResolvedCandidates(
            List<ResolvedReverseCandidate> candidates) {
        if (candidates == null) {
            return List.of();
        }
        return candidates.stream()
                .distinct()
                .sorted(java.util.Comparator
                        .comparingInt((ResolvedReverseCandidate candidate) -> candidate.candidate().canonicalOrder())
                        .thenComparing((ResolvedReverseCandidate candidate) -> candidate.candidate().schematicKey())
                        .thenComparing((ResolvedReverseCandidate candidate) -> candidate.candidate().slotKey())
                        .thenComparing((ResolvedReverseCandidate candidate) -> candidate.candidate().moduleKey())
                        .thenComparing((ResolvedReverseCandidate candidate) -> candidate.candidate().variantKey()))
                .toList();
    }

    static record SlotResolutionCandidate(String slotKey, List<ResolvedReverseCandidate> candidates) {
        SlotResolutionCandidate {
            Objects.requireNonNull(slotKey, "slotKey");
            Objects.requireNonNull(candidates, "candidates");
            candidates = List.copyOf(candidates);
        }
    }

    static record ResolvedReverseCandidate(
            TetraReverseMappingCandidate candidate,
            List<ItemStack> materialStacks) {
        ResolvedReverseCandidate {
            Objects.requireNonNull(candidate, "candidate");
            Objects.requireNonNull(materialStacks, "materialStacks");
            materialStacks = List.copyOf(materialStacks);
        }
    }
}
