package io.github.namekiandreams.worldgen.light;

import io.github.namekiandreams.config.NamekianDreamsConfig;

public final class NamekianDaylightOffset {
    private NamekianDaylightOffset() {}

    public static int applyOutdoorSkyLightOffset(int vanillaSkyLight, NamekianDreamsConfig config) {
        if (!config.enableActualSkyLightOffset()) return clamp(vanillaSkyLight, 0, 15);
        int shifted = vanillaSkyLight + config.outdoorSkyLightOffset();
        return clamp(shifted, 0, config.maxSkyLightLevel());
    }

    public static double visualDaylightMultiplier(NamekianDreamsConfig config) {
        return config.enableVisualDaylightDimming() ? config.visualDaylightMultiplier() : 1.0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
