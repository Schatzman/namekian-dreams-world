package io.github.namekiandreams.worldgen.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import org.junit.jupiter.api.Test;

class NamekianDaylightOffsetTest {
    @Test
    void appliesConfiguredOutdoorSkyLightOffsetAndClamp() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        assertEquals(10, NamekianDaylightOffset.applyOutdoorSkyLightOffset(15, config));
        assertEquals(7, NamekianDaylightOffset.applyOutdoorSkyLightOffset(12, config));
        assertEquals(0, NamekianDaylightOffset.applyOutdoorSkyLightOffset(4, config));
        assertEquals(0, NamekianDaylightOffset.applyOutdoorSkyLightOffset(0, config));
    }
}
