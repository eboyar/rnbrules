package ethan.hoenn.rnbrules.mixins.client;

import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import ethan.hoenn.rnbrules.utils.misc.StatueVisibilityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatueEntity.class)
public class CustomStatueEntityRenderer {

	@Inject(method = "getPixelmonScale", at = @At("RETURN"), cancellable = true, remap = false)
	private void onGetPixelmonScale(CallbackInfoReturnable<Float> cir) {
		StatueEntity self = (StatueEntity) (Object) this;

		if (self.level.isClientSide && !StatueVisibilityTracker.shouldRenderStatue(self.getUUID())) {
			cir.setReturnValue(0.001f);
		}
	}
}
