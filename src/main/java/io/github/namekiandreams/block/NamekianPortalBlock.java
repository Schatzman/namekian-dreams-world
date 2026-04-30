package io.github.namekiandreams.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public final class NamekianPortalBlock extends NetherPortalBlock {
    private static final Vector3f NAMEKIAN_GREEN = new Vector3f(0.027F, 0.459F, 0.388F); // #077563
    private static final Vector3f BRIGHT_GREEN = new Vector3f(0.18F, 0.82F, 0.68F);

    public NamekianPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            float scale = 0.55F + 0.35F * random.nextFloat();
            level.addParticle(new DustColorTransitionOptions(NAMEKIAN_GREEN, BRIGHT_GREEN, scale), x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
