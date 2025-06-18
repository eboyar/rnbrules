package ethan.hoenn.rnbrules.environment.server;

import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEnvironmentTransitionManager {

	private static final Map<UUID, TransitionData> playerTransitions = new HashMap<>();
	private static final Map<UUID, Environment> lastSentEnvironment = new HashMap<>();

	private static final float INTENSITY_INCREMENT = 0.1F;

	private static final int UPDATE_INTERVAL = 5;

	private static int tickCounter = 0;

	public static void startTransition(ServerPlayerEntity player, Environment newFinalTargetEnvironment) {
		if (player == null) return;

		UUID playerUUID = player.getUUID();
		TransitionData existingTransition = playerTransitions.get(playerUUID);
		Environment lastEnv = lastSentEnvironment.get(playerUUID);

		if (newFinalTargetEnvironment == Environment.RAIN || newFinalTargetEnvironment == Environment.THUNDERSTORM) {
			float startIntensity = 0.1F;
			playerTransitions.put(playerUUID, new TransitionData(newFinalTargetEnvironment, startIntensity, true, newFinalTargetEnvironment));
			ServerEnvironmentPacketHelper.sendEnvironmentState(player, newFinalTargetEnvironment, startIntensity);
			lastSentEnvironment.put(playerUUID, newFinalTargetEnvironment);
		} else {
			boolean isCurrentlyRainOrThunder = false;

			if (lastEnv == Environment.RAIN || lastEnv == Environment.THUNDERSTORM) {
				isCurrentlyRainOrThunder = true;
				float intensityToStartFadeFrom = 1.0f;
				playerTransitions.put(playerUUID, new TransitionData(lastEnv, intensityToStartFadeFrom, false, newFinalTargetEnvironment));
				ServerEnvironmentPacketHelper.sendEnvironmentState(player, lastEnv, intensityToStartFadeFrom);
			} else if (existingTransition != null && (existingTransition.activeTransitionEnvironment == Environment.RAIN || existingTransition.activeTransitionEnvironment == Environment.THUNDERSTORM)) {
				isCurrentlyRainOrThunder = true;
				Environment environmentToFadeOut = existingTransition.activeTransitionEnvironment;
				float intensityToStartFadeFrom = existingTransition.currentIntensity;
				playerTransitions.put(playerUUID, new TransitionData(environmentToFadeOut, intensityToStartFadeFrom, false, newFinalTargetEnvironment));
			}

			if (!isCurrentlyRainOrThunder) {
				ServerEnvironmentPacketHelper.sendEnvironmentState(player, newFinalTargetEnvironment);
				lastSentEnvironment.put(playerUUID, newFinalTargetEnvironment);
				if (existingTransition != null) {
					playerTransitions.remove(playerUUID);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) return;

		tickCounter++;
		if (tickCounter < UPDATE_INTERVAL) return;

		tickCounter = 0;

		playerTransitions
			.entrySet()
			.removeIf(entry -> {
				UUID playerUUID = entry.getKey();
				TransitionData transition = entry.getValue();

				ServerPlayerEntity player = ServerEnvironmentPacketHelper.getPlayerByUUID(playerUUID);
				if (player == null) return true;

				if (transition.isEntering) {
					transition.currentIntensity += INTENSITY_INCREMENT;
					if (transition.currentIntensity >= 1.0F) {
						transition.currentIntensity = 1.0F;

						ServerEnvironmentPacketHelper.sendEnvironmentState(player, transition.activeTransitionEnvironment, 1.0F);
						lastSentEnvironment.put(playerUUID, transition.activeTransitionEnvironment);

						if (transition.finalTargetEnvironment == null || transition.finalTargetEnvironment == transition.activeTransitionEnvironment) {
							return true;
						} else {
							return true;
						}
					}
				} else {
					transition.currentIntensity -= INTENSITY_INCREMENT;
					if (transition.currentIntensity <= 0.0F) {
						transition.currentIntensity = 0.0F;

						ServerEnvironmentPacketHelper.sendRainLevelChange(player, 0.0F);
						ServerEnvironmentPacketHelper.sendThunderLevelChange(player, 0.0F);
						ServerEnvironmentPacketHelper.sendRainEnvironment(player, false);
						lastSentEnvironment.put(playerUUID, null);

						if (transition.finalTargetEnvironment != null && transition.finalTargetEnvironment != transition.activeTransitionEnvironment) {
							ServerEnvironmentPacketHelper.sendEnvironmentState(player, transition.finalTargetEnvironment);
							lastSentEnvironment.put(playerUUID, transition.finalTargetEnvironment);
						}
						return true;
					}
				}

				ServerEnvironmentPacketHelper.sendEnvironmentState(player, transition.activeTransitionEnvironment, transition.currentIntensity);

				return false;
			});
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			UUID playerUUID = event.getPlayer().getUUID();

			playerTransitions.remove(playerUUID);
			lastSentEnvironment.remove(playerUUID);
		}
	}

	private static class TransitionData {

		final Environment activeTransitionEnvironment;
		float currentIntensity;
		final boolean isEntering;
		final Environment finalTargetEnvironment;

		TransitionData(Environment activeEnv, float startIntensity, boolean entering, Environment finalEnv) {
			this.activeTransitionEnvironment = activeEnv;
			this.currentIntensity = startIntensity;
			this.isEntering = entering;
			this.finalTargetEnvironment = finalEnv;
		}
	}
}
