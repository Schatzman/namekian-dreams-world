package io.github.namekiandreams.worldgen;

public final class FieldMath {
    private FieldMath() {}
    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    public static double lerp(double delta, double start, double end) { return start + (end - start) * delta; }
    public static double smoothstep(double edge0, double edge1, double value) {
        if (edge0 == edge1) throw new IllegalArgumentException("smoothstep edges must differ");
        double t = clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
    public static double signedHashNoise(long seed, int x, int y, int z) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= y * 0xBF58476D1CE4E5B9L;
        h ^= z * 0x94D049BB133111EBL;
        h ^= h >>> 30; h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27; h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }
    public static double valueNoise2D(long seed, double x, double z, double frequency) {
        double fx = x * frequency, fz = z * frequency;
        int x0 = fastFloor(fx), z0 = fastFloor(fz);
        double tx = smoothCurve(fx - x0), tz = smoothCurve(fz - z0);
        double a = signedHashNoise(seed, x0, 0, z0), b = signedHashNoise(seed, x0 + 1, 0, z0);
        double c = signedHashNoise(seed, x0, 0, z0 + 1), d = signedHashNoise(seed, x0 + 1, 0, z0 + 1);
        return lerp(tz, lerp(tx, a, b), lerp(tx, c, d));
    }
    public static double valueNoise3D(long seed, double x, double y, double z, double frequency) {
        double fx = x * frequency, fy = y * frequency, fz = z * frequency;
        int x0 = fastFloor(fx), y0 = fastFloor(fy), z0 = fastFloor(fz);
        double tx = smoothCurve(fx - x0), ty = smoothCurve(fy - y0), tz = smoothCurve(fz - z0);
        double x00 = lerp(tx, signedHashNoise(seed, x0, y0, z0), signedHashNoise(seed, x0 + 1, y0, z0));
        double x10 = lerp(tx, signedHashNoise(seed, x0, y0 + 1, z0), signedHashNoise(seed, x0 + 1, y0 + 1, z0));
        double x01 = lerp(tx, signedHashNoise(seed, x0, y0, z0 + 1), signedHashNoise(seed, x0 + 1, y0, z0 + 1));
        double x11 = lerp(tx, signedHashNoise(seed, x0, y0 + 1, z0 + 1), signedHashNoise(seed, x0 + 1, y0 + 1, z0 + 1));
        return lerp(tz, lerp(ty, x00, x10), lerp(ty, x01, x11));
    }
    private static double smoothCurve(double value) { return value * value * value * (value * (value * 6.0 - 15.0) + 10.0); }
    private static int fastFloor(double value) { int i = (int) value; return value < i ? i - 1 : i; }
}
