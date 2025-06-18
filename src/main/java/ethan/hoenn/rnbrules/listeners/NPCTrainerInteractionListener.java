package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.dialogue.DialogueManager;
import ethan.hoenn.rnbrules.dialogue.DialoguePage;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.dialogue.yaml.DialogueParser;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialoguePageData;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NPCTrainerInteractionListener {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getWorld().isClientSide() || event.getHand() != Hand.MAIN_HAND) return;

		Entity target = event.getTarget();

		if (target instanceof NPCTrainer && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			NPCTrainer trainer = (NPCTrainer) target;
			player.connection.send(new SAnimateHandPacket(player, 0));

			if (player.getMainHandItem().getItem().equals(PixelmonItems.trainer_editor)) {
				return;
			}

			if (!(target.getPersistentData().contains("IsPlayerPartner") && target.getPersistentData().getBoolean("IsPlayerPartner"))) {
				return;
			}

			BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);
			if (depManager.trainerHasDependencies(trainer)) {
				Set<String> trainerDeps = depManager.getTrainerDependencies(trainer);
				boolean missingDependency = false;

				for (String depId : trainerDeps) {
					if (!depManager.playerHasDependency(player.getUUID(), depId)) {
						missingDependency = true;
						String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

						player.sendMessage(new StringTextComponent("§9" + trainer.getName().getString() + "§7 wants to talk to you, but you must §6" + depDescription + "§7 first."), player.getUUID());

						event.setCanceled(true);
						break;
					}
				}

				if (missingDependency) {
					return;
				}
			}

			CompoundNBT data = trainer.getPersistentData();

			if (!data.contains("DialogueID")) {
				return;
			}

			String dialogueId = data.getString("DialogueID");
			if (dialogueId.isEmpty()) {
				return;
			}

			ListNBT completedDialogue = data.contains("DialogueCompleted") ? data.getList("DialogueCompleted", 10) : new ListNBT();
			UUID playerUUID = player.getUUID();

			for (int i = 0; i < completedDialogue.size(); i++) {
				CompoundNBT tag = completedDialogue.getCompound(i);
				if (tag.hasUUID("UUID") && tag.getUUID("UUID").equals(playerUUID)) {
					return;
				}
			}
			event.setCanceled(true);
			startDialogue(player, trainer, dialogueId);
		}
	}

	private static void startDialogue(ServerPlayerEntity player, NPCTrainer trainer, String dialogueId) {
		DialogueNPCManager dialogueManager = DialogueNPCManager.get();
		boolean success = dialogueManager.startDialogue(player, trainer, dialogueId);

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
}
