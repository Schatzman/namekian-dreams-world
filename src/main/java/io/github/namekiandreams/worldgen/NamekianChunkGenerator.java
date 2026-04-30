package io.github.namekiandreams.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.worldgen.ore.NamekianOreSampler;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

public final class NamekianChunkGenerator extends ChunkGenerator {
    public static final Codec<NamekianChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(NamekianChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(NamekianChunkGenerator::new)));

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();

    public NamekianChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            NamekianDreamsConfig config = NamekianDreamsWorld.CONFIG.validate();
            long seed = terrainSeed(randomState);
            NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
            NamekianOreSampler oreSampler = new NamekianOreSampler(config, seed);
            ChunkPos chunkPos = chunk.getPos();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int localX = 0; localX < 16; localX++) {
                int blockX = chunkPos.getMinBlockX() + localX;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int blockZ = chunkPos.getMinBlockZ() + localZ;
                    boolean topMarked = false;
                    for (int y = config.maxY(); y >= config.minY(); y--) {
                        pos.set(blockX, y, blockZ);
                        BlockState state = stateFor(config, sampler, oreSampler, blockX, y, blockZ, topMarked);
                        if (!state.isAir() && state != WATER) topMarked = true;
                        chunk.setBlockState(pos, state, false);
                    }
                }
            }
            return chunk;
        }, executor);
    }


    private static long terrainSeed(RandomState randomState) {
        // RandomState is created from the selected world's seed; this keeps the preset selectable
        // without baking one literal seed into every generated world.
        return randomState.getOrCreateRandomFactory(NamekianDreamsWorld.id("terrain_seed"))
                .fromHashOf("namekian_density")
                .nextLong();
    }

    private static BlockState stateFor(NamekianDreamsConfig config, NamekianDensitySampler sampler, NamekianOreSampler oreSampler, int x, int y, int z, boolean topAlreadyMarked) {
        if (y <= config.bedrockTopY()) return BEDROCK;
        if (sampler.isSolid(x, y, z)) {
            if (!topAlreadyMarked && y > config.seaLevel() - 6) return GRASS;
            if (y > config.seaLevel() - 12 && y < config.seaLevel() + 24) return DIRT;
            BlockState base = y < -64 ? DEEPSLATE : STONE;
            return oreSampler.replaceIfOre(base, x, y, z);
        }
        return y <= config.seaLevel() ? WATER : AIR;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        NamekianDreamsConfig config = NamekianDreamsWorld.CONFIG.validate();
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, terrainSeed(randomState));
        for (int y = config.maxY(); y >= config.minY(); y--) if (sampler.isSolid(x, y, z)) return y + 1;
        return config.minY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        NamekianDreamsConfig config = NamekianDreamsWorld.CONFIG.validate();
        long seed = terrainSeed(randomState);
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
        NamekianOreSampler oreSampler = new NamekianOreSampler(config, seed);
        BlockState[] states = new BlockState[config.height()];
        for (int i = 0; i < states.length; i++) {
            int y = config.minY() + i;
            states[i] = stateFor(config, sampler, oreSampler, x, y, z, y < config.maxY() && sampler.isSolid(x, y + 1, z));
        }
        return new NoiseColumn(config.minY(), states);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {}

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carving) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    @Override
    public int getGenDepth() { return NamekianDreamsWorld.CONFIG.height(); }

    @Override
    public int getSeaLevel() { return NamekianDreamsWorld.CONFIG.seaLevel(); }

    @Override
    public int getMinY() { return NamekianDreamsWorld.CONFIG.minY(); }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos) {
        NamekianDreamsConfig config = NamekianDreamsWorld.CONFIG.validate();
        NamekianDensitySampler.DensitySample sample = new NamekianDensitySampler(config, terrainSeed(randomState)).sample(pos.getX(), pos.getY(), pos.getZ());
        lines.add("Namekian density: " + Mth.floor(sample.density() * 1000.0) / 1000.0);
        lines.add("Namekian regime: " + sample.regime());
        lines.add("Namekian range: " + config.minY() + ".." + config.maxY());
        lines.add("Namekian daylight offset: " + config.outdoorSkyLightOffset() + " max=" + config.maxSkyLightLevel());
        lines.add("Namekian ores: multiplier=" + config.globalOreMultiplier() + " megaveins=" + config.enableMegaveins());
    }
}
