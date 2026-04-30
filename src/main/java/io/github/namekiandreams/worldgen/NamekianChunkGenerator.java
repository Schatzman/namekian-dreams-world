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
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState MOSS = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState MUD = Blocks.MUD.defaultBlockState();
    private static final BlockState DRIPSTONE = Blocks.DRIPSTONE_BLOCK.defaultBlockState();
    private static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
    private static final BlockState SCULK = Blocks.SCULK.defaultBlockState();
    private static final BlockState TUFF = Blocks.TUFF.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
    private static final BlockState CRIMSON_NYLIUM = Blocks.CRIMSON_NYLIUM.defaultBlockState();
    private static final BlockState WARPED_NYLIUM = Blocks.WARPED_NYLIUM.defaultBlockState();
    private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    private static final BlockState SOUL_SOIL = Blocks.SOUL_SOIL.defaultBlockState();

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
        if (sampler.isLavaLake(x, y, z)) return LAVA;
        if (sampler.isCaveAir(x, y, z)) return AIR;
        if (sampler.isSolid(x, y, z)) {
            NamekianDensitySampler.BiomeRegion region = sampler.biomeRegion(x, y, z);
            if (!topAlreadyMarked) return surfaceBlockFor(region, y, x, z, config);
            if (y > config.seaLevel() - 10 && y < config.seaLevel() + 26) return nearSurfaceBlockFor(region);
            BlockState decorative = deepBiomeBlockFor(region, x, y, z, config);
            if (decorative != null) return decorative;
            BlockState base = y < -64 ? DEEPSLATE : STONE;
            return oreSampler.replaceIfOre(base, x, y, z);
        }
        return y <= config.seaLevel() ? WATER : AIR;
    }

    public static BlockState diagnosticStateFor(NamekianDreamsConfig config, long seed, int x, int y, int z) {
        NamekianDensitySampler sampler = new NamekianDensitySampler(config, seed);
        NamekianOreSampler oreSampler = new NamekianOreSampler(config, seed);
        return stateFor(config, sampler, oreSampler, x, y, z, y < config.maxY() && sampler.isSolid(x, y + 1, z));
    }

    private static BlockState surfaceBlockFor(NamekianDensitySampler.BiomeRegion region, int y, int x, int z, NamekianDreamsConfig config) {
        return switch (region) {
            case DEEP_OCEAN -> deepOceanFloorNoise(x, z) > 0.35 ? GRAVEL : DEEPSLATE;
            case OCEAN -> deepOceanFloorNoise(x, z) > -0.20 ? GRAVEL : SAND;
            case FROZEN, FROZEN_PEAKS -> y <= config.seaLevel() ? PACKED_ICE : (deepOceanFloorNoise(x, z) > 0.55 ? ICE : SNOW_BLOCK);
            case JUNGLE_LUSH -> MOSS;
            case DRIPSTONE_CAVES -> DRIPSTONE;
            case DEEP_DARK -> SCULK;
            case GRAVELLY -> GRAVEL;
            case DARK_FOREST -> PODZOL;
            case MUSHROOM_FIELDS -> MYCELIUM;
            case BIRCH_FOREST -> y > config.seaLevel() - 6 ? GRASS : DIRT;
            case CRIMSON_FOREST -> CRIMSON_NYLIUM;
            case WARPED_FOREST -> WARPED_NYLIUM;
            case SOUL_SAND_VALLEY -> SOUL_SAND;
            case TEMPERATE -> y > config.seaLevel() - 6 ? GRASS : DIRT;
        };
    }

    private static BlockState nearSurfaceBlockFor(NamekianDensitySampler.BiomeRegion region) {
        return switch (region) {
            case JUNGLE_LUSH -> MUD;
            case FROZEN, FROZEN_PEAKS -> DIRT;
            case OCEAN, DEEP_OCEAN, GRAVELLY -> GRAVEL;
            case DARK_FOREST -> PODZOL;
            case MUSHROOM_FIELDS -> MYCELIUM;
            case CRIMSON_FOREST, WARPED_FOREST -> NETHERRACK;
            case SOUL_SAND_VALLEY -> SOUL_SOIL;
            default -> DIRT;
        };
    }

    private static BlockState deepBiomeBlockFor(NamekianDensitySampler.BiomeRegion region, int x, int y, int z, NamekianDreamsConfig config) {
        if (y > config.seaLevel() - 36) return null;
        double noise = FieldMath.valueNoise3D(0x4E414D4542494F4DL, x, y, z, 0.085);
        return switch (region) {
            case JUNGLE_LUSH -> noise > 0.50 ? MOSS : noise < -0.56 ? CLAY() : null;
            case DRIPSTONE_CAVES -> noise > 0.28 ? DRIPSTONE : noise < -0.50 ? CALCITE : null;
            case DEEP_DARK -> noise > 0.40 ? SCULK : noise < -0.42 ? TUFF : null;
            case FROZEN, FROZEN_PEAKS -> noise > 0.58 ? PACKED_ICE : null;
            case DARK_FOREST -> noise > 0.54 ? PODZOL : null;
            case MUSHROOM_FIELDS -> noise > 0.35 ? MYCELIUM : null;
            case CRIMSON_FOREST -> noise > -0.10 ? CRIMSON_NYLIUM : NETHERRACK;
            case WARPED_FOREST -> noise > -0.10 ? WARPED_NYLIUM : NETHERRACK;
            case SOUL_SAND_VALLEY -> noise > 0.05 ? SOUL_SAND : SOUL_SOIL;
            default -> null;
        };
    }

    private static BlockState CLAY() { return Blocks.CLAY.defaultBlockState(); }

    private static double deepOceanFloorNoise(int x, int z) {
        return FieldMath.valueNoise2D(0x4E414D454F43454EL, x, z, 0.035);
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
        lines.add("Namekian lava lakes: enabled=" + config.enableLargeLavaLakes() + " start_y=" + config.deepLavaStartY() + " threshold=" + config.lavaLakeThreshold());
        lines.add("Namekian ores: multiplier=" + config.globalOreMultiplier() + " megaveins=" + config.enableMegaveins());
    }
}
