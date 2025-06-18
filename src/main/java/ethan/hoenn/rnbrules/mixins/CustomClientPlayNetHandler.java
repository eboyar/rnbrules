package ethan.hoenn.rnbrules.mixins;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetHandler.class)
public class CustomClientPlayNetHandler {

	@Redirect(method = "handleGameEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setRainLevel(F)V", ordinal = 1))
	private void redirectSetRainLevelOnStopRaining(ClientWorld world, float originalRainLevel) {
		world.setRainLevel(0.0F);
	}
}
