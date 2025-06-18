package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import ethan.hoenn.rnbrules.gui.ferry.FerryGui;
import ethan.hoenn.rnbrules.utils.enums.FerryDestination;
import ethan.hoenn.rnbrules.utils.enums.FerryRoute;
import ethan.hoenn.rnbrules.utils.managers.FerryManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FerryListener {

	// Order of checks for chatting NPC: Dialogue / OneTimeRewards, Ferry / Heartscale / Gamecorner
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onInteractWithSailor(NPCEvent.Interact event) {
		if (
			event.npc instanceof NPCChatting &&
			event.player instanceof ServerPlayerEntity &&
			(event.player.getMainHandItem().getItem().equals(PixelmonItems.ss_ticket.getItem()) || event.player.getOffhandItem().getItem().equals(PixelmonItems.ss_ticket.getItem()))
		) {
			NPCChatting npc = (NPCChatting) event.npc;
			CompoundNBT nbtData = npc.getPersistentData();

			if (nbtData.contains("Sailor")) {
				String currentLocation = nbtData.getString("Sailor");

				FerryDestination currentDestination = FerryDestination.fromString(currentLocation);
				if (currentDestination != null) {
					ServerPlayerEntity player = (ServerPlayerEntity) event.player;
					FerryManager ferryManager = FerryManager.get((ServerWorld) player.level);

					if (ferryManager.hasDestination(player.getUUID(), currentDestination.name())) {
						if (FerryManager.canUseFerry(player)) {
							FerryRoute route = currentDestination.getRoute();
							FerryGui.openGui(player, route, currentDestination);
						} else {
							player.sendMessage(new StringTextComponent("You must wait before using the ferry again.").withStyle(TextFormatting.RED), player.getUUID());
						}
					} else {
						player.sendMessage(new StringTextComponent("You don't have access to this ferry route yet.").withStyle(TextFormatting.RED), player.getUUID());
					}
				} else {
					if (event.player.hasPermissions(2)) {
						event.player.sendMessage(new StringTextComponent("This sailor has an invalid destination: " + currentLocation).withStyle(TextFormatting.RED), event.player.getUUID());
					}
				}

				event.setCanceled(true);
			}
		}
	}
}
