package io.github.namekiandreams.init;

import io.github.namekiandreams.portal.NamekianFrameValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class NamekianPortalActivation {
    private NamekianPortalActivation() {
    }

    public static void tryFromItemEntity(ItemEntity item) {
        if (item.getItem().getItem() != Items.FIRE_CHARGE) {
            return;
        }
        if (!(item.level() instanceof ServerLevel level)) {
            return;
        }
        NamekianFrameValidator.ResolvedFrame frame = NamekianFrameValidator.findNear(level, item.blockPosition());
        if (frame == null) {
            return;
        }
        fillPortal(level, frame);
        item.getItem().shrink(1);
        if (item.getItem().isEmpty()) {
            item.discard();
        }
    }

    public static void fillPortal(ServerLevel level, NamekianFrameValidator.ResolvedFrame frame) {
        Direction.Axis portalAxis = frame.axis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        BlockState portal = ModBlocks.NAMEKIAN_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, portalAxis);
        for (int u = 0; u < frame.width(); u++) {
            for (int v = 0; v < frame.height(); v++) {
                BlockPos pos = frame.axis() == Direction.Axis.Z
                        ? new BlockPos(frame.minInside().getX() + u, frame.minInside().getY() + v, frame.minInside().getZ())
                        : new BlockPos(frame.minInside().getX(), frame.minInside().getY() + v, frame.minInside().getZ() + u);
                level.setBlock(pos, portal, 3);
            }
        }
    }
}
