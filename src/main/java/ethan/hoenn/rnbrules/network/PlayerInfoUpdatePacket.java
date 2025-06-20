package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.client.gui.PlayerInfoOverlay;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PlayerInfoUpdatePacket {

	private final String locationName;
	private final int levelCap;
	private final int balance;
	private final boolean enabled;

	public PlayerInfoUpdatePacket(String locationName, int levelCap, int balance, boolean enabled) {
		this.locationName = locationName != null ? locationName : "";
		this.levelCap = levelCap;
		this.balance = balance;
		this.enabled = enabled;
	}

	public static void encode(PlayerInfoUpdatePacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.locationName);
		buffer.writeInt(packet.levelCap);
		buffer.writeInt(packet.balance);
		buffer.writeBoolean(packet.enabled);
	}

	public static PlayerInfoUpdatePacket decode(PacketBuffer buffer) {
		String locationName = buffer.readUtf();
		int levelCap = buffer.readInt();
		int balance = buffer.readInt();
		boolean enabled = buffer.readBoolean();
		return new PlayerInfoUpdatePacket(locationName, levelCap, balance, enabled);
	}

	public static void handle(PlayerInfoUpdatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			PlayerInfoOverlay.updateInfo(packet.locationName, packet.levelCap, packet.balance);
			PlayerInfoOverlay.setEnabled(packet.enabled);
		});
		context.setPacketHandled(true);
	}
}
