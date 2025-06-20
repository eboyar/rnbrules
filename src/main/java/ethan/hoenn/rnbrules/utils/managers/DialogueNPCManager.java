package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class DialogueNPCManager {

	private static DialogueNPCManager instance;

	private final ConcurrentHashMap<UUID, ActiveDialogueData> activeChattingNPCDialogues = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, ActiveTrainerDialogueData> activeTrainerDialogues = new ConcurrentHashMap<>();

	public static DialogueNPCManager get() {
		if (instance == null) {
			instance = new DialogueNPCManager();
		}
		return instance;
	}

	public boolean startDialogue(ServerPlayerEntity player, NPCChatting npc, String dialogueId) {
		UUID playerUUID = player.getUUID();
		UUID npcUUID = npc.getUUID();
		int npcEntityId = npc.getId();

		activeChattingNPCDialogues.put(playerUUID, new ActiveDialogueData(dialogueId, npcUUID, npcEntityId));

		return true;
	}

	public boolean startDialogue(ServerPlayerEntity player, NPCTrainer trainer, String dialogueId) {
		UUID playerUUID = player.getUUID();
		UUID trainerUUID = trainer.getUUID();
		int trainerEntityId = trainer.getId();

		activeChattingNPCDialogues.put(playerUUID, new ActiveDialogueData(dialogueId, trainerUUID, trainerEntityId));

		return true;
	}

	public boolean completeDialogue(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();

		ActiveDialogueData dialogueData = activeChattingNPCDialogues.get(playerUUID);
		if (dialogueData == null) {
			return false;
		}

		ServerWorld world = (ServerWorld) player.level;
		NPCChatting npc = findNPC(world, dialogueData);

		if (npc != null) {
			markDialogueCompleted(npc, playerUUID);
			activeChattingNPCDialogues.remove(playerUUID);
			return true;
		}

		NPCTrainer trainer = findTrainer(world, new ActiveTrainerDialogueData(dialogueData.getDialogueId(), dialogueData.getNpcUUID(), dialogueData.getNpcEntityId(), null, null));

		if (trainer != null) {
			markTrainerDialogueCompleted(trainer, playerUUID);
			activeChattingNPCDialogues.remove(playerUUID);
			return true;
		}

		activeChattingNPCDialogues.remove(playerUUID);
		return false;
	}

	private NPCChatting findNPC(ServerWorld world, ActiveDialogueData data) {
		if (data.npcUUID != null) {
			if (world.getEntity(data.npcUUID) instanceof NPCChatting) {
				return (NPCChatting) world.getEntity(data.npcUUID);
			}
		}

		if (world.getEntity(data.npcEntityId) instanceof NPCChatting) {
			return (NPCChatting) world.getEntity(data.npcEntityId);
		}

		return null;
	}

	private void markDialogueCompleted(NPCChatting npc, UUID playerUUID) {
		// Get dialogue data from the active dialogue
		ActiveDialogueData dialogueData = activeChattingNPCDialogues.get(playerUUID);
		String dialogueId = dialogueData != null ? dialogueData.dialogueId : null;
		UUID npcUUID = npc.getUUID();

		// Mark the dialogue as completed in the progression manager
		if (dialogueId != null && !dialogueId.isEmpty()) {
			ProgressionManager.get().markDialogueCompleted(playerUUID, npcUUID, dialogueId);
		}
	}

	public boolean hasActiveDialogue(UUID playerUUID) {
		return activeChattingNPCDialogues.containsKey(playerUUID);
	}

	public ActiveDialogueData getActiveDialogue(UUID playerUUID) {
		return activeChattingNPCDialogues.get(playerUUID);
	}

	public boolean addDialogueToNPC(CommandSource source, Entity entity, String dialogueId) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();

		data.putString("DialogueID", dialogueId);

		source.sendSuccess(
			new StringTextComponent(TextFormatting.GREEN + "Added dialogue " + TextFormatting.GOLD + dialogueId + TextFormatting.GREEN + " to entity " + TextFormatting.AQUA + entity.getName().getString()),
			true
		);
		return true;
	}

	public boolean removeDialogueFromNPC(CommandSource source, Entity entity) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();

		if (!data.contains("DialogueID")) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Entity does not have any dialogue assigned."));
			return false;
		}

		String removedId = data.getString("DialogueID");

		data.remove("DialogueID");
		if (data.contains("DialogueCompleted")) {
			data.remove("DialogueCompleted");
		}

		source.sendSuccess(
			new StringTextComponent(
				TextFormatting.GREEN + "Removed dialogue " + TextFormatting.GOLD + removedId + TextFormatting.GREEN + " from entity " + TextFormatting.AQUA + entity.getName().getString()
			),
			true
		);
		return true;
	}

	public boolean clearCompletedDialogues(CommandSource source, Entity entity) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();

		if (!data.contains("DialogueCompleted")) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Entity does not have any completed dialogues."));
			return false;
		}

		ListNBT completedDialogue = data.getList("DialogueCompleted", 10);
		int count = completedDialogue.size();

		data.remove("DialogueCompleted");

		source.sendSuccess(
			new StringTextComponent(
				TextFormatting.GREEN + "Cleared " + TextFormatting.GOLD + count + TextFormatting.GREEN + " dialogue completion records from entity " + TextFormatting.AQUA + entity.getName().getString()
			),
			true
		);
		return true;
	}

	public boolean addMultipleDialoguesToNPC(CommandSource source, Entity entity, String dialogueId, int dialogOrder, String[] battleDeps) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();

		if (!data.contains("DialoguesMap")) {
			data.put("DialoguesMap", new CompoundNBT());
		}
		CompoundNBT dialoguesMap = data.getCompound("DialoguesMap");

		CompoundNBT dialogueEntry = new CompoundNBT();
		dialogueEntry.putString("DialogueID", dialogueId);

		if (battleDeps.length > 0) {
			ListNBT depsList = new ListNBT();
			for (String dep : battleDeps) {
				if (!dep.isEmpty()) {
					depsList.add(StringNBT.valueOf(dep));
				}
			}
			dialogueEntry.put("BattleDeps", depsList);
		}

		dialoguesMap.put(String.valueOf(dialogOrder), dialogueEntry);

		source.sendSuccess(
			new StringTextComponent(
				TextFormatting.GREEN +
				"Added dialogue " +
				TextFormatting.GOLD +
				dialogueId +
				TextFormatting.GREEN +
				" (order: " +
				TextFormatting.AQUA +
				dialogOrder +
				TextFormatting.GREEN +
				") to entity " +
				TextFormatting.AQUA +
				entity.getName().getString()
			),
			true
		);

		if (battleDeps.length > 0) {
			StringBuilder deps = new StringBuilder();
			for (int i = 0; i < battleDeps.length; i++) {
				deps.append(TextFormatting.GOLD).append(battleDeps[i]);
				if (i < battleDeps.length - 1) {
					deps.append(TextFormatting.WHITE).append(", ");
				}
			}

			source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "With battle dependencies: " + deps.toString()), true);
		}

		return true;
	}

	public int listDialoguesForNPC(CommandSource source, Entity entity) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return 0;
		}

		CompoundNBT data = entity.getPersistentData();
		boolean hasAnyDialogue = false;

		if (data.contains("DialogueID")) {
			String dialogueId = data.getString("DialogueID");
			if (!dialogueId.isEmpty()) {
				hasAnyDialogue = true;
				source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Legacy dialog: " + TextFormatting.GOLD + dialogueId), false);

				if (data.contains("BattleDeps") && data.getList("BattleDeps", 8).size() > 0) {
					ListNBT deps = data.getList("BattleDeps", 8);
					StringBuilder depsStr = new StringBuilder();
					for (int i = 0; i < deps.size(); i++) {
						depsStr.append(TextFormatting.GOLD).append(deps.getString(i));
						if (i < deps.size() - 1) {
							depsStr.append(TextFormatting.WHITE).append(", ");
						}
					}

					source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Legacy battle dependencies: " + depsStr.toString()), false);
				}
			}
		}

		if (data.contains("DialoguesMap")) {
			CompoundNBT dialoguesMap = data.getCompound("DialoguesMap");
			if (!dialoguesMap.isEmpty()) {
				hasAnyDialogue = true;
				source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Multiple dialogs setup found:"), false);

				dialoguesMap
					.getAllKeys()
					.stream()
					.sorted((a, b) -> {
						try {
							return Integer.parseInt(a) - Integer.parseInt(b);
						} catch (NumberFormatException e) {
							return a.compareTo(b);
						}
					})
					.forEach(key -> {
						CompoundNBT dialogEntry = dialoguesMap.getCompound(key);
						String dialogId = dialogEntry.getString("DialogueID");

						source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Dialog order " + TextFormatting.AQUA + key + TextFormatting.GREEN + ": " + TextFormatting.GOLD + dialogId), false);

						if (dialogEntry.contains("BattleDeps") && dialogEntry.getList("BattleDeps", 8).size() > 0) {
							ListNBT deps = dialogEntry.getList("BattleDeps", 8);
							StringBuilder depsStr = new StringBuilder();
							for (int i = 0; i < deps.size(); i++) {
								depsStr.append(TextFormatting.GOLD).append(deps.getString(i));
								if (i < deps.size() - 1) {
									depsStr.append(TextFormatting.WHITE).append(", ");
								}
							}

							source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "  Battle dependencies: " + depsStr.toString()), false);
						}
					});
			}
		}

		if (!hasAnyDialogue) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Entity does not have any dialogue assigned."));
			return 0;
		}

		return 1;
	}

	public boolean removeSpecificDialogueFromNPC(CommandSource source, Entity entity, int dialogOrder) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();

		if (!data.contains("DialoguesMap")) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Entity does not have multiple dialogs setup."));
			return false;
		}

		CompoundNBT dialoguesMap = data.getCompound("DialoguesMap");
		String orderKey = String.valueOf(dialogOrder);

		if (!dialoguesMap.contains(orderKey)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "No dialog with order " + TextFormatting.GOLD + dialogOrder + TextFormatting.RED + " found for this entity."));
			return false;
		}

		CompoundNBT dialogEntry = dialoguesMap.getCompound(orderKey);
		String dialogId = dialogEntry.getString("DialogueID");
		dialoguesMap.remove(orderKey);

		if (dialoguesMap.isEmpty()) {
			data.remove("DialoguesMap");
		}

		source.sendSuccess(
			new StringTextComponent(
				TextFormatting.GREEN +
				"Removed dialogue " +
				TextFormatting.GOLD +
				dialogId +
				TextFormatting.GREEN +
				" (order: " +
				TextFormatting.AQUA +
				dialogOrder +
				TextFormatting.GREEN +
				") from entity " +
				TextFormatting.AQUA +
				entity.getName().getString()
			),
			true
		);

		return true;
	}

	public boolean removeAllDialoguesFromNPC(CommandSource source, Entity entity) {
		if (!(entity instanceof NPCChatting || entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target is not a chatting NPC or trainer!"));
			return false;
		}

		CompoundNBT data = entity.getPersistentData();
		boolean hasChanges = false;

		if (data.contains("DialogueID")) {
			data.remove("DialogueID");
			hasChanges = true;
		}

		if (data.contains("DialoguesMap")) {
			data.remove("DialoguesMap");
			hasChanges = true;
		}

		if (!hasChanges) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Entity does not have any dialogue assigned."));
			return false;
		}

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Removed all dialogues from entity " + TextFormatting.AQUA + entity.getName().getString()), true);

		return true;
	}

	public static class ActiveDialogueData {

		private final String dialogueId;
		private final UUID npcUUID;
		private final int npcEntityId;

		public ActiveDialogueData(String dialogueId, UUID npcUUID, int npcEntityId) {
			this.dialogueId = dialogueId;
			this.npcUUID = npcUUID;
			this.npcEntityId = npcEntityId;
		}

		public String getDialogueId() {
			return dialogueId;
		}

		public UUID getNpcUUID() {
			return npcUUID;
		}

		public int getNpcEntityId() {
			return npcEntityId;
		}
	}

	public boolean startTrainerDialogue(ServerPlayerEntity player, NPCTrainer trainer, String dialogueId, TrainerParticipant trainerParticipant, PlayerParticipant playerParticipant) {
		UUID playerUUID = player.getUUID();
		UUID trainerUUID = trainer.getUUID();
		int trainerEntityId = trainer.getId();

		activeTrainerDialogues.put(playerUUID, new ActiveTrainerDialogueData(dialogueId, trainerUUID, trainerEntityId, trainerParticipant, playerParticipant));

		return true;
	}

	public boolean completeTrainerDialogue(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();

		ActiveTrainerDialogueData dialogueData = activeTrainerDialogues.get(playerUUID);
		if (dialogueData == null) {
			return false;
		}

		ServerWorld world = (ServerWorld) player.level;
		NPCTrainer trainer = findTrainer(world, dialogueData);

		if (trainer == null) {
			activeTrainerDialogues.remove(playerUUID);
			return false;
		}

		markTrainerDialogueCompleted(trainer, playerUUID);

		return true;
	}

	public NPCTrainer findTrainer(ServerWorld world, ActiveTrainerDialogueData data) {
		if (data.trainerUUID != null) {
			if (world.getEntity(data.trainerUUID) instanceof NPCTrainer) {
				return (NPCTrainer) world.getEntity(data.trainerUUID);
			}
		}

		if (world.getEntity(data.trainerEntityId) instanceof NPCTrainer) {
			return (NPCTrainer) world.getEntity(data.trainerEntityId);
		}

		return null;
	}

	private void markTrainerDialogueCompleted(NPCTrainer trainer, UUID playerUUID) {
		ActiveTrainerDialogueData dialogueData = activeTrainerDialogues.get(playerUUID);
		String dialogueId = dialogueData != null ? dialogueData.getDialogueId() : null;
		UUID trainerUUID = trainer.getUUID();

		if (dialogueId != null && !dialogueId.isEmpty()) {
			ProgressionManager.get().markDialogueCompleted(playerUUID, trainerUUID, dialogueId);
		}
	}

	public boolean startPendingBattleAfterDialogue(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		ActiveTrainerDialogueData dialogueData = activeTrainerDialogues.get(playerUUID);

		if (dialogueData == null) {
			return false;
		}

		//boolean result = MultiBattleManager.startTrainerBattle(dialogueData.playerParticipant, dialogueData.trainerParticipant);
		TeamSelectionRegistry.builder()
				.members(new Entity[]{dialogueData.getTrainerParticipant().trainer, dialogueData.getPlayerParticipant().player})
				.showRules()
				.showOpponentTeam()
				.closeable()
				.battleRules(dialogueData.getTrainerParticipant().trainer.battleRules)
				.start();

		activeTrainerDialogues.remove(playerUUID);
		return true;
	}

	public boolean hasActiveTrainerDialogue(UUID playerUUID) {
		return activeTrainerDialogues.containsKey(playerUUID);
	}

	public ActiveTrainerDialogueData getActiveTrainerDialogue(UUID playerUUID) {
		return activeTrainerDialogues.get(playerUUID);
	}

	public static class ActiveTrainerDialogueData {

		private final String dialogueId;
		private final UUID trainerUUID;
		private final int trainerEntityId;
		private final TrainerParticipant trainerParticipant;
		private final PlayerParticipant playerParticipant;

		public ActiveTrainerDialogueData(String dialogueId, UUID trainerUUID, int trainerEntityId, TrainerParticipant trainerParticipant, PlayerParticipant playerParticipant) {
			this.dialogueId = dialogueId;
			this.trainerUUID = trainerUUID;
			this.trainerEntityId = trainerEntityId;
			this.trainerParticipant = trainerParticipant;
			this.playerParticipant = playerParticipant;
		}

		public String getDialogueId() {
			return dialogueId;
		}

		public UUID getTrainerUUID() {
			return trainerUUID;
		}

		public int getTrainerEntityId() {
			return trainerEntityId;
		}

		public TrainerParticipant getTrainerParticipant() {
			return trainerParticipant;
		}

		public PlayerParticipant getPlayerParticipant() {
			return playerParticipant;
		}
	}
}
