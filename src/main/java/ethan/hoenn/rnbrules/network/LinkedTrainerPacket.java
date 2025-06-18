package ethan.hoenn.rnbrules.network;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.data.gui.LinkedTrainerData;
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

public class LinkedTrainerPacket {

	private final int trainerEntityId;
	private final String linkedTrainerName;
	private final int linkedTeamSize;
	private final boolean hasLinked;

	public LinkedTrainerPacket(int trainerEntityId, String linkedTrainerName, int linkedTeamSize, boolean hasLinked) {
		this.trainerEntityId = trainerEntityId;
		this.linkedTrainerName = linkedTrainerName;
		this.linkedTeamSize = linkedTeamSize;
		this.hasLinked = hasLinked;
	}

	public static void encode(LinkedTrainerPacket message, PacketBuffer buffer) {
		buffer.writeInt(message.trainerEntityId);
		buffer.writeBoolean(message.hasLinked);
		buffer.writeUtf(message.linkedTrainerName);
		buffer.writeInt(message.linkedTeamSize);
	}

	public static LinkedTrainerPacket decode(PacketBuffer buffer) {
		int trainerEntityId = buffer.readInt();
		boolean hasLinked = buffer.readBoolean();
		String linkedTrainerName = buffer.readUtf(32767);
		int linkedTeamSize = buffer.readInt();
		return new LinkedTrainerPacket(trainerEntityId, linkedTrainerName, linkedTeamSize, hasLinked);
	}

	public static void handle(LinkedTrainerPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && Minecraft.getInstance().level != null) {
				LinkedTrainerData.storeLinkedTrainerData(message.trainerEntityId, message.linkedTrainerName, message.linkedTeamSize, message.hasLinked);
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

						if (trainer.getPersistentData().contains("Linked")) {
							UUID linkedTrainerUUID = trainer.getPersistentData().getUUID("Linked");

							double searchDistance = 10.0;
							List<NPCTrainer> entities = world.getEntitiesOfClass(NPCTrainer.class, trainer.getBoundingBox().inflate(searchDistance), entity2 -> entity2.getUUID().equals(linkedTrainerUUID));

							if (!entities.isEmpty()) {
								NPCTrainer linkedTrainer = entities.get(0);
								String linkedName = linkedTrainer.getName("en_us");
								int linkedTeamSize = linkedTrainer.getPokemonStorage().countAblePokemon();

								PacketHandler.INSTANCE.sendTo(new LinkedTrainerPacket(message.trainerEntityId, linkedName, linkedTeamSize, true), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
							} else {
								PacketHandler.INSTANCE.sendTo(new LinkedTrainerPacket(message.trainerEntityId, "", 0, true), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
							}
						} else {
							PacketHandler.INSTANCE.sendTo(new LinkedTrainerPacket(message.trainerEntityId, "", 0, false), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
						}
					}
				});
			}

			context.setPacketHandled(true);
		}
	}
}
