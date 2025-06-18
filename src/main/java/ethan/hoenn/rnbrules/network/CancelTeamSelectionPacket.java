package ethan.hoenn.rnbrules.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class CancelTeamSelectionPacket {

	//this is specifically for neater compatibility with the MultiBattle system
	//pixelmon's native CancelTeamSelectionPacket is fine for all other cases
	private final String reason;

	public CancelTeamSelectionPacket(String reason) {
		this.reason = reason;
	}

	public static void encode(CancelTeamSelectionPacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.reason);
	}

	public static CancelTeamSelectionPacket decode(PacketBuffer buffer) {
		return new CancelTeamSelectionPacket(buffer.readUtf(32767));
	}

	public static void handle(CancelTeamSelectionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (context.getDirection().getReceptionSide().isClient()) {
				handleOnClient(packet);
			}
		});
		context.setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	private static void handleOnClient(CancelTeamSelectionPacket packet) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof com.pixelmonmod.pixelmon.client.gui.battles.rules.TeamSelectScreen) {
			mc.setScreen(null);
		}

		if (packet.reason != null && !packet.reason.isEmpty()) {
			mc.player.displayClientMessage(new net.minecraft.util.text.StringTextComponent(packet.reason), false);
		}
	}
}
