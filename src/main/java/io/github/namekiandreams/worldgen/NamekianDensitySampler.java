package io.github.namekiandreams.worldgen;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class NamekianDensitySampler {
    private static final long BASE_SEED_SALT = 0x4E414D454B49414EL;
    private final NamekianDreamsConfig config;
    private final long seed;

    public NamekianDensitySampler(NamekianDreamsConfig config, long seed) {
        this.config = config.validate();
        this.seed = seed ^ BASE_SEED_SALT;
    }

    public DensitySample sample(double x, double y, double z) {
        double warpX = FieldMath.valueNoise2D(seed + 11, x, z, 0.0028) * config.domainWarpStrength();
        double warpZ = FieldMath.valueNoise2D(seed + 17, x + 101.0, z - 53.0, 0.0024) * config.domainWarpStrength();
        double wx = x + warpX;
        double wz = z + warpZ;
        double continent = octave2D(seed + 101, wx, wz, 0.00042, 4, 0.55);
        double uplift = FieldMath.smoothstep(-0.48, 0.82, continent);
        double oceanMask = FieldMath.smoothstep(-0.08, -0.54, continent);
        double basinNoise = octave2D(seed + 137, wx - 700.0, wz + 350.0, 0.00082, 4, 0.54) * 0.5 + 0.5;
        double trenchNoise = octave2D(seed + 139, wx + 1200.0, wz - 800.0, 0.00155, 3, 0.50) * 0.5 + 0.5;
        double basinMask = FieldMath.smoothstep(0.34, 0.88, basinNoise) * oceanMask;
        double abyssMask = FieldMath.smoothstep(0.72, 0.96, trenchNoise) * basinMask;
        double amplificationMask = FieldMath.smoothstep(0.08, 0.92,
                octave2D(seed + 151, wx, wz, config.amplificationMaskFrequency(), 3, 0.50) * 0.5 + 0.5);
        double extremeMask = FieldMath.smoothstep(0.58, 0.94, amplificationMask);
        double amplitude = FieldMath.lerp(amplificationMask, config.normalAmplitude(), config.amplifiedAmplitude());
        amplitude = FieldMath.lerp(extremeMask, amplitude, config.extremeAmplitude());
        double mountain = Math.abs(octave2D(seed + 211, wx, wz, 0.00175, 5, 0.50));
        double ridges = Math.pow(1.0 - FieldMath.clamp(mountain, 0.0, 1.0), 2.05);
        double ridgeLift = ridges * amplitude * (0.46 + extremeMask * 0.28);
        double detail = octave2D(seed + 307, wx, wz, 0.012, 4, 0.48) * 22.0;
        double basinCut = (oceanMask * 118.0) + (basinMask * 154.0) + (abyssMask * 104.0);
        double targetHeight = config.seaLevel() - 10.0 + uplift * amplitude + ridgeLift + detail - basinCut;
        double verticalDensity = (targetHeight - y) / 56.0;
        double caveSignal = caveSignal(wx, y, wz, targetHeight);
        double fractal = fractalContribution(wx, y, wz, extremeMask);
        TerrainRegime regime = extremeMask > 0.45 ? TerrainRegime.EXTREME : amplificationMask > 0.45 ? TerrainRegime.AMPLIFIED : TerrainRegime.NORMAL;
        return new DensitySample(verticalDensity + caveSignal + fractal, caveSignal, fractal, amplificationMask, extremeMask, targetHeight, oceanMask, basinMask, regime);
    }

    public boolean isSolid(double x, double y, double z) {
        if (y <= config.bedrockTopY()) return true;
        if (isLavaLake(x, y, z)) return false;
        if (isCaveAir(x, y, z)) return false;
        return sample(x, y, z).density() > 0.0;
    }

    public boolean isCaveAir(double x, double y, double z) {
        if (y <= config.bedrockTopY() + 4 || y >= config.maxY() - 4) return false;
        DensitySample sample = sample(x, y, z);
        double buriedDepth = sample.targetHeight() - y;
        if (buriedDepth < 10.0) return false;
        return sample.caveSignal() < -1.15 || shaftSignal(x, z) > 0.82;
    }

    public boolean isLavaLake(double x, double y, double z) {
        if (!config.enableLargeLavaLakes() || y <= config.bedrockTopY() + 2 || y >= config.maxY() - 4) return false;
        return isDeepLavaLake(x, y, z) || isCaveLavaBasin(x, y, z) || isSurfaceLavaLake(x, y, z);
    }

    public boolean isOpenWaterColumn(double x, double z) {
        return surfaceHeight(x, z) < config.seaLevel() - 2;
    }

    public int surfaceHeight(double x, double z) {
        for (int y = config.maxY(); y >= config.minY(); y--) {
            if (isSolid(x, y, z)) return y;
        }
        return config.minY();
    }

    public BiomeRegion biomeRegion(double x, double y, double z) {
        DensitySample seaSample = sample(x, config.seaLevel(), z);
        double estimatedSurfaceY = seaSample.targetHeight();
        if (estimatedSurfaceY <= config.seaLevel() - 205 || seaSample.basinMask() > 0.70) return BiomeRegion.DEEP_OCEAN;
        if (estimatedSurfaceY <= config.seaLevel() - 8 || seaSample.oceanMask() > 0.72) return BiomeRegion.OCEAN;
        double temperature = octave2D(seed + 601, x + 900.0, z - 400.0, 0.00095, 3, 0.55);
        double humidity = octave2D(seed + 607, x - 300.0, z + 1100.0, 0.00105, 3, 0.52);
        double weirdness = octave2D(seed + 613, x + 1700.0, z + 700.0, 0.00135, 3, 0.50);
        if (estimatedSurfaceY > 430 || (estimatedSurfaceY > 210 && temperature < -0.22)) return BiomeRegion.FROZEN_PEAKS;
        if (y < config.seaLevel() - 70 && weirdness > 0.38 && humidity < -0.08) return BiomeRegion.DEEP_DARK;
        if (y < config.seaLevel() - 48 && weirdness < -0.34) return BiomeRegion.DRIPSTONE_CAVES;
        ResourceKey<Biome> selectedBiome = NamekianBiomeClassifier.selectBiomeKey((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        BiomeRegion selectedRegion = NamekianBiomeClassifier.regionForBiomeKey(selectedBiome);
        if (selectedRegion != BiomeRegion.TEMPERATE) return selectedRegion;
        if (humidity > 0.26 && temperature > -0.02) return BiomeRegion.JUNGLE_LUSH;
        if (temperature < -0.36) return BiomeRegion.FROZEN;
        if (weirdness > 0.46) return BiomeRegion.GRAVELLY;
        return BiomeRegion.TEMPERATE;
    }

    private boolean isDeepLavaLake(double x, double y, double z) {
        if (y > config.deepLavaStartY() + config.lavaLakeMaxRadius() / 3.0) return false;
        double lakeNoise = octave2D(seed + 701, x, z, config.lavaLakeFrequency(), 4, 0.56) * 0.5 + 0.5;
        if (lakeNoise < config.lavaLakeThreshold()) return false;
        double strength = (lakeNoise - config.lavaLakeThreshold()) / Math.max(1.0e-6, 1.0 - config.lavaLakeThreshold());
        int lavaTop = config.deepLavaStartY() + (int) Math.round(strength * config.lavaLakeMaxRadius() * 0.55);
        int lavaBottom = Math.max(config.bedrockTopY() + 3, lavaTop - config.lavaLakeMaxRadius());
        return y >= lavaBottom && y <= lavaTop;
    }

    private boolean isCaveLavaBasin(double x, double y, double z) {
        if (y > config.seaLevel() - 44 || y < config.bedrockTopY() + 6) return false;
        DensitySample sample = sample(x, y, z);
        if (sample.caveSignal() > -0.72 && sample.density() > 0.25) return false;
        double basin = FieldMath.valueNoise3D(seed + 733, x, y * 0.45, z, config.lavaLakeFrequency() * 2.7) * 0.5 + 0.5;
        double broad = FieldMath.valueNoise2D(seed + 739, x + 320.0, z - 680.0, config.lavaLakeFrequency() * 0.72) * 0.5 + 0.5;
        return basin > 0.91 && broad > 0.62;
    }

    private boolean isSurfaceLavaLake(double x, double y, double z) {
        if (config.surfaceLavaLakeChance() <= 0.0) return false;
        DensitySample sample = sample(x, config.seaLevel(), z);
        if (sample.extremeMask() < 0.58 || sample.targetHeight() < config.seaLevel() + 180.0) return false;
        double chance = FieldMath.valueNoise2D(seed + 761, x - 1100.0, z + 410.0, config.lavaLakeFrequency() * 1.45) * 0.5 + 0.5;
        if (chance > config.surfaceLavaLakeChance()) return false;
        int surface = (int) Math.round(sample.targetHeight());
        return y >= surface - 5 && y <= surface + 1;
    }

    private double caveSignal(double x, double y, double z, double targetHeight) {
        double tunnels = Math.abs(FieldMath.valueNoise3D(seed + 401, x, y * 0.72, z, config.caveFrequency()));
        double chambers = Math.abs(FieldMath.valueNoise3D(seed + 409, x + 400.0, y, z - 200.0, config.caveFrequency() * 0.36));
        double fracture = Math.abs(FieldMath.valueNoise3D(seed + 419, x - 150.0, y * 1.35, z + 90.0, config.caveFrequency() * 1.85));
        double tunnelMask = FieldMath.smoothstep(config.caveThreshold(), 0.92, Math.max(tunnels, fracture));
        double chamberMask = FieldMath.smoothstep(config.caveThreshold() * 0.82, 0.82, chambers);
        double depthBoost = FieldMath.smoothstep(config.seaLevel() + 42.0, config.minY() + 36.0, y);
        double buriedBoost = FieldMath.smoothstep(18.0, 160.0, targetHeight - y);
        double voidMask = FieldMath.clamp(Math.max(tunnelMask, chamberMask * 1.18), 0.0, 1.0);
        return -voidMask * config.caveAmplitude() * (4.2 + depthBoost * 5.8 + buriedBoost * 3.2);
    }

    private double shaftSignal(double x, double z) {
        double broad = Math.abs(FieldMath.valueNoise2D(seed + 431, x, z, 0.0065));
        double tight = Math.abs(FieldMath.valueNoise2D(seed + 433, x + 71.0, z - 39.0, 0.021));
        return FieldMath.smoothstep(0.58, 0.92, Math.max(broad, tight));
    }

    private double fractalContribution(double x, double y, double z, double extremeMask) {
        if (config.fractalStrength() <= 0.0 || config.fractalQuality() == 0) return 0.0;
        double region = FieldMath.smoothstep(0.12, 0.86, FieldMath.valueNoise2D(seed + 503, x, z, config.fractalRegionFrequency()) * 0.5 + 0.5);
        double mandelbulb = mandelbulbShell(x * config.fractalScale(), y * config.fractalScale(), z * config.fractalScale());
        double julia = juliaFold((x + 180.0) * config.fractalScale(), y * config.fractalScale(), (z - 90.0) * config.fractalScale());
        double shell = (mandelbulb * 0.62 + julia * 0.38) * 2.0 - 1.0;
        return shell * config.fractalStrength() * (0.35 + region * 0.45 + extremeMask * 0.35);
    }

    private double mandelbulbShell(double x, double y, double z) {
        double zx = x, zy = y, zz = z;
        int escapedAt = config.fractalQuality();
        for (int i = 0; i < config.fractalQuality(); i++) {
            double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
            if (r > 2.0) { escapedAt = i; break; }
            double theta = Math.atan2(Math.sqrt(zx * zx + zy * zy), zz);
            double phi = Math.atan2(zy, zx);
            double power = Math.pow(Math.max(r, 1.0e-6), 8.0);
            double sinTheta = Math.sin(theta * 8.0);
            zx = power * sinTheta * Math.cos(phi * 8.0) + x;
            zy = power * sinTheta * Math.sin(phi * 8.0) + y;
            zz = power * Math.cos(theta * 8.0) + z;
        }
        return 1.0 - ((double) escapedAt / Math.max(1, config.fractalQuality()));
    }

    private double juliaFold(double x, double y, double z) {
        double zx = x, zy = y, zz = z, score = 0.0;
        for (int i = 0; i < config.fractalQuality(); i++) {
            double nx = Math.abs(zx) / (zx * zx + 0.72) - 0.82;
            double ny = Math.abs(zy) / (zy * zy + 0.81) - 0.31;
            double nz = Math.abs(zz) / (zz * zz + 0.77) - 0.47;
            zx = nx + 0.18 * Math.sin(zy);
            zy = ny + 0.18 * Math.cos(zz);
            zz = nz + 0.18 * Math.sin(zx);
            score += Math.exp(-Math.abs(zx * zy * zz));
        }
        return FieldMath.clamp(score / Math.max(1, config.fractalQuality()), 0.0, 1.0);
    }

    private double octave2D(long octaveSeed, double x, double z, double frequency, int octaves, double persistence) {
        double total = 0.0, amplitude = 1.0, max = 0.0, currentFrequency = frequency;
        for (int i = 0; i < octaves; i++) {
            total += FieldMath.valueNoise2D(octaveSeed + i * 31L, x, z, currentFrequency) * amplitude;
            max += amplitude;
            amplitude *= persistence;
            currentFrequency *= 2.0;
        }
        return total / max;
    }

    public enum TerrainRegime { NORMAL, AMPLIFIED, EXTREME }
    public enum BiomeRegion { TEMPERATE, OCEAN, DEEP_OCEAN, FROZEN, FROZEN_PEAKS, JUNGLE_LUSH, DRIPSTONE_CAVES, DEEP_DARK, GRAVELLY, DARK_FOREST, MUSHROOM_FIELDS, BIRCH_FOREST, CRIMSON_FOREST, WARPED_FOREST, SOUL_SAND_VALLEY }
    public record DensitySample(double density, double caveSignal, double fractalContribution, double amplificationMask,
                                double extremeMask, double targetHeight, double oceanMask, double basinMask,
                                TerrainRegime regime) {}
}
