package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.battleoverlays.LocationPopupOverlay;
import ethan.hoenn.rnbrules.utils.enums.BoardType;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class LocationPopupPacket {

	private final String locationName;
	private final BoardType boardType;

	public LocationPopupPacket(String locationName, BoardType boardType) {
		this.locationName = locationName;
		this.boardType = boardType;
	}

	public static void encode(LocationPopupPacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.locationName);
		buffer.writeEnum(packet.boardType);
	}

	public static LocationPopupPacket decode(PacketBuffer buffer) {
		String locationName = buffer.readUtf();
		BoardType boardType = buffer.readEnum(BoardType.class);
		return new LocationPopupPacket(locationName, boardType);
	}

	public static void handle(LocationPopupPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			LocationPopupOverlay.showLocation(packet.locationName, packet.boardType);
		});
		context.setPacketHandled(true);
	}
}
