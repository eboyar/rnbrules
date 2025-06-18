package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.RNBConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class GauntletManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "gauntlets";
	private static GauntletManager instance;

	private final Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();
	private final Map<String, Boolean> gauntletHealingAllowed = new HashMap<>();

	private final Map<UUID, Map<String, Boolean>> playerDeathlessStatus = new HashMap<>();
	private final Map<UUID, Map<String, Boolean>> previousGauntletDeathlessStatus = new HashMap<>();
	private final Map<UUID, Map<String, Integer>> playerInitialFaintedCount = new HashMap<>();
	private final Map<UUID, Map<String, List<Integer>>> playerGauntletProgress = new HashMap<>();

	public GauntletManager() {
		super(DATA_TAG);
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerGauntletProgress.clear();
		gauntletHealingAllowed.clear();
		playerDeathlessStatus.clear();

		ListNBT gauntletList = nbt.getList(DATA_TAG, 10);
		for (int i = 0; i < gauntletList.size(); i++) {
			CompoundNBT playerData = gauntletList.getCompound(i);
			UUID uuid = UUID.fromString(playerData.getString("uuid"));
			Map<String, List<Integer>> playerProgress = new HashMap<>();

			ListNBT gauntletProgressList = playerData.getList("gauntletProgress", 10);
			for (int j = 0; j < gauntletProgressList.size(); j++) {
				CompoundNBT gauntletData = gauntletProgressList.getCompound(j);
				String gauntletId = gauntletData.getString("gauntletId");
				List<Integer> progressList = new ArrayList<>();

				ListNBT progressListNBT = gauntletData.getList("progress", 8);
				for (int k = 0; k < progressListNBT.size(); k++) {
					progressList.add(progressListNBT.getInt(k));
				}
				playerProgress.put(gauntletId, progressList);
			}
			playerGauntletProgress.put(uuid, playerProgress);
		}

		if (nbt.contains("healingAllowed")) {
			CompoundNBT healingData = nbt.getCompound("healingAllowed");
			for (String key : healingData.getAllKeys()) {
				gauntletHealingAllowed.put(key, healingData.getBoolean(key));
			}
		}
		if (nbt.contains("deathlessStatus")) {
			ListNBT deathlessList = nbt.getList("deathlessStatus", 10);
			for (int i = 0; i < deathlessList.size(); i++) {
				CompoundNBT playerData = deathlessList.getCompound(i);
				UUID uuid = UUID.fromString(playerData.getString("uuid"));
				Map<String, Boolean> playerStatus = new HashMap<>();

				CompoundNBT gauntletStatusNBT = playerData.getCompound("gauntletStatus");
				for (String key : gauntletStatusNBT.getAllKeys()) {
					playerStatus.put(key, gauntletStatusNBT.getBoolean(key));
				}
				playerDeathlessStatus.put(uuid, playerStatus);
			}
		}

		if (nbt.contains("previousDeathlessStatus")) {
			ListNBT previousDeathlessList = nbt.getList("previousDeathlessStatus", 10);
			for (int i = 0; i < previousDeathlessList.size(); i++) {
				CompoundNBT playerData = previousDeathlessList.getCompound(i);
				UUID uuid = UUID.fromString(playerData.getString("uuid"));
				Map<String, Boolean> previousStatus = new HashMap<>();

				CompoundNBT prevGauntletStatusNBT = playerData.getCompound("prevGauntletStatus");
				for (String key : prevGauntletStatusNBT.getAllKeys()) {
					previousStatus.put(key, prevGauntletStatusNBT.getBoolean(key));
				}
				previousGauntletDeathlessStatus.put(uuid, previousStatus);
			}
		}

		if (nbt.contains("initialFaintedCount")) {
			ListNBT faintedList = nbt.getList("initialFaintedCount", 10);
			for (int i = 0; i < faintedList.size(); i++) {
				CompoundNBT playerData = faintedList.getCompound(i);
				UUID uuid = UUID.fromString(playerData.getString("uuid"));
				Map<String, Integer> playerCounts = new HashMap<>();

				CompoundNBT countNBT = playerData.getCompound("faintedCounts");
				for (String key : countNBT.getAllKeys()) {
					playerCounts.put(key, countNBT.getInt(key));
				}
				playerInitialFaintedCount.put(uuid, playerCounts);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT gauntletList = new ListNBT();
		for (Map.Entry<UUID, Map<String, List<Integer>>> playerEntry : playerGauntletProgress.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", playerEntry.getKey().toString());
			ListNBT gauntletProgressList = new ListNBT();

			for (Map.Entry<String, List<Integer>> gauntletEntry : playerEntry.getValue().entrySet()) {
				CompoundNBT gauntletData = new CompoundNBT();
				gauntletData.putString("gauntletId", gauntletEntry.getKey());

				ListNBT progressListNBT = new ListNBT();
				for (Integer progress : gauntletEntry.getValue()) {
					progressListNBT.add(IntNBT.valueOf(progress));
				}
				gauntletData.put("progress", progressListNBT);
				gauntletProgressList.add(gauntletData);
			}
			playerData.put("gauntletProgress", gauntletProgressList);
			gauntletList.add(playerData);
		}
		nbt.put(DATA_TAG, gauntletList);

		CompoundNBT healingData = new CompoundNBT();
		for (Map.Entry<String, Boolean> entry : gauntletHealingAllowed.entrySet()) {
			healingData.putBoolean(entry.getKey(), entry.getValue());
		}
		nbt.put("healingAllowed", healingData);

		ListNBT deathlessList = getDeathlessList(playerDeathlessStatus, "gauntletStatus");
		nbt.put("deathlessStatus", deathlessList);

		ListNBT previousDeathlessList = getDeathlessList(previousGauntletDeathlessStatus, "prevGauntletStatus");
		nbt.put("previousDeathlessStatus", previousDeathlessList);

		ListNBT faintedList = getFaintedList();
		nbt.put("initialFaintedCount", faintedList);

		return nbt;
	}

	private ListNBT getFaintedList() {
		ListNBT faintedList = new ListNBT();
		for (Map.Entry<UUID, Map<String, Integer>> playerEntry : playerInitialFaintedCount.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", playerEntry.getKey().toString());

			CompoundNBT countNBT = new CompoundNBT();
			for (Map.Entry<String, Integer> countEntry : playerEntry.getValue().entrySet()) {
				countNBT.putInt(countEntry.getKey(), countEntry.getValue());
			}
			playerData.put("faintedCounts", countNBT);
			faintedList.add(playerData);
		}
		return faintedList;
	}

	private ListNBT getDeathlessList(Map<UUID, Map<String, Boolean>> playerDeathlessStatus, String gauntletStatus) {
		ListNBT deathlessList = new ListNBT();
		for (Map.Entry<UUID, Map<String, Boolean>> playerEntry : playerDeathlessStatus.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", playerEntry.getKey().toString());

			CompoundNBT gauntletStatusNBT = new CompoundNBT();
			for (Map.Entry<String, Boolean> statusEntry : playerEntry.getValue().entrySet()) {
				gauntletStatusNBT.putBoolean(statusEntry.getKey(), statusEntry.getValue());
			}
			playerData.put(gauntletStatus, gauntletStatusNBT);
			deathlessList.add(playerData);
		}
		return deathlessList;
	}

	public void addGauntlet(UUID playerUUID, String gauntletId, int initialFaintedCount) {
		if (!gauntlets.containsKey(gauntletId)) {
			throw new IllegalArgumentException("Invalid gauntlet ID: " + gauntletId);
		}

		int expectedSize = gauntlets.get(gauntletId).size();
		playerGauntletProgress.computeIfAbsent(playerUUID, k -> new HashMap<>());
		Map<String, List<Integer>> playerProgress = playerGauntletProgress.get(playerUUID);

		if (!playerProgress.isEmpty()) {
			throw new IllegalStateException("Player is already part of a gauntlet.");
		}

		List<Integer> progressList = new ArrayList<>(Collections.nCopies(expectedSize, 0));
		playerProgress.put(gauntletId, progressList);

		playerDeathlessStatus.computeIfAbsent(playerUUID, k -> new HashMap<>());
		playerDeathlessStatus.get(playerUUID).put(gauntletId, true);

		playerInitialFaintedCount.computeIfAbsent(playerUUID, k -> new HashMap<>());
		playerInitialFaintedCount.get(playerUUID).put(gauntletId, initialFaintedCount);

		setDirty();
	}

	public void addGauntlet(UUID playerUUID, String gauntletId) {
		addGauntlet(playerUUID, gauntletId, 0);
	}

	public static GauntletManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(GauntletManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = false;

		if (playerGauntletProgress.remove(playerUUID) != null) {
			hadData = true;
		}

		if (playerDeathlessStatus.remove(playerUUID) != null) {
			hadData = true;
		}

		if (previousGauntletDeathlessStatus.remove(playerUUID) != null) {
			hadData = true;
		}

		if (playerInitialFaintedCount.remove(playerUUID) != null) {
			hadData = true;
		}

		if (hadData) {
			setDirty();
		}

		return hadData;
	}

	public void removeGauntlet(UUID playerUUID, String gauntletId) {
		if (playerGauntletProgress.containsKey(playerUUID)) {
			Map<String, List<Integer>> playerProgress = playerGauntletProgress.get(playerUUID);
			playerProgress.remove(gauntletId);
			gauntletHealingAllowed.remove(gauntletId);

			boolean wasDeathless = isDeathlessGauntlet(playerUUID, gauntletId);

			if (playerInitialFaintedCount.containsKey(playerUUID)) {
				playerInitialFaintedCount.get(playerUUID).remove(gauntletId);
				if (playerInitialFaintedCount.get(playerUUID).isEmpty()) {
					playerInitialFaintedCount.remove(playerUUID);
				}
			}

			if (playerDeathlessStatus.containsKey(playerUUID)) {
				playerDeathlessStatus.get(playerUUID).remove(gauntletId);
				if (playerDeathlessStatus.get(playerUUID).isEmpty()) {
					playerDeathlessStatus.remove(playerUUID);
				}
			}

			setDirty();

			if (wasDeathless) {
				return;
			}
		}
	}

	public int getInitialFaintedCount(UUID playerUUID, String gauntletId) {
		if (playerInitialFaintedCount.containsKey(playerUUID) && playerInitialFaintedCount.get(playerUUID).containsKey(gauntletId)) {
			return playerInitialFaintedCount.get(playerUUID).get(gauntletId);
		}
		return 0;
	}

	public boolean isDeathlessGauntlet(UUID playerUUID, String gauntletId) {
		if (playerDeathlessStatus.containsKey(playerUUID) && playerDeathlessStatus.get(playerUUID).containsKey(gauntletId)) {
			return playerDeathlessStatus.get(playerUUID).get(gauntletId);
		}
		return false;
	}

	public void setDeathlessStatus(UUID playerUUID, String gauntletId, boolean isDeathless) {
		playerDeathlessStatus.computeIfAbsent(playerUUID, k -> new HashMap<>());
		playerDeathlessStatus.get(playerUUID).put(gauntletId, isDeathless);
		setDirty();
	}

	public void setPreviousGauntletDeathlessStatus(UUID playerUUID, String gauntletId, boolean wasDeathless) {
		previousGauntletDeathlessStatus.computeIfAbsent(playerUUID, k -> new HashMap<>());
		previousGauntletDeathlessStatus.get(playerUUID).put(gauntletId, wasDeathless);
		setDirty();
	}

	public boolean wasPreviousGauntletDeathless(UUID playerUUID, String gauntletId) {
		if (previousGauntletDeathlessStatus.containsKey(playerUUID) && previousGauntletDeathlessStatus.get(playerUUID).containsKey(gauntletId)) {
			return previousGauntletDeathlessStatus.get(playerUUID).get(gauntletId);
		}
		return true;
	}

	public void clearPreviousGauntletDeathlessStatus(UUID playerUUID, String gauntletId) {
		if (previousGauntletDeathlessStatus.containsKey(playerUUID)) {
			previousGauntletDeathlessStatus.get(playerUUID).remove(gauntletId);
			if (previousGauntletDeathlessStatus.get(playerUUID).isEmpty()) {
				previousGauntletDeathlessStatus.remove(playerUUID);
			}
			setDirty();
		}
	}

	public boolean isPartOfGauntlet(UUID playerUUID, String gauntletId) {
		return (playerGauntletProgress.containsKey(playerUUID) && playerGauntletProgress.get(playerUUID).containsKey(gauntletId));
	}

	public boolean isPartOfAnyGauntlet(UUID playerUUID) {
		return (playerGauntletProgress.containsKey(playerUUID) && !playerGauntletProgress.get(playerUUID).isEmpty());
	}

	public String getCurrentGauntletName(UUID playerUUID) {
		if (isPartOfAnyGauntlet(playerUUID)) {
			return playerGauntletProgress.get(playerUUID).keySet().iterator().next();
		}
		return null;
	}

	public List<Boolean> getGauntletProgress(UUID playerUUID, String gauntletId) {
		if (isPartOfGauntlet(playerUUID, gauntletId)) {
			List<Integer> progress = playerGauntletProgress.get(playerUUID).get(gauntletId);
			List<Boolean> booleanProgress = new ArrayList<>();
			for (Integer p : progress) {
				booleanProgress.add(p == 1);
			}
			return booleanProgress;
		}
		return null;
	}

	public int getNextTrainerPosition(UUID playerUUID, String gauntletId) {
		if (isPartOfGauntlet(playerUUID, gauntletId)) {
			List<Integer> progress = playerGauntletProgress.get(playerUUID).get(gauntletId);
			for (int i = 0; i < progress.size(); i++) {
				if (progress.get(i) == 0) {
					return i;
				}
			}
			return progress.size();
		}
		return 0;
	}

	public String findGauntletForTrainer(String trainerUUID) {
		for (Map.Entry<String, List<String>> entry : gauntlets.entrySet()) {
			if (entry.getValue().contains(trainerUUID)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public int getTrainerPositionInGauntlet(String gauntletId, String trainerUUID) {
		if (gauntlets.containsKey(gauntletId)) {
			return gauntlets.get(gauntletId).indexOf(trainerUUID);
		}
		return -1;
	}

	public void completePortion(UUID playerUUID, String gauntletId, int position) {
		if (isPartOfGauntlet(playerUUID, gauntletId)) {
			List<Integer> progress = playerGauntletProgress.get(playerUUID).get(gauntletId);

			if (position >= 0 && position < progress.size()) {
				progress.set(position, 1);
				setDirty();
			} else {
				throw new IllegalArgumentException("Invalid position: " + position);
			}
		} else {
			throw new IllegalStateException("Player is not part of the specified gauntlet.");
		}
	}

	public void resetProgress(UUID playerUUID) {
		if (playerGauntletProgress.containsKey(playerUUID)) {
			Map<String, List<Integer>> playerProgress = playerGauntletProgress.get(playerUUID);

			if (playerProgress.size() == 1) {
				String gauntletId = playerProgress.keySet().iterator().next();
				List<Integer> progressList = new ArrayList<>(Collections.nCopies(gauntlets.get(gauntletId).size(), 0));
				playerProgress.put(gauntletId, progressList);
				setDirty();
			} else {
				throw new IllegalStateException("Player is not part of any gauntlet.");
			}
		}
	}

	public void setHealingAllowed(String gauntletId, boolean allowHealing) {
		if (!gauntlets.containsKey(gauntletId)) {
			throw new IllegalArgumentException("Invalid gauntlet ID: " + gauntletId);
		}

		gauntletHealingAllowed.put(gauntletId, allowHealing);
		setDirty();
	}

	public boolean isHealingAllowed(String gauntletId) {
		if (!gauntlets.containsKey(gauntletId)) {
			throw new IllegalArgumentException("Invalid gauntlet ID: " + gauntletId);
		}

		return gauntletHealingAllowed.getOrDefault(gauntletId, false);
	}

	public boolean isHealingAllowedForPlayer(UUID playerUUID) {
		String gauntletId = getCurrentGauntletName(playerUUID);
		if (gauntletId == null) {
			return true;
		}

		return isHealingAllowed(gauntletId);
	}

	public String findGauntletForTrainerWithPartners(String trainerUUID, ServerWorld world) {
		String gauntletId = findGauntletForTrainer(trainerUUID);
		if (gauntletId != null) {
			return gauntletId;
		}

		try {
			UUID uuid = UUID.fromString(trainerUUID);
			Entity entity = world.getEntity(uuid);
			if (entity instanceof NPCTrainer) {
				NPCTrainer trainer = (NPCTrainer) entity;
				CompoundNBT persistentData = trainer.getPersistentData();

				if (persistentData.contains("Linked")) {
					UUID linkedUUID = persistentData.getUUID("Linked");
					gauntletId = findGauntletForTrainer(linkedUUID.toString());
					if (gauntletId != null) {
						return gauntletId;
					}
				}

				if (persistentData.contains("Paired")) {
					UUID pairedUUID = persistentData.getUUID("Paired");
					gauntletId = findGauntletForTrainer(pairedUUID.toString());
					if (gauntletId != null) {
						return gauntletId;
					}
				}
			}
		} catch (IllegalArgumentException e) {}

		return null;
	}

	public int getEffectiveTrainerPosition(String trainerUUID, String gauntletId, ServerWorld world) {
		int position = getTrainerPositionInGauntlet(gauntletId, trainerUUID);
		if (position != -1) {
			return position;
		}

		try {
			UUID uuid = UUID.fromString(trainerUUID);
			Entity entity = world.getEntity(uuid);
			if (entity instanceof NPCTrainer) {
				NPCTrainer trainer = (NPCTrainer) entity;
				CompoundNBT persistentData = trainer.getPersistentData();

				if (persistentData.contains("Linked")) {
					UUID linkedUUID = persistentData.getUUID("Linked");
					position = getTrainerPositionInGauntlet(gauntletId, linkedUUID.toString());
					if (position != -1) {
						return position;
					}
				}

				if (persistentData.contains("Paired")) {
					UUID pairedUUID = persistentData.getUUID("Paired");
					position = getTrainerPositionInGauntlet(gauntletId, pairedUUID.toString());
					if (position != -1) {
						return position;
					}
				}
			}
		} catch (IllegalArgumentException e) {}

		return -1;
	}
}
