package ethan.hoenn.rnbrules.utils.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class StarterSelectionManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "starters";
	private static StarterSelectionManager instance;

	public enum StarterChoice {
		PIPLUP,
		CHIMCHAR,
		TURTWIG;

		public static StarterChoice fromString(String name) {
			for (StarterChoice choice : values()) {
				if (choice.name().equalsIgnoreCase(name)) {
					return choice;
				}
			}
			return null;
		}
	}

	private final Map<UUID, StarterChoice> playerSelections = new HashMap<>();

	public StarterSelectionManager() {
		super(DATA_TAG);
	}

	public static StarterSelectionManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(StarterSelectionManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerSelections.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public void setPlayerSelection(UUID uuid, StarterChoice choice) {
		this.playerSelections.put(uuid, choice);
		this.setDirty();
	}

	public StarterChoice getPlayerSelection(UUID uuid) {
		return this.playerSelections.get(uuid);
	}

	public void removePlayer(UUID uuid) {
		this.playerSelections.remove(uuid);
		this.setDirty();
	}

	@Override
	public void load(CompoundNBT nbt) {
		ListNBT list = nbt.getList("Selections", 10);
		this.playerSelections.clear();

		for (int i = 0; i < list.size(); i++) {
			CompoundNBT entry = list.getCompound(i);
			UUID uuid = UUID.fromString(entry.getString("UUID"));
			StarterChoice choice = StarterChoice.fromString(entry.getString("Choice"));

			if (choice != null) {
				this.playerSelections.put(uuid, choice);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT list = new ListNBT();

		for (Map.Entry<UUID, StarterChoice> entry : this.playerSelections.entrySet()) {
			CompoundNBT compound = new CompoundNBT();
			compound.putString("UUID", entry.getKey().toString());
			compound.putString("Choice", entry.getValue().name());
			list.add(compound);
		}

		nbt.put("Selections", list);
		return nbt;
	}
}
