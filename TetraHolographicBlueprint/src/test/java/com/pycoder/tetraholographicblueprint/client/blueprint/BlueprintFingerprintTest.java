package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlueprintFingerprintTest {
    @Test
    void normalizesTransientStackStateOutOfTags() {
        CompoundTag noisy = new CompoundTag();
        noisy.putInt("Damage", 12);
        noisy.putInt("RepairCost", 4);
        noisy.putString("CustomModelData", "7");
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"text\":\"Fancy\"}");
        display.putString("Lore", "ignored");
        noisy.put("display", display);
        noisy.putString("Enchantments", "sharpness");

        assertEquals("", BlueprintFingerprint.normalizedTag(noisy));
    }

    @Test
    void fingerprintsCanonicalizeSelectionsAndCopyInputLists() {
        List<BlueprintPartSelection> reversed = new ArrayList<>(List.of(
                selection("minor/0", "tetra:second", 2),
                selection("major/0", "tetra:first", 1)));

        BlueprintFingerprint fingerprint = BlueprintFingerprint.fromNormalizedData(
                new MaterialKey("minecraft:stick", ""),
                reversed);

        assertEquals(List.of("major/0", "minor/0"),
                fingerprint.selections().stream().map(BlueprintPartSelection::slotKey).toList());
        assertEquals("minecraft:stick|", fingerprint.baseKey().itemId() + "|" + fingerprint.baseKey().distinguishingTag());
        assertThrows(UnsupportedOperationException.class,
                () -> fingerprint.selections().add(selection("extra", "tetra:extra", 3)));
    }

    @Test
    void fingerprintsRemainStableAcrossEquivalentInputOrderings() {
        BlueprintFingerprint first = BlueprintFingerprint.fromNormalizedData(
                new MaterialKey("minecraft:stick", ""),
                List.of(selection("major/0", "tetra:first", 1), selection("minor/0", "tetra:second", 2)));
        BlueprintFingerprint second = BlueprintFingerprint.fromNormalizedData(
                new MaterialKey("minecraft:stick", ""),
                List.of(selection("minor/0", "tetra:second", 2), selection("major/0", "tetra:first", 1)));

        assertEquals(first.signature(), second.signature());
        assertEquals(first, second);
    }

    private static BlueprintPartSelection selection(String slotKey, String schematicKey, int canonicalOrder) {
        return new BlueprintPartSelection(
                slotKey,
                schematicKey,
                "module",
                "variant",
                List.of(),
                canonicalOrder);
    }
}
