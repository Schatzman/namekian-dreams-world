package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.worldgen.light.NamekianDaylightOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAndTintGetter.class)
public abstract class NamekianSkyLightMixin {
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private void namekian_dreams_world$offsetNamekianSkyLight(LightLayer lightLayer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (lightLayer == LightLayer.SKY && self instanceof Level level && namekian_dreams_world$isNamekianLevel(level)) {
            cir.setReturnValue(NamekianDaylightOffset.applyOutdoorSkyLightOffset(cir.getReturnValueI(), NamekianDreamsWorld.CONFIG.validate()));
        }
    }

    @Unique
    private static boolean namekian_dreams_world$isNamekianLevel(Level level) {
        return level.dimensionType().effectsLocation().equals(NamekianDreamsWorld.id("namekian_overworld"));
    }
}
