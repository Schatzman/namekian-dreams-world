package io.github.namekiandreams.worldgen.ore;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NamekianOreConfig {
    private final NamekianDreamsConfig config;
    private final List<OreBand> bands;

    private NamekianOreConfig(NamekianDreamsConfig config, List<OreBand> bands) {
        this.config = config.validate();
        this.bands = List.copyOf(bands);
    }

    public static NamekianOreConfig defaults(NamekianDreamsConfig config) {
        int min = config.minY();
        int max = config.maxY();
        return new NamekianOreConfig(config, List.of(
                new OreBand("coal", Blocks.COAL_ORE.defaultBlockState(), Blocks.DEEPSLATE_COAL_ORE.defaultBlockState(), min, max, 0.020, OreDistributionType.UNIFORM),
                new OreBand("iron", Blocks.IRON_ORE.defaultBlockState(), Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), min, max, 0.024, OreDistributionType.TRIANGULAR),
                new OreBand("copper", Blocks.COPPER_ORE.defaultBlockState(), Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(), -96, max, 0.018, OreDistributionType.MOUNTAIN_BIAS),
                new OreBand("gold", Blocks.GOLD_ORE.defaultBlockState(), Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(), min, 96, 0.018, OreDistributionType.DEEP_BIAS),
                new OreBand("redstone", Blocks.REDSTONE_ORE.defaultBlockState(), Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(), min, 48, 0.020, OreDistributionType.DEEP_BIAS),
                new OreBand("lapis", Blocks.LAPIS_ORE.defaultBlockState(), Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(), min, 128, 0.014, OreDistributionType.CAVE_BIAS),
                new OreBand("diamond", Blocks.DIAMOND_ORE.defaultBlockState(), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(), min, 64, 0.013, OreDistributionType.DEEP_BIAS),
                new OreBand("emerald", Blocks.EMERALD_ORE.defaultBlockState(), Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(), 96, max, 0.011, OreDistributionType.MOUNTAIN_BIAS)
        ));
    }

    public NamekianDreamsConfig config() { return config; }
    public List<OreBand> bands() { return bands; }

    public record OreBand(String name, BlockState stoneOre, BlockState deepslateOre, int minY, int maxY, double baseChance, OreDistributionType distribution) {
        public boolean includesY(int y) { return y >= minY && y <= maxY; }
        public BlockState oreForBase(BlockState base) { return base.is(Blocks.DEEPSLATE) ? deepslateOre : stoneOre; }
    }
}
