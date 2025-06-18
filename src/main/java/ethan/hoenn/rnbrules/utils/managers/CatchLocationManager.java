package ethan.hoenn.rnbrules.utils.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class CatchLocationManager extends WorldSavedData implements ResetableManager {

	private final Map<UUID, HashSet<String>> playerCatchLocations = new ConcurrentHashMap<>();

	private static final String DATA_TAG = "catchlocation";
	private static CatchLocationManager instance;

	public CatchLocationManager() {
		super(DATA_TAG);
	}

	public static CatchLocationManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(CatchLocationManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerCatchLocations.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerCatchLocations.clear();

		CompoundNBT playersNBT = nbt.getCompound("Players");
		for (String uuidString : playersNBT.getAllKeys()) {
			UUID playerUUID = UUID.fromString(uuidString);
			HashSet<String> locations = new HashSet<>();

			ListNBT locationsList = playersNBT.getList(uuidString, 8);
			for (int i = 0; i < locationsList.size(); i++) {
				locations.add(locationsList.getString(i));
			}

			playerCatchLocations.put(playerUUID, locations);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT playersNBT = new CompoundNBT();

		for (Map.Entry<UUID, HashSet<String>> entry : playerCatchLocations.entrySet()) {
			ListNBT locationsList = new ListNBT();
			for (String location : entry.getValue()) {
				locationsList.add(StringNBT.valueOf(location));
			}

			playersNBT.put(entry.getKey().toString(), locationsList);
		}

		nbt.put("Players", playersNBT);
		return nbt;
	}

	public boolean hasPlayerCaughtAtLocation(UUID playerUUID, String location) {
		HashSet<String> locations = playerCatchLocations.get(playerUUID);
		return locations != null && locations.contains(location);
	}

	public void addCatchLocation(UUID playerUUID, String location) {
		playerCatchLocations.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(location);
		setDirty();
	}

	public HashSet<String> getPlayerCatchLocations(UUID playerUUID) {
		return playerCatchLocations.getOrDefault(playerUUID, new HashSet<>());
	}
}
