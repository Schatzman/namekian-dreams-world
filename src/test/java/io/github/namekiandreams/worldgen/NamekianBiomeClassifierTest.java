package io.github.namekiandreams.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NamekianBiomeClassifierTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void selectsRareNetherAndBoostedOverworldBiomesInDiagnosticRange() {
        boolean darkForest = false;
        boolean mushroom = false;
        boolean birch = false;
        boolean crimson = false;
        boolean warped = false;
        boolean soulSand = false;
        for (int x = -8192; x <= 8192; x += 256) {
            for (int z = -8192; z <= 8192; z += 256) {
                var key = NamekianBiomeClassifier.selectBiomeKey(x, 80, z);
                darkForest |= key == Biomes.DARK_FOREST;
                mushroom |= key == Biomes.MUSHROOM_FIELDS;
                birch |= key == Biomes.BIRCH_FOREST;
                crimson |= key == Biomes.CRIMSON_FOREST;
                warped |= key == Biomes.WARPED_FOREST;
                soulSand |= key == Biomes.SOUL_SAND_VALLEY;
            }
        }

        assertTrue(darkForest);
        assertTrue(mushroom);
        assertTrue(birch);
        assertTrue(crimson);
        assertTrue(warped);
        assertTrue(soulSand);
    }

    @Test
    void weightedFallbackIsDeterministic() {
        int[] weights = { 12, 2, 1 };
        assertEquals(NamekianBiomeClassifier.selectWeightedIndex(12, 34, 56, weights),
                NamekianBiomeClassifier.selectWeightedIndex(12, 34, 56, weights));
    }
}
