package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.utils.misc.StatueVisibilityTracker;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class StatueVisibilityPacket {

	private final UUID statueUUID;
	private final boolean visible;

	public StatueVisibilityPacket(UUID statueUUID, boolean visible) {
		this.statueUUID = statueUUID;
		this.visible = visible;
	}

	public static void encode(StatueVisibilityPacket msg, PacketBuffer buf) {
		buf.writeUUID(msg.statueUUID);
		buf.writeBoolean(msg.visible);
	}

	public static StatueVisibilityPacket decode(PacketBuffer buf) {
		return new StatueVisibilityPacket(buf.readUUID(), buf.readBoolean());
	}

	public static void handle(StatueVisibilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				handleClient(msg);
			});
		ctx.get().setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	private static void handleClient(StatueVisibilityPacket msg) {
		if (msg.visible) {
			StatueVisibilityTracker.showStatue(msg.statueUUID);
		} else {
			StatueVisibilityTracker.hideStatue(msg.statueUUID);
		}
	}
}
