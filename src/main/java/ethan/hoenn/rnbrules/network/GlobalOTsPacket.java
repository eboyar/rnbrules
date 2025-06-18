package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerGui;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class GlobalOTsPacket {

	private String globalOTName;
	private boolean hasGlobalOT;

	public GlobalOTsPacket(String globalOTName, boolean hasGlobalOT) {
		this.globalOTName = globalOTName;
		this.hasGlobalOT = hasGlobalOT;
	}

	public static void encode(GlobalOTsPacket msg, PacketBuffer buf) {
		buf.writeUtf(msg.globalOTName);
		buf.writeBoolean(msg.hasGlobalOT);
	}

	public static GlobalOTsPacket decode(PacketBuffer buf) {
		String otName = buf.readUtf();
		boolean hasOT = buf.readBoolean();
		return new GlobalOTsPacket(otName, hasOT);
	}

	public static void handle(GlobalOTsPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				GamecornerGui.setClientGlobalOT(msg.globalOTName, msg.hasGlobalOT);
			});
		ctx.get().setPacketHandled(true);
	}
}
