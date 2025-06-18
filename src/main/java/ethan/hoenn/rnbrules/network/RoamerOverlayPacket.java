package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.battleoverlays.RoamerOverlay;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class RoamerOverlayPacket {

	private final String titleText;
	private final String subtitleText;
	private final int durationTicks;

	public RoamerOverlayPacket(String titleText, String subtitleText, int durationTicks) {
		this.titleText = titleText;
		this.subtitleText = subtitleText;
		this.durationTicks = durationTicks;
	}

	public static void encode(RoamerOverlayPacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.titleText);
		buffer.writeUtf(packet.subtitleText);
		buffer.writeInt(packet.durationTicks);
	}

	public static RoamerOverlayPacket decode(PacketBuffer buffer) {
		String titleText = buffer.readUtf(32767);
		String subtitleText = buffer.readUtf(32767);
		int durationTicks = buffer.readInt();

		return new RoamerOverlayPacket(titleText, subtitleText, durationTicks);
	}

	public static void handle(RoamerOverlayPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClientSide(packet));
			});
		ctx.get().setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	private static void handleClientSide(RoamerOverlayPacket packet) {
		RoamerOverlay.showRoamerOverlay(packet.titleText, packet.subtitleText, packet.durationTicks);
	}
}
