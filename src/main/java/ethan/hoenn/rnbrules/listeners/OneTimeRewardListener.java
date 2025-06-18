package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OneTimeRewardListener {

	// Order of checks for chatting NPC: Dialogue / OneTimeRewards, Ferry / Heartscale / Gamecorner
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onInteractWithChattingNPC(NPCEvent.Interact event) {
		if (event.npc instanceof NPCChatting && event.player instanceof ServerPlayerEntity && !(event.player.getMainHandItem().getItem().equals(PixelmonItems.trainer_editor))) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.player;
			CompoundNBT data = event.npc.getPersistentData();
			NPCChatting npc = (NPCChatting) event.npc;

			if (!data.contains("OneTimeReward") || !data.contains("RewardItem")) return;

			UUID playerUUID = player.getUUID();
			UUID npcUUID = npc.getUUID();
			
			// Check if player has already claimed this reward using the ProgressionManager
			if (ProgressionManager.get().hasClaimedReward(playerUUID, npcUUID)) {
				return;
			}

			CompoundNBT itemNBT = data.getCompound("RewardItem");
			ItemStack reward = ItemStack.of(itemNBT);
			String npcName = npc.getName("en_us");
			String itemName = reward.getHoverName().getString();

			if (!player.inventory.add(reward.copy())) {
				player.sendMessage(new StringTextComponent("§9" + npcName + "§7 wants to give you §6" + itemName + "§7, but your inventory is full!"), player.getUUID());
				event.setCanceled(true);
				return;
			}

			// Mark reward as claimed in the ProgressionManager
			ProgressionManager.get().markRewardClaimed(playerUUID, npcUUID);

			event.setCanceled(true);
			player.sendMessage(new StringTextComponent("§9" + npcName + "§7 has given you §6" + itemName + "§7!"), player.getUUID());
		}
	}
}
