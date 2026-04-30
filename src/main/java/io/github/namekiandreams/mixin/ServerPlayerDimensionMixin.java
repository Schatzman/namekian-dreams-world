package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.util.PortalLanding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDimensionMixin {
    @Inject(method = "findDimensionEntryPoint", at = @At("RETURN"), cancellable = true)
    private void namekian_dreams_world$adjustNamekianExit(ServerLevel dest, CallbackInfoReturnable<PortalInfo> cir) {
        PortalInfo info = cir.getReturnValue();
        if (info == null || !dest.dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS)) {
            return;
        }
        Vec3 original = info.pos;
        Vec3 adjusted = PortalLanding.nudgeNamekianExit(dest, original);
        if (original.distanceToSqr(adjusted) < 1e-4) {
            return;
        }
        cir.setReturnValue(new PortalInfo(adjusted, info.speed, info.yRot, info.xRot));
    }
}
