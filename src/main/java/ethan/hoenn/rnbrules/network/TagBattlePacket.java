package ethan.hoenn.rnbrules.network;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.data.gui.TagBattleData;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class TagBattlePacket {

	private final int trainerEntityId;
	private final String partnerTrainerName;

	public TagBattlePacket(int trainerEntityId, String partnerTrainerName) {
		this.trainerEntityId = trainerEntityId;
		this.partnerTrainerName = partnerTrainerName;
	}

	public static void encode(TagBattlePacket message, PacketBuffer buffer) {
		buffer.writeInt(message.trainerEntityId);
		buffer.writeUtf(message.partnerTrainerName);
	}

	public static TagBattlePacket decode(PacketBuffer buffer) {
		int trainerEntityId = buffer.readInt();
		String partnerTrainerName = buffer.readUtf(32767);
		return new TagBattlePacket(trainerEntityId, partnerTrainerName);
	}

	public static void handle(TagBattlePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && Minecraft.getInstance().level != null) {
				TagBattleData.storePartnerData(message.trainerEntityId, message.partnerTrainerName);
			}
		});
		context.setPacketHandled(true);
	}

	public static class Request {

		private final int trainerEntityId;

		public Request(int trainerEntityId) {
			this.trainerEntityId = trainerEntityId;
		}

		public static void encode(Request message, PacketBuffer buffer) {
			buffer.writeInt(message.trainerEntityId);
		}

		public static Request decode(PacketBuffer buffer) {
			return new Request(buffer.readInt());
		}

		public static void handle(Request message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			ServerPlayerEntity player = context.getSender();

			if (player != null) {
				context.enqueueWork(() -> {
					World world = player.level;
					Entity entity = world.getEntity(message.trainerEntityId);

					if (entity instanceof NPCTrainer) {
						NPCTrainer trainer = (NPCTrainer) entity;

						if (trainer.getPersistentData().contains("PlayerPartner")) {
							UUID partnerTrainerUUID = trainer.getPersistentData().getUUID("PlayerPartner");

							double searchDistance = 10.0;
							List<NPCTrainer> matches = world.getEntitiesOfClass(NPCTrainer.class, trainer.getBoundingBox().inflate(searchDistance), candidate -> candidate.getUUID().equals(partnerTrainerUUID));

							if (!matches.isEmpty()) {
								NPCTrainer partnerTrainer = matches.get(0);
								String partnerName = partnerTrainer.getName("en_us");

								PacketHandler.INSTANCE.sendTo(new TagBattlePacket(message.trainerEntityId, partnerName), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
							}
						}
					}
				});
			}

			context.setPacketHandled(true);
		}
	}
}
