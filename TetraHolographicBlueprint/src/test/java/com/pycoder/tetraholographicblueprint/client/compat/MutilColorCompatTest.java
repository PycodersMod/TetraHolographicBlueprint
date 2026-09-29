package com.pycoder.tetraholographicblueprint.client.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MutilColorCompatTest {
    @Test
    void withBrightnessMatchesMutilSimpleColorBehavior() {
        assertEquals(0xFF4D0000, MutilColorCompat.withBrightness(0xFFFF0000, 0.3));
    }
}
