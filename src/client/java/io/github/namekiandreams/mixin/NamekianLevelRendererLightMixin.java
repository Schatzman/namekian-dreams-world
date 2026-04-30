package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.client.NamekianClientColors;
import io.github.namekiandreams.worldgen.light.NamekianPackedLight;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class NamekianLevelRendererLightMixin {
    @Inject(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true)
    private static void namekian_dreams_world$dimNamekianPackedLight(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (level instanceof ClientLevel clientLevel && NamekianClientColors.isNamekianLevel(clientLevel)) {
            cir.setReturnValue(NamekianPackedLight.adjustPackedSkyLight(cir.getReturnValueI(), NamekianDreamsWorld.CONFIG.validate()));
        }
    }
}
