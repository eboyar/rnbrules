package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.dialogue.DialogueManager;
import ethan.hoenn.rnbrules.dialogue.DialoguePage;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.dialogue.yaml.DialogueParser;
import ethan.hoenn.rnbrules.network.CancelTeamSelectionPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialoguePageData;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DialogueNPCListener {

	// Order of checks for chatting NPC: BattleDeps, Dialogue, OneTimeRewards, Ferry / Heartscale / Gamecorner
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onInteractWithChattingNPC(NPCEvent.Interact event) {
		if (event.npc instanceof NPCChatting && event.player instanceof ServerPlayerEntity && !(event.player.getMainHandItem().getItem().equals(PixelmonItems.trainer_editor))) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.player;
			CompoundNBT data = event.npc.getPersistentData();
			NPCChatting npc = (NPCChatting) event.npc;
			BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);
			UUID playerUUID = player.getUUID();

			if (data.contains("DialoguesMap")) {
				CompoundNBT dialoguesMap = data.getCompound("DialoguesMap");
				if (!dialoguesMap.isEmpty()) {
					DialogResult result = findApplicableDialog(dialoguesMap, data, player, playerUUID, depManager, npc.getName("en_us"), true);

					if (result.shouldCancel) {
						event.setCanceled(true);
						return;
					}

					if (result.dialogId != null) {
						event.setCanceled(true);
						startDialogue(player, npc, result.dialogId);
						return;
					}
				}
			}

			if (!data.contains("DialogueID")) {
				return;
			}

			String dialogueId = data.getString("DialogueID");
			if (dialogueId.isEmpty()) {
				return;
			}

			ListNBT completedDialogue = data.contains("DialogueCompleted") ? data.getList("DialogueCompleted", 10) : new ListNBT();
			for (int i = 0; i < completedDialogue.size(); i++) {
				CompoundNBT tag = completedDialogue.getCompound(i);
				if (tag.hasUUID("UUID") && tag.getUUID("UUID").equals(playerUUID)) {
					return;
				}
			}

			event.setCanceled(true);
			startDialogue(player, npc, dialogueId);
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onBattleStart(BattleStartedEvent.Pre event) {
		List<BattleParticipant> bps = event.getBattleController().participants;

		PlayerParticipant pp = null;
		TrainerParticipant tp = null;

		for (BattleParticipant bp : bps) {
			if (bp instanceof TrainerParticipant) {
				tp = (TrainerParticipant) bp;
			} else if (bp instanceof PlayerParticipant) {
				pp = (PlayerParticipant) bp;
			}
		}

		if (tp == null || pp == null) {
			return;
		}

		ServerPlayerEntity player = pp.player;
		NPCTrainer trainer = tp.trainer;
		CompoundNBT data = trainer.getPersistentData();

		data.putBoolean("IsTrainer", true);

		UUID playerUUID = player.getUUID();
		BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);

		if (data.contains("DialoguesMap")) {
			CompoundNBT dialoguesMap = data.getCompound("DialoguesMap");
			if (!dialoguesMap.isEmpty()) {
				DialogResult result = findApplicableDialog(dialoguesMap, data, player, playerUUID, depManager, trainer.getName("en_us"), true);

				if (result.shouldCancel) {
					event.setCanceled(true);
					return;
				}

				if (result.dialogId != null) {
					event.setCanceled(true);
					startTrainerDialogue(player, trainer, result.dialogId, tp, pp);
					return;
				}
			}
		}

		if (!data.contains("DialogueID")) {
			return;
		}

		String dialogueId = data.getString("DialogueID");
		if (dialogueId.isEmpty()) {
			return;
		}

		ListNBT completedDialogue = data.contains("DialogueCompleted") ? data.getList("DialogueCompleted", 10) : new ListNBT();

		for (int i = 0; i < completedDialogue.size(); i++) {
			CompoundNBT tag = completedDialogue.getCompound(i);
			if (tag.hasUUID("UUID") && tag.getUUID("UUID").equals(playerUUID)) {
				return;
			}
		}

		if (depManager.trainerHasDependencies(trainer)) {
			Set<String> npcDeps = depManager.getTrainerDependencies(trainer);
			boolean missingDependency = false;

			for (String depId : npcDeps) {
				if (!depManager.playerHasDependency(player.getUUID(), depId)) {
					missingDependency = true;
					String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

					player.sendMessage(new StringTextComponent("§9" + trainer.getName("en_us") + "§7 wants to battle you, but you must §6" + depDescription + "§7 first."), player.getUUID());

					event.setCanceled(true);
					break;
				}
			}

			if (missingDependency) {
				return;
			}
		}

		event.setCanceled(true);
		startTrainerDialogue(player, trainer, dialogueId, tp, pp);
	}

	private void startTrainerDialogue(ServerPlayerEntity player, NPCTrainer trainer, String dialogueId, TrainerParticipant tp, PlayerParticipant pp) {
		DialogueNPCManager dialogueManager = DialogueNPCManager.get();
		boolean success = dialogueManager.startTrainerDialogue(player, trainer, dialogueId, tp, pp);

		if (!success) {
			player.sendMessage(new StringTextComponent("§cError starting dialogue."), player.getUUID());
			return;
		}

		DialogueFileData dialogueData = DialogueRegistry.INSTANCE.getDialogue(dialogueId);
		if (dialogueData == null) {
			player.sendMessage(new StringTextComponent("§cDialogue not found: §6" + dialogueId), player.getUUID());
			return;
		}

		List<DialoguePageData> pages = dialogueData.getPages();
		if (pages == null || pages.isEmpty()) {
			player.sendMessage(new StringTextComponent("§cInvalid dialogue structure for: §6" + dialogueId), player.getUUID());
			return;
		}

		boolean hasStartPage = pages.stream().anyMatch(page -> "start".equals(page.getId()));
		if (!hasStartPage) {
			player.sendMessage(new StringTextComponent("§cMissing 'start' page in dialogue: §6" + dialogueId), player.getUUID());
			return;
		}

		List<DialoguePage> dialoguePages = DialogueParser.getInstance().buildDialogueChainFromPage(dialogueData, "start", player);

		if (dialoguePages != null && !dialoguePages.isEmpty()) {
			DialogueManager.INSTANCE.createChainedDialogue(player, dialoguePages);
		} else {
			player.sendMessage(new StringTextComponent("§cFailed to build dialogue: §6" + dialogueId), player.getUUID());
		}
	}

	private void startDialogue(ServerPlayerEntity player, NPCChatting npc, String dialogueId) {
		DialogueNPCManager dialogueManager = DialogueNPCManager.get();
		boolean success = dialogueManager.startDialogue(player, npc, dialogueId);

		if (!success) {
			player.sendMessage(new StringTextComponent("§cError starting dialogue."), player.getUUID());
			return;
		}

		DialogueFileData dialogueData = DialogueRegistry.INSTANCE.getDialogue(dialogueId);
		if (dialogueData == null) {
			player.sendMessage(new StringTextComponent("§cDialogue not found: §6" + dialogueId), player.getUUID());
			return;
		}

		List<DialoguePageData> pages = dialogueData.getPages();
		if (pages == null || pages.isEmpty()) {
			player.sendMessage(new StringTextComponent("§cInvalid dialogue structure for: §6" + dialogueId), player.getUUID());
			return;
		}

		boolean hasStartPage = pages.stream().anyMatch(page -> "start".equals(page.getId()));
		if (!hasStartPage) {
			player.sendMessage(new StringTextComponent("§cMissing 'start' page in dialogue: §6" + dialogueId), player.getUUID());
			return;
		}

		List<DialoguePage> dialoguePages = DialogueParser.getInstance().buildDialogueChainFromPage(dialogueData, "start", player);

		if (dialoguePages != null && !dialoguePages.isEmpty()) {
			DialogueManager.INSTANCE.createChainedDialogue(player, dialoguePages);
		} else {
			player.sendMessage(new StringTextComponent("§cFailed to build dialogue: §6" + dialogueId), player.getUUID());
		}
	}

	private DialogResult findApplicableDialog(
		CompoundNBT dialoguesMap,
		CompoundNBT data,
		ServerPlayerEntity player,
		UUID playerUUID,
		BattleDependencyManager depManager,
		String npcName,
		boolean showMessages
	) {
		List<Integer> dialogOrders = new ArrayList<>();
		for (String key : dialoguesMap.getAllKeys()) {
			try {
				dialogOrders.add(Integer.parseInt(key));
			} catch (NumberFormatException e) {
				continue;
			}
		}
		Collections.sort(dialogOrders);

		Set<String> completedDialogs = new HashSet<>();
		if (data.contains("DialogueCompleted")) {
			ListNBT completedList = data.getList("DialogueCompleted", 10);
			for (int i = 0; i < completedList.size(); i++) {
				CompoundNBT tag = completedList.getCompound(i);
				if (tag.hasUUID("UUID") && tag.getUUID("UUID").equals(playerUUID) && tag.contains("DialogueID")) {
					completedDialogs.add(tag.getString("DialogueID"));
				}
			}
		}

		String firstBlockedDialogId = null;
		String firstMissingDepId = null;
		boolean hasOneTimeReward = data.contains("OneTimeReward");

		for (Integer order : dialogOrders) {
			String orderKey = String.valueOf(order);
			CompoundNBT dialogEntry = dialoguesMap.getCompound(orderKey);
			String dialogID = dialogEntry.getString("DialogueID");

			if (dialogID.isEmpty()) {
				continue;
			}

			if (completedDialogs.contains(dialogID)) {
				continue;
			}

			if (dialogEntry.contains("BattleDeps")) {
				ListNBT depsList = dialogEntry.getList("BattleDeps", 8);
				boolean allDependenciesMet = true;
				String missingDepId = null;

				for (int i = 0; i < depsList.size(); i++) {
					String depId = depsList.getString(i);
					if (!depManager.playerHasDependency(playerUUID, depId)) {
						allDependenciesMet = false;
						missingDepId = depId;
						break;
					}
				}

				if (!allDependenciesMet) {
					if (firstBlockedDialogId == null) {
						firstBlockedDialogId = dialogID;
						firstMissingDepId = missingDepId;
					}

					if (order == dialogOrders.get(0) && showMessages) {
						String depDescription = depManager.getDependency(missingDepId) != null ? depManager.getDependency(missingDepId).getDescription() : "Unknown requirement";

						if (hasOneTimeReward) {
							player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to give you something, but you must §6" + depDescription + "§7 first."), player.getUUID());
						} else if (data.contains("IsTrainer") && data.getBoolean("IsTrainer")) {
							player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to battle you, but you must §6" + depDescription + "§7 first."), player.getUUID());
						} else {
							player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to talk to you, but you must §6" + depDescription + "§7 first."), player.getUUID());
						}

						return new DialogResult(null, true);
					}

					continue;
				}
			}

			return new DialogResult(dialogID, false);
		}

		if (firstBlockedDialogId != null && firstMissingDepId != null && showMessages) {
			String depDescription = depManager.getDependency(firstMissingDepId) != null ? depManager.getDependency(firstMissingDepId).getDescription() : "Unknown requirement";

			if (hasOneTimeReward) {
				player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to give you something, but you must §6" + depDescription + "§7 first."), player.getUUID());
			} else if (data.contains("IsTrainer") && data.getBoolean("IsTrainer")) {
				player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to battle you, but you must §6" + depDescription + "§7 first."), player.getUUID());
			} else {
				player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to talk to you, but you must §6" + depDescription + "§7 first."), player.getUUID());
			}

			return new DialogResult(null, true);
		}

		return new DialogResult(null, false);
	}

	private class DialogResult {

		public final String dialogId;
		public final boolean shouldCancel;

		public DialogResult(String dialogId, boolean shouldCancel) {
			this.dialogId = dialogId;
			this.shouldCancel = shouldCancel;
		}
	}
}
