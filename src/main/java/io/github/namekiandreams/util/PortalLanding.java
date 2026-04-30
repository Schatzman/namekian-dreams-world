package io.github.namekiandreams.util;

import io.github.namekiandreams.NamekianDreamsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class PortalLanding {
    private static final int SEARCH_VERTICAL = 192;

    private PortalLanding() {
    }

    public static Vec3 nudgeNamekianExit(ServerLevel level, Vec3 pos) {
        if (!level.dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS)) {
            return pos;
        }
        int x = Mth.floor(pos.x);
        int z = Mth.floor(pos.z);
        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
        int minFeetY = level.getMinBuildHeight() + 1;
        int maxFeetY = level.getMaxBuildHeight() - 2;
        int startY = Mth.clamp(Mth.floor(pos.y), minFeetY, maxFeetY);
        for (int y = startY; y >= minFeetY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isDryFooting(level, feet)) {
                return new Vec3(pos.x, y + 0.01, pos.z);
            }
        }
        for (int y = startY + 1; y <= Math.min(startY + SEARCH_VERTICAL, maxFeetY); y++) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isDryFooting(level, feet)) {
                return new Vec3(pos.x, y + 0.01, pos.z);
            }
        }
        for (int y = maxFeetY; y >= minFeetY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isDryFooting(level, feet)) {
                return new Vec3(pos.x, y + 0.01, pos.z);
            }
        }
        int motionBlocking = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        int safeY = Mth.clamp(motionBlocking, minFeetY, maxFeetY);
        if (isDryFooting(level, new BlockPos(x, safeY, z))) {
            return new Vec3(pos.x, safeY + 0.01, pos.z);
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int surfaceY = Mth.clamp(surface, minFeetY, maxFeetY);
        if (isDryFooting(level, new BlockPos(x, surfaceY, z))) {
            return new Vec3(pos.x, surfaceY + 0.01, pos.z);
        }
        return new Vec3(pos.x, (double) safeY + 0.01, pos.z);
    }

    public static void nudgeNamekianExit(Entity entity) {
        if (entity.level().isClientSide || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(NamekianDreamsWorld.NAMEKIAN_DREAMS)) {
            return;
        }
        Vec3 adjusted = nudgeNamekianExit(level, entity.position());
        entity.setPos(adjusted.x, adjusted.y, adjusted.z);
    }

    private static boolean isDryFooting(ServerLevel level, BlockPos feet) {
        if (feet.getY() < level.getMinBuildHeight() + 1 || feet.getY() >= level.getMaxBuildHeight() - 1) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(feet.above()).isEmpty()) {
            return false;
        }
        BlockState below = level.getBlockState(feet.below());
        return !below.getCollisionShape(level, feet.below()).isEmpty();
    }
}
