package com.pycoder.tetraholographicblueprint.client.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintSessionTask5Test {
    @Test
    void fingerprintLifecycleIsClearedWithTheSession() {
        BlueprintSession session = BlueprintSession.empty(BlueprintSourceMode.HOLOSPHERE_NEW_BUILD);
        BlueprintFingerprint fingerprint = BlueprintFingerprint.fromNormalizedData(
                new MaterialKey("minecraft:stick", ""),
                List.of());

        session.setBlueprintFingerprint(fingerprint);

        assertTrue(session.blueprintFingerprint().isPresent());
        assertEquals(fingerprint, session.blueprintFingerprint().orElseThrow());

        session.close();

        assertFalse(session.blueprintFingerprint().isPresent());
    }
}
