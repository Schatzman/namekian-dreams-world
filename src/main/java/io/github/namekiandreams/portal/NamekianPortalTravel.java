package io.github.namekiandreams.portal;

import io.github.namekiandreams.init.ModBlocks;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public final class NamekianPortalTravel {
    private static final int SEARCH_RADIUS = 128;
    private static final int CREATE_RADIUS = 16;
    private static final BlockState FRAME_BLOCK = Blocks.EMERALD_BLOCK.defaultBlockState();

    private NamekianPortalTravel() {
    }

    public static Optional<BlockUtil.FoundRectangle> findPortalAround(
            ServerLevel level,
            BlockPos center,
            boolean shortRange,
            WorldBorder border
    ) {
        int horizontalRadius = shortRange ? 16 : SEARCH_RADIUS;
        PoiManager poi = level.getPoiManager();
        poi.ensureLoadedAndValid(level, center, horizontalRadius);
        BlockPos bestFound = null;
        double bestDist = Double.MAX_VALUE;
        int maxY = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
        for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(center, horizontalRadius, Direction.EAST, Direction.SOUTH)) {
            if (!border.isWithinBounds(column)) {
                continue;
            }
            int surface = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
            int yHigh = Math.min(maxY, surface + 64);
            int yLow = Math.max(level.getMinBuildHeight(), surface - 128);
            for (int y = yHigh; y >= yLow; y--) {
                column.setY(y);
                BlockState state = level.getBlockState(column);
                if (!state.is(ModBlocks.NAMEKIAN_PORTAL)) {
                    continue;
                }
                Direction.Axis axis = state.getValue(NetherPortalBlock.AXIS);
                Optional<BlockUtil.FoundRectangle> rectangle = rectangleFromPortalBlock(level, column.immutable(), axis, border);
                if (rectangle.isEmpty()) {
                    continue;
                }
                double distance = center.distSqr(new BlockPos(column.getX(), y, column.getZ()));
                if (bestFound == null || distance < bestDist) {
                    bestDist = distance;
                    bestFound = column.immutable();
                }
            }
        }
        if (bestFound == null) {
            return Optional.empty();
        }
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(bestFound), 3, bestFound);
        BlockState state = level.getBlockState(bestFound);
        Direction.Axis axis = state.getValue(NetherPortalBlock.AXIS);
        return rectangleFromPortalBlock(level, bestFound, axis, border);
    }

    private static Optional<BlockUtil.FoundRectangle> rectangleFromPortalBlock(
            ServerLevel level,
            BlockPos portalBlock,
            Direction.Axis axis,
            WorldBorder border
    ) {
        BlockState template = level.getBlockState(portalBlock);
        BlockUtil.FoundRectangle rectangle = BlockUtil.getLargestRectangleAround(
                portalBlock,
                axis,
                NamekianFrameValidator.MAX_WIDTH,
                Direction.Axis.Y,
                NamekianFrameValidator.MAX_HEIGHT,
                pos -> level.getBlockState(pos) == template
        );
        if (rectangle.axis1Size < NamekianFrameValidator.MIN_WIDTH || rectangle.axis2Size < NamekianFrameValidator.MIN_HEIGHT) {
            return Optional.empty();
        }
        if (!border.isWithinBounds(rectangle.minCorner)) {
            return Optional.empty();
        }
        return Optional.of(rectangle);
    }

    public static Optional<BlockUtil.FoundRectangle> createPortal(
            ServerLevel level,
            BlockPos target,
            Direction.Axis axis,
            WorldBorder border
    ) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double best = -1.0;
        BlockPos bestPos = null;
        double fallback = -1.0;
        BlockPos fallbackPos = null;
        int maxY = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos scratchTarget = target.mutable();

        for (BlockPos.MutableBlockPos mutable : BlockPos.spiralAround(target, CREATE_RADIUS, Direction.EAST, Direction.SOUTH)) {
            int surfaceY = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutable.getX(), mutable.getZ()));
            if (border.isWithinBounds(mutable) && border.isWithinBounds(mutable.move(direction, 1))) {
                mutable.move(direction.getOpposite(), 1);
                for (int y = surfaceY; y >= level.getMinBuildHeight(); y--) {
                    mutable.setY(y);
                    if (!canPortalReplaceBlock(level, mutable)) {
                        continue;
                    }
                    int topY = y;
                    while (y > level.getMinBuildHeight() && canPortalReplaceBlock(level, mutable.move(Direction.DOWN))) {
                        y--;
                    }
                    if (y + 4 <= maxY) {
                        int heightDelta = topY - y;
                        if (heightDelta <= 0 || heightDelta >= 3) {
                            mutable.setY(y);
                            if (canHostFrame(level, border, mutable, scratchTarget, direction, 0)) {
                                double distance = target.distSqr(mutable);
                                if (canHostFrame(level, border, mutable, scratchTarget, direction, -1)
                                        && canHostFrame(level, border, mutable, scratchTarget, direction, 1)
                                        && (best == -1.0 || best > distance)) {
                                    best = distance;
                                    bestPos = mutable.immutable();
                                }
                                if (best == -1.0 && (fallback == -1.0 || fallback > distance)) {
                                    fallback = distance;
                                    fallbackPos = mutable.immutable();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (best == -1.0 && fallback != -1.0) {
            bestPos = fallbackPos;
            best = fallback;
        }

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        if (best == -1.0) {
            int minPortalY = Math.max(level.getMinBuildHeight() + 1, 70);
            int maxPortalY = maxY - 9;
            if (maxPortalY < minPortalY) {
                return Optional.empty();
            }
            bestPos = new BlockPos(target.getX(), Mth.clamp(target.getY(), minPortalY, maxPortalY), target.getZ()).immutable();
            Direction perpendicular = direction.getClockWise();
            if (!border.isWithinBounds(bestPos)) {
                return Optional.empty();
            }
            for (int k = -1; k < 2; k++) {
                for (int lx = 0; lx < 2; lx++) {
                    for (int m = -1; m < 3; m++) {
                        BlockState place = m < 0 ? FRAME_BLOCK : Blocks.AIR.defaultBlockState();
                        mutableBlockPos.setWithOffset(
                                bestPos,
                                lx * direction.getStepX() + k * perpendicular.getStepX(),
                                m,
                                lx * direction.getStepZ() + k * perpendicular.getStepZ()
                        );
                        level.setBlockAndUpdate(mutableBlockPos, place);
                    }
                }
            }
        }

        for (int width = -1; width < 3; width++) {
            for (int y = -1; y < 4; y++) {
                if (width == -1 || width == 2 || y == -1 || y == 3) {
                    mutableBlockPos.setWithOffset(bestPos, width * direction.getStepX(), y, width * direction.getStepZ());
                    level.setBlock(mutableBlockPos, FRAME_BLOCK, 3);
                }
            }
        }

        BlockState portal = ModBlocks.NAMEKIAN_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int width = 0; width < 2; width++) {
            for (int y = 0; y < 3; y++) {
                mutableBlockPos.setWithOffset(bestPos, width * direction.getStepX(), y, width * direction.getStepZ());
                level.setBlock(mutableBlockPos, portal, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(bestPos.immutable(), 2, 3));
    }

    private static boolean canPortalReplaceBlock(ServerLevel level, BlockPos.MutableBlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean canHostFrame(
            ServerLevel level,
            WorldBorder border,
            BlockPos origin,
            BlockPos.MutableBlockPos scratch,
            Direction direction,
            int widthOffset
    ) {
        Direction perpendicular = direction.getClockWise();
        for (int j = -1; j < 3; j++) {
            for (int k = -1; k < 4; k++) {
                scratch.setWithOffset(
                        origin,
                        direction.getStepX() * j + perpendicular.getStepX() * widthOffset,
                        k,
                        direction.getStepZ() * j + perpendicular.getStepZ() * widthOffset
                );
                if (!border.isWithinBounds(scratch)) {
                    return false;
                }
                if (k < 0 && !level.getBlockState(scratch).isSolid()) {
                    return false;
                }
                if (k >= 0 && !canPortalReplaceBlock(level, scratch)) {
                    return false;
                }
            }
        }
        return true;
    }
}
