package ethan.hoenn.rnbrules.utils.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class HiddenMachineManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "hidden_machines";
	private static HiddenMachineManager instance;
	private final Map<UUID, Set<String>> playerHMs = new HashMap<>();

	public HiddenMachineManager() {
		super(DATA_TAG);
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerHMs.clear();
		ListNBT hmsList = nbt.getList("hms", 10);
		for (int i = 0; i < hmsList.size(); i++) {
			CompoundNBT playerData = hmsList.getCompound(i);
			UUID uuid = UUID.fromString(playerData.getString("uuid"));
			Set<String> hms = new HashSet<>();
			ListNBT hmsNBT = playerData.getList("hms", 8);
			for (int j = 0; j < hmsNBT.size(); j++) {
				hms.add(hmsNBT.getString(j));
			}
			playerHMs.put(uuid, hms);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT hmsList = new ListNBT();
		for (Map.Entry<UUID, Set<String>> entry : playerHMs.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", entry.getKey().toString());
			ListNBT hmsNBT = new ListNBT();
			for (String hm : entry.getValue()) {
				hmsNBT.add(StringNBT.valueOf(hm));
			}
			playerData.put("hms", hmsNBT);
			hmsList.add(playerData);
		}
		nbt.put("hms", hmsList);
		return nbt;
	}

	public static HiddenMachineManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(HiddenMachineManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerHMs.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public Set<String> getPlayerHMs(UUID uuid) {
		return playerHMs.getOrDefault(uuid, new HashSet<>());
	}

	public void addHM(UUID uuid, String hm) {
		playerHMs.computeIfAbsent(uuid, k -> new HashSet<>()).add(hm);
		setDirty();
	}

	public boolean hasHM(UUID uuid, String hm) {
		Set<String> hms = playerHMs.get(uuid);
		return hms != null && hms.contains(hm);
	}

	public void removeHM(UUID uuid, String hm) {
		Set<String> hms = playerHMs.get(uuid);
		if (hms != null) {
			hms.remove(hm);
			setDirty();
		}
	}

	public void resetHMs(UUID uuid) {
		if (playerHMs.containsKey(uuid)) {
			playerHMs.remove(uuid);
			setDirty();
		}
	}

	public int getHMCount(UUID uuid) {
		Set<String> hms = playerHMs.get(uuid);
		return hms != null ? hms.size() : 0;
	}
}
