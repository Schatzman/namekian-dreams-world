package io.github.namekiandreams.worldgen.ore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NamekianOreSamplerTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void samplingIsDeterministicForSameSeedAndPosition() {
        NamekianOreSampler sampler = new NamekianOreSampler(NamekianDreamsConfig.defaults(), 42L);
        BlockState first = sampler.replaceIfOre(Blocks.DEEPSLATE.defaultBlockState(), 128, -220, -64);
        BlockState second = sampler.replaceIfOre(Blocks.DEEPSLATE.defaultBlockState(), 128, -220, -64);
        assertEquals(first, second);
    }

    @Test
    void differentSeedsCanVaryOreDecisions() {
        NamekianOreSampler first = new NamekianOreSampler(NamekianDreamsConfig.defaults(), 1234L);
        NamekianOreSampler second = new NamekianOreSampler(NamekianDreamsConfig.defaults(), 9876L);
        int differences = 0;
        for (int i = 0; i < 256; i++) {
            BlockState a = first.replaceIfOre(Blocks.DEEPSLATE.defaultBlockState(), i * 11, -220 + (i % 80), i * -7);
            BlockState b = second.replaceIfOre(Blocks.DEEPSLATE.defaultBlockState(), i * 11, -220 + (i % 80), i * -7);
            if (!a.equals(b)) differences++;
        }
        assertTrue(differences > 0);
    }

    @Test
    void diagnosticsShowHighDeepAndMegaveinAvailability() {
        OreDiagnostics.DiagnosticResult result = OreDiagnostics.run(NamekianDreamsConfig.defaults(), 8675309L);
        assertTrue(result.acceptancePassed(), result.toReport());
        assertTrue(result.belowVanillaBedrock());
        assertTrue(result.aboveVanillaBuildHeight());
        assertTrue(result.megaveinCandidatePresent());
    }
}
