package com.example.espmod.mixin;

import com.example.espmod.Freecam;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    // Keep your real player still while flying the camera around.
    @Inject(method = "tick", at = @At("TAIL"))
    private void espmod$freeze(CallbackInfo ci) {
        if (Freecam.active) {
            this.playerInput = PlayerInput.DEFAULT;
            this.movementForward = 0f;
            this.movementSideways = 0f;
        }
    }
}
