package com.pycoder.tetraholographicblueprint.client.compat;

import java.awt.Color;

/** 重新实现 Tetra 已移除的 Mutil 辅助类所提供的颜色运算。 */
public final class MutilColorCompat {
    private MutilColorCompat() {
    }

    public static int withBrightness(int color, double brightness) {
        float[] hsb = new float[3];
        Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsb);
        return Color.HSBtoRGB(hsb[0], hsb[1], (float) brightness);
    }
}
