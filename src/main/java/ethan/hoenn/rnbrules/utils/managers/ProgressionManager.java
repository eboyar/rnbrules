package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.api.pokedex.PlayerPokedex;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import ethan.hoenn.rnbrules.multiplayer.Rank;
import ethan.hoenn.rnbrules.multiplayer.StaffRank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ProgressionManager extends WorldSavedData {

	private static final String DATA_TAG = "progression";
	private static ProgressionManager instance;

	private static final Map<UUID, HashSet<UUID>> defeatedTrainers = new HashMap<>();
	private static final Map<UUID, HashSet<UUID>> gauntletDefeatedTrainers = new HashMap<>();
	private static final Map<UUID, String> playerActiveGauntlets = new HashMap<>();
	private static final Map<UUID, HashMap<UUID, List<String>>> completedDialogues = new HashMap<>();
	private static final Map<UUID, HashSet<UUID>> OTRewardClaimers = new HashMap<>();
	private static final Map<UUID, HashSet<String>> claimedPokeChests = new HashMap<>();
	private static final Map<UUID, Boolean> isFirstJoin = new HashMap<>();
	private static final Map<UUID, String> playerRanks = new HashMap<>();
	private static final Map<UUID, String> playerStaffRanks = new HashMap<>();

	public ProgressionManager() {
		super(DATA_TAG);
	}

	public static void init(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(ProgressionManager::new, DATA_TAG);
		}
	}

	public static ProgressionManager get() {
		return instance;
	}

	public static List<ResetableManager> getAllResetableManagers(ServerWorld world) {
		List<ResetableManager> managers = new ArrayList<>();

		BadgeManager badgeManager = BadgeManager.get(world);
		if (badgeManager != null) {
			managers.add(badgeManager);
		}

		StatueVisibilityManager statueManager = StatueVisibilityManager.get(world);
		if (statueManager != null) {
			managers.add(statueManager);
		}

		StarterSelectionManager starterManager = StarterSelectionManager.get(world);
		if (starterManager != null) {
			managers.add(starterManager);
		}

		LevelCapManager levelCapManager = LevelCapManager.get(world);
		if (levelCapManager != null) {
			managers.add(levelCapManager);
		}

		HiddenMachineManager hmManager = HiddenMachineManager.get(world);
		if (hmManager != null) {
			managers.add(hmManager);
		}

		GlobalOTManager otManager = GlobalOTManager.get(world);
		if (otManager != null) {
			managers.add(otManager);
		}

		GauntletManager gauntletManager = GauntletManager.get(world);
		if (gauntletManager != null) {
			managers.add(gauntletManager);
		}

		FlyManager flyManager = FlyManager.get(world);
		if (flyManager != null) {
			managers.add(flyManager);
		}

		FerryManager ferryManager = FerryManager.get(world);
		if (ferryManager != null) {
			managers.add(ferryManager);
		}

		BattleDependencyManager battleDepManager = BattleDependencyManager.get(world);
		if (battleDepManager != null) {
			managers.add(battleDepManager);
		}

		SafariManager safariManager = SafariManager.get(world);
		if (safariManager != null) {
			managers.add(safariManager);
		}

		LeagueManager leagueManager = LeagueManager.get(world);
		if (leagueManager != null) {
			managers.add(leagueManager);
		}

		CatchLocationManager catchManager = CatchLocationManager.get(world);
		if (catchManager != null) {
			managers.add(catchManager);
		}

		EncounterManager encounterManager = EncounterManager.get(world);
		if (encounterManager != null) {
			managers.add(encounterManager);
		}

		return managers;
	}

	public static Map<String, Boolean> resetPlayerData(List<ResetableManager> managers, UUID playerUUID) {
		Map<String, Boolean> resetResults = new HashMap<>();

		for (ResetableManager manager : managers) {
			String managerName = manager.getClass().getSimpleName();
			boolean hadData = manager.resetPlayerData(playerUUID);
			resetResults.put(managerName, hadData);
		}

		return resetResults;
	}

	public static Map<String, Boolean> globalResetPlayerData(ServerWorld world, UUID playerUUID) {
		List<ResetableManager> managers = getAllResetableManagers(world);

		Map<String, Boolean> resetResults = resetPlayerData(managers, playerUUID);
		isFirstJoin.put(playerUUID, true);

		ProgressionManager progressionManager = ProgressionManager.get();
		if (progressionManager != null) {
			boolean hadProgressionData = false;

			if (completedDialogues.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (OTRewardClaimers.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (defeatedTrainers.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (gauntletDefeatedTrainers.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (playerActiveGauntlets.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (claimedPokeChests.remove(playerUUID) != null) {
				hadProgressionData = true;
			}

			if (hadProgressionData) {
				progressionManager.setDirty();
			}

			progressionManager.setPlayerRank(playerUUID, Rank.NEWCOMER);

			resetResults.put("ProgressionManager", hadProgressionData);
		}

		BankAccount userAccount = (BankAccount) BankAccountProxy.getBankAccount(playerUUID).orElseThrow(() -> new IllegalStateException("Bank account not found for player: " + playerUUID));
		userAccount.setBalance(0);

		StorageProxy.getParty(playerUUID).playerPokedex.wipe();
		PlayerPartyStorage pps = StorageProxy.getParty(playerUUID);

		PCStorage pcs = StorageProxy.getPCForPlayer(playerUUID);
		for (int i = 0; i < 6; i++) {
			pps.set(i, (Pokemon) null);
		}

		pcs.forEach((pokemon, position) -> {
			if (pokemon != null) {
				pcs.set(position, (Pokemon) null);
			}
		});

		ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
		if (player != null) {
			for (int i = 0; i < player.inventory.getContainerSize(); i++) {
				player.inventory.setItem(i, ItemStack.EMPTY);
			}
		}

		LocationManager.get(player.getLevel()).updatePlayerLocation(player, "&eLittleroot Town");

		return resetResults;
	}

	public void markDialogueCompleted(UUID playerUUID, UUID npcUUID, String dialogueId) {
		if (dialogueId == null || dialogueId.isEmpty()) return;

		completedDialogues.computeIfAbsent(playerUUID, k -> new HashMap<>()).computeIfAbsent(npcUUID, k -> new ArrayList<>()).add(dialogueId);

		this.setDirty();
	}

	public boolean hasCompletedDialogue(UUID playerUUID, UUID npcUUID, String dialogueId) {
		if (dialogueId == null || dialogueId.isEmpty()) return false;

		HashMap<UUID, List<String>> playerDialogues = completedDialogues.get(playerUUID);
		if (playerDialogues == null) return false;

		List<String> npcDialogues = playerDialogues.get(npcUUID);
		return npcDialogues != null && npcDialogues.contains(dialogueId);
	}

	public void markRewardClaimed(UUID playerUUID, UUID npcUUID) {
		OTRewardClaimers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(npcUUID);
		this.setDirty();
	}

	public boolean hasClaimedReward(UUID playerUUID, UUID npcUUID) {
		HashSet<UUID> claimedNpcs = OTRewardClaimers.get(playerUUID);
		return claimedNpcs != null && claimedNpcs.contains(npcUUID);
	}

	public void markTrainerDefeated(UUID playerUUID, UUID trainerUUID) {
		defeatedTrainers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(trainerUUID);
		this.setDirty();
	}

	public boolean hasDefeatedTrainer(UUID playerUUID, UUID trainerUUID) {
		HashSet<UUID> defeated = defeatedTrainers.get(playerUUID);
		return defeated != null && defeated.contains(trainerUUID);
	}

	public boolean removeDefeatedTrainer(UUID playerUUID, UUID trainerUUID) {
		HashSet<UUID> defeated = defeatedTrainers.get(playerUUID);
		if (defeated != null && defeated.remove(trainerUUID)) {
			this.setDirty();
			return true;
		}
		return false;
	}

	public void markTempTrainerDefeated(UUID playerUUID, UUID trainerUUID) {
		gauntletDefeatedTrainers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(trainerUUID);
		this.setDirty();
	}

	public boolean hasTempDefeatedTrainer(UUID playerUUID, UUID trainerUUID) {
		HashSet<UUID> defeated = gauntletDefeatedTrainers.get(playerUUID);
		return defeated != null && defeated.contains(trainerUUID);
	}

	public void clearTempDefeatedTrainers(UUID playerUUID) {
		gauntletDefeatedTrainers.remove(playerUUID);
		this.setDirty();
	}

	public void setPlayerActiveGauntlet(UUID playerUUID, String gauntletId) {
		playerActiveGauntlets.put(playerUUID, gauntletId);
		this.setDirty();
	}

	public void removePlayerActiveGauntlet(UUID playerUUID) {
		playerActiveGauntlets.remove(playerUUID);
		this.setDirty();
	}

	public void startGauntlet(UUID playerUUID, String gauntletId) {
		playerActiveGauntlets.put(playerUUID, gauntletId);
		gauntletDefeatedTrainers.remove(playerUUID);
		this.setDirty();
	}

	public void markGauntletTrainerDefeated(UUID playerUUID, UUID trainerUUID) {
		gauntletDefeatedTrainers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(trainerUUID);
		this.setDirty();
	}

	public boolean hasGauntletTrainerBeenDefeated(UUID playerUUID, UUID trainerUUID) {
		HashSet<UUID> defeated = gauntletDefeatedTrainers.get(playerUUID);
		return defeated != null && defeated.contains(trainerUUID);
	}

	public boolean removeGauntletTrainerDefeated(UUID playerUUID, UUID trainerUUID) {
		HashSet<UUID> defeated = gauntletDefeatedTrainers.get(playerUUID);
		if (defeated != null && defeated.remove(trainerUUID)) {
			this.setDirty();
			return true;
		}
		return false;
	}

	public void completeGauntletSuccessfully(UUID playerUUID) {
		HashSet<UUID> tempDefeated = gauntletDefeatedTrainers.get(playerUUID);
		if (tempDefeated != null && !tempDefeated.isEmpty()) {
			HashSet<UUID> permanent = defeatedTrainers.computeIfAbsent(playerUUID, k -> new HashSet<>());
			permanent.addAll(tempDefeated);
		}

		gauntletDefeatedTrainers.remove(playerUUID);
		playerActiveGauntlets.remove(playerUUID);
		this.setDirty();
	}

	public void failGauntlet(UUID playerUUID) {
		gauntletDefeatedTrainers.remove(playerUUID);
		playerActiveGauntlets.remove(playerUUID);
		this.setDirty();
	}

	public boolean isInGauntlet(UUID playerUUID) {
		return playerActiveGauntlets.containsKey(playerUUID);
	}

	public String getActiveGauntletId(UUID playerUUID) {
		return playerActiveGauntlets.get(playerUUID);
	}

	public void markPokeChestClaimed(UUID playerUUID, String chestId) {
		claimedPokeChests.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(chestId);
		this.setDirty();
	}

	public boolean hasClaimedPokeChest(UUID playerUUID, String chestId) {
		HashSet<String> chests = claimedPokeChests.get(playerUUID);
		return chests != null && chests.contains(chestId);
	}

	public boolean isPlayerFirstJoin(UUID playerUUID) {
		return isFirstJoin.getOrDefault(playerUUID, true);
	}

	public void markPlayerAsJoined(UUID playerUUID) {
		isFirstJoin.put(playerUUID, false);
		this.setDirty();
	}

	public void resetPlayerFirstJoinStatus(UUID playerUUID) {
		isFirstJoin.put(playerUUID, true);
		this.setDirty();
	}

	public void setPlayerRank(UUID playerUUID, String rankId) {
		playerRanks.put(playerUUID, rankId);
		this.setDirty();
	}

	public String getPlayerRank(UUID playerUUID) {
		return playerRanks.get(playerUUID);
	}

	public Rank getPlayerRankObject(UUID playerUUID) {
		String rankId = playerRanks.get(playerUUID);
		if (rankId == null) {
			return Rank.NEWCOMER;
		}

		switch (rankId) {
			case "newcomer":
				return Rank.NEWCOMER;
			case "novice":
				return Rank.NOVICE;
			case "rookie":
				return Rank.ROOKIE;
			case "junior":
				return Rank.JUNIOR;
			case "adept":
				return Rank.ADEPT;
			case "ace":
				return Rank.ACE;
			case "veteran":
				return Rank.VETERAN;
			case "expert":
				return Rank.EXPERT;
			case "master":
				return Rank.MASTER;
			case "champion":
				return Rank.CHAMPION;
			default:
				return Rank.NEWCOMER;
		}
	}

	public void setPlayerRank(UUID playerUUID, Rank rank) {
		playerRanks.put(playerUUID, rank.getId());
		this.setDirty();
	}

	public void setPlayerStaffRank(UUID playerUUID, String staffRankId) {
		playerStaffRanks.put(playerUUID, staffRankId);
		this.setDirty();
	}

	public String getPlayerStaffRank(UUID playerUUID) {
		return playerStaffRanks.get(playerUUID);
	}

	public StaffRank getPlayerStaffRankObject(UUID playerUUID) {
		String staffRankId = playerStaffRanks.get(playerUUID);
		if (staffRankId == null) {
			return null;
		}

		switch (staffRankId) {
			case "staff":
				return StaffRank.STAFF;
			case "admin":
				return StaffRank.ADMIN;
			default:
				return null;
		}
	}

	public void setPlayerStaffRank(UUID playerUUID, StaffRank staffRank) {
		if (staffRank == null) {
			playerStaffRanks.remove(playerUUID);
		} else {
			playerStaffRanks.put(playerUUID, staffRank.getId());
		}
		this.setDirty();
	}

	public void removePlayerStaffRank(UUID playerUUID) {
		playerStaffRanks.remove(playerUUID);
		this.setDirty();
	}

	@Override
	public void load(CompoundNBT nbt) {
		completedDialogues.clear();
		OTRewardClaimers.clear();
		defeatedTrainers.clear();
		gauntletDefeatedTrainers.clear();
		playerActiveGauntlets.clear();
		claimedPokeChests.clear();
		playerRanks.clear();
		playerStaffRanks.clear();

		if (nbt.contains("CompletedDialogues")) {
			CompoundNBT dialoguesNBT = nbt.getCompound("CompletedDialogues");

			for (String playerUUIDStr : dialoguesNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				CompoundNBT playerDialoguesNBT = dialoguesNBT.getCompound(playerUUIDStr);

				HashMap<UUID, List<String>> playerDialogues = new HashMap<>();
				completedDialogues.put(playerUUID, playerDialogues);

				for (String npcUUIDStr : playerDialoguesNBT.getAllKeys()) {
					UUID npcUUID = UUID.fromString(npcUUIDStr);
					ListNBT dialogueListNBT = playerDialoguesNBT.getList(npcUUIDStr, 8);

					List<String> dialogueList = new ArrayList<>();
					for (int i = 0; i < dialogueListNBT.size(); i++) {
						dialogueList.add(dialogueListNBT.getString(i));
					}

					playerDialogues.put(npcUUID, dialogueList);
				}
			}
		}

		if (nbt.contains("OTRewardClaimers")) {
			CompoundNBT claimersNBT = nbt.getCompound("OTRewardClaimers");

			for (String playerUUIDStr : claimersNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				ListNBT claimedNpcsNBT = claimersNBT.getList(playerUUIDStr, 8);

				HashSet<UUID> claimedNpcs = new HashSet<>();
				OTRewardClaimers.put(playerUUID, claimedNpcs);

				for (int i = 0; i < claimedNpcsNBT.size(); i++) {
					claimedNpcs.add(UUID.fromString(claimedNpcsNBT.getString(i)));
				}
			}
		}

		if (nbt.contains("DefeatedTrainers")) {
			CompoundNBT trainersNBT = nbt.getCompound("DefeatedTrainers");

			for (String playerUUIDStr : trainersNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				ListNBT defeatedTrainersNBT = trainersNBT.getList(playerUUIDStr, 8);

				HashSet<UUID> playerDefeatedTrainers = new HashSet<>();
				defeatedTrainers.put(playerUUID, playerDefeatedTrainers);
				for (int i = 0; i < defeatedTrainersNBT.size(); i++) {
					playerDefeatedTrainers.add(UUID.fromString(defeatedTrainersNBT.getString(i)));
				}
			}
		}

		if (nbt.contains("TempGauntletDefeatedTrainers")) {
			CompoundNBT tempTrainersNBT = nbt.getCompound("TempGauntletDefeatedTrainers");

			for (String playerUUIDStr : tempTrainersNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				ListNBT tempDefeatedTrainersNBT = tempTrainersNBT.getList(playerUUIDStr, 8);

				HashSet<UUID> tempPlayerDefeatedTrainers = new HashSet<>();
				gauntletDefeatedTrainers.put(playerUUID, tempPlayerDefeatedTrainers);

				for (int i = 0; i < tempDefeatedTrainersNBT.size(); i++) {
					tempPlayerDefeatedTrainers.add(UUID.fromString(tempDefeatedTrainersNBT.getString(i)));
				}
			}
		}

		if (nbt.contains("PlayerActiveGauntlets")) {
			CompoundNBT activeGauntletsNBT = nbt.getCompound("PlayerActiveGauntlets");

			for (String playerUUIDStr : activeGauntletsNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				String gauntletId = activeGauntletsNBT.getString(playerUUIDStr);
				playerActiveGauntlets.put(playerUUID, gauntletId);
			}
		}

		if (nbt.contains("ClaimedPokeChests")) {
			CompoundNBT chestsNBT = nbt.getCompound("ClaimedPokeChests");
			for (String playerUUIDStr : chestsNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				ListNBT chestListNBT = chestsNBT.getList(playerUUIDStr, 8);
				HashSet<String> playerClaimedChests = new HashSet<>();
				for (int i = 0; i < chestListNBT.size(); i++) {
					playerClaimedChests.add(chestListNBT.getString(i));
				}
				claimedPokeChests.put(playerUUID, playerClaimedChests);
			}
		}

		if (nbt.contains("IsFirstJoin")) {
			CompoundNBT firstJoinNBT = nbt.getCompound("IsFirstJoin");
			for (String playerUUIDStr : firstJoinNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				boolean firstJoin = firstJoinNBT.getBoolean(playerUUIDStr);
				isFirstJoin.put(playerUUID, firstJoin);
			}
		}

		if (nbt.contains("PlayerRanks")) {
			CompoundNBT ranksNBT = nbt.getCompound("PlayerRanks");
			for (String playerUUIDStr : ranksNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				String rankId = ranksNBT.getString(playerUUIDStr);
				playerRanks.put(playerUUID, rankId);
			}
		}

		if (nbt.contains("PlayerStaffRanks")) {
			CompoundNBT staffRanksNBT = nbt.getCompound("PlayerStaffRanks");
			for (String playerUUIDStr : staffRanksNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				String staffRankId = staffRanksNBT.getString(playerUUIDStr);
				playerStaffRanks.put(playerUUID, staffRankId);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT dialoguesNBT = new CompoundNBT();

		for (Map.Entry<UUID, HashMap<UUID, List<String>>> playerEntry : completedDialogues.entrySet()) {
			CompoundNBT playerDialoguesNBT = new CompoundNBT();

			for (Map.Entry<UUID, List<String>> npcEntry : playerEntry.getValue().entrySet()) {
				ListNBT dialogueListNBT = new ListNBT();

				for (String dialogueId : npcEntry.getValue()) {
					dialogueListNBT.add(StringNBT.valueOf(dialogueId));
				}

				playerDialoguesNBT.put(npcEntry.getKey().toString(), dialogueListNBT);
			}

			dialoguesNBT.put(playerEntry.getKey().toString(), playerDialoguesNBT);
		}

		nbt.put("CompletedDialogues", dialoguesNBT);

		CompoundNBT claimersNBT = new CompoundNBT();

		for (Map.Entry<UUID, HashSet<UUID>> playerEntry : OTRewardClaimers.entrySet()) {
			ListNBT claimedNpcsNBT = new ListNBT();

			for (UUID npcUUID : playerEntry.getValue()) {
				claimedNpcsNBT.add(StringNBT.valueOf(npcUUID.toString()));
			}

			claimersNBT.put(playerEntry.getKey().toString(), claimedNpcsNBT);
		}

		nbt.put("OTRewardClaimers", claimersNBT);

		CompoundNBT trainersNBT = new CompoundNBT();

		for (Map.Entry<UUID, HashSet<UUID>> playerEntry : defeatedTrainers.entrySet()) {
			ListNBT defeatedTrainersNBT = new ListNBT();

			for (UUID trainerUUID : playerEntry.getValue()) {
				defeatedTrainersNBT.add(StringNBT.valueOf(trainerUUID.toString()));
			}

			trainersNBT.put(playerEntry.getKey().toString(), defeatedTrainersNBT);
		}
		nbt.put("DefeatedTrainers", trainersNBT);

		CompoundNBT tempTrainersNBT = new CompoundNBT();

		for (Map.Entry<UUID, HashSet<UUID>> playerEntry : gauntletDefeatedTrainers.entrySet()) {
			ListNBT tempDefeatedTrainersNBT = new ListNBT();

			for (UUID trainerUUID : playerEntry.getValue()) {
				tempDefeatedTrainersNBT.add(StringNBT.valueOf(trainerUUID.toString()));
			}

			tempTrainersNBT.put(playerEntry.getKey().toString(), tempDefeatedTrainersNBT);
		}

		nbt.put("TempGauntletDefeatedTrainers", tempTrainersNBT);

		CompoundNBT activeGauntletsNBT = new CompoundNBT();

		for (Map.Entry<UUID, String> entry : playerActiveGauntlets.entrySet()) {
			activeGauntletsNBT.putString(entry.getKey().toString(), entry.getValue());
		}

		nbt.put("PlayerActiveGauntlets", activeGauntletsNBT);

		CompoundNBT chestsNBT = new CompoundNBT();
		for (Map.Entry<UUID, HashSet<String>> playerEntry : claimedPokeChests.entrySet()) {
			ListNBT chestListNBT = new ListNBT();
			for (String chestId : playerEntry.getValue()) {
				chestListNBT.add(StringNBT.valueOf(chestId));
			}
			chestsNBT.put(playerEntry.getKey().toString(), chestListNBT);
		}
		nbt.put("ClaimedPokeChests", chestsNBT);

		CompoundNBT firstJoinNBT = new CompoundNBT();
		for (Map.Entry<UUID, Boolean> entry : isFirstJoin.entrySet()) {
			firstJoinNBT.putBoolean(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("IsFirstJoin", firstJoinNBT);

		CompoundNBT ranksNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
			ranksNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("PlayerRanks", ranksNBT);

		CompoundNBT staffRanksNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : playerStaffRanks.entrySet()) {
			staffRanksNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("PlayerStaffRanks", staffRanksNBT);

		return nbt;
	}
}
