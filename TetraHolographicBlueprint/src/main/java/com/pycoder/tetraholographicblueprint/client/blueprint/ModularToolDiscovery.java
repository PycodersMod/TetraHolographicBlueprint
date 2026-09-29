package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModularToolDiscovery {
    private static final Comparator<DiscoveredModularTool> BY_ID =
            Comparator.comparing(tool -> tool.id().toString());
    private static final Comparator<DiscoveredModularSnapshot<?>> SNAPSHOT_BY_ID =
            Comparator.comparing(DiscoveredModularSnapshot::id);

    private ModularToolDiscovery() {
    }

    public static List<DiscoveredModularTool> scan(Iterable<Item> items) {
        return scan(items, ForgeRegistries.ITEMS::getKey, Item::getDefaultInstance);
    }

    static List<DiscoveredModularTool> scan(
            Iterable<Item> items,
            Map<Item, ResourceLocation> ids,
            Map<Item, ItemStack> stacks) {
        return scan(items, ids::get, stacks::get);
    }

    static List<DiscoveredModularTool> scan(
            Iterable<Item> items,
            Function<Item, ResourceLocation> idResolver,
            Function<Item, ItemStack> stackFactory) {
        if (items == null) {
            return List.of();
        }

        List<DiscoveredModularTool> discovered = new ArrayList<>();
        for (Item item : items) {
            if (!(item instanceof IModularItem modularItem)) {
                continue;
            }

            ResourceLocation id = idResolver.apply(item);
            if (id == null) {
                continue;
            }

            SnapshotReadResult snapshotResult = readSnapshotSafely(() -> {
                ItemStack snapshotSource = createSnapshotSource(item, stackFactory);
                return TetraToolSnapshotReader.fromModularItem(modularItem, snapshotSource);
            });
            if (snapshotResult.succeeded()) {
                TetraToolSnapshot snapshot = snapshotResult.snapshot();
                boolean blueprintSupported = !snapshot.slots().isEmpty();
                String failureReason = blueprintSupported ? "" : "No occupied modules discovered";
                discovered.add(new DiscoveredModularTool(
                        item,
                        id,
                        snapshot.slots(),
                        blueprintSupported,
                        failureReason));
            } else {
                discovered.add(new DiscoveredModularTool(
                        item,
                        id,
                        List.of(),
                        false,
                        describeFailure(snapshotResult.failure())));
            }
        }

        return discovered.stream()
                .sorted(BY_ID)
                .toList();
    }

    static <R, S> List<DiscoveredModularSnapshot<R>> scanCandidates(
            Iterable<DiscoveryCandidate<R, S>> candidates) {
        if (candidates == null) {
            return List.of();
        }

        List<DiscoveredModularSnapshot<R>> discovered = new ArrayList<>();
        for (DiscoveryCandidate<R, S> candidate : candidates) {
            if (candidate == null || !candidate.modular()) {
                continue;
            }

            SnapshotReadResult snapshotResult = readSnapshotSafely(
                    () -> candidate.snapshotReader().apply(candidate.copySource()));
            if (snapshotResult.succeeded()) {
                TetraToolSnapshot snapshot = snapshotResult.snapshot();
                boolean blueprintSupported = !snapshot.slots().isEmpty();
                String failureReason = blueprintSupported ? "" : "No occupied modules discovered";
                discovered.add(new DiscoveredModularSnapshot<>(
                        candidate.reference(),
                        candidate.id(),
                        snapshot.slots(),
                        blueprintSupported,
                        failureReason));
            } else {
                discovered.add(new DiscoveredModularSnapshot<>(
                        candidate.reference(),
                        candidate.id(),
                        List.of(),
                        false,
                        describeFailure(snapshotResult.failure())));
            }
        }

        return discovered.stream()
                .sorted(SNAPSHOT_BY_ID)
                .toList();
    }

    private static ItemStack createSnapshotSource(Item item, Function<Item, ItemStack> stackFactory) {
        ItemStack stack = stackFactory.apply(item);
        if (stack == null || stack.isEmpty()) {
            stack = item.getDefaultInstance();
        }
        if (stack == null || stack.isEmpty()) {
            stack = new ItemStack(item);
        }
        return stack.copy();
    }

    private static SnapshotReadResult readSnapshotSafely(Supplier<TetraToolSnapshot> reader) {
        try {
            return SnapshotReadResult.success(reader.get());
        } catch (Throwable throwable) {
            if (throwable instanceof VirtualMachineError virtualMachineError) {
                throw virtualMachineError;
            }
            if (throwable instanceof ThreadDeath threadDeath) {
                throw threadDeath;
            }
            return SnapshotReadResult.failure(throwable);
        }
    }

    private static String describeFailure(Throwable exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getClass().getSimpleName() + ": " + message;
    }

    static record DiscoveryCandidate<R, S>(
            R reference,
            String id,
            boolean modular,
            S source,
            Function<S, S> sourceCopier,
            Function<S, TetraToolSnapshot> snapshotReader) {

        DiscoveryCandidate {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(sourceCopier, "sourceCopier");
            Objects.requireNonNull(snapshotReader, "snapshotReader");
        }

        S copySource() {
            return sourceCopier.apply(source);
        }
    }

    static record DiscoveredModularSnapshot<R>(
            R reference,
            String id,
            List<TetraSlotSnapshot> slots,
            boolean blueprintSupported,
            String failureReason) {

        DiscoveredModularSnapshot {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(slots, "slots");
            slots = List.copyOf(slots);
            failureReason = failureReason == null ? "" : failureReason;
        }
    }

    private record SnapshotReadResult(TetraToolSnapshot snapshot, Throwable failure) {
        private static SnapshotReadResult success(TetraToolSnapshot snapshot) {
            return new SnapshotReadResult(snapshot, null);
        }

        private static SnapshotReadResult failure(Throwable failure) {
            return new SnapshotReadResult(null, Objects.requireNonNull(failure, "failure"));
        }

        private boolean succeeded() {
            return failure == null;
        }
    }
}
