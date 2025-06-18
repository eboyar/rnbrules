package ethan.hoenn.rnbrules.multiplayer;

import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerListHandler {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			updatePlayerTabListName(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			updatePlayerTabListName(player);
		}
	}

	@SubscribeEvent
	public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			ProgressionManager progressionManager = ProgressionManager.get();

			if (progressionManager != null) {
				Rank playerRank = progressionManager.getPlayerRankObject(player.getUUID());
				StaffRank staffRank = progressionManager.getPlayerStaffRankObject(player.getUUID());

				String formattedPrefix = playerRank.getFormattedPrefix();
				String convertedPrefix = convertColorCodes(formattedPrefix);

				String playerNameColor = "&f";
				if (staffRank != null) {
					playerNameColor = staffRank.getNameColor();
				}
				String convertedNameColor = convertColorCodes(playerNameColor);

				String tabListName = convertedPrefix + " " + convertedNameColor + player.getDisplayName().getString();
				event.setDisplayName(new StringTextComponent(tabListName));
			}
		}
	}

	public static void updatePlayerTabListName(ServerPlayerEntity player) {
		player.refreshTabListName();
	}

	public static void updatePlayerRankDisplay(ServerPlayerEntity player) {
		updatePlayerTabListName(player);
	}

	private static String convertColorCodes(String text) {
		return text
			.replace("&0", TextFormatting.BLACK.toString())
			.replace("&1", TextFormatting.DARK_BLUE.toString())
			.replace("&2", TextFormatting.DARK_GREEN.toString())
			.replace("&3", TextFormatting.DARK_AQUA.toString())
			.replace("&4", TextFormatting.DARK_RED.toString())
			.replace("&5", TextFormatting.DARK_PURPLE.toString())
			.replace("&6", TextFormatting.GOLD.toString())
			.replace("&7", TextFormatting.GRAY.toString())
			.replace("&8", TextFormatting.DARK_GRAY.toString())
			.replace("&9", TextFormatting.BLUE.toString())
			.replace("&a", TextFormatting.GREEN.toString())
			.replace("&b", TextFormatting.AQUA.toString())
			.replace("&c", TextFormatting.RED.toString())
			.replace("&d", TextFormatting.LIGHT_PURPLE.toString())
			.replace("&e", TextFormatting.YELLOW.toString())
			.replace("&f", TextFormatting.WHITE.toString())
			.replace("&l", TextFormatting.BOLD.toString())
			.replace("&m", TextFormatting.STRIKETHROUGH.toString())
			.replace("&n", TextFormatting.UNDERLINE.toString())
			.replace("&o", TextFormatting.ITALIC.toString())
			.replace("&r", TextFormatting.RESET.toString());
	}
}
