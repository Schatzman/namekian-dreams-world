package io.github.namekiandreams.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import org.junit.jupiter.api.Test;

class NamekianDensitySamplerTest {
    @Test
    void samplingIsDeterministicAndFractalDefaultsOn() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, 42L);
        NamekianDensitySampler.DensitySample first = sampler.sample(512.0, 80.0, -768.0);
        NamekianDensitySampler.DensitySample second = sampler.sample(512.0, 80.0, -768.0);
        assertEquals(first.density(), second.density());
        assertTrue(config.fractalStrength() > 0.0);
        assertTrue(config.fractalQuality() > 0);
    }
    @Test
    void diagnosticSeesRequiredSignals() {
        FieldDiagnostics.DiagnosticResult result = FieldDiagnostics.run(NamekianDreamsConfig.defaults(), 8675309L);
        assertTrue(result.acceptancePassed(), result.toReport());
    }
}
