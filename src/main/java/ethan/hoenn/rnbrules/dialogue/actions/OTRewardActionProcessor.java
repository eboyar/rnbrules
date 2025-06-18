package ethan.hoenn.rnbrules.dialogue.actions;

import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class OTRewardActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "OT_REWARD";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					try {
						UUID playerUUID = player.getUUID();
						DialogueNPCManager dialogueManager = DialogueNPCManager.get();

						if (!dialogueManager.hasActiveDialogue(playerUUID)) {
							player.sendMessage(new StringTextComponent("§cError: No active dialogue found for reward."), playerUUID);
							return;
						}

						DialogueNPCManager.ActiveDialogueData dialogueData = dialogueManager.getActiveDialogue(playerUUID);

						if (dialogueData == null) {
							player.sendMessage(new StringTextComponent("§cError: No active dialogue data found."), playerUUID);
							return;
						}

						ServerWorld world = (ServerWorld) player.level;
						NPCChatting npc = null;

						if (dialogueData.getNpcUUID() != null) {
							if (world.getEntity(dialogueData.getNpcUUID()) instanceof NPCChatting) {
								npc = (NPCChatting) world.getEntity(dialogueData.getNpcUUID());
							}
						}

						if (npc == null) {
							return;
						}

						CompoundNBT data = npc.getPersistentData();
						if (!data.contains("RewardItem")) {
							return;
						}
						
						UUID npcUUID = npc.getUUID();
						
						// Check if the player has already claimed this reward using the ProgressionManager
						if (ProgressionManager.get().hasClaimedReward(playerUUID, npcUUID)) {
							player.sendMessage(new StringTextComponent("§cYou have already received this reward."), playerUUID);
							return;
						}

						CompoundNBT itemNBT = data.getCompound("RewardItem");
						ItemStack reward = ItemStack.of(itemNBT);
						String npcName = npc.getName().getString();
						String itemName = reward.getHoverName().getString();

						if (!player.inventory.add(reward.copy())) {
							player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to give you §6" + itemName + "§7, but your inventory is full!"), playerUUID);
							return;
						}

						// Mark the reward as claimed in the ProgressionManager
						ProgressionManager.get().markRewardClaimed(playerUUID, npcUUID);

						player.sendMessage(new StringTextComponent("§9" + npcName + "§7 has given you §6" + itemName + "§7!"), playerUUID);

						DialogueActionData completeAction = new DialogueActionData();
						completeAction.setType("COMPLETE");
						DialogueActionManager.getInstance().processAction(completeAction, player);
					} catch (Exception e) {
						System.err.println("Error processing OT_REWARD action: " + e.getMessage());
						e.printStackTrace();
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
