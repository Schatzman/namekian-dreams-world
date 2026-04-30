package io.github.namekiandreams.client;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public final class NamekianClientColors {
    private NamekianClientColors() {}

    public static boolean isNamekianLevel(ClientLevel level) {
        return level != null && level.dimensionType().effectsLocation().equals(NamekianDreamsWorld.id("namekian_overworld"));
    }

    public static Vec3 skyColor(NamekianDreamsConfig config) {
        return color(config.skyColor()).scale(NamekianDreamsWorld.CONFIG.enableVisualDaylightDimming()
                ? Math.max(0.78, NamekianDreamsWorld.CONFIG.visualDaylightMultiplier())
                : 1.0);
    }

    public static Vec3 cloudColor(NamekianDreamsConfig config) {
        return color(config.cloudColor());
    }

    public static Vec3 waterFogColor(NamekianDreamsConfig config) {
        return color(config.waterFogColor());
    }

    public static Vec3 color(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return new Vec3(((rgb >> 16) & 255) / 255.0, ((rgb >> 8) & 255) / 255.0, (rgb & 255) / 255.0);
    }
}
