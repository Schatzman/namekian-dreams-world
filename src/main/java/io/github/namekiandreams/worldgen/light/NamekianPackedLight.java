package io.github.namekiandreams.worldgen.light;

import io.github.namekiandreams.config.NamekianDreamsConfig;

public final class NamekianPackedLight {
    private NamekianPackedLight() {}

    public static int adjustPackedSkyLight(int packedLight, NamekianDreamsConfig config) {
        int block = block(packedLight);
        int sky = sky(packedLight);
        return pack(block, NamekianDaylightOffset.applyOutdoorSkyLightOffset(sky, config));
    }

    public static int pack(int block, int sky) {
        return (clamp(block) << 4) | (clamp(sky) << 20);
    }

    public static int block(int packedLight) {
        return (packedLight >> 4) & 0xFFFF;
    }

    public static int sky(int packedLight) {
        return (packedLight >> 20) & 0xFFFF;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(15, value));
    }
}
