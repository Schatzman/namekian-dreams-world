package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.client.NamekianClientColors;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class NamekianWaterFogMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("RETURN"))
    private static void namekian_dreams_world$forceNamekianWaterFog(Camera camera, float tickDelta, ClientLevel level, int renderDistance, float darkenWorldAmount, CallbackInfo ci) {
        if (camera.getFluidInCamera() == FogType.WATER && NamekianClientColors.isNamekianLevel(level)) {
            Vec3 color = NamekianClientColors.waterFogColor(NamekianDreamsWorld.CONFIG.validate());
            fogRed = (float) color.x;
            fogGreen = (float) color.y;
            fogBlue = (float) color.z;
        }
    }
}
