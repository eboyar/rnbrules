package ethan.hoenn.rnbrules.listeners;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.gui.safari.SafariCountdown;
import ethan.hoenn.rnbrules.utils.managers.SafariManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SafariListener {

	private static final Map<UUID, double[]> lastPositions = new HashMap<>();

	private static final double MIN_STEP_DISTANCE_SQUARED = 1.0;

	private static int tickCounter = 0;

	@SubscribeEvent
	public static void onLivingUpdate(LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide || !(event.getEntity() instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
		ServerWorld world = (ServerWorld) player.level;
		UUID playerUUID = player.getUUID();

		SafariManager safariManager = SafariManager.get(world);
		if (!safariManager.isPlayerInSafari(playerUUID)) {
			lastPositions.remove(playerUUID);
			return;
		}

		double[] currentPos = { player.getX(), player.getY(), player.getZ() };

		double[] lastPos = lastPositions.get(playerUUID);
		if (lastPos != null) {
			double dx = currentPos[0] - lastPos[0];
			double dz = currentPos[2] - lastPos[2];
			double distSquared = dx * dx + dz * dz;

			if (distSquared >= MIN_STEP_DISTANCE_SQUARED) {
				SafariManager.SafariPlayerData playerData = safariManager.getPlayerData(playerUUID);
				playerData.incrementSteps();
				safariManager.updateScoreboard(player);

				if (playerData.getStepsTaken() >= SafariManager.MAX_STEPS) {
					player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "You've run out of steps in the Safari Zone!"), playerUUID);

					RNBConfig.TeleportLocation exitPoint = RNBConfig.getSafariExitPoint();

					if (exitPoint != null) {
						SafariCountdown.startExiting(player, exitPoint);
					} else {
						safariManager.endSafari(playerUUID);
						safariManager.disableScoreboard(player);
					}
				}

				lastPositions.put(playerUUID, currentPos);
			}
		} else {
			lastPositions.put(playerUUID, currentPos);
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
			return;
		}
		tickCounter++;
		if (tickCounter % 20 == 0) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null) {
				ServerWorld world = ServerLifecycleHooks.getCurrentServer().overworld();
				SafariManager safariManager = SafariManager.get(world);

				for (UUID playerUUID : safariManager.getActiveSafariPlayers()) {
					ServerPlayerEntity player = server.getPlayerList().getPlayer(playerUUID);

					if (player == null) {
						continue;
					}

					SafariManager.SafariPlayerData playerData = safariManager.getPlayerData(playerUUID);
					playerData.incrementTimePlayed();
					safariManager.updateScoreboard(player);
				}
			}
		}
	}
}
