package ethan.hoenn.rnbrules.utils.managers;

import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.network.StatueVisibilityPacket;
import java.util.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.network.PacketDistributor;

public class StatueVisibilityManager extends WorldSavedData implements ResetableManager{

	private static final String DATA_TAG = "statue_visibility";
	private static final String PLAYER_ENTRIES_TAG = "player_statue_visibility";
	private static final String PLAYER_UUID_TAG = "player_uuid";
	private static final String HIDDEN_STATUES_TAG = "hidden_statues_uuids";

	private final Map<UUID, Set<UUID>> hiddenStatues = new HashMap<>();

	public StatueVisibilityManager() {
		super(DATA_TAG);
	}

	public static StatueVisibilityManager get(ServerWorld world) {
		return world.getDataStorage().computeIfAbsent(StatueVisibilityManager::new, DATA_TAG);
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = hiddenStatues.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public void hideStatueForPlayer(UUID statueUUID, ServerPlayerEntity player) {
		if (statueUUID == null || player == null) return;

		boolean added = hiddenStatues.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(statueUUID);

		if (added) {
			setDirty();
		}

		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new StatueVisibilityPacket(statueUUID, false));
	}

	public void showStatueForPlayer(UUID statueUUID, ServerPlayerEntity player) {
		if (statueUUID == null || player == null) return;

		Set<UUID> statues = hiddenStatues.get(player.getUUID());
		if (statues != null) {
			boolean removed = statues.remove(statueUUID);
			if (removed) {
				setDirty();
			}

			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new StatueVisibilityPacket(statueUUID, true));
		}
	}

	public boolean isStatueVisibleToPlayer(UUID statueUUID, UUID playerUUID) {
		if (statueUUID == null || playerUUID == null) return true;

		Set<UUID> hiddenForPlayer = hiddenStatues.get(playerUUID);
		return hiddenForPlayer == null || !hiddenForPlayer.contains(statueUUID);
	}

	public void clearPlayerData(UUID playerUUID) {
		if (hiddenStatues.remove(playerUUID) != null) {
			setDirty();
		}
	}

	public Set<UUID> getHiddenStatuesForPlayer(UUID playerUUID) {
		return hiddenStatues.getOrDefault(playerUUID, Collections.emptySet());
	}

	public void syncPlayerOnLogin(ServerPlayerEntity player) {
		Set<UUID> hiddenForPlayer = getHiddenStatuesForPlayer(player.getUUID());

		for (UUID statueUUID : hiddenForPlayer) {
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new StatueVisibilityPacket(statueUUID, false));
		}
	}

	@Override
	public void load(CompoundNBT nbt) {
		hiddenStatues.clear();
		ListNBT playerEntriesList = nbt.getList(PLAYER_ENTRIES_TAG, 10);

		for (int i = 0; i < playerEntriesList.size(); i++) {
			CompoundNBT playerData = playerEntriesList.getCompound(i);
			UUID playerUUID = UUID.fromString(playerData.getString(PLAYER_UUID_TAG));

			Set<UUID> statuesForPlayer = new HashSet<>();
			ListNBT statuesListNBT = playerData.getList(HIDDEN_STATUES_TAG, 8);

			for (int j = 0; j < statuesListNBT.size(); j++) {
				try {
					statuesForPlayer.add(UUID.fromString(statuesListNBT.getString(j)));
				} catch (IllegalArgumentException e) {
					System.err.println("Failed to parse UUID from NBT: " + statuesListNBT.getString(j) + " for player " + playerUUID);
				}
			}
			hiddenStatues.put(playerUUID, statuesForPlayer);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT playerEntriesList = new ListNBT();

		for (Map.Entry<UUID, Set<UUID>> entry : hiddenStatues.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString(PLAYER_UUID_TAG, entry.getKey().toString());

			ListNBT statuesListNBT = new ListNBT();
			for (UUID statueUUID : entry.getValue()) {
				statuesListNBT.add(StringNBT.valueOf(statueUUID.toString()));
			}

			playerData.put(HIDDEN_STATUES_TAG, statuesListNBT);
			playerEntriesList.add(playerData);
		}
		nbt.put(PLAYER_ENTRIES_TAG, playerEntriesList);
		return nbt;
	}
}
