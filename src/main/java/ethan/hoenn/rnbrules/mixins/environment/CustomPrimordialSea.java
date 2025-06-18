package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.PrimordialSea;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimordialSea.class)
public class CustomPrimordialSea {
    
    
    @Inject(method = "applySwitchOutEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private void preventSwitchOutRemoval(PixelmonWrapper oldPokemon, CallbackInfo ci) {
        
        ci.cancel();
    }
    
    
    @Inject(method = "onAbilityLost", at = @At("HEAD"), cancellable = true, remap = false)  
    private void preventAbilityLostRemoval(PixelmonWrapper pokemon, CallbackInfo ci) {
        
        ci.cancel();
    }
    
    
    @Inject(method = "checkForRemoval", at = @At("HEAD"), cancellable = true, remap = false)
    private void preventRemoval(PixelmonWrapper pokemon, CallbackInfo ci) {
        
        ci.cancel();
    }
}
