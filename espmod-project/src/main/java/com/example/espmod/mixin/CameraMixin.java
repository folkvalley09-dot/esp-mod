package com.example.espmod.mixin;

import com.example.espmod.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("RETURN"))
    private void espmod$freecam(BlockView area, Entity focused, boolean thirdPerson,
                                boolean inverse, float tickDelta, CallbackInfo ci) {
        if (Freecam.active) {
            setPos(Freecam.x, Freecam.y, Freecam.z);
            setRotation(Freecam.yaw, Freecam.pitch);
        }
    }

    // Lets you see your own body while the camera is detached.
    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void espmod$showBody(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.active) cir.setReturnValue(true);
    }
}
