package io.github.namekiandreams.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NamekianDreamsConfigTest {
    @Test
    void defaultsExposeExpandedVerticalRange() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertEquals(-304, config.minY());
        assertEquals(1328, config.height());
        assertEquals(1023, config.maxY());
        assertEquals(0, config.seaLevel());
        assertEquals(-300, config.bedrockTopY());
    }
    @Test
    void defaultsExposeAtmosphereDaylightAndOreKeys() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertEquals("#3F9678", config.skyColor());
        assertEquals("#4FAE8A", config.fogColor());
        assertEquals("#24584F", config.waterFogColor());
        assertEquals("#A7D9BF", config.cloudColor());
        assertEquals(-5, config.outdoorSkyLightOffset());
        assertEquals(10, config.maxSkyLightLevel());
        assertTrue(config.enableVisualDaylightDimming());
        assertTrue(config.enableLargeLavaLakes());
        assertEquals(-144, config.deepLavaStartY());
        assertTrue(config.lavaLakeMaxRadius() > 1);
        assertTrue(config.enableMegaveins());
        assertTrue(config.globalOreMultiplier() > 1.0);
    }

    @Test
    void validationRejectsInvalidRange() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertThrows(IllegalArgumentException.class, () -> new NamekianDreamsConfig(
                -303, config.height(), config.seaLevel(), config.normalAmplitude(), config.amplifiedAmplitude(),
                config.extremeAmplitude(), config.amplificationMaskFrequency(), config.caveFrequency(),
                config.caveAmplitude(), config.caveThreshold(), config.fractalStrength(), config.fractalScale(),
                config.fractalRegionFrequency(), config.fractalQuality(), config.domainWarpStrength(),
                config.performanceQualityPreset(), config.skyColor(), config.fogColor(), config.waterFogColor(), config.cloudColor(),
                config.enableActualSkyLightOffset(), config.outdoorSkyLightOffset(), config.maxSkyLightLevel(),
                config.enableVisualDaylightDimming(), config.visualDaylightMultiplier(), config.enableLargeLavaLakes(),
                config.lavaLakeFrequency(), config.lavaLakeThreshold(), config.deepLavaStartY(), config.lavaLakeMaxRadius(),
                config.surfaceLavaLakeChance(), config.globalOreMultiplier(),
                config.globalVeinSizeMultiplier(), config.globalVeinsPerChunkMultiplier(), config.enableMegaveins(),
                config.megaveinRarity(), config.megaveinMaxRadius(), config.megaveinVerticalSpan(),
                config.allowOresAboveVanillaHeight(), config.allowOresBelowVanillaDepth(), config.allowHighMountainOres(),
                config.allowDeepLavaOreZones()).validate());
    }
}
