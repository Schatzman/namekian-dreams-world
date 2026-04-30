package io.github.namekiandreams.worldgen.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import org.junit.jupiter.api.Test;

class NamekianPackedLightTest {
    @Test
    void adjustsOnlySkyComponentInsidePackedLight() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        int adjusted = NamekianPackedLight.adjustPackedSkyLight(NamekianPackedLight.pack(9, 15), config);

        assertEquals(9, NamekianPackedLight.block(adjusted));
        assertEquals(10, NamekianPackedLight.sky(adjusted));
    }

    @Test
    void clampsPackedSkyComponentAtZero() {
        int adjusted = NamekianPackedLight.adjustPackedSkyLight(NamekianPackedLight.pack(12, 4), NamekianDreamsConfig.defaults());

        assertEquals(12, NamekianPackedLight.block(adjusted));
        assertEquals(0, NamekianPackedLight.sky(adjusted));
    }
}
