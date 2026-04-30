package io.github.namekiandreams.worldgen;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import java.util.EnumSet;

public final class FieldDiagnostics {
    private FieldDiagnostics() {}
    public static void main(String[] args) {
        DiagnosticResult result = run(NamekianDreamsConfig.defaults(), 8675309L);
        System.out.println(result.toReport());
        if (!result.acceptancePassed()) throw new IllegalStateException("Field diagnostic did not observe every required terrain signal");
    }
    public static DiagnosticResult run(NamekianDreamsConfig config, long seed) {
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
        EnumSet<NamekianDensitySampler.TerrainRegime> regimes = EnumSet.noneOf(NamekianDensitySampler.TerrainRegime.class);
        boolean cavePresent = false, fractalPresent = false;
        double minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY;
        for (int x = -4096; x <= 4096; x += 256) {
            for (int z = -4096; z <= 4096; z += 256) {
                for (int y = config.minY() + 16; y <= Math.min(config.maxY(), 768); y += 96) {
                    NamekianDensitySampler.DensitySample sample = sampler.sample(x, y, z);
                    regimes.add(sample.regime());
                    cavePresent |= sample.caveSignal() < -0.18;
                    fractalPresent |= Math.abs(sample.fractalContribution()) > 0.025;
                    minHeight = Math.min(minHeight, sample.targetHeight());
                    maxHeight = Math.max(maxHeight, sample.targetHeight());
                }
            }
        }
        return new DiagnosticResult(config.minY(), config.height(), config.maxY(), regimes, cavePresent, fractalPresent, minHeight, maxHeight);
    }
    public record DiagnosticResult(int minY, int height, int maxY, EnumSet<NamekianDensitySampler.TerrainRegime> regimes,
                                   boolean cavePresent, boolean fractalPresent, double minObservedTargetHeight, double maxObservedTargetHeight) {
        public boolean acceptancePassed() {
            return minY == -304 && height == 1328 && maxY == 1023
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.NORMAL)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.AMPLIFIED)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.EXTREME)
                    && cavePresent && fractalPresent;
        }
        public String toReport() {
            return "Namekian Dreams diagnostic: min_y=" + minY + ", height=" + height + ", max_y=" + maxY
                    + ", regimes=" + regimes + ", cave_present=" + cavePresent + ", fractal_present=" + fractalPresent
                    + ", observed_target_height_range=" + Math.round(minObservedTargetHeight) + ".." + Math.round(maxObservedTargetHeight);
        }
    }
}
