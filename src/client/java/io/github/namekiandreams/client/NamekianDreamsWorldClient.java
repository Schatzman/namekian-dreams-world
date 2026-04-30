package io.github.namekiandreams.client;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.worldgen.light.NamekianDaylightOffset;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public final class NamekianDreamsWorldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DimensionRenderingRegistry.registerDimensionEffects(NamekianDreamsWorld.id("namekian_overworld"), new NamekianDimensionEffects());
    }

    private static final class NamekianDimensionEffects extends DimensionSpecialEffects {
        private NamekianDimensionEffects() {
            super(192.0F, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 baseFogColor, float sunHeight) {
            NamekianDreamsConfig config = NamekianDreamsWorld.CONFIG;
            Vec3 fog = color(config.fogColor());
            double dim = NamekianDaylightOffset.visualDaylightMultiplier(config);
            double brightness = 0.55 + Math.max(0.0F, sunHeight) * 0.45;
            return fog.scale(dim * brightness);
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        private static Vec3 color(String hex) {
            int rgb = Integer.parseInt(hex.substring(1), 16);
            return new Vec3(((rgb >> 16) & 255) / 255.0, ((rgb >> 8) & 255) / 255.0, (rgb & 255) / 255.0);
        }
    }
}
