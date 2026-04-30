package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.worldgen.light.NamekianDaylightOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAndTintGetter.class)
public interface NamekianBrightnessMixin {
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private void namekian_dreams_world$offsetNamekianSkyBrightness(LightLayer lightLayer, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (lightLayer == LightLayer.SKY && (Object) this instanceof Level level
                && level.dimensionType().effectsLocation().equals(NamekianDreamsWorld.id("namekian_overworld"))) {
            cir.setReturnValue(NamekianDaylightOffset.applyOutdoorSkyLightOffset(cir.getReturnValueI(), NamekianDreamsWorld.CONFIG.validate()));
        }
    }
}
