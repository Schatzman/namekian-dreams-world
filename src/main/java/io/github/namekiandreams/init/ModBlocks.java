package io.github.namekiandreams.init;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.block.NamekianPortalBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static NamekianPortalBlock NAMEKIAN_PORTAL;

    private ModBlocks() {
    }

    public static void register() {
        NAMEKIAN_PORTAL = Registry.register(
                BuiltInRegistries.BLOCK,
                NamekianDreamsWorld.id("namekian_portal"),
                new NamekianPortalBlock(
                        BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL)
                                .noCollission()
                                .strength(-1.0F)
                                .lightLevel(state -> 11)));
        Registry.register(
                BuiltInRegistries.ITEM,
                NamekianDreamsWorld.id("namekian_portal"),
                new BlockItem(NAMEKIAN_PORTAL, new Item.Properties()));
    }
}
