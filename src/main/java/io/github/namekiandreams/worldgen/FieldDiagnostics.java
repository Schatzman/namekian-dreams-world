package io.github.namekiandreams.worldgen;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.worldgen.light.NamekianPackedLight;
import java.util.EnumSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FieldDiagnostics {
    private static boolean bootstrapped = false;

    private FieldDiagnostics() {}

    public static void main(String[] args) {
        DiagnosticResult result = run(NamekianDreamsConfig.defaults(), 8675309L);
        System.out.println(result.toReport());
        if (!result.acceptancePassed()) throw new IllegalStateException("Field diagnostic did not observe every required generated block outcome");
    }

    public static DiagnosticResult run(NamekianDreamsConfig config, long seed) {
        bootstrapMinecraft();
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
        EnumSet<NamekianDensitySampler.TerrainRegime> regimes = EnumSet.noneOf(NamekianDensitySampler.TerrainRegime.class);
        boolean cavePresent = false, fractalPresent = false;
        double minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY;
        Coordinate ocean = null, deepOceanFloor = null, abyssalFloor = null, mountain = null, cave = null, lavaLake = null;
        Coordinate frozen = null, jungleLush = null, dripstone = null, deepDark = null, gravel = null;
        Coordinate darkForest = null, mushroom = null, birch = null, crimson = null, warped = null, soulSand = null;
        int lowColumns = 0, midColumns = 0, highColumns = 0;
        int sampledColumns = 0;
        int clientLightBefore = NamekianPackedLight.pack(9, 15);
        int clientLightAfter = NamekianPackedLight.adjustPackedSkyLight(clientLightBefore, config);
        for (int x = -8192; x <= 8192; x += 256) {
            for (int z = -8192; z <= 8192; z += 256) {
                sampledColumns++;
                int surfaceY = sampler.surfaceHeight(x, z);
                NamekianDensitySampler.DensitySample columnSample = sampler.sample(x, config.seaLevel(), z);
                regimes.add(columnSample.regime());
                minHeight = Math.min(minHeight, columnSample.targetHeight());
                maxHeight = Math.max(maxHeight, columnSample.targetHeight());
                if (surfaceY < config.seaLevel() - 8) {
                    lowColumns++;
                    if (ocean == null && largeWaterBodyScore(config, seed, sampler, x, z) >= 30) {
                        ocean = coordinate(x, config.seaLevel(), z, stateAt(config, seed, x, config.seaLevel(), z), "large_ocean_water_body surface_y=" + surfaceY + " water_columns=" + largeWaterBodyScore(config, seed, sampler, x, z));
                    }
                } else if (surfaceY > 430) {
                    highColumns++;
                    if (mountain == null) mountain = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "high_mountain_peak surface_y=" + surfaceY);
                } else {
                    midColumns++;
                }
                if (surfaceY <= -200 && deepOceanFloor == null) {
                    deepOceanFloor = coordinate(x, surfaceY + 1, z, stateAt(config, seed, x, surfaceY, z), "deep_ocean_floor surface_y=" + surfaceY);
                }
                if (surfaceY <= config.minY() + 36 && abyssalFloor == null) {
                    abyssalFloor = coordinate(x, surfaceY + 1, z, stateAt(config, seed, x, surfaceY, z), "abyssal_floor_near_bottom surface_y=" + surfaceY);
                }
                NamekianDensitySampler.BiomeRegion surfaceRegion = sampler.biomeRegion(x, surfaceY, z);
                if ((surfaceRegion == NamekianDensitySampler.BiomeRegion.FROZEN || surfaceRegion == NamekianDensitySampler.BiomeRegion.FROZEN_PEAKS) && frozen == null) {
                    frozen = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "frozen_or_ice_region region=" + surfaceRegion + " surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.JUNGLE_LUSH && jungleLush == null) {
                    jungleLush = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "jungle_lush_surface region=" + surfaceRegion + " surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.GRAVELLY && gravel == null) {
                    gravel = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "gravelly_surface region=" + surfaceRegion + " surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.DARK_FOREST && darkForest == null) {
                    darkForest = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "dark_forest_surface block_palette=podzol surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.MUSHROOM_FIELDS && mushroom == null) {
                    mushroom = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "mushroom_fields_surface block_palette=mycelium surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.BIRCH_FOREST && birch == null) {
                    birch = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "birch_forest_surface surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.CRIMSON_FOREST && crimson == null) {
                    crimson = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "crimson_forest_surface block_palette=crimson_nylium surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.WARPED_FOREST && warped == null) {
                    warped = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "warped_forest_surface block_palette=warped_nylium surface_y=" + surfaceY);
                }
                if (surfaceRegion == NamekianDensitySampler.BiomeRegion.SOUL_SAND_VALLEY && soulSand == null) {
                    soulSand = coordinate(x, surfaceY + 2, z, stateAt(config, seed, x, surfaceY, z), "soul_sand_valley_surface block_palette=soul_sand surface_y=" + surfaceY);
                }
                for (int y = config.minY() + 16; y <= Math.min(config.maxY(), 768); y += 32) {
                    NamekianDensitySampler.DensitySample sample = sampler.sample(x, y, z);
                    regimes.add(sample.regime());
                    cavePresent |= sampler.isCaveAir(x, y, z);
                    fractalPresent |= Math.abs(sample.fractalContribution()) > 0.025;
                    BlockState state = stateAt(config, seed, x, y, z);
                    if (lavaLake == null && state.is(Blocks.LAVA) && lavaLakeScore(config, seed, sampler, x, y, z) >= 6) {
                        lavaLake = coordinate(x, y, z, state, "large_lava_lake lava_blocks=" + lavaLakeScore(config, seed, sampler, x, y, z));
                    }
                    if (cave == null && state.isAir() && sampler.isCaveAir(x, y, z)) {
                        cave = coordinate(x, y, z, state, "actual_cave_air surface_y=" + surfaceY);
                    }
                    if (dripstone == null && (state.is(Blocks.DRIPSTONE_BLOCK) || state.is(Blocks.CALCITE))) {
                        dripstone = coordinate(x, y, z, state, "dripstone_style_region region=" + sampler.biomeRegion(x, y, z));
                    }
                    if (deepDark == null && (state.is(Blocks.SCULK) || state.is(Blocks.TUFF))) {
                        deepDark = coordinate(x, y, z, state, "deep_dark_style_region region=" + sampler.biomeRegion(x, y, z));
                    }
                }
            }
        }
        return new DiagnosticResult(config.minY(), config.height(), config.maxY(), regimes, cavePresent, fractalPresent,
                minHeight, maxHeight, sampledColumns, lowColumns, midColumns, highColumns,
                NamekianPackedLight.sky(clientLightAfter), NamekianPackedLight.block(clientLightAfter), ocean, deepOceanFloor, abyssalFloor,
                mountain, cave, lavaLake, frozen, jungleLush, dripstone, deepDark, gravel, darkForest, mushroom, birch, crimson, warped, soulSand);
    }

    private static void bootstrapMinecraft() {
        if (!bootstrapped) {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            bootstrapped = true;
        }
    }

    private static int largeWaterBodyScore(NamekianDreamsConfig config, long seed, NamekianDensitySampler sampler, int centerX, int centerZ) {
        int waterColumns = 0;
        for (int dx = -48; dx <= 48; dx += 16) {
            for (int dz = -48; dz <= 48; dz += 16) {
                int x = centerX + dx;
                int z = centerZ + dz;
                if (sampler.surfaceHeight(x, z) < config.seaLevel() - 8 && stateAt(config, seed, x, config.seaLevel(), z).is(Blocks.WATER)) waterColumns++;
            }
        }
        return waterColumns;
    }

    private static int lavaLakeScore(NamekianDreamsConfig config, long seed, NamekianDensitySampler sampler, int centerX, int centerY, int centerZ) {
        int lavaBlocks = 0;
        for (int dx = -16; dx <= 16; dx += 8) {
            for (int dy = -4; dy <= 4; dy += 4) {
                for (int dz = -16; dz <= 16; dz += 8) {
                    int x = centerX + dx;
                    int y = centerY + dy;
                    int z = centerZ + dz;
                    if (sampler.isLavaLake(x, y, z) && stateAt(config, seed, x, y, z).is(Blocks.LAVA)) lavaBlocks++;
                }
            }
        }
        return lavaBlocks;
    }

    private static BlockState stateAt(NamekianDreamsConfig config, long seed, int x, int y, int z) {
        return NamekianChunkGenerator.diagnosticStateFor(config, seed, x, y, z);
    }

    private static Coordinate coordinate(int x, int y, int z, BlockState state, String signal) {
        return new Coordinate(x, y, z, blockId(state), signal);
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    public record Coordinate(int x, int y, int z, String block, String signal) {
        public String command() { return "/tp @p " + x + " " + y + " " + z + " (block=" + block + ", " + signal + ")"; }
    }

    public record DiagnosticResult(int minY, int height, int maxY, EnumSet<NamekianDensitySampler.TerrainRegime> regimes,
                                   boolean cavePresent, boolean fractalPresent, double minObservedTargetHeight,
                                   double maxObservedTargetHeight, int sampledColumns, int lowColumns, int midColumns, int highColumns,
                                   int clientSkyLightAfterDimmer, int clientBlockLightAfterDimmer,
                                   Coordinate oceanCoordinate, Coordinate deepOceanFloorCoordinate, Coordinate abyssalFloorCoordinate,
                                   Coordinate mountainCoordinate, Coordinate caveCoordinate, Coordinate lavaLakeCoordinate,
                                   Coordinate frozenCoordinate, Coordinate jungleLushCoordinate, Coordinate dripstoneCoordinate,
                                   Coordinate deepDarkCoordinate, Coordinate gravelCoordinate, Coordinate darkForestCoordinate,
                                   Coordinate mushroomCoordinate, Coordinate birchCoordinate, Coordinate crimsonForestCoordinate,
                                   Coordinate warpedForestCoordinate, Coordinate soulSandValleyCoordinate) {
        public boolean acceptancePassed() {
            return minY == -304 && height == 1328 && maxY == 1023
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.NORMAL)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.AMPLIFIED)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.EXTREME)
                    && cavePresent && fractalPresent
                    && clientSkyLightAfterDimmer == 10 && clientBlockLightAfterDimmer == 9
                    && oceanCoordinate != null && deepOceanFloorCoordinate != null && mountainCoordinate != null && caveCoordinate != null
                    && lavaLakeCoordinate != null && frozenCoordinate != null && jungleLushCoordinate != null
                    && dripstoneCoordinate != null && deepDarkCoordinate != null && gravelCoordinate != null
                    && darkForestCoordinate != null && mushroomCoordinate != null && birchCoordinate != null
                    && crimsonForestCoordinate != null && warpedForestCoordinate != null && soulSandValleyCoordinate != null
                    && lowColumns > 0 && midColumns > 0 && highColumns > 0
                    && Math.round(maxObservedTargetHeight - minObservedTargetHeight) >= 320;
        }

        public String toReport() {
            return "Namekian Dreams diagnostic: min_y=" + minY + ", height=" + height + ", max_y=" + maxY
                    + ", regimes=" + regimes + ", cave_present=" + cavePresent + ", fractal_present=" + fractalPresent
                    + ", client_light_dimmer=sky15_to_" + clientSkyLightAfterDimmer + " block_preserved=" + clientBlockLightAfterDimmer
                    + ", observed_target_height_range=" + Math.round(minObservedTargetHeight) + ".." + Math.round(maxObservedTargetHeight)
                    + ", sampled_columns=" + sampledColumns + ", low/mid/high_columns=" + lowColumns + "/" + midColumns + "/" + highColumns
                    + ", ocean=" + format(oceanCoordinate)
                    + ", deep_ocean_floor=" + format(deepOceanFloorCoordinate)
                    + ", abyssal_floor=" + format(abyssalFloorCoordinate)
                    + ", mountain=" + format(mountainCoordinate)
                    + ", cave=" + format(caveCoordinate)
                    + ", lava_lake=" + format(lavaLakeCoordinate)
                    + ", frozen=" + format(frozenCoordinate)
                    + ", jungle_lush=" + format(jungleLushCoordinate)
                    + ", dripstone=" + format(dripstoneCoordinate)
                    + ", deep_dark=" + format(deepDarkCoordinate)
                    + ", gravel=" + format(gravelCoordinate)
                    + ", dark_forest=" + format(darkForestCoordinate)
                    + ", mushroom_fields=" + format(mushroomCoordinate)
                    + ", birch_forest=" + format(birchCoordinate)
                    + ", crimson_forest=" + format(crimsonForestCoordinate)
                    + ", warped_forest=" + format(warpedForestCoordinate)
                    + ", soul_sand_valley=" + format(soulSandValleyCoordinate);
        }

        private static String format(Coordinate coordinate) {
            return coordinate == null ? "none" : coordinate.command();
        }
    }
}
