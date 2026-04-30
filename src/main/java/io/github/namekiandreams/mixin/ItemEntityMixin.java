package io.github.namekiandreams.mixin;

import io.github.namekiandreams.init.NamekianPortalActivation;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void namekian_dreams_world$activatePortal(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide || !self.isAlive()) {
            return;
        }
        NamekianPortalActivation.tryFromItemEntity(self);
    }
}
