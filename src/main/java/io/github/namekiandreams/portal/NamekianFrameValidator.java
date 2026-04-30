package io.github.namekiandreams.portal;

import io.github.namekiandreams.NamekianDreamsWorld;
import io.github.namekiandreams.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NamekianFrameValidator {
    public static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final int SEARCH_RADIUS = 3;

    public record ResolvedFrame(Direction.Axis axis, BlockPos minInside, int width, int height) {
    }

    @FunctionalInterface
    public interface CellPredicate {
        CellType cellAt(int u, int v);
    }

    public enum CellType {
        FRAME,
        INNER,
        INVALID
    }

    private NamekianFrameValidator() {
    }

    public static ResolvedFrame findNear(BlockGetter level, BlockPos from) {
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    BlockPos target = from.offset(dx, dy, dz);
                    ResolvedFrame z = tryZ(level, target.getX(), target.getY(), target.getZ());
                    if (z != null) {
                        return z;
                    }
                    ResolvedFrame x = tryX(level, target.getX(), target.getY(), target.getZ());
                    if (x != null) {
                        return x;
                    }
                }
            }
        }
        return null;
    }

    public static boolean frameCellsValid(int width, int height, CellPredicate cells) {
        if (width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
            return false;
        }
        for (int u = -1; u <= width; u++) {
            for (int v = -1; v <= height; v++) {
                boolean inner = u >= 0 && u < width && v >= 0 && v < height;
                CellType cell = cells.cellAt(u, v);
                if (inner) {
                    if (cell != CellType.INNER) {
                        return false;
                    }
                } else if (cell != CellType.FRAME) {
                    return false;
                }
            }
        }
        return true;
    }

    private static ResolvedFrame tryZ(BlockGetter level, int minX, int minY, int z) {
        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
                int w = width;
                int h = height;
                if (frameCellsValid(w, h, (u, v) -> classify(level.getBlockState(new BlockPos(minX + u, minY + v, z))))) {
                    return new ResolvedFrame(Direction.Axis.Z, new BlockPos(minX, minY, z), w, h);
                }
            }
        }
        return null;
    }

    private static ResolvedFrame tryX(BlockGetter level, int x, int minY, int minZ) {
        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
                int w = width;
                int h = height;
                if (frameCellsValid(w, h, (u, v) -> classify(level.getBlockState(new BlockPos(x, minY + v, minZ + u))))) {
                    return new ResolvedFrame(Direction.Axis.X, new BlockPos(x, minY, minZ), w, h);
                }
            }
        }
        return null;
    }

    private static CellType classify(BlockState state) {
        if (isFrame(state)) {
            return CellType.FRAME;
        }
        if (isInnerOk(state)) {
            return CellType.INNER;
        }
        return CellType.INVALID;
    }

    private static boolean isFrame(BlockState state) {
        return state.is(NamekianDreamsWorld.NAMEKIAN_PORTAL_FRAME);
    }

    private static boolean isInnerOk(BlockState state) {
        return state.isAir() || state.is(Blocks.CAVE_AIR) || state.is(Blocks.NETHER_PORTAL) || state.is(ModBlocks.NAMEKIAN_PORTAL);
    }
}
