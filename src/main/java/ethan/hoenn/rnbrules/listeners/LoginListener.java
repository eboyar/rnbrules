package ethan.hoenn.rnbrules.listeners;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LoginListener {

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		UUID playerUUID = player.getUUID();
		String playerName = player.getName().getString();

		ProgressionManager pm = ProgressionManager.get();
		if (pm == null) {
			return;
		}

		if (pm.isPlayerFirstJoin(playerUUID)) {
			executeFirstJoinCommands(player, playerName);
			pm.markPlayerAsJoined(playerUUID);
			//player.sendMessage(new StringTextComponent("§aWelcome to Pixelmon Run n' Bun!"), playerUUID);
		}
	}

	private static void executeFirstJoinCommands(ServerPlayerEntity player, String playerName) {
		List<String> commands = RNBConfig.getFirstJoinCommands();
		MinecraftServer server = player.getServer();

		if (server == null || commands.isEmpty()) {
			return;
		}

		for (String command : commands) {
			try {
				String processedCommand = command.replace("@pl", playerName);
				server.getCommands().performCommand(server.createCommandSourceStack(), processedCommand);
			} catch (Exception e) {
				System.err.println("Failed to execute first join command: " + command);
				e.printStackTrace();
			}
		}
	}
}
