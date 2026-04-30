package io.github.namekiandreams.worldgen;

import io.github.namekiandreams.config.NamekianDreamsConfig;

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
        double oceanMask = FieldMath.smoothstep(-0.18, -0.62, continent);
        double basinMask = FieldMath.smoothstep(0.52, 0.94,
                (octave2D(seed + 137, wx - 700.0, wz + 350.0, 0.00105, 3, 0.54) * 0.5 + 0.5)) * oceanMask;
        double amplificationMask = FieldMath.smoothstep(0.08, 0.92,
                octave2D(seed + 151, wx, wz, config.amplificationMaskFrequency(), 3, 0.50) * 0.5 + 0.5);
        double extremeMask = FieldMath.smoothstep(0.58, 0.94, amplificationMask);
        double amplitude = FieldMath.lerp(amplificationMask, config.normalAmplitude(), config.amplifiedAmplitude());
        amplitude = FieldMath.lerp(extremeMask, amplitude, config.extremeAmplitude());
        double mountain = Math.abs(octave2D(seed + 211, wx, wz, 0.00175, 5, 0.50));
        double ridges = Math.pow(1.0 - FieldMath.clamp(mountain, 0.0, 1.0), 2.05);
        double ridgeLift = ridges * amplitude * (0.46 + extremeMask * 0.28);
        double detail = octave2D(seed + 307, wx, wz, 0.012, 4, 0.48) * 22.0;
        double basinCut = (oceanMask * 86.0) + (basinMask * 70.0);
        double targetHeight = config.seaLevel() - 18.0 + uplift * amplitude + ridgeLift + detail - basinCut;
        double verticalDensity = (targetHeight - y) / 56.0;
        double caveSignal = caveSignal(wx, y, wz, targetHeight);
        double fractal = fractalContribution(wx, y, wz, extremeMask);
        TerrainRegime regime = extremeMask > 0.45 ? TerrainRegime.EXTREME : amplificationMask > 0.45 ? TerrainRegime.AMPLIFIED : TerrainRegime.NORMAL;
        return new DensitySample(verticalDensity + caveSignal + fractal, caveSignal, fractal, amplificationMask, extremeMask, targetHeight, oceanMask, basinMask, regime);
    }

    public boolean isSolid(double x, double y, double z) {
        if (y <= config.bedrockTopY()) return true;
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

    public boolean isOpenWaterColumn(double x, double z) {
        return surfaceHeight(x, z) < config.seaLevel() - 2;
    }

    public int surfaceHeight(double x, double z) {
        for (int y = config.maxY(); y >= config.minY(); y--) {
            if (isSolid(x, y, z)) return y;
        }
        return config.minY();
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
    public record DensitySample(double density, double caveSignal, double fractalContribution, double amplificationMask,
                                double extremeMask, double targetHeight, double oceanMask, double basinMask,
                                TerrainRegime regime) {}
}
