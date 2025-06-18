package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import com.pixelmonmod.pixelmon.entities.npcs.*;
import ethan.hoenn.rnbrules.utils.data.BattleDependency;
import java.util.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class BattleDependencyManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "battle_dependencies";
	private static final String DEP_TAG = "BattleDeps";
	private static BattleDependencyManager instance;
	private final Map<String, BattleDependency> allDependencies = new HashMap<>();
	private final Map<UUID, Set<String>> playerDependencies = new HashMap<>();

	public BattleDependencyManager() {
		super(DATA_TAG);
	}

	public static BattleDependencyManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(BattleDependencyManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerDependencies.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public boolean addDependency(String id, String description) {
		if (allDependencies.containsKey(id)) return false;
		allDependencies.put(id, new BattleDependency(id, description));
		setDirty();
		return true;
	}

	public boolean dependencyExists(String id) {
		return allDependencies.containsKey(id);
	}

	public BattleDependency getDependency(String id) {
		return allDependencies.get(id);
	}

	public Set<String> getAllDependencyIds() {
		return allDependencies.keySet();
	}

	//player
	public boolean addPlayerDependency(UUID player, String depId) {
		if (!dependencyExists(depId)) return false;
		playerDependencies.computeIfAbsent(player, k -> new HashSet<>()).add(depId);
		setDirty();
		return true;
	}

	public boolean removePlayerDependency(UUID player, String depId) {
		Set<String> deps = playerDependencies.get(player);
		if (deps == null || !deps.remove(depId)) return false;
		setDirty();
		return true;
	}

	public boolean playerHasDependency(UUID player, String depId) {
		Set<String> deps = playerDependencies.get(player);
		return deps != null && deps.contains(depId);
	}

	public Set<String> getPlayerDependencies(UUID player) {
		return playerDependencies.getOrDefault(player, Collections.emptySet());
	}

	//trainer
	public boolean addDependencyToTrainer(NPCTrainer trainer, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = trainer.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getTrainerDependencies(NPCTrainer trainer) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = trainer.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearTrainerDependencies(NPCTrainer trainer) {
		trainer.getPersistentData().remove(DEP_TAG);
	}

	public boolean trainerHasDependencies(NPCTrainer trainer) {
		return trainer.getPersistentData().contains(DEP_TAG, 9);
	}

	//chatting
	public boolean addDependencyToNPC(NPCChatting npc, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = npc.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getNPCDependencies(NPCChatting npc) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = npc.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearNPCDependencies(NPCChatting npc) {
		npc.getPersistentData().remove(DEP_TAG);
	}

	public boolean npcHasDependencies(NPCChatting npc) {
		return npc.getPersistentData().contains(DEP_TAG, 9);
	}

	//trader
	public boolean addDependencyToNPCTrader(NPCTrader npc, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = npc.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getNPCTraderDependencies(NPCTrader npc) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = npc.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearNPCTraderDependencies(NPCTrader npc) {
		npc.getPersistentData().remove(DEP_TAG);
	}

	public boolean npcTraderHasDependencies(NPCTrader npc) {
		return npc.getPersistentData().contains(DEP_TAG, 9);
	}

	//move tutor
	public boolean addDependencyToTutor(NPCTutor tutor, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = tutor.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getTutorDependencies(NPCTutor tutor) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = tutor.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearTutorDependencies(NPCTutor tutor) {
		tutor.getPersistentData().remove(DEP_TAG);
	}

	public boolean tutorHasDependencies(NPCTutor tutor) {
		return tutor.getPersistentData().contains(DEP_TAG, 9);
	}

	// shopkeeper
	public boolean addDependencyToShopkeeper(NPCShopkeeper shopkeeper, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = shopkeeper.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getShopkeeperDependencies(NPCShopkeeper shopkeeper) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = shopkeeper.getPersistentData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearShopkeeperDependencies(NPCShopkeeper shopkeeper) {
		shopkeeper.getPersistentData().remove(DEP_TAG);
	}

	public boolean shopkeeperHasDependencies(NPCShopkeeper shopkeeper) {
		return shopkeeper.getPersistentData().contains(DEP_TAG, 9);
	}

	// PokeChest
	public boolean addDependencyToPokeChest(PokeChestTileEntity chest, String depId) {
		if (!dependencyExists(depId)) return false;

		CompoundNBT data = chest.getTileData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			if (list.getString(i).equals(depId)) {
				return false;
			}
		}

		list.add(StringNBT.valueOf(depId));
		data.put(DEP_TAG, list);
		return true;
	}

	public Set<String> getPokeChestDependencies(PokeChestTileEntity chest) {
		Set<String> deps = new HashSet<>();
		CompoundNBT data = chest.getTileData();
		ListNBT list = data.getList(DEP_TAG, 8);

		for (int i = 0; i < list.size(); i++) {
			deps.add(list.getString(i));
		}

		return deps;
	}

	public void clearPokeChestDependencies(PokeChestTileEntity chest) {
		chest.getTileData().remove(DEP_TAG);
	}

	public boolean pokeChestHasDependencies(PokeChestTileEntity chest) {
		return chest.getTileData().contains(DEP_TAG, 9);
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		CompoundNBT depsTag = new CompoundNBT();
		for (Map.Entry<String, BattleDependency> entry : allDependencies.entrySet()) {
			depsTag.putString(entry.getKey(), entry.getValue().getDescription());
		}
		tag.put("AllDependencies", depsTag);

		CompoundNBT playerTag = new CompoundNBT();
		for (Map.Entry<UUID, Set<String>> entry : playerDependencies.entrySet()) {
			ListNBT list = new ListNBT();
			for (String depId : entry.getValue()) {
				list.add(StringNBT.valueOf(depId));
			}
			playerTag.put(entry.getKey().toString(), list);
		}
		tag.put("PlayerDependencies", playerTag);

		return tag;
	}

	@Override
	public void load(CompoundNBT tag) {
		allDependencies.clear();
		playerDependencies.clear();

		CompoundNBT depsTag = tag.getCompound("AllDependencies");
		for (String key : depsTag.getAllKeys()) {
			String description = depsTag.getString(key);
			allDependencies.put(key, new BattleDependency(key, description));
		}

		CompoundNBT playerTag = tag.getCompound("PlayerDependencies");
		for (String uuidStr : playerTag.getAllKeys()) {
			try {
				UUID uuid = UUID.fromString(uuidStr);
				ListNBT list = playerTag.getList(uuidStr, 8);
				Set<String> deps = new HashSet<>();
				for (int i = 0; i < list.size(); i++) {
					deps.add(list.getString(i));
				}
				playerDependencies.put(uuid, deps);
			} catch (IllegalArgumentException e) {}
		}
	}
}
