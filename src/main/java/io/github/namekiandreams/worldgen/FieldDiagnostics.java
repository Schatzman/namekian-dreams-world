package io.github.namekiandreams.worldgen;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import java.util.EnumSet;

public final class FieldDiagnostics {
    private FieldDiagnostics() {}
    public static void main(String[] args) {
        DiagnosticResult result = run(NamekianDreamsConfig.defaults(), 8675309L);
        System.out.println(result.toReport());
        if (!result.acceptancePassed()) throw new IllegalStateException("Field diagnostic did not observe every required generated-column signal");
    }
    public static DiagnosticResult run(NamekianDreamsConfig config, long seed) {
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
        EnumSet<NamekianDensitySampler.TerrainRegime> regimes = EnumSet.noneOf(NamekianDensitySampler.TerrainRegime.class);
        boolean cavePresent = false, fractalPresent = false;
        double minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY;
        ColumnCoordinate water = null, mountain = null, extreme = null;
        CaveCoordinate cave = null;
        int lowColumns = 0, midColumns = 0, highColumns = 0;
        for (int x = -6144; x <= 6144; x += 128) {
            for (int z = -6144; z <= 6144; z += 128) {
                NamekianDensitySampler.DensitySample columnSample = sampler.sample(x, config.seaLevel(), z);
                regimes.add(columnSample.regime());
                minHeight = Math.min(minHeight, columnSample.targetHeight());
                maxHeight = Math.max(maxHeight, columnSample.targetHeight());
                int surfaceY = sampler.surfaceHeight(x, z);
                if (surfaceY < config.seaLevel() - 2) {
                    lowColumns++;
                    if (water == null) water = new ColumnCoordinate(x, config.seaLevel(), z, surfaceY);
                } else if (surfaceY > 320) {
                    highColumns++;
                    if (mountain == null) mountain = new ColumnCoordinate(x, surfaceY + 2, z, surfaceY);
                } else {
                    midColumns++;
                }
                if (columnSample.regime() == NamekianDensitySampler.TerrainRegime.EXTREME && surfaceY > 320 && extreme == null) {
                    extreme = new ColumnCoordinate(x, surfaceY + 2, z, surfaceY);
                }
                for (int y = config.minY() + 16; y <= Math.min(config.maxY(), 768); y += 32) {
                    NamekianDensitySampler.DensitySample sample = sampler.sample(x, y, z);
                    regimes.add(sample.regime());
                    cavePresent |= sampler.isCaveAir(x, y, z);
                    fractalPresent |= Math.abs(sample.fractalContribution()) > 0.025;
                    if (cave == null && sampler.isCaveAir(x, y, z)) cave = new CaveCoordinate(x, y, z, surfaceY);
                }
            }
        }
        return new DiagnosticResult(config.minY(), config.height(), config.maxY(), regimes, cavePresent, fractalPresent,
                minHeight, maxHeight, water, mountain, extreme, cave, lowColumns, midColumns, highColumns);
    }
    public record ColumnCoordinate(int x, int y, int z, int surfaceY) {
        public String command() { return "/tp @p " + x + " " + y + " " + z + " (surface_y=" + surfaceY + ")"; }
    }
    public record CaveCoordinate(int x, int y, int z, int surfaceY) {
        public String command() { return "/tp @p " + x + " " + y + " " + z + " (surface_y=" + surfaceY + ")"; }
    }
    public record DiagnosticResult(int minY, int height, int maxY, EnumSet<NamekianDensitySampler.TerrainRegime> regimes,
                                   boolean cavePresent, boolean fractalPresent, double minObservedTargetHeight,
                                   double maxObservedTargetHeight, ColumnCoordinate waterCoordinate,
                                   ColumnCoordinate mountainCoordinate, ColumnCoordinate extremeCoordinate,
                                   CaveCoordinate caveCoordinate, int lowColumns, int midColumns, int highColumns) {
        public boolean acceptancePassed() {
            return minY == -304 && height == 1328 && maxY == 1023
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.NORMAL)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.AMPLIFIED)
                    && regimes.contains(NamekianDensitySampler.TerrainRegime.EXTREME)
                    && cavePresent && fractalPresent
                    && waterCoordinate != null && mountainCoordinate != null && extremeCoordinate != null && caveCoordinate != null
                    && lowColumns > 0 && midColumns > 0 && highColumns > 0
                    && Math.round(maxObservedTargetHeight - minObservedTargetHeight) >= 260;
        }
        public String toReport() {
            return "Namekian Dreams diagnostic: min_y=" + minY + ", height=" + height + ", max_y=" + maxY
                    + ", regimes=" + regimes + ", cave_present=" + cavePresent + ", fractal_present=" + fractalPresent
                    + ", observed_target_height_range=" + Math.round(minObservedTargetHeight) + ".." + Math.round(maxObservedTargetHeight)
                    + ", low/mid/high_columns=" + lowColumns + "/" + midColumns + "/" + highColumns
                    + ", water=" + format(waterCoordinate)
                    + ", mountain=" + format(mountainCoordinate)
                    + ", extreme=" + format(extremeCoordinate)
                    + ", cave=" + format(caveCoordinate);
        }
        private static String format(ColumnCoordinate coordinate) {
            return coordinate == null ? "none" : coordinate.command();
        }
        private static String format(CaveCoordinate coordinate) {
            return coordinate == null ? "none" : coordinate.command();
        }
    }
}
