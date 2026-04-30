package io.github.namekiandreams.mixin;

import io.github.namekiandreams.NamekianDreamsWorld;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityNamekianFindDimensionEntryMixin {
    @Shadow
    protected BlockPos portalEntrancePos;

    @Shadow
    protected abstract Optional<BlockUtil.FoundRectangle> getExitPortal(
            ServerLevel serverLevel, BlockPos blockPos, boolean bl, WorldBorder worldBorder
    );

    @Shadow
    protected abstract Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle);

    /**
     * @author Governance Nexus
     * @reason Include namekian_dreams_world:namekian_dreams in vanilla nether-portal entry resolution.
     */
    @Nullable
    @Overwrite
    public PortalInfo findDimensionEntryPoint(ServerLevel serverLevel) {
        Entity self = (Entity) (Object) this;
        boolean returningFromEnd = self.level().dimension() == Level.END && serverLevel.dimension() == Level.OVERWORLD;
        boolean enteringEnd = serverLevel.dimension() == Level.END;
        if (!returningFromEnd && !enteringEnd) {
            boolean destinationPortalDimension = serverLevel.dimension() == Level.NETHER
                    || serverLevel.dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS);
            boolean sourcePortalDimension = self.level().dimension() == Level.NETHER
                    || self.level().dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS);
            if (!sourcePortalDimension && !destinationPortalDimension) {
                return null;
            }
            boolean shortRange = destinationPortalDimension;
            WorldBorder worldBorder = serverLevel.getWorldBorder();
            double scale = DimensionType.getTeleportationScale(self.level().dimensionType(), serverLevel.dimensionType());
            BlockPos scaledPos = worldBorder.clampToBounds(self.getX() * scale, self.getY(), self.getZ() * scale);
            return this.getExitPortal(serverLevel, scaledPos, shortRange, worldBorder)
                    .map(foundRectangle -> {
                        BlockState blockState = self.level().getBlockState(this.portalEntrancePos);
                        Direction.Axis axis;
                        Vec3 relativePos;
                        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                            axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                            BlockUtil.FoundRectangle sourceRectangle = BlockUtil.getLargestRectangleAround(
                                    this.portalEntrancePos, axis, 21, Direction.Axis.Y, 21,
                                    pos -> self.level().getBlockState(pos) == blockState
                            );
                            relativePos = this.getRelativePortalPosition(axis, sourceRectangle);
                        } else {
                            axis = Direction.Axis.X;
                            relativePos = new Vec3(0.5, 0.0, 0.0);
                        }
                        return PortalShape.createPortalInfo(
                                serverLevel, foundRectangle, axis, relativePos, self,
                                self.getDeltaMovement(), self.getYRot(), self.getXRot()
                        );
                    })
                    .orElse(null);
        }
        BlockPos destination;
        if (enteringEnd) {
            destination = ServerLevel.END_SPAWN_POINT;
        } else {
            destination = serverLevel.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, serverLevel.getSharedSpawnPos()
            );
        }
        return new PortalInfo(
                new Vec3(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5),
                self.getDeltaMovement(), self.getYRot(), self.getXRot()
        );
    }
}
