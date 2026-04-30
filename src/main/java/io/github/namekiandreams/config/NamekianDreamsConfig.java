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
        String performanceQualityPreset,
        String skyColor,
        String fogColor,
        String waterFogColor,
        String cloudColor,
        boolean enableActualSkyLightOffset,
        int outdoorSkyLightOffset,
        int maxSkyLightLevel,
        boolean enableVisualDaylightDimming,
        double visualDaylightMultiplier,
        double globalOreMultiplier,
        double globalVeinSizeMultiplier,
        double globalVeinsPerChunkMultiplier,
        boolean enableMegaveins,
        double megaveinRarity,
        int megaveinMaxRadius,
        int megaveinVerticalSpan,
        boolean allowOresAboveVanillaHeight,
        boolean allowOresBelowVanillaDepth,
        boolean allowHighMountainOres,
        boolean allowDeepLavaOreZones
) {
    public static final String FILE_NAME = "namekian_dreams_world.properties";

    public static NamekianDreamsConfig defaults() {
        return new NamekianDreamsConfig(-304, 1328, 0, 118.0, 360.0, 820.0, 0.00072,
                0.014, 0.64, 0.43, 0.42, 0.0058, 0.00115, 6, 44.0, "high",
                "#3F9678", "#4FAE8A", "#24584F", "#A7D9BF",
                true, -5, 10, true, 0.72,
                2.85, 2.25, 1.75, true, 0.090, 72, 192,
                true, true, true, true).validate();
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
        validateHexColor(skyColor, "sky_color");
        validateHexColor(fogColor, "fog_color");
        validateHexColor(waterFogColor, "water_fog_color");
        validateHexColor(cloudColor, "cloud_color");
        if (outdoorSkyLightOffset > 0) throw new IllegalArgumentException("outdoor_sky_light_offset must be zero or negative");
        if (maxSkyLightLevel < 0 || maxSkyLightLevel > 15) throw new IllegalArgumentException("max_sky_light_level must be 0..15");
        if (visualDaylightMultiplier <= 0.0 || visualDaylightMultiplier > 1.0) throw new IllegalArgumentException("visual_daylight_multiplier must be >0 and <=1");
        if (globalOreMultiplier <= 0.0 || globalVeinSizeMultiplier <= 0.0 || globalVeinsPerChunkMultiplier <= 0.0) throw new IllegalArgumentException("ore multipliers must be positive");
        if (megaveinRarity < 0.0 || megaveinRarity > 1.0) throw new IllegalArgumentException("megavein_rarity must be 0..1");
        if (megaveinMaxRadius <= 0 || megaveinVerticalSpan <= 0) throw new IllegalArgumentException("megavein dimensions must be positive");
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
        p.setProperty("sky_color", skyColor);
        p.setProperty("fog_color", fogColor);
        p.setProperty("water_fog_color", waterFogColor);
        p.setProperty("cloud_color", cloudColor);
        p.setProperty("enable_actual_sky_light_offset", Boolean.toString(enableActualSkyLightOffset));
        p.setProperty("outdoor_sky_light_offset", Integer.toString(outdoorSkyLightOffset));
        p.setProperty("max_sky_light_level", Integer.toString(maxSkyLightLevel));
        p.setProperty("enable_visual_daylight_dimming", Boolean.toString(enableVisualDaylightDimming));
        p.setProperty("visual_daylight_multiplier", Double.toString(visualDaylightMultiplier));
        p.setProperty("global_ore_multiplier", Double.toString(globalOreMultiplier));
        p.setProperty("global_vein_size_multiplier", Double.toString(globalVeinSizeMultiplier));
        p.setProperty("global_veins_per_chunk_multiplier", Double.toString(globalVeinsPerChunkMultiplier));
        p.setProperty("enable_megaveins", Boolean.toString(enableMegaveins));
        p.setProperty("megavein_rarity", Double.toString(megaveinRarity));
        p.setProperty("megavein_max_radius", Integer.toString(megaveinMaxRadius));
        p.setProperty("megavein_vertical_span", Integer.toString(megaveinVerticalSpan));
        p.setProperty("allow_ores_above_vanilla_height", Boolean.toString(allowOresAboveVanillaHeight));
        p.setProperty("allow_ores_below_vanilla_depth", Boolean.toString(allowOresBelowVanillaDepth));
        p.setProperty("allow_high_mountain_ores", Boolean.toString(allowHighMountainOres));
        p.setProperty("allow_deep_lava_ore_zones", Boolean.toString(allowDeepLavaOreZones));
        return p;
    }

    public static NamekianDreamsConfig fromProperties(Properties p) {
        return new NamekianDreamsConfig(
                intValue(p, "min_y", -304), intValue(p, "height", 1328), intValue(p, "sea_level", 0),
                doubleValue(p, "normal_amplitude", 118.0), doubleValue(p, "amplified_amplitude", 360.0),
                doubleValue(p, "extreme_amplitude", 820.0), doubleValue(p, "amplification_mask_frequency", 0.00072),
                doubleValue(p, "cave_frequency", 0.014), doubleValue(p, "cave_amplitude", 0.64),
                doubleValue(p, "cave_threshold", 0.43), doubleValue(p, "fractal_strength", 0.42),
                doubleValue(p, "fractal_scale", 0.0058), doubleValue(p, "fractal_region_frequency", 0.00115),
                intValue(p, "fractal_quality", 6), doubleValue(p, "domain_warp_strength", 44.0),
                p.getProperty("performance_quality_preset", "high"),
                p.getProperty("sky_color", "#3F9678"), p.getProperty("fog_color", "#4FAE8A"),
                p.getProperty("water_fog_color", "#24584F"), p.getProperty("cloud_color", "#A7D9BF"),
                booleanValue(p, "enable_actual_sky_light_offset", true), intValue(p, "outdoor_sky_light_offset", -5),
                intValue(p, "max_sky_light_level", 10), booleanValue(p, "enable_visual_daylight_dimming", true),
                doubleValue(p, "visual_daylight_multiplier", 0.72), doubleValue(p, "global_ore_multiplier", 2.85),
                doubleValue(p, "global_vein_size_multiplier", 2.25), doubleValue(p, "global_veins_per_chunk_multiplier", 1.75),
                booleanValue(p, "enable_megaveins", true), doubleValue(p, "megavein_rarity", 0.090),
                intValue(p, "megavein_max_radius", 72), intValue(p, "megavein_vertical_span", 192),
                booleanValue(p, "allow_ores_above_vanilla_height", true), booleanValue(p, "allow_ores_below_vanilla_depth", true),
                booleanValue(p, "allow_high_mountain_ores", true), booleanValue(p, "allow_deep_lava_ore_zones", true));
    }

    private static void validateHexColor(String value, String key) {
        if (value == null || !value.matches("#[0-9A-Fa-f]{6}")) throw new IllegalArgumentException(key + " must be #RRGGBB");
    }
    private static int intValue(Properties p, String key, int fallback) { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback)).trim()); }
    private static double doubleValue(Properties p, String key, double fallback) { return Double.parseDouble(p.getProperty(key, Double.toString(fallback)).trim()); }
    private static boolean booleanValue(Properties p, String key, boolean fallback) { return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(fallback)).trim()); }
}
