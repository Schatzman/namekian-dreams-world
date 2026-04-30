package io.github.namekiandreams;

import io.github.namekiandreams.config.NamekianDreamsConfig;
import io.github.namekiandreams.init.ModBlocks;
import io.github.namekiandreams.worldgen.NamekianBiomeSource;
import io.github.namekiandreams.worldgen.NamekianChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NamekianDreamsWorld implements ModInitializer {
    public static final String MOD_ID = "namekian_dreams_world";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ResourceKey<Level> NAMEKIAN_DREAMS = ResourceKey.create(
            Registries.DIMENSION,
            id("namekian_dreams")
    );
    public static final TagKey<Block> NAMEKIAN_PORTAL_FRAME = TagKey.create(
            Registries.BLOCK,
            id("namekian_portal_frame")
    );
    public static NamekianDreamsConfig CONFIG = NamekianDreamsConfig.defaults();

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CONFIG = NamekianDreamsConfig.load(FabricLoader.getInstance().getConfigDir());
        Registry.register(BuiltInRegistries.BIOME_SOURCE, id("namekian_biome_source"), NamekianBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("namekian_chunk_generator"), NamekianChunkGenerator.CODEC);
        ModBlocks.register();
        LOGGER.info("Loaded Namekian Dreams World config: min_y={}, height={}, max_y={}, fractal_strength={}",
                CONFIG.minY(), CONFIG.height(), CONFIG.maxY(), CONFIG.fractalStrength());
    }
}
