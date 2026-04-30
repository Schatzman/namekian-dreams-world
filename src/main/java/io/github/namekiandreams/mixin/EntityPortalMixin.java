package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public abstract class EntityPortalMixin {
    @Shadow
    protected BlockPos portalEntrancePos;

    @ModifyArg(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            ),
            index = 0
    )
    private ResourceKey<Level> namekian_dreams_world$portalTarget(ResourceKey<Level> key) {
        Entity entity = (Entity) (Object) this;
        if (portalEntrancePos == null) {
            return key;
        }
        if (entity.level().getBlockState(portalEntrancePos).is(ModBlocks.NAMEKIAN_PORTAL)) {
            if (entity.level().dimension() == Level.OVERWORLD) {
                return NamekianDreamsWorld.NAMEKIAN_DREAMS;
            }
            if (entity.level().dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS)) {
                return Level.OVERWORLD;
            }
        }
        return key;
    }
}
