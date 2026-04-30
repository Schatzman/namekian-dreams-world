package io.github.namekiandreams.worldgen.ore;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import net.minecraft.SharedConstants;
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
        if (!result.acceptancePassed()) throw new IllegalStateException("Ore diagnostic did not observe required ore signals");
    }

    public static DiagnosticResult run(NamekianDreamsConfig config, long seed) {
        NamekianOreSampler sampler = new NamekianOreSampler(config, seed);
        boolean belowVanillaBedrock = false;
        boolean aboveVanillaBuildHeight = false;
        boolean boostedOrePresent = false;
        boolean megaveinCandidate = false;
        int oreCount = 0;
        int samples = 0;
        for (int x = -2048; x <= 2048; x += 37) {
            for (int z = -2048; z <= 2048; z += 41) {
                for (int y = config.minY() + 8; y <= config.maxY(); y += 31) {
                    BlockState base = y < -64 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                    BlockState replacement = sampler.replaceIfOre(base, x, y, z);
                    boolean ore = replacement != base;
                    samples++;
                    if (ore) {
                        oreCount++;
                        belowVanillaBedrock |= y < -64;
                        aboveVanillaBuildHeight |= y > 320;
                        boostedOrePresent = true;
                    }
                    megaveinCandidate |= sampler.isMegaveinCandidate(x, y, z);
                }
            }
        }
        return new DiagnosticResult(samples, oreCount, belowVanillaBedrock, aboveVanillaBuildHeight, boostedOrePresent, megaveinCandidate);
    }

    public record DiagnosticResult(int samples, int oreCount, boolean belowVanillaBedrock, boolean aboveVanillaBuildHeight,
                                   boolean boostedOrePresent, boolean megaveinCandidatePresent) {
        public boolean acceptancePassed() {
            return samples > 0 && oreCount > 0 && belowVanillaBedrock && aboveVanillaBuildHeight && boostedOrePresent && megaveinCandidatePresent;
        }
        public String toReport() {
            return "Namekian Dreams ore diagnostic: samples=" + samples + ", ore_count=" + oreCount
                    + ", below_vanilla_bedrock=" + belowVanillaBedrock
                    + ", above_vanilla_build_height=" + aboveVanillaBuildHeight
                    + ", boosted_ore_present=" + boostedOrePresent
                    + ", megavein_candidate_present=" + megaveinCandidatePresent;
        }
    }
}
