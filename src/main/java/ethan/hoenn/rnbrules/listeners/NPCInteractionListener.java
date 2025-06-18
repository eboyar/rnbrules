package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.items.MintItem;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.gui.fossils.FossilGui;
import ethan.hoenn.rnbrules.gui.fossils.underpass.UnderpassGui;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerGui;
import ethan.hoenn.rnbrules.gui.heartscale.HeartscaleExchangeGui;
import ethan.hoenn.rnbrules.gui.intriguingstone.IntriguingStoneExchangeGui;
import ethan.hoenn.rnbrules.gui.itemupgrade.ItemUpgradeGui;
import ethan.hoenn.rnbrules.gui.league.LeagueGui;
import ethan.hoenn.rnbrules.gui.safari.SafariCountdown;
import ethan.hoenn.rnbrules.items.SafariPass;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NPCInteractionListener {

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onInteractWithNPC(NPCEvent.Interact event) {
		if (!(event.npc instanceof NPCChatting) || !(event.player instanceof ServerPlayerEntity) || (event.player.getMainHandItem().getItem().equals(PixelmonItems.trainer_editor))) {
			return;
		}

		NPCChatting npc = (NPCChatting) event.npc;
		CompoundNBT entityData = npc.getPersistentData();
		ServerPlayerEntity player = (ServerPlayerEntity) event.player;

		if (entityData.isEmpty()) {
			return;
		}

		if (entityData.contains("HeartscaleExchange") && entityData.getBoolean("HeartscaleExchange")) {
			if ((player.getMainHandItem().getItem().equals(PixelmonItems.heart_scale.getItem()) || player.getOffhandItem().getItem().equals(PixelmonItems.heart_scale.getItem()))) {
				event.setCanceled(true);
				HeartscaleExchangeGui.openGui(player);
				return;
			} else if (
				player.getMainHandItem().getItem().equals(PixelmonItems.silver_bottle_cap) ||
				player.getMainHandItem().getItem().equals(PixelmonItems.gold_bottle_cap) ||
				(player.getMainHandItem().getItem() instanceof MintItem)
			) {
				event.setCanceled(true);

				int heartScalesToGive = 1;
				if (player.getMainHandItem().getItem() instanceof MintItem) {
					heartScalesToGive = 3;
				} else if (player.getMainHandItem().getItem().equals(PixelmonItems.gold_bottle_cap)) {
					heartScalesToGive = 5;
				}

				ItemStack heartScales = new ItemStack(PixelmonItems.heart_scale, heartScalesToGive);

				if (!playerHasInventorySpace(player, heartScales)) {
					player.displayClientMessage(new StringTextComponent("§cYour inventory is full!"), true);
					return;
				}

				player.getMainHandItem().shrink(1);

				player.inventory.add(heartScales);
				player.inventory.setChanged();
				player.containerMenu.broadcastChanges();

				player.displayClientMessage(new StringTextComponent("§aTraded for " + heartScalesToGive + " Heart Scale(s)!"), true);
				return;
			}
		}

		if (entityData.contains("Widget") && entityData.getBoolean("Widget")) {
			event.setCanceled(true);
			ItemUpgradeGui.openGui(player);
		}

		if (entityData.contains("IntriguingStone") && entityData.getBoolean("IntriguingStone")) {
			event.setCanceled(true);
			IntriguingStoneExchangeGui.openGui(player);
		}

		if (entityData.contains("Gamecorner") && entityData.getBoolean("Gamecorner")) {
			event.setCanceled(true);
			GamecornerGui.openGui(player);
		}

		if (entityData.contains("Fossil") && entityData.getBoolean("Fossil")) {
			event.setCanceled(true);
			FossilGui.openGui(player);
		}

		if (entityData.contains("League") && entityData.getBoolean("League")) {
			event.setCanceled(true);
			LeagueGui.openGui(player);
		}

		if (entityData.contains("Underpass") && entityData.getBoolean("Underpass")) {
			GlobalOTManager globalot = GlobalOTManager.get(player.getLevel());
			if (!globalot.playerHasGlobalOT(player.getUUID(), "underpass")) {
				event.setCanceled(true);
				UnderpassGui.openGui(player);
			}
		}

		if (entityData.contains("SafariZone") && entityData.getBoolean("SafariZone") && player.getMainHandItem().getItem() instanceof SafariPass) {
			event.setCanceled(true);
			RNBConfig.TeleportLocation entryPoint = RNBConfig.getSafariEntryPoint();
			if (entryPoint == null) {
				player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Safari Zone entry point has not been set!"), true);
				return;
			}
			player.getMainHandItem().shrink(1);
			SafariCountdown.startEntering(player, entryPoint, 5);
		}

		if (entityData.contains("Bikeshop") && entityData.getBoolean("Bikeshop")) {
			ItemStack heldItem = player.getMainHandItem();
			Item held = heldItem.getItem();

			if (held.equals(PixelmonItems.bike_voucher)) {
				if (player.inventory.getFreeSlot() != -1) {
					ItemStack bike = new ItemStack(PixelmonItems.acro_bike);
					player.inventory.add(bike);
					heldItem.shrink(1);
					player.sendMessage(bikeMessage("You received an ", "Acro Bike", "!"), player.getUUID());
					event.setCanceled(true);
				} else {
					player.sendMessage(new StringTextComponent("You don't have space in your inventory.").withStyle(TextFormatting.GRAY), player.getUUID());
				}
			} else if (held.equals(PixelmonItems.acro_bike)) {
				ItemStack machBike = new ItemStack(PixelmonItems.mach_bike);
				player.setItemInHand(Hand.MAIN_HAND, machBike);
				player.sendMessage(bikeMessage("You exchanged your ", "Acro Bike", " for a ", "Mach Bike", "!"), player.getUUID());
				event.setCanceled(true);
			} else if (held.equals(PixelmonItems.mach_bike)) {
				ItemStack acroBike = new ItemStack(PixelmonItems.acro_bike);
				player.setItemInHand(Hand.MAIN_HAND, acroBike);
				player.sendMessage(bikeMessage("You exchanged your ", "Mach Bike", " for an ", "Acro Bike", "!"), player.getUUID());
				event.setCanceled(true);
			}
		}
	}

	private boolean playerHasInventorySpace(ServerPlayerEntity player, ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}

		ItemStack itemToCheck = stack.copy();
		for (int i = 0; i < 36; i++) {
			ItemStack existingStack = player.inventory.getItem(i);
			if (existingStack.isEmpty()) {
				return true;
			}
			if (existingStack.isStackable() && ItemStack.isSame(existingStack, itemToCheck) && ItemStack.tagMatches(existingStack, itemToCheck)) {
				int remainingSpace = existingStack.getMaxStackSize() - existingStack.getCount();
				if (remainingSpace >= itemToCheck.getCount()) {
					return true;
				}
				itemToCheck.shrink(remainingSpace);
				if (itemToCheck.isEmpty()) {
					return true;
				}
			}
		}
		return itemToCheck.isEmpty();
	}

	private static ITextComponent bikeMessage(String... parts) {
		StringTextComponent message = new StringTextComponent("");
		boolean gold = false;
		for (String part : parts) {
			TextFormatting color = gold ? TextFormatting.GOLD : TextFormatting.GRAY;
			message.append(new StringTextComponent(part).withStyle(color));
			gold = !gold;
		}
		return message;
	}
}
