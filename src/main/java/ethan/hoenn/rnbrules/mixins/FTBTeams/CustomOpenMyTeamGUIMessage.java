package ethan.hoenn.rnbrules.mixins.FTBTeams;

import dev.ftb.mods.ftbteams.net.OpenMyTeamGUIMessage;
import me.shedaniel.architectury.networking.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenMyTeamGUIMessage.class)
public class CustomOpenMyTeamGUIMessage {

	@Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
	private void restrictToOperators(NetworkManager.PacketContext context, CallbackInfo ci) {
		ci.cancel();
	}
}
