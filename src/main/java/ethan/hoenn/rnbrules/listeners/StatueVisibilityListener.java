package ethan.hoenn.rnbrules.listeners;

import ethan.hoenn.rnbrules.utils.managers.StatueVisibilityManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StatueVisibilityListener {

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			StatueVisibilityManager.get(player.getLevel()).syncPlayerOnLogin(player);
		}
	}
}
