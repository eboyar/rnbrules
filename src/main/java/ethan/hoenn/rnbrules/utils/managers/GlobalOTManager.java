package ethan.hoenn.rnbrules.utils.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class GlobalOTManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "globalot";
	private static final String PLAYER_OTS_TAG = "playerOTs";
	private static final String AVAILABLE_OTS_TAG = "availableOTs";
	private static final String GLOBAL_OT_NBT_PREFIX = "GlobalOT";

	private static GlobalOTManager instance;

	private Map<UUID, Set<String>> playerOTs = new HashMap<>();
	private Set<String> availableOTs = new HashSet<>();

	public GlobalOTManager() {
		super(DATA_TAG);
	}

	public static GlobalOTManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(GlobalOTManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerOTs.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public boolean addGlobalOT(String otName) {
		boolean added = availableOTs.add(otName);
		if (added) {
			this.setDirty();
		}
		return added;
	}

	public Set<String> listGlobalOTs() {
		return Collections.unmodifiableSet(new HashSet<>(availableOTs));
	}

	public Set<String> listPlayerGlobalOTs(UUID playerUUID) {
		Set<String> playerOTSet = playerOTs.get(playerUUID);
		if (playerOTSet == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(new HashSet<>(playerOTSet));
	}

	public boolean addPlayerGlobalOT(UUID playerUUID, String otName) {
		if (!availableOTs.contains(otName)) {
			return false;
		}

		Set<String> playerOTSet = playerOTs.computeIfAbsent(playerUUID, k -> new HashSet<>());
		boolean added = playerOTSet.add(otName);
		if (added) {
			this.setDirty();
		}
		return added;
	}

	public boolean playerHasGlobalOT(UUID playerUUID, String otName) {
		Set<String> playerOTSet = playerOTs.get(playerUUID);
		return playerOTSet != null && playerOTSet.contains(otName);
	}

	public boolean removePlayerGlobalOT(UUID playerUUID, String otName) {
		Set<String> playerOTSet = playerOTs.get(playerUUID);
		if (playerOTSet != null && playerOTSet.remove(otName)) {
			this.setDirty();
			return true;
		}
		return false;
	}

	public boolean npcAddGlobalOT(Entity entity, String otName) {
		if (!availableOTs.contains(otName)) {
			return false;
		}

		CompoundNBT persistentData = entity.getPersistentData();
		persistentData.putString(GLOBAL_OT_NBT_PREFIX, otName);
		return true;
	}

	public String npcGetGlobalOT(Entity entity) {
		CompoundNBT persistentData = entity.getPersistentData();
		if (persistentData.contains(GLOBAL_OT_NBT_PREFIX)) {
			return persistentData.getString(GLOBAL_OT_NBT_PREFIX);
		}
		return null;
	}

	public boolean npcRemoveGlobalOT(Entity entity) {
		CompoundNBT persistentData = entity.getPersistentData();

		if (persistentData.contains(GLOBAL_OT_NBT_PREFIX)) {
			persistentData.remove(GLOBAL_OT_NBT_PREFIX);
			return true;
		}

		return false;
	}

	@Override
	public void load(CompoundNBT nbt) {
		ListNBT availableOTsList = nbt.getList(AVAILABLE_OTS_TAG, 8);
		availableOTs.clear();
		for (int i = 0; i < availableOTsList.size(); i++) {
			availableOTs.add(availableOTsList.getString(i));
		}

		CompoundNBT playerOTsNBT = nbt.getCompound(PLAYER_OTS_TAG);
		playerOTs.clear();

		for (String uuidString : playerOTsNBT.getAllKeys()) {
			UUID playerUUID = UUID.fromString(uuidString);
			ListNBT playerOTsList = playerOTsNBT.getList(uuidString, 8);

			Set<String> playerOTSet = new HashSet<>();
			for (int i = 0; i < playerOTsList.size(); i++) {
				playerOTSet.add(playerOTsList.getString(i));
			}

			playerOTs.put(playerUUID, playerOTSet);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT availableOTsList = new ListNBT();
		for (String otName : availableOTs) {
			availableOTsList.add(StringNBT.valueOf(otName));
		}
		nbt.put(AVAILABLE_OTS_TAG, availableOTsList);

		CompoundNBT playerOTsNBT = new CompoundNBT();
		for (Map.Entry<UUID, Set<String>> entry : playerOTs.entrySet()) {
			UUID playerUUID = entry.getKey();
			Set<String> playerOTSet = entry.getValue();

			ListNBT playerOTsList = new ListNBT();
			for (String otName : playerOTSet) {
				playerOTsList.add(StringNBT.valueOf(otName));
			}

			playerOTsNBT.put(playerUUID.toString(), playerOTsList);
		}
		nbt.put(PLAYER_OTS_TAG, playerOTsNBT);

		return nbt;
	}
}
