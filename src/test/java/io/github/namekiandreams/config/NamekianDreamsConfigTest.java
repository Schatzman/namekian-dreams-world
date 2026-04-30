package io.github.namekiandreams.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class NamekianDreamsConfigTest {
    @Test
    void defaultsExposeExpandedVerticalRange() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertEquals(-304, config.minY());
        assertEquals(1328, config.height());
        assertEquals(1023, config.maxY());
        assertEquals(-300, config.bedrockTopY());
    }
    @Test
    void validationRejectsInvalidRange() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertThrows(IllegalArgumentException.class, () -> new NamekianDreamsConfig(
                -303, config.height(), config.seaLevel(), config.normalAmplitude(), config.amplifiedAmplitude(),
                config.extremeAmplitude(), config.amplificationMaskFrequency(), config.caveFrequency(),
                config.caveAmplitude(), config.caveThreshold(), config.fractalStrength(), config.fractalScale(),
                config.fractalRegionFrequency(), config.fractalQuality(), config.domainWarpStrength(),
                config.performanceQualityPreset()).validate());
    }
}
