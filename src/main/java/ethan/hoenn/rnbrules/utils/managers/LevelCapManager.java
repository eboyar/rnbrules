package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.blocks.PokeChestBlock;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class LevelCapManager extends WorldSavedData implements ResetableManager{

	private static final String DATA_TAG = "levelcaps";
	private static LevelCapManager instance;
	private final Map<UUID, Integer> playerLevelCaps = new HashMap<>();

	public LevelCapManager() {
		super(DATA_TAG);
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerLevelCaps.clear();
		ListNBT capsList = nbt.getList("caps", 10);
		for (int i = 0; i < capsList.size(); i++) {
			CompoundNBT playerData = capsList.getCompound(i);
			UUID uuid = UUID.fromString(playerData.getString("uuid"));
			int levelCap = playerData.getInt("levelcap");
			playerLevelCaps.put(uuid, levelCap);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT capsList = new ListNBT();
		for (Map.Entry<UUID, Integer> entry : playerLevelCaps.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString("uuid", entry.getKey().toString());
			playerData.putInt("levelcap", entry.getValue());
			capsList.add(playerData);
		}
		nbt.put("caps", capsList);
		return nbt;
	}

	public static LevelCapManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(LevelCapManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerLevelCaps.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public int getLevelCap(UUID uuid) {
		return playerLevelCaps.getOrDefault(uuid, 5);
	}

	public void setLevelCap(UUID uuid, int levelCap) {
		playerLevelCaps.put(uuid, levelCap);
		setDirty();
	}

	public boolean hasLevelCap(UUID uuid) {
		return playerLevelCaps.containsKey(uuid);
	}
}
