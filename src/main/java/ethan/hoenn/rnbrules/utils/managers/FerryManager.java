package ethan.hoenn.rnbrules.utils.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class FerryManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "ferrys";
	private static final int COOLDOWN_TICKS = 200;
	private static FerryManager instance;

	private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();

	private final Map<UUID, Set<String>> playerDestinations = new HashMap<>();

	public FerryManager() {
		super(DATA_TAG);
	}

	public static FerryManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(FerryManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerDestinations.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public static boolean canUseFerry(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		if (playerCooldowns.containsKey(playerUUID)) {
			int remainingTicks = playerCooldowns.get(playerUUID);
			if (remainingTicks > 0) {
				return false;
			}
		}
		return true;
	}

	public static void setPlayerCooldown(ServerPlayerEntity player) {
		playerCooldowns.put(player.getUUID(), COOLDOWN_TICKS);
	}

	public static void tickAll() {
		playerCooldowns
			.entrySet()
			.forEach(entry -> {
				int cooldown = entry.getValue();
				if (cooldown > 0) {
					entry.setValue(cooldown - 1);
				}
			});
	}

	public boolean hasDestination(UUID playerUUID, String destination) {
		Set<String> destinations = playerDestinations.get(playerUUID);
		return destinations != null && destinations.contains(destination);
	}

	public void addDestination(UUID playerUUID, String destination) {
		playerDestinations.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(destination);
		setDirty();
	}

	public void removeDestination(UUID playerUUID, String destination) {
		Set<String> destinations = playerDestinations.get(playerUUID);
		if (destinations != null) {
			destinations.remove(destination);
			setDirty();
		}
	}

	public Set<String> getPlayerDestinations(UUID playerUUID) {
		return playerDestinations.getOrDefault(playerUUID, new HashSet<>());
	}

	public void resetDestinations(UUID playerUUID) {
		playerDestinations.remove(playerUUID);
		setDirty();
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerDestinations.clear();

		ListNBT playersList = nbt.getList("playerDestinations", 10);
		for (int i = 0; i < playersList.size(); i++) {
			CompoundNBT playerData = playersList.getCompound(i);
			UUID playerUUID = UUID.fromString(playerData.getString("uuid"));

			Set<String> destinations = new HashSet<>();
			ListNBT destinationsNBT = playerData.getList("destinations", 8);
			for (int j = 0; j < destinationsNBT.size(); j++) {
				destinations.add(destinationsNBT.getString(j));
			}

			playerDestinations.put(playerUUID, destinations);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT playersList = new ListNBT();

		for (Map.Entry<UUID, Set<String>> entry : playerDestinations.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", entry.getKey().toString());

			ListNBT destinationsNBT = new ListNBT();
			for (String destination : entry.getValue()) {
				destinationsNBT.add(StringNBT.valueOf(destination));
			}

			playerData.put("destinations", destinationsNBT);
			playersList.add(playerData);
		}

		nbt.put("playerDestinations", playersList);
		return nbt;
	}
}
