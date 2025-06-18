package ethan.hoenn.rnbrules.environment.server;

import ethan.hoenn.rnbrules.network.EnvironmentPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ServerEnvironmentPacketHelper {

	public static void sendRainEnvironment(ServerPlayerEntity player, boolean startRain) {
		if (player == null) return;

		if (startRain) {
			SChangeGameStatePacket packet = new SChangeGameStatePacket(SChangeGameStatePacket.START_RAINING, 0.0F);
			player.connection.send(packet);
		} else {
			SChangeGameStatePacket packet = new SChangeGameStatePacket(SChangeGameStatePacket.STOP_RAINING, 0.0F);
			sendRainLevelChange(player, 0.0F);
			player.connection.send(packet);
		}
	}

	public static void sendRainLevelChange(ServerPlayerEntity player, float rainLevel) {
		if (player == null) return;

		rainLevel = Math.min(1.0F, Math.max(0.0F, rainLevel));

		SChangeGameStatePacket packet = new SChangeGameStatePacket(SChangeGameStatePacket.RAIN_LEVEL_CHANGE, rainLevel);
		player.connection.send(packet);
	}

	public static void sendThunderLevelChange(ServerPlayerEntity player, float thunderLevel) {
		if (player == null) return;

		thunderLevel = Math.min(1.0F, Math.max(0.0F, thunderLevel));

		SChangeGameStatePacket packet = new SChangeGameStatePacket(SChangeGameStatePacket.THUNDER_LEVEL_CHANGE, thunderLevel);
		player.connection.send(packet);
	}

	public static void sendEnvironmentState(ServerPlayerEntity player, Environment environment) {
		sendEnvironmentState(player, environment, 1.0F);
	}

	public static void sendEnvironmentState(ServerPlayerEntity player, Environment environment, float intensity) {
		if (player == null) return;

		intensity = Math.min(1.0F, Math.max(0.0F, intensity));

		switch (environment) {
			case RAIN:
				if (intensity > 0.0F) {
					sendRainEnvironment(player, true);
					sendRainLevelChange(player, intensity);
				} else {
					sendRainEnvironment(player, false);
				}
				break;
			case THUNDERSTORM:
				if (intensity > 0.0F) {
					sendRainEnvironment(player, true);
					sendRainLevelChange(player, intensity);
					sendThunderLevelChange(player, intensity);
				} else {
					sendRainEnvironment(player, false);
				}
				break;
			case NONE:
			case SUN:
				sendRainLevelChange(player, 0.0F);
				sendThunderLevelChange(player, 0.0F);
				sendRainEnvironment(player, false);
				break;
			default:
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new EnvironmentPacket(environment.getId(), intensity));
				break;
		}
	}

	public static ServerPlayerEntity getPlayerByUUID(UUID uuid) {
		if (uuid == null || ServerLifecycleHooks.getCurrentServer() == null) return null;

		return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
	}

	public static void setWorldRain(ServerWorld world, boolean isRaining) {
		if (world == null) return;

		world.setWeatherParameters(0, isRaining ? 6000 : 0, isRaining, false);

		for (ServerPlayerEntity player : world.players()) {
			if (isRaining) {
				sendRainEnvironment(player, true);
				sendRainLevelChange(player, 1.0F);
			} else {
				sendRainLevelChange(player, 0.0F);
				sendThunderLevelChange(player, 0.0F);
				sendRainEnvironment(player, false);
			}
		}
	}
}
