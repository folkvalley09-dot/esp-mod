package com.example.espmod.mixin;

import com.example.espmod.Freecam;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Mouse movement rotates the freecam instead of the player.
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void espmod$look(double dx, double dy, CallbackInfo ci) {
        if (Freecam.active && (Object) this instanceof ClientPlayerEntity) {
            Freecam.yaw += (float) dx * 0.15f;
            Freecam.pitch = MathHelper.clamp(Freecam.pitch + (float) dy * 0.15f, -90f, 90f);
            ci.cancel();
        }
    }
}
