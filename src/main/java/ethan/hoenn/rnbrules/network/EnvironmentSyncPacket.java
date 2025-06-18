package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.utils.enums.Environment;
import ethan.hoenn.rnbrules.utils.managers.ClientLocationManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Packet for syncing environment data from server to client
 */
public class EnvironmentSyncPacket {

	private final String locationName;
	private final String environmentId;

	public EnvironmentSyncPacket(String locationName, String environmentId) {
		this.locationName = locationName;
		this.environmentId = environmentId;
	}

	public static void encode(EnvironmentSyncPacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.locationName != null ? packet.locationName : "");
		buffer.writeUtf(packet.environmentId != null ? packet.environmentId : "");
	}

	public static EnvironmentSyncPacket decode(PacketBuffer buffer) {
		String locationName = buffer.readUtf();
		if (locationName.isEmpty()) {
			locationName = null;
		}

		String environmentId = buffer.readUtf();
		if (environmentId.isEmpty()) {
			environmentId = null;
		}

		return new EnvironmentSyncPacket(locationName, environmentId);
	}

	public static void handle(EnvironmentSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			// Update client-side environment data
			if (packet.locationName != null) {
				Environment env = Environment.fromString(packet.environmentId);
				ClientLocationManager.getInstance().updateLocationEnvironment(packet.locationName, env);
			}
		});
		context.setPacketHandled(true);
	}
}
