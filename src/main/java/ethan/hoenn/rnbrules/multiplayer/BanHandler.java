package ethan.hoenn.rnbrules.multiplayer;

import ethan.hoenn.rnbrules.utils.managers.SettingsManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BanHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		SettingsManager settingsManager = SettingsManager.get();

		if (settingsManager != null && settingsManager.isPlayerTempBanned(player.getUUID())) {
			String reason = settingsManager.getTempBanReason(player.getUUID());
			long expiry = settingsManager.getTempBanExpiry(player.getUUID());
			long timeLeft = expiry - System.currentTimeMillis();

			String timeLeftStr = formatTime(timeLeft);
			player.connection.disconnect(new StringTextComponent(TextFormatting.RED + "You are temporarily banned for " + timeLeftStr + "\\nReason: " + reason));
		}
	}

	private static String formatTime(long milliseconds) {
		if (milliseconds <= 0) {
			return "0 seconds";
		}

		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		StringBuilder result = new StringBuilder();

		if (days > 0) {
			result.append(days).append(" day").append(days > 1 ? "s" : "");
		}
		if (hours % 24 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(hours % 24).append(" hour").append(hours % 24 > 1 ? "s" : "");
		}
		if (minutes % 60 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(minutes % 60).append(" minute").append(minutes % 60 > 1 ? "s" : "");
		}
		if (seconds % 60 > 0 && result.length() == 0) {
			result.append(seconds % 60).append(" second").append(seconds % 60 > 1 ? "s" : "");
		}

		return result.toString();
	}
}
