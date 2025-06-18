package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.gui.battleinfo.BattleInfoOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class BattleInfoPacket {

	public final boolean isDoubleBattle;
	public final boolean isTagBattle;
	public final boolean isLinkedBattle;

	public final String environment;
	public final String playerName;
	public final String trainerName;
	public final String secondEnemyTrainerName;
	public final String allyTrainerName;

	//singles
	public final String enemyStats;
	public final String allyStats;
	public final boolean enemyIsMega;
	public final boolean allyIsMega;
	public final boolean inBattle;
	public final String playerStatuses;
	public final String trainerStatuses;
	public final String secondEnemyTrainerStatuses;
	public final String allyTrainerStatuses;

	//doubles
	public final List<String> enemyStatsList;
	public final List<Boolean> enemyIsMegaList;
	public final List<String> allyStatsList;
	public final List<Boolean> allyIsMegaList;

	public BattleInfoPacket(
		List<String> enemyStatsList,
		List<Boolean> enemyIsMegaList,
		List<String> allyStatsList,
		List<Boolean> allyIsMegaList,
		boolean inBattle,
		String environment,
		String playerName,
		String trainerName,
		String playerStatuses,
		String trainerStatuses,
		boolean isTagBattle,
		boolean isLinkedBattle,
		String secondEnemyTrainerName,
		String allyTrainerName,
		String secondEnemyTrainerStatuses,
		String allyTrainerStatuses
	) {
		this.isDoubleBattle = true;
		this.isTagBattle = isTagBattle;
		this.isLinkedBattle = isLinkedBattle;
		this.enemyStatsList = enemyStatsList;
		this.enemyIsMegaList = enemyIsMegaList;
		this.allyStatsList = allyStatsList;
		this.allyIsMegaList = allyIsMegaList;
		this.inBattle = inBattle;
		this.environment = environment;
		this.playerName = playerName;
		this.trainerName = trainerName;
		this.playerStatuses = playerStatuses;
		this.trainerStatuses = trainerStatuses;
		this.secondEnemyTrainerName = secondEnemyTrainerName;
		this.allyTrainerName = allyTrainerName;
		this.secondEnemyTrainerStatuses = secondEnemyTrainerStatuses;
		this.allyTrainerStatuses = allyTrainerStatuses;

		this.enemyStats = enemyStatsList.isEmpty() ? "" : enemyStatsList.get(0);
		this.allyStats = allyStatsList.isEmpty() ? "" : allyStatsList.get(0);
		this.enemyIsMega = !enemyIsMegaList.isEmpty() && enemyIsMegaList.get(0);
		this.allyIsMega = !allyIsMegaList.isEmpty() && allyIsMegaList.get(0);
	}

	// Original constructor for double battles
	public BattleInfoPacket(
		List<String> enemyStatsList,
		List<Boolean> enemyIsMegaList,
		List<String> allyStatsList,
		List<Boolean> allyIsMegaList,
		boolean inBattle,
		String environment,
		String playerName,
		String trainerName,
		String playerStatuses,
		String trainerStatuses
	) {
		this.isDoubleBattle = true;
		this.isTagBattle = false;
		this.isLinkedBattle = false;
		this.enemyStatsList = enemyStatsList;
		this.enemyIsMegaList = enemyIsMegaList;
		this.allyStatsList = allyStatsList;
		this.allyIsMegaList = allyIsMegaList;
		this.inBattle = inBattle;
		this.environment = environment;
		this.playerName = playerName;
		this.trainerName = trainerName;
		this.playerStatuses = playerStatuses;
		this.trainerStatuses = trainerStatuses;
		this.secondEnemyTrainerName = "";
		this.allyTrainerName = "";
		this.secondEnemyTrainerStatuses = "";
		this.allyTrainerStatuses = "";

		this.enemyStats = enemyStatsList.isEmpty() ? "" : enemyStatsList.get(0);
		this.allyStats = allyStatsList.isEmpty() ? "" : allyStatsList.get(0);
		this.enemyIsMega = !enemyIsMegaList.isEmpty() && enemyIsMegaList.get(0);
		this.allyIsMega = !allyIsMegaList.isEmpty() && allyIsMegaList.get(0);
	}

	public BattleInfoPacket(
		String enemyStats,
		String allyStats,
		boolean enemyIsMega,
		boolean allyIsMega,
		boolean inBattle,
		String environment,
		String playerName,
		String trainerName,
		String playerStatuses,
		String trainerStatuses
	) {
		this.isDoubleBattle = false;
		this.isTagBattle = false;
		this.isLinkedBattle = false;
		this.enemyStats = enemyStats;
		this.allyStats = allyStats;
		this.enemyIsMega = enemyIsMega;
		this.allyIsMega = allyIsMega;
		this.inBattle = inBattle;
		this.environment = environment;
		this.playerName = playerName;
		this.trainerName = trainerName;
		this.playerStatuses = playerStatuses;
		this.trainerStatuses = trainerStatuses;
		this.secondEnemyTrainerName = "";
		this.allyTrainerName = "";
		this.secondEnemyTrainerStatuses = "";
		this.allyTrainerStatuses = "";

		this.enemyStatsList = new ArrayList<>();
		this.enemyIsMegaList = new ArrayList<>();
		this.allyStatsList = new ArrayList<>();
		this.allyIsMegaList = new ArrayList<>();

		if (!enemyStats.isEmpty()) {
			this.enemyStatsList.add(enemyStats);
			this.enemyIsMegaList.add(enemyIsMega);
		}

		if (!allyStats.isEmpty()) {
			this.allyStatsList.add(allyStats);
			this.allyIsMegaList.add(allyIsMega);
		}
	}

	public BattleInfoPacket(String enemyStats, String allyStats, boolean enemyIsMega, boolean allyIsMega, boolean inBattle) {
		this(enemyStats, allyStats, enemyIsMega, allyIsMega, inBattle, "", "", "", "", "");
	}

	public static void encode(BattleInfoPacket packet, PacketBuffer buffer) {
		buffer.writeBoolean(packet.isDoubleBattle);
		buffer.writeBoolean(packet.isTagBattle);
		buffer.writeBoolean(packet.isLinkedBattle);

		buffer.writeBoolean(packet.inBattle);
		buffer.writeUtf(packet.environment);
		buffer.writeUtf(packet.playerName);
		buffer.writeUtf(packet.trainerName);

		if (packet.isLinkedBattle || packet.isTagBattle) {
			buffer.writeUtf(packet.secondEnemyTrainerName);
			buffer.writeUtf(packet.allyTrainerName);
			buffer.writeUtf(packet.secondEnemyTrainerStatuses);
			buffer.writeUtf(packet.allyTrainerStatuses);
		}

		if (packet.isDoubleBattle) {
			buffer.writeVarInt(packet.enemyStatsList.size());
			for (int i = 0; i < packet.enemyStatsList.size(); i++) {
				buffer.writeUtf(packet.enemyStatsList.get(i));
				buffer.writeBoolean(packet.enemyIsMegaList.get(i));
			}

			buffer.writeVarInt(packet.allyStatsList.size());
			for (int i = 0; i < packet.allyStatsList.size(); i++) {
				buffer.writeUtf(packet.allyStatsList.get(i));
				buffer.writeBoolean(packet.allyIsMegaList.get(i));
			}

			buffer.writeUtf(packet.playerStatuses);
			buffer.writeUtf(packet.trainerStatuses);
		} else {
			buffer.writeUtf(packet.enemyStats);
			buffer.writeUtf(packet.allyStats);
			buffer.writeBoolean(packet.enemyIsMega);
			buffer.writeBoolean(packet.allyIsMega);
			buffer.writeUtf(packet.playerStatuses);
			buffer.writeUtf(packet.trainerStatuses);
		}
	}

	public static BattleInfoPacket decode(PacketBuffer buffer) {
		boolean isDoubleBattle = buffer.readBoolean();
		boolean isTagBattle = buffer.readBoolean();
		boolean isLinkedBattle = buffer.readBoolean();

		boolean inBattle = buffer.readBoolean();
		String environment = buffer.readUtf(32767);
		String playerName = buffer.readUtf(32767);
		String trainerName = buffer.readUtf(32767);

		String secondEnemyTrainerName = "";
		String allyTrainerName = "";
		String secondEnemyTrainerStatuses = "";
		String allyTrainerStatuses = "";

		if (isLinkedBattle || isTagBattle) {
			secondEnemyTrainerName = buffer.readUtf(32767);
			allyTrainerName = buffer.readUtf(32767);
			secondEnemyTrainerStatuses = buffer.readUtf(32767);
			allyTrainerStatuses = buffer.readUtf(32767);
		}

		if (isDoubleBattle) {
			int enemySize = buffer.readVarInt();
			List<String> enemyStatsList = new ArrayList<>(enemySize);
			List<Boolean> enemyIsMegaList = new ArrayList<>(enemySize);

			for (int i = 0; i < enemySize; i++) {
				enemyStatsList.add(buffer.readUtf(32767));
				enemyIsMegaList.add(buffer.readBoolean());
			}

			int allySize = buffer.readVarInt();
			List<String> allyStatsList = new ArrayList<>(allySize);
			List<Boolean> allyIsMegaList = new ArrayList<>(allySize);

			for (int i = 0; i < allySize; i++) {
				allyStatsList.add(buffer.readUtf(32767));
				allyIsMegaList.add(buffer.readBoolean());
			}

			String playerStatuses = buffer.readUtf(32767);
			String trainerStatuses = buffer.readUtf(32767);

			if (isLinkedBattle || isTagBattle) {
				return new BattleInfoPacket(
					enemyStatsList,
					enemyIsMegaList,
					allyStatsList,
					allyIsMegaList,
					inBattle,
					environment,
					playerName,
					trainerName,
					playerStatuses,
					trainerStatuses,
					isTagBattle,
					isLinkedBattle,
					secondEnemyTrainerName,
					allyTrainerName,
					secondEnemyTrainerStatuses,
					allyTrainerStatuses
				);
			} else {
				return new BattleInfoPacket(enemyStatsList, enemyIsMegaList, allyStatsList, allyIsMegaList, inBattle, environment, playerName, trainerName, playerStatuses, trainerStatuses);
			}
		} else {
			String enemyStats = buffer.readUtf(32767);
			String allyStats = buffer.readUtf(32767);
			boolean enemyIsMega = buffer.readBoolean();
			boolean allyIsMega = buffer.readBoolean();
			String playerStatuses = buffer.readUtf(32767);
			String trainerStatuses = buffer.readUtf(32767);

			return new BattleInfoPacket(enemyStats, allyStats, enemyIsMega, allyIsMega, inBattle, environment, playerName, trainerName, playerStatuses, trainerStatuses);
		}
	}

	public static void handle(BattleInfoPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx
			.get()
			.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClientSide(packet));
			});
		ctx.get().setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	private static void handleClientSide(BattleInfoPacket packet) {
		BattleInfoOverlay.updateFromPacket(packet);
	}
}
