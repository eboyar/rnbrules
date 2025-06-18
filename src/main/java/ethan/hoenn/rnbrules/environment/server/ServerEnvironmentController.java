package ethan.hoenn.rnbrules.environment.server;

import ethan.hoenn.rnbrules.network.EnvironmentPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEnvironmentController {

	private static final ServerEnvironmentController INSTANCE = new ServerEnvironmentController();

	private final Map<UUID, Environment> playerEnvironments;

	private boolean autoDetectionEnabled;

	private static final int AUTO_DETECTION_FREQUENCY = 20;

	private ServerEnvironmentController() {
		playerEnvironments = new HashMap<>();
		autoDetectionEnabled = true;
	}

	public static ServerEnvironmentController getInstance() {
		return INSTANCE;
	}

	public void setPlayerEnvironment(ServerPlayerEntity player, Environment environment) {
		if (player == null) return;

		UUID playerId = player.getUUID();
		Environment currentEnv = playerEnvironments.getOrDefault(playerId, Environment.NONE);

		if (!environment.equals(currentEnv)) {
			playerEnvironments.put(playerId, environment);

			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new EnvironmentPacket(environment.getId()));

			ServerEnvironmentTransitionManager.startTransition(player, environment);
		}
	}

	public Environment getPlayerEnvironment(ServerPlayerEntity player) {
		if (player == null) return Environment.NONE;
		return playerEnvironments.getOrDefault(player.getUUID(), Environment.NONE);
	}

	public void setAutoDetectionEnabled(boolean enabled) {
		this.autoDetectionEnabled = enabled;
	}

	public boolean isAutoDetectionEnabled() {
		return autoDetectionEnabled;
	}

	public void detectPlayerEnvironment(ServerPlayerEntity player) {
		if (player == null || player.level == null) return;

		UUID playerUUID = player.getUUID();
		Environment currentPlayerEnv = getPlayerEnvironment(player);

		// Check location-based environments
		if (player.level instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) player.level;
			LocationManager locationManager = LocationManager.get(serverWorld);
			String currentLocation = locationManager.getPlayerCurrentLocation(playerUUID);

			if (currentLocation != null) {
				String normalizedLocation = LocationManager.normalizeLocationName(currentLocation);
				Environment locationEnv = locationManager.getLocationEnvironment(normalizedLocation);

				// Case 1: Location has a defined environment
				if (locationEnv != null && locationEnv != currentPlayerEnv) {
					setPlayerEnvironment(player, locationEnv);
					return;
				}

				// Case 2: Location has no defined environment but player has an active environment
				// This happens when moving from a location with environment to one without
				if (locationEnv == null && currentPlayerEnv != Environment.NONE) {
					// Reset to NONE when leaving a special environment area
					setPlayerEnvironment(player, Environment.NONE);
					return;
				}
			}
		}
		// No world-based environment checks (rain, thunder, etc.)
		// All environments are now set explicitly via location or commands
	}

	private static int autoDetectionCounter = 0;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
			autoDetectionCounter++;
			if (autoDetectionCounter >= AUTO_DETECTION_FREQUENCY) {
				autoDetectionCounter = 0;

				if (ServerLifecycleHooks.getCurrentServer() != null && INSTANCE.isAutoDetectionEnabled()) {
					for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						INSTANCE.detectPlayerEnvironment(player);
					}
				}
			}
		}
	}
}
