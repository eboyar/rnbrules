package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.flight.FlyGui;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class FlyDestinationsPacket {

	private Set<String> unlockedDestinations;

	public FlyDestinationsPacket(Set<String> unlockedDestinations) {
		this.unlockedDestinations = unlockedDestinations;
	}

	public static void encode(FlyDestinationsPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.unlockedDestinations.size());
		for (String dest : msg.unlockedDestinations) {
			buf.writeUtf(dest);
		}
	}

	public static FlyDestinationsPacket decode(PacketBuffer buf) {
		int size = buf.readInt();
		Set<String> destinations = new HashSet<>();
		for (int i = 0; i < size; i++) {
			destinations.add(buf.readUtf());
		}
		return new FlyDestinationsPacket(destinations);
	}

	public static void handle(FlyDestinationsPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				FlyGui.setClientUnlockedDestinations(msg.unlockedDestinations);
			});
		ctx.get().setPacketHandled(true);
	}
}
