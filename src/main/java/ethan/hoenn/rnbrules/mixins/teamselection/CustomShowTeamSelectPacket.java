package ethan.hoenn.rnbrules.mixins.teamselection;

import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.rules.selection.ShowTeamSelectPacket;
import ethan.hoenn.rnbrules.network.LinkedTrainerPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.network.TagBattlePacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShowTeamSelectPacket.class)
public class CustomShowTeamSelectPacket {

	@OnlyIn(Dist.CLIENT)
	@Inject(method = "handlePacket", at = @At("HEAD"), remap = false)
	private void beforeHandlePacket(NetworkEvent.Context context, CallbackInfo ci) {
		ShowTeamSelectPacket packet = (ShowTeamSelectPacket) (Object) this;

		if (packet.npcID > 0) {
			PacketHandler.INSTANCE.sendToServer(new LinkedTrainerPacket.Request(packet.npcID));
			PacketHandler.INSTANCE.sendToServer(new TagBattlePacket.Request(packet.npcID));
		}
	}
}
