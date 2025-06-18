package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.ferry.FerryGui;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class FerryDestinationsPacket {

	private Set<String> unlockedDestinations;
	private String currentLocation;

	public FerryDestinationsPacket(Set<String> unlockedDestinations, String currentLocation) {
		this.unlockedDestinations = unlockedDestinations;
		this.currentLocation = currentLocation;
	}

	public static void encode(FerryDestinationsPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.unlockedDestinations.size());
		for (String dest : msg.unlockedDestinations) {
			buf.writeUtf(dest);
		}

		buf.writeBoolean(msg.currentLocation != null);
		if (msg.currentLocation != null) {
			buf.writeUtf(msg.currentLocation);
		}
	}

	public static FerryDestinationsPacket decode(PacketBuffer buf) {
		int size = buf.readInt();
		Set<String> destinations = new HashSet<>();
		for (int i = 0; i < size; i++) {
			destinations.add(buf.readUtf());
		}

		String currentLocation = null;
		if (buf.readBoolean()) {
			currentLocation = buf.readUtf();
		}

		return new FerryDestinationsPacket(destinations, currentLocation);
	}

	public static void handle(FerryDestinationsPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				FerryGui.setClientUnlockedDestinations(msg.unlockedDestinations);
				FerryGui.setClientCurrentLocation(msg.currentLocation);
			});
		ctx.get().setPacketHandled(true);
	}
}
