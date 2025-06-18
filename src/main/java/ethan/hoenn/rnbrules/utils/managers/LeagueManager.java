package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.EnumEncounterMode;
import com.pixelmonmod.pixelmon.enums.EnumMegaItemsUnlocked;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.RNBConfig.TeleportLocation;
import ethan.hoenn.rnbrules.dialogue.DialogueManager;
import ethan.hoenn.rnbrules.dialogue.DialoguePage;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.dialogue.yaml.DialogueParser;
import ethan.hoenn.rnbrules.gui.league.LeagueBattleCountdown;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialoguePageData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class LeagueManager extends WorldSavedData implements ResetableManager {

	private static final int[] ARENA_PRIORITY = { 3, 2, 4, 1, 5 };
	private static final String DATA_TAG = "pokemonleague";
	private static LeagueManager instance;

	private final Map<UUID, List<Progress>> playerProgress = new HashMap<>();
	private final Map<Integer, UUID> arenasInUse = new HashMap<>();
	private final Map<UUID, LeagueMember> leagueDialogueCompleted = new HashMap<>();

	private final Map<UUID, UUID> leagueBattleCopies = new HashMap<>();
	private final Map<UUID, UUID> copiedToOriginal = new HashMap<>();
	private final Map<UUID, LeagueMember> copiedToLeagueMember = new HashMap<>();

	private static final int CLEANUP_DELAY_TICKS = 100;
	private final List<LeagueBattleCleanupJob> cleanupQueue = new ArrayList<>();

	private static class LeagueBattleCleanupJob {

		final UUID playerUUID;
		int ticksRemaining;

		LeagueBattleCleanupJob(UUID playerUUID, int ticksRemaining) {
			this.playerUUID = playerUUID;
			this.ticksRemaining = ticksRemaining;
		}
	}

	public enum Progress {
		NONE,
		SINGLES,
		DOUBLES,
		CHAMPION,
	}

	public enum LeagueMember {
		SIDNEY,
		PHOEBE,
		GLACIA,
		DRAKE,
		WALLACE,
	}

	public LeagueManager() {
		super(DATA_TAG);
	}

	public static LeagueManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(LeagueManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = false;

		if (playerProgress.remove(playerUUID) != null) {
			hadData = true;
		}

		if (leagueDialogueCompleted.remove(playerUUID) != null) {
			hadData = true;
		}

		
		if (leagueBattleCopies.remove(playerUUID) != null) {
			hadData = true;
		}

		
		Integer arenaToRelease = null;
		for (Map.Entry<Integer, UUID> entry : arenasInUse.entrySet()) {
			if (entry.getValue().equals(playerUUID)) {
				arenaToRelease = entry.getKey();
				break;
			}
		}
		if (arenaToRelease != null) {
			arenasInUse.remove(arenaToRelease);
			hadData = true;
		}

		if (hadData) {
			setDirty();
		}

		return hadData;
	}

	public void initializePlayerProgress(UUID playerUUID) {
		if (!playerProgress.containsKey(playerUUID)) {
			List<Progress> progress = Arrays.asList(Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE);
			playerProgress.put(playerUUID, progress);
			setDirty();
		}
	}

	public List<Progress> getPlayerProgress(UUID playerUUID) {
		initializePlayerProgress(playerUUID);
		return playerProgress.get(playerUUID);
	}

	public void setPlayerProgress(UUID playerUUID, int memberIndex, Progress progress) {
		initializePlayerProgress(playerUUID);
		if (memberIndex >= 0 && memberIndex < 5) {
			List<Progress> currentProgress = playerProgress.get(playerUUID);
			currentProgress.set(memberIndex, progress);
			setDirty();
		}
	}

	public boolean hasPlayerCompleted(UUID playerUUID, int memberIndex) {
		List<Progress> progress = getPlayerProgress(playerUUID);
		if (memberIndex >= 0 && memberIndex < 5) {
			Progress memberProgress = progress.get(memberIndex);
			return memberProgress == Progress.SINGLES || memberProgress == Progress.DOUBLES || memberProgress == Progress.CHAMPION;
		}
		return false;
	}

	public boolean isEligibleForChampion(UUID playerUUID) {
		List<Progress> progress = getPlayerProgress(playerUUID);

		for (int i = 0; i < 4; i++) {
			if (progress.get(i) == Progress.NONE) {
				return false;
			}
		}
		return true;
	}

	public static LeagueMember getLeagueMemberFromUUID(UUID npcUUID) {
		if (npcUUID.equals(RNBConfig.getSidneySinglesUUID()) || npcUUID.equals(RNBConfig.getSidneyDoublesUUID())) {
			return LeagueMember.SIDNEY;
		} else if (npcUUID.equals(RNBConfig.getPhoebeSinglesUUID()) || npcUUID.equals(RNBConfig.getPhoebeDoublesUUID())) {
			return LeagueMember.PHOEBE;
		} else if (npcUUID.equals(RNBConfig.getGlaciaSinglesUUID()) || npcUUID.equals(RNBConfig.getGlaciaDoublesUUID())) {
			return LeagueMember.GLACIA;
		} else if (npcUUID.equals(RNBConfig.getDrakeSinglesUUID()) || npcUUID.equals(RNBConfig.getDrakeDoublesUUID())) {
			return LeagueMember.DRAKE;
		} else if (npcUUID.equals(RNBConfig.getWallaceUUID())) {
			return LeagueMember.WALLACE;
		}
		return null;
	}

	private static UUID getLeagueMemberTemplateUUID(LeagueMember leagueMember, boolean isDoubles) {
		switch (leagueMember) {
			case SIDNEY:
				return isDoubles ? RNBConfig.getSidneyDoublesUUID() : RNBConfig.getSidneySinglesUUID();
			case PHOEBE:
				return isDoubles ? RNBConfig.getPhoebeDoublesUUID() : RNBConfig.getPhoebeSinglesUUID();
			case GLACIA:
				return isDoubles ? RNBConfig.getGlaciaDoublesUUID() : RNBConfig.getGlaciaSinglesUUID();
			case DRAKE:
				return isDoubles ? RNBConfig.getDrakeDoublesUUID() : RNBConfig.getDrakeSinglesUUID();
			case WALLACE:
				return RNBConfig.getWallaceUUID();
			default:
				return null;
		}
	}

	public boolean hasCompletedLeagueDialogue(UUID playerUUID, LeagueMember leagueMember) {
		return leagueDialogueCompleted.containsKey(playerUUID) && leagueDialogueCompleted.get(playerUUID) == leagueMember;
	}

	public boolean hasCompletedLeagueDialogue(UUID playerUUID, UUID npcUUID) {
		LeagueMember member = getLeagueMemberFromUUID(npcUUID);
		return member != null && hasCompletedLeagueDialogue(playerUUID, member);
	}

	public void markLeagueDialogueCompleted(UUID playerUUID, LeagueMember leagueMember) {
		leagueDialogueCompleted.put(playerUUID, leagueMember);
		setDirty();
	}

	public void markLeagueDialogueCompleted(UUID playerUUID, UUID npcUUID) {
		LeagueMember member = getLeagueMemberFromUUID(npcUUID);
		if (member != null) {
			markLeagueDialogueCompleted(playerUUID, member);
		}
	}

	public boolean isLeagueMember(UUID npcUUID) {
		return getLeagueMemberFromUUID(npcUUID) != null;
	}

	public boolean shouldStartLeagueDialogue(UUID playerUUID, UUID npcUUID) {
		return !hasCompletedLeagueDialogue(playerUUID, npcUUID);
	}

	public void clearCompletedLeagueDialogues(UUID playerUUID) {
		leagueDialogueCompleted.remove(playerUUID);
		setDirty();
	}

	public void resetPlayerLeagueProgress(UUID playerUUID) {
		List<Progress> resetProgress = Arrays.asList(Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE);
		playerProgress.put(playerUUID, resetProgress);

		setDirty();
	}

	public boolean startLeagueDialogue(ServerPlayerEntity player, String dialogueId, NPCTrainer copyTrainer) {
		DialogueNPCManager dialogueManager = DialogueNPCManager.get();
		boolean success = dialogueManager.startDialogue(player, copyTrainer, dialogueId);

		if (!success) {
			player.sendMessage(new StringTextComponent("§cError starting dialogue."), player.getUUID());
			return false;
		}

		DialogueFileData dialogueData = DialogueRegistry.INSTANCE.getDialogue(dialogueId);
		if (dialogueData == null) {
			player.sendMessage(new StringTextComponent("§cDialogue not found: §6" + dialogueId), player.getUUID());
			return false;
		}

		List<DialoguePageData> pages = dialogueData.getPages();
		if (pages == null || pages.isEmpty()) {
			player.sendMessage(new StringTextComponent("§cInvalid dialogue structure for: §6" + dialogueId), player.getUUID());
			return false;
		}

		boolean hasStartPage = pages.stream().anyMatch(page -> "start".equals(page.getId()));
		if (!hasStartPage) {
			player.sendMessage(new StringTextComponent("§cMissing 'start' page in dialogue: §6" + dialogueId), player.getUUID());
			return false;
		}

		List<DialoguePage> dialoguePages = DialogueParser.getInstance().buildDialogueChainFromPage(dialogueData, "start", player);

		if (dialoguePages != null && !dialoguePages.isEmpty()) {
			DialogueManager.INSTANCE.createChainedDialogue(player, dialoguePages);
			return true;
		} else {
			player.sendMessage(new StringTextComponent("§cFailed to build dialogue: §6" + dialogueId), player.getUUID());
			return false;
		}
	}

	private NPCTrainer createLeagueTrainerCopy(NPCTrainer originalTrainer, ServerPlayerEntity player) {
		try {
			NPCTrainer copy = new NPCTrainer(player.level);

			copyOriginalTeam(originalTrainer, copy);

			copy.updateTrainerLevel();
			copy.setBattleAIMode(originalTrainer.getBattleAIMode());
			copy.setNoAi(true);
			copy.setEncounterMode(EnumEncounterMode.Unlimited);

			copy.battleRules = originalTrainer.battleRules.clone();
			List<BattleClause> battleClauses = new ArrayList<>();
			battleClauses.add(BattleClauseRegistry.BAG_CLAUSE);
			battleClauses.add(BattleClauseRegistry.FORFEIT_CLAUSE);
			copy.battleRules.setNewClauses(battleClauses);

			copy.greeting = originalTrainer.greeting;
			copy.winMessage = originalTrainer.winMessage;
			copy.loseMessage = originalTrainer.loseMessage;
			copy.winMoney = originalTrainer.winMoney;
			copy.updateDrops(originalTrainer.getWinnings());
			copy.playerEncounters = originalTrainer.playerEncounters;
			copy.setMegaItem(EnumMegaItemsUnlocked.Mega);
			copy.isGymLeader = originalTrainer.isGymLeader;

			copy.getPersistentData().putUUID("LeagueTrainer", originalTrainer.getUUID());

			CompoundNBT originalData = originalTrainer.getPersistentData();
			if (originalData.contains("trainercommands")) {
				CompoundNBT originalCommands = originalData.getCompound("trainercommands");
				CompoundNBT copiedCommands = originalCommands.copy();
				copy.getPersistentData().put("trainercommands", copiedCommands);
			}

			copy.winCommands = originalTrainer.winCommands;
			copy.loseCommands = originalTrainer.loseCommands;
			copy.forfeitCommands = originalTrainer.forfeitCommands;
			copy.preBattleCommands = originalTrainer.preBattleCommands;
			copy.interactCommands = originalTrainer.interactCommands;

			copy.setName(originalTrainer.getName("en_us"));
			copy.setNickName(originalTrainer.getNickName());
			copy.usingDefaultGreeting = false;
			copy.usingDefaultLose = false;
			copy.usingDefaultWin = false;
			copy.usingDefaultName = true;

			copy.setTextureIndex(originalTrainer.getTextureIndex());
			copy.setCustomSteveTexture(originalTrainer.getCustomSteveTexture());

			copy.setUUID(UUID.randomUUID());

			return copy;
		} catch (Exception e) {
			System.err.println("Failed to create league trainer copy: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static boolean initLeagueBattle(ServerPlayerEntity player, LeagueMember leagueMember, boolean isDoubles) {
		player.closeContainer();

		UUID templateUUID = getLeagueMemberTemplateUUID(leagueMember, isDoubles);
		if (templateUUID == null) {
			System.err.println("No template UUID found for league member: " + leagueMember + " (doubles: " + isDoubles + ")");
			player.sendMessage(new StringTextComponent("§cError: Template trainer not found."), player.getUUID());
			return false;
		}

		initLeagueBattle(player, templateUUID);
		return true;
	}

	private static void initLeagueBattle(ServerPlayerEntity player, UUID templateUUID) {
		Entity trainer = player.getLevel().getEntity(templateUUID);

		if (!(trainer instanceof NPCTrainer)) {
			player.sendMessage(new StringTextComponent("§cError: Trainer not found."), player.getUUID());
			return;
		}

		NPCTrainer templateTrainer = (NPCTrainer) trainer;
		LeagueManager leagueManager = LeagueManager.get((ServerWorld) player.level);

		NPCTrainer copyTrainer = leagueManager.createLeagueTrainerCopy(templateTrainer, player);
		if (copyTrainer == null) {
			player.sendMessage(new StringTextComponent("§cError: Could not create battle trainer."), player.getUUID());
			return;
		}

		int arenaNumber = leagueManager.getAvailableArena(player.getUUID());
		if (arenaNumber == -1) {
			player.sendMessage(new StringTextComponent("§cError: No available arenas."), player.getUUID());
			return;
		}

		TeleportLocation opponentPosition = getArenaOpponentPosition(arenaNumber);
		copyTrainer.setPos(opponentPosition.x, opponentPosition.y, opponentPosition.z);
		copyTrainer.yRot = 90.0f;
		copyTrainer.yBodyRot = 90.0f;
		copyTrainer.yHeadRot = 90.0f;

		player.getLevel().addFreshEntity(copyTrainer);

		LeagueMember leagueMember = getLeagueMemberFromUUID(templateUUID);
		if (leagueMember != null) {
			leagueManager.registerLeagueBattle(player.getUUID(), copyTrainer.getUUID(), templateUUID, leagueMember);
		} else {}

		TeleportLocation playerPosition = getArenaPlayerPosition(arenaNumber);
		player.teleportTo(player.getLevel(), playerPosition.x, playerPosition.y, playerPosition.z, -90.0f, 0.0f);

		setupLeagueBattle(player, copyTrainer, templateUUID, leagueManager, leagueMember);
	}

	public static String getDialogueIdForLeagueMember(LeagueMember leagueMember) {
		if (leagueMember == null) return null;

		switch (leagueMember) {
			case SIDNEY:
				return "sidney_intro";
			case PHOEBE:
				return "phoebe_intro";
			case GLACIA:
				return "glacia_intro";
			case DRAKE:
				return "drake_intro";
			case WALLACE:
				return "wallace_champion_intro";
			default:
				return null;
		}
	}

	private void copyOriginalTeam(NPCTrainer originalTrainer, NPCTrainer copy) {
		for (int i = 0; i < originalTrainer.getPokemonStorage().countAll(); i++) {
			if (originalTrainer.getPokemonStorage().get(i) != null) {
				Pokemon pokemon = PokemonBuilder.copy(Objects.requireNonNull(originalTrainer.getPokemonStorage().get(i))).build();
				copy.getPokemonStorage().set(i, pokemon);
			}
		}
	}

	public UUID getOriginalTrainerUUID(UUID copyTrainerUUID) {
		return copiedToOriginal.get(copyTrainerUUID);
	}

	private void registerLeagueBattle(UUID playerUUID, UUID copyTrainerUUID, UUID originalTrainerUUID, LeagueMember leagueMember) {
		leagueBattleCopies.put(playerUUID, copyTrainerUUID);
		copiedToOriginal.put(copyTrainerUUID, originalTrainerUUID);
		copiedToLeagueMember.put(copyTrainerUUID, leagueMember);
		setDirty();
	}

	public void queueCleanupLeagueBattle(UUID playerUUID) {
		boolean alreadyQueued = cleanupQueue.stream().anyMatch(job -> job.playerUUID.equals(playerUUID));

		if (!alreadyQueued && leagueBattleCopies.containsKey(playerUUID)) {
			cleanupQueue.add(new LeagueBattleCleanupJob(playerUUID, CLEANUP_DELAY_TICKS));
		}
	}

	private void cleanupLeagueBattle(UUID playerUUID) {
		UUID copyTrainerUUID = leagueBattleCopies.remove(playerUUID);
		if (copyTrainerUUID != null) {
			ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);

			NPCTrainer toRemove = null;
			if (player != null) {
				ServerWorld world = (ServerWorld) player.level;
				Entity entity = world.getEntity(copyTrainerUUID);
				if (entity instanceof NPCTrainer) {
					toRemove = (NPCTrainer) entity;
				}
			} else {
				toRemove = findNPCTrainerByUUID(copyTrainerUUID);
			}

			if (toRemove != null) {
				if (toRemove.level instanceof ServerWorld && !toRemove.level.isClientSide) {
					toRemove.teleportTo(toRemove.getX(), toRemove.getY() - 30, toRemove.getZ());
					toRemove.remove();
				}
			}

			copiedToOriginal.remove(copyTrainerUUID);
			copiedToLeagueMember.remove(copyTrainerUUID);

			Integer arenaToRelease = null;
			for (Map.Entry<Integer, UUID> entry : arenasInUse.entrySet()) {
				if (entry.getValue().equals(playerUUID)) {
					arenaToRelease = entry.getKey();
					break;
				}
			}
			if (arenaToRelease != null) {
				releaseArena(arenaToRelease, playerUUID);
			}

			setDirty();
		}
	}

	private NPCTrainer findNPCTrainerByUUID(UUID npcUUID) {
		for (ServerWorld world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
			Entity entity = world.getEntity(npcUUID);
			if (entity instanceof NPCTrainer) {
				return (NPCTrainer) entity;
			}
		}
		return null;
	}

	public void tickCleanupQueue() {
		if (cleanupQueue.isEmpty()) {
			return;
		}

		Iterator<LeagueBattleCleanupJob> iterator = cleanupQueue.iterator();
		while (iterator.hasNext()) {
			LeagueBattleCleanupJob job = iterator.next();
			job.ticksRemaining--;
			if (job.ticksRemaining <= 0) {
				cleanupLeagueBattle(job.playerUUID);
				iterator.remove();
			}
		}
	}

	public boolean isInLeagueBattle(UUID playerUUID) {
		return leagueBattleCopies.containsKey(playerUUID);
	}

	public LeagueMember getLeagueBattleMember(UUID playerUUID) {
		UUID copyTrainerUUID = leagueBattleCopies.get(playerUUID);
		if (copyTrainerUUID != null) {
			return copiedToLeagueMember.get(copyTrainerUUID);
		}
		return null;
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerProgress.clear();
		leagueDialogueCompleted.clear();

		if (nbt.contains("PlayerProgress")) {
			CompoundNBT progressNBT = nbt.getCompound("PlayerProgress");

			for (String playerUUIDString : progressNBT.getAllKeys()) {
				try {
					UUID playerUUID = UUID.fromString(playerUUIDString);
					ListNBT progressList = progressNBT.getList(playerUUIDString, 8);

					List<Progress> progress = Arrays.asList(Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE, Progress.NONE);

					for (int i = 0; i < Math.min(progressList.size(), 5); i++) {
						String progressString = progressList.getString(i);
						try {
							progress.set(i, Progress.valueOf(progressString));
						} catch (IllegalArgumentException e) {
							progress.set(i, Progress.NONE);
						}
					}

					playerProgress.put(playerUUID, progress);
				} catch (IllegalArgumentException e) {}
			}
		}

		if (nbt.contains("LeagueDialogueCompletion")) {
			CompoundNBT leagueNBT = nbt.getCompound("LeagueDialogueCompletion");
			for (String playerUUIDString : leagueNBT.getAllKeys()) {
				try {
					UUID playerUUID = UUID.fromString(playerUUIDString);
					String leagueMemberString = leagueNBT.getString(playerUUIDString);
					try {
						LeagueMember leagueMember = LeagueMember.valueOf(leagueMemberString);
						leagueDialogueCompleted.put(playerUUID, leagueMember);
					} catch (IllegalArgumentException e) {}
				} catch (IllegalArgumentException e) {}
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT progressNBT = new CompoundNBT();

		for (Map.Entry<UUID, List<Progress>> entry : playerProgress.entrySet()) {
			ListNBT progressList = new ListNBT();

			for (Progress progress : entry.getValue()) {
				progressList.add(StringNBT.valueOf(progress.name()));
			}

			progressNBT.put(entry.getKey().toString(), progressList);
		}
		nbt.put("PlayerProgress", progressNBT);

		CompoundNBT leagueNBT = new CompoundNBT();
		for (Map.Entry<UUID, LeagueMember> entry : leagueDialogueCompleted.entrySet()) {
			leagueNBT.putString(entry.getKey().toString(), entry.getValue().name());
		}
		nbt.put("LeagueDialogueCompletion", leagueNBT);

		return nbt;
	}

	public int getAvailableArena(UUID playerUUID) {
		for (Map.Entry<Integer, UUID> entry : arenasInUse.entrySet()) {
			if (entry.getValue().equals(playerUUID)) {
				return entry.getKey();
			}
		}

		for (int arenaNumber : ARENA_PRIORITY) {
			if (!arenasInUse.containsKey(arenaNumber)) {
				if (reserveArena(arenaNumber, playerUUID)) {
					return arenaNumber;
				}
			}
		}

		return -1;
	}

	public boolean reserveArena(int arenaNumber, UUID playerUUID) {
		if (arenaNumber < 1 || arenaNumber > 5) {
			return false;
		}

		if (arenasInUse.containsKey(arenaNumber)) {
			return arenasInUse.get(arenaNumber).equals(playerUUID);
		}

		arenasInUse.put(arenaNumber, playerUUID);
		return true;
	}

	public void releaseArena(int arenaNumber, UUID playerUUID) {
		if (arenasInUse.containsKey(arenaNumber)) {
			UUID currentUser = arenasInUse.get(arenaNumber);
			if (currentUser.equals(playerUUID)) {
				arenasInUse.remove(arenaNumber);
			}
		}
	}

	public void forceReleaseArena(int arenaNumber) {
		arenasInUse.remove(arenaNumber);
	}

	public boolean isArenaInUse(int arenaNumber) {
		return arenasInUse.containsKey(arenaNumber);
	}

	public UUID getArenaUser(int arenaNumber) {
		return arenasInUse.get(arenaNumber);
	}

	public Map<Integer, UUID> getArenasInUse() {
		return new HashMap<>(arenasInUse);
	}

	public static TeleportLocation getArenaPlayerPosition(int arenaNumber) {
		return RNBConfig.getLeaguePlayerPosition(arenaNumber);
	}

	public static TeleportLocation getArenaOpponentPosition(int arenaNumber) {
		return RNBConfig.getLeagueOpponentPosition(arenaNumber);
	}

	public String getArenaStatusString() {
		StringBuilder status = new StringBuilder("Arena Status:\n");
		for (int arenaNumber : ARENA_PRIORITY) {
			status.append("Arena ").append(arenaNumber).append(": ");
			if (arenasInUse.containsKey(arenaNumber)) {
				UUID user = arenasInUse.get(arenaNumber);
				status.append("IN USE by ").append(user.toString());
			} else {
				status.append("AVAILABLE");
			}
			status.append("\n");
		}
		return status.toString();
	}

	public void startLeagueBattle(ServerPlayerEntity player, NPCTrainer trainer) {
		TeamSelectionRegistry.builder().members(new Entity[] { trainer, player }).showOpponentTeam().closeable().battleRules(trainer.battleRules).start();
	}

	private static void setupLeagueBattle(ServerPlayerEntity player, NPCTrainer copyTrainer, UUID templateUUID, LeagueManager leagueManager, LeagueMember leagueMember) {
		startBattleCountdown(player, copyTrainer, leagueMember);
	}

	private static void startBattleCountdown(ServerPlayerEntity player, NPCTrainer copyTrainer, LeagueMember leagueMember) {
		LeagueBattleCountdown.start(player, copyTrainer, leagueMember);
	}
}
