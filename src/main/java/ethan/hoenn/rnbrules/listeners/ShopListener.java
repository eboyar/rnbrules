package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.ShopkeeperEvent;
import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCShopkeeper;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.managers.BadgeManager;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShopListener {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onInteractWithChattingNPC(NPCEvent.Interact event) {
		if (event.npc instanceof NPCShopkeeper && event.player instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.player;
			NPCShopkeeper shopkeeper = (NPCShopkeeper) event.npc;

			BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);
			if (depManager.shopkeeperHasDependencies(shopkeeper)) {
				Set<String> shopkeeperDeps = depManager.getShopkeeperDependencies(shopkeeper);

				for (String depId : shopkeeperDeps) {
					if (!depManager.playerHasDependency(player.getUUID(), depId)) {
						String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";
						String name = shopkeeper.getName().getString();

						player.sendMessage(new StringTextComponent("§7You must §6" + depDescription + "§7 before you can shop with this §9shopkeeper§7."), player.getUUID());
						event.setCanceled(true);
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemPurchase(ShopkeeperEvent.Purchase event) {
		PlayerEntity player = event.getEntityPlayer();
		BadgeManager badgeManager = BadgeManager.get((ServerWorld) player.level);
		Map<String, Integer> badgeRequirements = RNBConfig.getBadgeRequirements();

		String itemRegistryName = "";
		ItemStack itemS = event.getItem();

		if (itemS.getItem() == PixelmonItems.poke_ball) {
			CompoundNBT itemTag = itemS.getTag();

			if (itemTag != null && itemTag.contains("PokeBallID")) {
				itemRegistryName = "pixelmon:" + itemTag.getString("PokeBallID");
			}
		} else itemRegistryName = itemS.getItem().getRegistryName().toString();

		if (badgeRequirements.containsKey(itemRegistryName)) {
			Integer badgeRequirement = badgeRequirements.get(itemRegistryName);
			int playerBadgeCount = badgeManager.getBadgeCount(player.getUUID());

			if (playerBadgeCount < badgeRequirement) {
				event.setCanceled(true);
				player.sendMessage(new StringTextComponent("§7You need §c" + badgeRequirement + "§7 badges to purchase this item. You have §6" + playerBadgeCount + "§7."), player.getUUID());
			}
		}
	}
}
