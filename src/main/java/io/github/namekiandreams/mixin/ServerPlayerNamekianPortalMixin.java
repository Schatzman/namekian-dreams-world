package io.github.namekiandreams.mixin;

import io.github.namekiandreams.init.ModBlocks;
import io.github.namekiandreams.portal.NamekianPortalTravel;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerNamekianPortalMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("namekian_dreams_world/portal_exit");

    @Inject(method = "getExitPortal", at = @At("HEAD"), cancellable = true)
    private void namekian_dreams_world$exitPortal(
            ServerLevel dest,
            BlockPos scaledPos,
            boolean isDestNether,
            WorldBorder border,
            CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> cir
    ) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        BlockPos entrance = ((EntityPortalEntranceAccessor) self).namekian_dreams_world$getPortalEntrancePos();
        if (entrance == null) {
            return;
        }
        BlockState state = self.level().getBlockState(entrance);
        if (!state.is(ModBlocks.NAMEKIAN_PORTAL)) {
            return;
        }
        Direction.Axis axis = state.getValue(NetherPortalBlock.AXIS);
        Optional<BlockUtil.FoundRectangle> found = NamekianPortalTravel.findPortalAround(dest, scaledPos, isDestNether, border);
        if (found.isPresent()) {
            cir.setReturnValue(found);
            cir.cancel();
            return;
        }
        Optional<BlockUtil.FoundRectangle> created = NamekianPortalTravel.createPortal(dest, scaledPos, axis, border);
        if (created.isEmpty()) {
            LOGGER.error("Unable to create a Namekian portal, likely target out of worldborder");
        }
        cir.setReturnValue(created);
        cir.cancel();
    }
}
