package io.github.namekiandreams.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityPortalEntranceAccessor {
    @Accessor("portalEntrancePos")
    BlockPos namekian_dreams_world$getPortalEntrancePos();
}
