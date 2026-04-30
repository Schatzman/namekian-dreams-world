package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.client.NamekianClientColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class NamekianClientAtmosphereMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void namekian_dreams_world$forceNamekianSkyColor(Vec3 cameraPos, float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (NamekianClientColors.isNamekianLevel(level)) {
            cir.setReturnValue(NamekianClientColors.skyColor(NamekianDreamsWorld.CONFIG.validate()));
        }
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void namekian_dreams_world$forceNamekianCloudColor(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (NamekianClientColors.isNamekianLevel(level)) {
            cir.setReturnValue(NamekianClientColors.cloudColor(NamekianDreamsWorld.CONFIG.validate()));
        }
    }
}
