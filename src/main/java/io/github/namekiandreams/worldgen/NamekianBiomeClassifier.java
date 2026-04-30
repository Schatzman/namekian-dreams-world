package io.github.namekiandreams.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class NamekianBiomeClassifier {
    private NamekianBiomeClassifier() {}

    public static ResourceKey<Biome> selectBiomeKey(int x, int y, int z) {
        double temperature = FieldMath.valueNoise2D(0x4E414D4542494F31L, x + 900.0, z - 400.0, 0.00095);
        double humidity = FieldMath.valueNoise2D(0x4E414D4542494F32L, x - 300.0, z + 1100.0, 0.00105);
        double weirdness = FieldMath.valueNoise2D(0x4E414D4542494F33L, x + 1700.0, z + 700.0, 0.00135);
        double rare = FieldMath.valueNoise2D(0x4E414D4542494F34L, x - 2500.0, z + 2400.0, 0.00068) * 0.5 + 0.5;
        double forest = FieldMath.valueNoise2D(0x4E414D4542494F35L, x + 410.0, z - 960.0, 0.00155);
        double coast = FieldMath.valueNoise2D(0x4E414D4542494F36L, x - 1200.0, z - 2200.0, 0.00055);

        if (y < -96 && weirdness > 0.34) return Biomes.DEEP_DARK;
        if (y < -64 && humidity > 0.30) return Biomes.LUSH_CAVES;
        if (y < -48 && weirdness < -0.34) return Biomes.DRIPSTONE_CAVES;
        if (coast < -0.72) return temperature < -0.34 ? Biomes.DEEP_FROZEN_OCEAN : humidity > 0.26 ? Biomes.DEEP_LUKEWARM_OCEAN : Biomes.DEEP_OCEAN;
        if (coast < -0.50) return temperature < -0.34 ? Biomes.FROZEN_OCEAN : humidity > 0.30 ? Biomes.LUKEWARM_OCEAN : Biomes.OCEAN;
        if (rare > 0.975) {
            int netherChoice = selectWeightedIndex(x, y, z, new int[] { 1, 1, 1 });
            return netherChoice == 0 ? Biomes.CRIMSON_FOREST : netherChoice == 1 ? Biomes.WARPED_FOREST : Biomes.SOUL_SAND_VALLEY;
        }
        if (rare > 0.945 && humidity > 0.02) return Biomes.MUSHROOM_FIELDS;
        if (temperature < -0.54) return weirdness > 0.30 ? Biomes.ICE_SPIKES : Biomes.SNOWY_PLAINS;
        if (temperature < -0.36) return humidity > 0.12 ? Biomes.SNOWY_TAIGA : Biomes.GROVE;
        if (humidity > 0.44 && temperature > 0.06) return weirdness > 0.25 ? Biomes.BAMBOO_JUNGLE : Biomes.JUNGLE;
        if (humidity > 0.28 && temperature > -0.04) return Biomes.LUSH_CAVES;
        if (forest > 0.16 && humidity > -0.22) return Biomes.DARK_FOREST;
        if (forest < -0.62 && humidity > -0.10 && humidity < 0.32) return Biomes.BIRCH_FOREST;
        if (temperature > 0.48 && humidity < -0.35) return Biomes.DESERT;
        if (temperature > 0.30 && humidity < -0.15) return weirdness > 0.25 ? Biomes.BADLANDS : Biomes.SAVANNA;
        if (weirdness > 0.48) return Biomes.WINDSWEPT_GRAVELLY_HILLS;
        if (humidity > 0.16) return forest > -0.18 ? Biomes.FOREST : Biomes.PLAINS;
        return Biomes.PLAINS;
    }

    public static NamekianDensitySampler.BiomeRegion regionForBiomeKey(ResourceKey<Biome> key) {
        if (key == Biomes.DEEP_OCEAN || key == Biomes.DEEP_LUKEWARM_OCEAN || key == Biomes.DEEP_COLD_OCEAN || key == Biomes.DEEP_FROZEN_OCEAN) return NamekianDensitySampler.BiomeRegion.DEEP_OCEAN;
        if (key == Biomes.OCEAN || key == Biomes.LUKEWARM_OCEAN || key == Biomes.WARM_OCEAN || key == Biomes.COLD_OCEAN || key == Biomes.FROZEN_OCEAN) return NamekianDensitySampler.BiomeRegion.OCEAN;
        if (key == Biomes.FROZEN_PEAKS || key == Biomes.JAGGED_PEAKS || key == Biomes.SNOWY_SLOPES || key == Biomes.GROVE) return NamekianDensitySampler.BiomeRegion.FROZEN_PEAKS;
        if (key == Biomes.SNOWY_PLAINS || key == Biomes.SNOWY_TAIGA || key == Biomes.ICE_SPIKES || key == Biomes.FROZEN_RIVER) return NamekianDensitySampler.BiomeRegion.FROZEN;
        if (key == Biomes.JUNGLE || key == Biomes.SPARSE_JUNGLE || key == Biomes.BAMBOO_JUNGLE || key == Biomes.LUSH_CAVES) return NamekianDensitySampler.BiomeRegion.JUNGLE_LUSH;
        if (key == Biomes.DRIPSTONE_CAVES) return NamekianDensitySampler.BiomeRegion.DRIPSTONE_CAVES;
        if (key == Biomes.DEEP_DARK) return NamekianDensitySampler.BiomeRegion.DEEP_DARK;
        if (key == Biomes.WINDSWEPT_GRAVELLY_HILLS || key == Biomes.STONY_SHORE) return NamekianDensitySampler.BiomeRegion.GRAVELLY;
        if (key == Biomes.DARK_FOREST) return NamekianDensitySampler.BiomeRegion.DARK_FOREST;
        if (key == Biomes.MUSHROOM_FIELDS) return NamekianDensitySampler.BiomeRegion.MUSHROOM_FIELDS;
        if (key == Biomes.BIRCH_FOREST || key == Biomes.OLD_GROWTH_BIRCH_FOREST) return NamekianDensitySampler.BiomeRegion.BIRCH_FOREST;
        if (key == Biomes.CRIMSON_FOREST) return NamekianDensitySampler.BiomeRegion.CRIMSON_FOREST;
        if (key == Biomes.WARPED_FOREST) return NamekianDensitySampler.BiomeRegion.WARPED_FOREST;
        if (key == Biomes.SOUL_SAND_VALLEY) return NamekianDensitySampler.BiomeRegion.SOUL_SAND_VALLEY;
        return NamekianDensitySampler.BiomeRegion.TEMPERATE;
    }

    public static int selectWeightedIndex(int x, int y, int z, int[] weights) {
        int total = 0;
        for (int weight : weights) if (weight > 0) total += weight;
        if (total <= 0) return 0;
        long hash = 0x9E3779B97F4A7C15L;
        hash ^= x * 0xBF58476D1CE4E5B9L;
        hash ^= y * 0x94D049BB133111EBL;
        hash ^= z * 0xD6E8FEB86659FD93L;
        hash ^= hash >>> 30; hash *= 0xBF58476D1CE4E5B9L;
        hash ^= hash >>> 27; hash *= 0x94D049BB133111EBL;
        hash ^= hash >>> 31;
        int target = (int) Long.remainderUnsigned(hash, total);
        int cursor = 0;
        for (int i = 0; i < weights.length; i++) {
            int weight = Math.max(0, weights[i]);
            if (target < cursor + weight) return i;
            cursor += weight;
        }
        return weights.length - 1;
    }
}
