package io.github.namekiandreams.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    void differentWorldSeedsProduceDifferentTerrain() {
        NamekianDreamsConfig config = NamekianDreamsConfig.defaults();
        NamekianDensitySampler firstSeed = new NamekianDensitySampler(config, 1234L);
        NamekianDensitySampler sameSeed = new NamekianDensitySampler(config, 1234L);
        NamekianDensitySampler differentSeed = new NamekianDensitySampler(config, 9876L);

        double x = 1408.0;
        double y = 112.0;
        double z = -2336.0;
        assertEquals(firstSeed.sample(x, y, z).density(), sameSeed.sample(x, y, z).density());
        assertNotEquals(firstSeed.sample(x, y, z).density(), differentSeed.sample(x, y, z).density());
    }

    @Test
    void worldPresetDoesNotBakeAFixedSeed() throws IOException {
        String preset = Files.readString(Path.of(
                "src/main/resources/data/namekian_dreams_world/worldgen/world_preset/namekian_dreams_overworld.json"));

        assertTrue(preset.contains("namekian_dreams_world:namekian_chunk_generator"));
        assertTrue(preset.contains("minecraft:overworld"));
        assertTrue(!preset.contains("\"seed\""));
    }

    @Test
    void diagnosticSeesRequiredSignals() {
        FieldDiagnostics.DiagnosticResult result = FieldDiagnostics.run(NamekianDreamsConfig.defaults(), 8675309L);
        assertTrue(result.acceptancePassed(), result.toReport());
    }
}
