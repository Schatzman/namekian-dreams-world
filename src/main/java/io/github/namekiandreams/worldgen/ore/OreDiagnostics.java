package io.github.namekiandreams.worldgen.ore;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.worldgen.NamekianChunkGenerator;
import io.github.namekiandreams.worldgen.NamekianDensitySampler;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OreDiagnostics {
    private OreDiagnostics() {}

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        DiagnosticResult result = run(NamekianDreamsConfig.defaults(), 8675309L);
        System.out.println(result.toReport());
        if (!result.acceptancePassed()) throw new IllegalStateException("Ore diagnostic did not observe required generated ore block outcomes");
    }

    public static DiagnosticResult run(NamekianDreamsConfig config, long seed) {
        NamekianDensitySampler terrain = new NamekianDensitySampler(config, seed);
        NamekianOreSampler oreSampler = new NamekianOreSampler(config, seed);
        Coordinate deepOre = null;
        Coordinate highOre = null;
        Coordinate megaveinOre = null;
        int oreCount = 0;
        int generatedSolidSamples = 0;
        int lodeOreCount = 0;
        NamekianOreSampler.MegaveinLode lode = null;
        for (int x = -4096; x <= 4096; x += 64) {
            for (int z = -4096; z <= 4096; z += 64) {
                for (int y = config.minY() + 8; y <= config.maxY(); y += 37) {
                    if (!terrain.isSolid(x, y, z) || terrain.isCaveAir(x, y, z)) continue;
                    generatedSolidSamples++;
                    BlockState state = stateAt(config, seed, x, y, z);
                    if (!isOre(state)) continue;
                    oreCount++;
                    if (y < -128 && deepOre == null) {
                        deepOre = coordinate(x, y, z, state, "deep_generated_ore solid=true surface_y=" + terrain.surfaceHeight(x, z));
                    }
                    if (y > 320 && highOre == null) {
                        highOre = coordinate(x, y, z, state, "high_generated_ore solid=true surface_y=" + terrain.surfaceHeight(x, z));
                    }
                    if (oreSampler.isMegaveinCandidate(x, y, z)) {
                        int cluster = lodeOreClusterSize(config, seed, terrain, oreSampler, x, y, z);
                        if (cluster > lodeOreCount) {
                            lodeOreCount = cluster;
                            lode = oreSampler.megaveinLodeAt(x, y, z);
                            megaveinOre = coordinate(x, y, z, state, "visible_megavein_lode cluster_ore_blocks=" + cluster + " " + (lode == null ? "lode=none" : lode.summary()));
                        }
                    }
                }
            }
        }
        return new DiagnosticResult(generatedSolidSamples, oreCount, deepOre, highOre, megaveinOre, lodeOreCount, lode);
    }

    private static int lodeOreClusterSize(NamekianDreamsConfig config, long seed, NamekianDensitySampler terrain, NamekianOreSampler oreSampler, int centerX, int centerY, int centerZ) {
        int count = 0;
        for (int dx = -8; dx <= 8; dx += 2) {
            for (int dy = -8; dy <= 8; dy += 2) {
                for (int dz = -8; dz <= 8; dz += 2) {
                    int x = centerX + dx, y = centerY + dy, z = centerZ + dz;
                    if (terrain.isSolid(x, y, z) && !terrain.isCaveAir(x, y, z) && oreSampler.isMegaveinCandidate(x, y, z) && isOre(stateAt(config, seed, x, y, z))) count++;
                }
            }
        }
        return count;
    }

    private static BlockState stateAt(NamekianDreamsConfig config, long seed, int x, int y, int z) {
        return NamekianChunkGenerator.diagnosticStateFor(config, seed, x, y, z);
    }

    private static boolean isOre(BlockState state) {
        return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)
                || state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
                || state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
                || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    private static Coordinate coordinate(int x, int y, int z, BlockState state, String signal) {
        return new Coordinate(x, y, z, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), signal);
    }

    public record Coordinate(int x, int y, int z, String block, String signal) {
        public String command() { return "/tp @p " + x + " " + y + " " + z + " (block=" + block + ", " + signal + ")"; }
    }

    public record DiagnosticResult(int generatedSolidSamples, int oreCount, Coordinate deepOreCoordinate,
                                   Coordinate highOreCoordinate, Coordinate megaveinCoordinate, int megaveinClusterOreCount,
                                   NamekianOreSampler.MegaveinLode megaveinLode) {
        public boolean belowVanillaBedrock() { return deepOreCoordinate != null; }
        public boolean aboveVanillaBuildHeight() { return highOreCoordinate != null; }
        public boolean visibleMegaveinPresent() { return megaveinCoordinate != null && megaveinClusterOreCount >= 10; }
        public boolean acceptancePassed() {
            return generatedSolidSamples > 0 && oreCount > 0 && belowVanillaBedrock() && aboveVanillaBuildHeight() && visibleMegaveinPresent();
        }
        public String toReport() {
            return "Namekian Dreams ore diagnostic: generated_solid_samples=" + generatedSolidSamples + ", ore_count=" + oreCount
                    + ", deep_ore=" + format(deepOreCoordinate)
                    + ", high_ore=" + format(highOreCoordinate)
                    + ", visible_megavein_lode=" + format(megaveinCoordinate)
                    + ", megavein_cluster_ore_count=" + megaveinClusterOreCount
                    + ", megavein_lode=" + (megaveinLode == null ? "none" : megaveinLode.summary());
        }
        private static String format(Coordinate coordinate) {
            return coordinate == null ? "none" : coordinate.command();
        }
    }
}
