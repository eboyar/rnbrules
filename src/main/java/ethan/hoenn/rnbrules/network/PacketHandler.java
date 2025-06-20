package ethan.hoenn.rnbrules.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("rnbrules", "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	private static int packetId = 0;

	public static void register() {
		INSTANCE.registerMessage(packetId++, BattleInfoPacket.class, BattleInfoPacket::encode, BattleInfoPacket::decode, BattleInfoPacket::handle);

		INSTANCE.registerMessage(packetId++, CancelTeamSelectionPacket.class, CancelTeamSelectionPacket::encode, CancelTeamSelectionPacket::decode, CancelTeamSelectionPacket::handle);

		INSTANCE.registerMessage(packetId++, FlyDestinationsPacket.class, FlyDestinationsPacket::encode, FlyDestinationsPacket::decode, FlyDestinationsPacket::handle);

		INSTANCE.registerMessage(packetId++, FerryDestinationsPacket.class, FerryDestinationsPacket::encode, FerryDestinationsPacket::decode, FerryDestinationsPacket::handle);

		INSTANCE.registerMessage(packetId++, BadgesPacket.class, BadgesPacket::encode, BadgesPacket::decode, BadgesPacket::handle);

		INSTANCE.registerMessage(packetId++, GlobalOTsPacket.class, GlobalOTsPacket::encode, GlobalOTsPacket::decode, GlobalOTsPacket::handle);

		INSTANCE.registerMessage(packetId++, LinkedTrainerPacket.class, LinkedTrainerPacket::encode, LinkedTrainerPacket::decode, LinkedTrainerPacket::handle);

		INSTANCE.registerMessage(packetId++, LinkedTrainerPacket.Request.class, LinkedTrainerPacket.Request::encode, LinkedTrainerPacket.Request::decode, LinkedTrainerPacket.Request::handle);

		INSTANCE.registerMessage(packetId++, TagBattlePacket.class, TagBattlePacket::encode, TagBattlePacket::decode, TagBattlePacket::handle);

		INSTANCE.registerMessage(packetId++, TagBattlePacket.Request.class, TagBattlePacket.Request::encode, TagBattlePacket.Request::decode, TagBattlePacket.Request::handle);

		INSTANCE.registerMessage(packetId++, StatueVisibilityPacket.class, StatueVisibilityPacket::encode, StatueVisibilityPacket::decode, StatueVisibilityPacket::handle);

		INSTANCE.registerMessage(packetId++, RoamerOverlayPacket.class, RoamerOverlayPacket::encode, RoamerOverlayPacket::decode, RoamerOverlayPacket::handle);

		INSTANCE.registerMessage(packetId++, EnvironmentPacket.class, EnvironmentPacket::encode, EnvironmentPacket::decode, EnvironmentPacket::handle);

		INSTANCE.registerMessage(packetId++, LocationSyncPacket.class, LocationSyncPacket::encode, LocationSyncPacket::decode, LocationSyncPacket::handle);

		INSTANCE.registerMessage(packetId++, EnvironmentSyncPacket.class, EnvironmentSyncPacket::encode, EnvironmentSyncPacket::decode, EnvironmentSyncPacket::handle);

		INSTANCE.registerMessage(packetId++, LocationPopupPacket.class, LocationPopupPacket::encode, LocationPopupPacket::decode, LocationPopupPacket::handle);

		INSTANCE.registerMessage(packetId++, PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::encode, PlayerInfoUpdatePacket::decode, PlayerInfoUpdatePacket::handle);
	}
}
