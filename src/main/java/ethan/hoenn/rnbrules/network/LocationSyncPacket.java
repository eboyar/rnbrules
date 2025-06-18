package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.utils.enums.Environment;
import ethan.hoenn.rnbrules.utils.managers.ClientLocationManager;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class LocationSyncPacket {

	private final UUID playerUUID;
	private final String locationName;

	public LocationSyncPacket(UUID playerUUID, String locationName) {
		this.playerUUID = playerUUID;
		this.locationName = locationName;
	}

	public static void encode(LocationSyncPacket packet, PacketBuffer buffer) {
		buffer.writeUUID(packet.playerUUID);
		buffer.writeUtf(packet.locationName != null ? packet.locationName : "");
	}

	public static LocationSyncPacket decode(PacketBuffer buffer) {
		UUID playerUUID = buffer.readUUID();
		String locationName = buffer.readUtf();
		if (locationName.isEmpty()) {
			locationName = null;
		}
		return new LocationSyncPacket(playerUUID, locationName);
	}

	public static void handle(LocationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ClientLocationManager.getInstance().updateClientPlayerLocation(packet.playerUUID, packet.locationName);
		});
		context.setPacketHandled(true);
	}
}
