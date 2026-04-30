package io.github.namekiandreams.worldgen.ore;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.worldgen.FieldMath;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NamekianOreSampler {
    private static final long ORE_SALT = 0x4F52455F4E414D45L;
    private static final int MEGAVEIN_CELL = 128;

    private final NamekianOreConfig oreConfig;
    private final NamekianDreamsConfig config;
    private final long seed;

    public NamekianOreSampler(NamekianDreamsConfig config, long seed) {
        this.config = config.validate();
        this.oreConfig = NamekianOreConfig.defaults(config);
        this.seed = seed ^ ORE_SALT;
    }

    public BlockState replaceIfOre(BlockState base, int x, int y, int z) {
        if (!isEligibleBase(base) || !isAllowedHeight(y)) return base;
        Optional<BlockState> megavein = megaveinOre(base, x, y, z);
        if (megavein.isPresent()) return megavein.get();
        NamekianOreConfig.OreBand best = null;
        double bestScore = 0.0;
        for (NamekianOreConfig.OreBand band : oreConfig.bands()) {
            if (!band.includesY(y) || !bandAllowedAtHeight(band, y)) continue;
            double chance = band.baseChance() * config.globalOreMultiplier() * distributionWeight(band, x, y, z);
            double roll = normalized(seed + band.name().hashCode() * 131L, x, y, z);
            double veinNoise = FieldMath.valueNoise3D(seed + band.name().hashCode() * 911L, x, y, z, 0.075 / config.globalVeinSizeMultiplier());
            double score = chance * (0.72 + Math.max(0.0, veinNoise) * 1.85);
            if (roll < score && score > bestScore) {
                best = band;
                bestScore = score;
            }
        }
        return best == null ? base : best.oreForBase(base);
    }

    public boolean isMegaveinCandidate(int x, int y, int z) {
        return config.enableMegaveins() && megaveinDensity(x, y, z) > 0.86;
    }

    public double distributionWeight(NamekianOreConfig.OreBand band, int x, int y, int z) {
        double t = (double) (y - band.minY()) / Math.max(1.0, band.maxY() - band.minY());
        t = FieldMath.clamp(t, 0.0, 1.0);
        return switch (band.distribution()) {
            case UNIFORM -> 1.0;
            case TRIANGULAR -> 0.25 + (1.0 - Math.abs(t * 2.0 - 1.0)) * 1.35;
            case DEEP_BIAS -> 0.35 + Math.pow(1.0 - t, 1.8) * 2.65;
            case MOUNTAIN_BIAS -> 0.25 + FieldMath.smoothstep(80.0, 760.0, y) * 2.50;
            case CAVE_BIAS -> 0.35 + Math.abs(FieldMath.valueNoise3D(seed + 707L, x, y, z, 0.018)) * 2.30;
            case MEGAVEIN -> isMegaveinCandidate(x, y, z) ? 6.0 : 0.0;
        };
    }

    private Optional<BlockState> megaveinOre(BlockState base, int x, int y, int z) {
        if (!config.enableMegaveins() || !isMegaveinCandidate(x, y, z)) return Optional.empty();
        if (y < -96) return Optional.of(base.is(Blocks.DEEPSLATE) ? Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState() : Blocks.DIAMOND_ORE.defaultBlockState());
        if (y < 48) return Optional.of(base.is(Blocks.DEEPSLATE) ? Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState() : Blocks.GOLD_ORE.defaultBlockState());
        if (y > 220) return Optional.of(Blocks.EMERALD_ORE.defaultBlockState());
        return Optional.of(base.is(Blocks.DEEPSLATE) ? Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState() : Blocks.COPPER_ORE.defaultBlockState());
    }

    private double megaveinDensity(int x, int y, int z) {
        if (!isAllowedHeight(y)) return 0.0;
        double best = 0.0;
        int cellX = Math.floorDiv(x, MEGAVEIN_CELL);
        int cellY = Math.floorDiv(y, config.megaveinVerticalSpan());
        int cellZ = Math.floorDiv(z, MEGAVEIN_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int cx = cellX + dx, cy = cellY + dy, cz = cellZ + dz;
                    if (normalized(seed + 5001L, cx, cy, cz) > config.megaveinRarity() * config.globalVeinsPerChunkMultiplier()) continue;
                    double centerX = cx * MEGAVEIN_CELL + 16.0 + normalized(seed + 5003L, cx, cy, cz) * 96.0;
                    double centerY = cy * config.megaveinVerticalSpan() + 16.0 + normalized(seed + 5009L, cx, cy, cz) * (config.megaveinVerticalSpan() - 32.0);
                    double centerZ = cz * MEGAVEIN_CELL + 16.0 + normalized(seed + 5011L, cx, cy, cz) * 96.0;
                    double rx = config.megaveinMaxRadius() * (0.45 + normalized(seed + 5021L, cx, cy, cz) * 0.55);
                    double ry = config.megaveinVerticalSpan() * (0.25 + normalized(seed + 5023L, cx, cy, cz) * 0.35);
                    double rz = config.megaveinMaxRadius() * (0.45 + normalized(seed + 5027L, cx, cy, cz) * 0.55);
                    double warp = FieldMath.valueNoise3D(seed + 5039L, x, y, z, 0.025) * 9.0;
                    double nx = (x + warp - centerX) / rx;
                    double ny = (y - centerY) / ry;
                    double nz = (z - warp - centerZ) / rz;
                    double ellipsoid = 1.0 - (nx * nx + ny * ny + nz * nz);
                    double branch = Math.abs(FieldMath.valueNoise3D(seed + 5051L, x, y, z, 0.055));
                    best = Math.max(best, ellipsoid + branch * 0.42);
                }
            }
        }
        return best;
    }

    private boolean bandAllowedAtHeight(NamekianOreConfig.OreBand band, int y) {
        if (y > 320 && !config.allowOresAboveVanillaHeight()) return false;
        if (y < -64 && !config.allowOresBelowVanillaDepth()) return false;
        if (band.distribution() == OreDistributionType.MOUNTAIN_BIAS && y > 180 && !config.allowHighMountainOres()) return false;
        if (y < -128 && !config.allowDeepLavaOreZones()) return false;
        return true;
    }

    private boolean isAllowedHeight(int y) {
        if (y < config.minY() || y > config.maxY()) return false;
        if (y > 320 && !config.allowOresAboveVanillaHeight()) return false;
        if (y < -64 && !config.allowOresBelowVanillaDepth()) return false;
        return true;
    }

    private static boolean isEligibleBase(BlockState base) {
        return base.is(Blocks.STONE) || base.is(Blocks.DEEPSLATE);
    }

    static double normalized(long seed, int x, int y, int z) {
        return (FieldMath.signedHashNoise(seed, x, y, z) + 1.0) * 0.5;
    }
}
