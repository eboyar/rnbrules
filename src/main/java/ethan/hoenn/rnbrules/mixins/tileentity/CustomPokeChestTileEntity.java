package ethan.hoenn.rnbrules.mixins.tileentity;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;

import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;

@Mixin(value = PokeChestTileEntity.class, remap = false)
public abstract class CustomPokeChestTileEntity {

    @Shadow private boolean dropOneTime;

    @Inject(method = "canClaim(Ljava/util/UUID;)Z", at = @At("HEAD"), cancellable = true)
    private void onCanClaim(UUID playerID, CallbackInfoReturnable<Boolean> cir) {
        if (this.dropOneTime) {
            ProgressionManager progressionManager = ProgressionManager.get();
            String chestId = ((PokeChestTileEntity)(Object)this).getBlockPos().toString();

            
            if (progressionManager.hasClaimedPokeChest(playerID, chestId)) {
                cir.setReturnValue(false);
            }
            
            else {
                cir.setReturnValue(true);
            }
        } 
    }

    @Inject(method = "addClaimer(Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void onAddClaimer(UUID playerID, CallbackInfo ci) {
        if (this.dropOneTime) {
            ProgressionManager progressionManager = ProgressionManager.get();
            String chestId = ((PokeChestTileEntity)(Object)this).getBlockPos().toString();

            progressionManager.markPokeChestClaimed(playerID, chestId);

            ci.cancel();
        }
        
    }
}
