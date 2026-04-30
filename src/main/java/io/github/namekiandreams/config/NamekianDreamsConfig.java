package io.github.namekiandreams.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public record NamekianDreamsConfig(
        int minY,
        int height,
        int seaLevel,
        double normalAmplitude,
        double amplifiedAmplitude,
        double extremeAmplitude,
        double amplificationMaskFrequency,
        double caveFrequency,
        double caveAmplitude,
        double caveThreshold,
        double fractalStrength,
        double fractalScale,
        double fractalRegionFrequency,
        int fractalQuality,
        double domainWarpStrength,
        String performanceQualityPreset
) {
    public static final String FILE_NAME = "namekian_dreams_world.properties";

    public static NamekianDreamsConfig defaults() {
        return new NamekianDreamsConfig(-304, 1328, 63, 118.0, 360.0, 820.0, 0.00072,
                0.014, 0.64, 0.43, 0.42, 0.0058, 0.00115, 6, 44.0, "high").validate();
    }

    public static NamekianDreamsConfig load(Path configDir) {
        Path path = configDir.resolve(FILE_NAME);
        Properties properties = defaults().toProperties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load " + path, exception);
            }
        } else {
            try {
                Files.createDirectories(configDir);
                try (Writer writer = Files.newBufferedWriter(path)) {
                    properties.store(writer, "Namekian Dreams World startup config. Restart Minecraft after changing worldgen values.");
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to write default config " + path, exception);
            }
        }
        return fromProperties(properties).validate();
    }

    public int maxY() { return minY + height - 1; }
    public int bedrockTopY() { return minY + 4; }

    public NamekianDreamsConfig validate() {
        if (minY % 16 != 0) throw new IllegalArgumentException("min_y must be a multiple of 16");
        if (height <= 0 || height % 16 != 0) throw new IllegalArgumentException("height must be positive and divisible by 16");
        if (seaLevel <= minY || seaLevel >= maxY()) throw new IllegalArgumentException("sea_level must be inside the configured height range");
        if (normalAmplitude <= 0 || amplifiedAmplitude <= normalAmplitude || extremeAmplitude <= amplifiedAmplitude) {
            throw new IllegalArgumentException("amplitudes must be positive and ordered normal < amplified < extreme");
        }
        if (amplificationMaskFrequency <= 0 || caveFrequency <= 0 || fractalScale <= 0 || fractalRegionFrequency <= 0) {
            throw new IllegalArgumentException("field frequencies and fractal scale must be positive");
        }
        if (fractalQuality < 0 || fractalQuality > 12) throw new IllegalArgumentException("fractal_quality must be between 0 and 12");
        if (fractalStrength < 0 || caveAmplitude < 0 || domainWarpStrength < 0) throw new IllegalArgumentException("strength/amplitude values must be non-negative");
        String preset = performanceQualityPreset.toLowerCase(Locale.ROOT);
        if (!preset.equals("low") && !preset.equals("normal") && !preset.equals("high") && !preset.equals("extreme")) {
            throw new IllegalArgumentException("performance_quality_preset must be low, normal, high, or extreme");
        }
        return this;
    }

    public Properties toProperties() {
        Properties p = new Properties();
        p.setProperty("min_y", Integer.toString(minY));
        p.setProperty("height", Integer.toString(height));
        p.setProperty("max_y", Integer.toString(maxY()));
        p.setProperty("sea_level", Integer.toString(seaLevel));
        p.setProperty("normal_amplitude", Double.toString(normalAmplitude));
        p.setProperty("amplified_amplitude", Double.toString(amplifiedAmplitude));
        p.setProperty("extreme_amplitude", Double.toString(extremeAmplitude));
        p.setProperty("amplification_mask_frequency", Double.toString(amplificationMaskFrequency));
        p.setProperty("cave_frequency", Double.toString(caveFrequency));
        p.setProperty("cave_amplitude", Double.toString(caveAmplitude));
        p.setProperty("cave_threshold", Double.toString(caveThreshold));
        p.setProperty("fractal_strength", Double.toString(fractalStrength));
        p.setProperty("fractal_scale", Double.toString(fractalScale));
        p.setProperty("fractal_region_frequency", Double.toString(fractalRegionFrequency));
        p.setProperty("fractal_quality", Integer.toString(fractalQuality));
        p.setProperty("domain_warp_strength", Double.toString(domainWarpStrength));
        p.setProperty("performance_quality_preset", performanceQualityPreset);
        return p;
    }

    public static NamekianDreamsConfig fromProperties(Properties p) {
        return new NamekianDreamsConfig(
                intValue(p, "min_y", -304), intValue(p, "height", 1328), intValue(p, "sea_level", 63),
                doubleValue(p, "normal_amplitude", 118.0), doubleValue(p, "amplified_amplitude", 360.0),
                doubleValue(p, "extreme_amplitude", 820.0), doubleValue(p, "amplification_mask_frequency", 0.00072),
                doubleValue(p, "cave_frequency", 0.014), doubleValue(p, "cave_amplitude", 0.64),
                doubleValue(p, "cave_threshold", 0.43), doubleValue(p, "fractal_strength", 0.42),
                doubleValue(p, "fractal_scale", 0.0058), doubleValue(p, "fractal_region_frequency", 0.00115),
                intValue(p, "fractal_quality", 6), doubleValue(p, "domain_warp_strength", 44.0),
                p.getProperty("performance_quality_preset", "high"));
    }

    private static int intValue(Properties p, String key, int fallback) { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback)).trim()); }
    private static double doubleValue(Properties p, String key, double fallback) { return Double.parseDouble(p.getProperty(key, Double.toString(fallback)).trim()); }
}
