package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerGui;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class BadgesPacket {

	private final Set<String> playerBadges;

	public BadgesPacket(Set<String> playerBadges) {
		this.playerBadges = playerBadges;
	}

	public static void encode(BadgesPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.playerBadges.size());
		for (String badge : msg.playerBadges) {
			buf.writeUtf(badge);
		}
	}

	public static BadgesPacket decode(PacketBuffer buf) {
		int size = buf.readInt();
		Set<String> badges = new HashSet<>();
		for (int i = 0; i < size; i++) {
			badges.add(buf.readUtf());
		}
		return new BadgesPacket(badges);
	}

	public static void handle(BadgesPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				GamecornerGui.setClientPlayerBadges(msg.playerBadges);
			});
		ctx.get().setPacketHandled(true);
	}
}
