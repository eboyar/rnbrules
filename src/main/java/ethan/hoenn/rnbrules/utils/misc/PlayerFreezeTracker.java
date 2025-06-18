package ethan.hoenn.rnbrules.utils.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerFreezeTracker {

	private static final Map<UUID, FrozenPlayerData> FROZEN_PLAYERS = new HashMap<>();

	private static class FrozenPlayerData {

		private final ServerPlayerEntity player;
		private final int duration;
		private final float targetYaw;
		private int ticksRemaining;
		private boolean wasFlying;
		private final Vector3d initialPosition;
		private static final double POSITION_TOLERANCE = 0.0025;

		public FrozenPlayerData(ServerPlayerEntity player, int duration, float targetYaw) {
			this.player = player;
			this.duration = duration;
			this.targetYaw = targetYaw;
			this.ticksRemaining = duration;
			this.wasFlying = player.abilities.flying;
			this.initialPosition = player.position();
		}

		public void apply() {
			if (player.abilities.flying) {
				player.abilities.flying = false;
			}

			player.setDeltaMovement(0, 0, 0);

			player.yRot = targetYaw;
			player.yRotO = targetYaw;
			player.yHeadRot = targetYaw;
			player.yHeadRotO = targetYaw;

			player.xxa = 0;
			player.zza = 0;
			player.yya = 0;

			Vector3d currentPos = player.position();
			if (currentPos.distanceTo(initialPosition) > POSITION_TOLERANCE) {
				player.teleportTo(initialPosition.x, initialPosition.y, initialPosition.z);
			}

			player.hurtMarked = true;
		}

		public void release() {
			if (wasFlying && player.abilities.mayfly) {
				player.abilities.flying = true;
			}
		}

		public boolean tick() {
			if (--ticksRemaining <= 0) {
				release();
				return true;
			}

			apply();
			return false;
		}
	}

	public static void freezePlayer(ServerPlayerEntity player, int durationTicks, float targetYaw) {
		UUID playerID = player.getUUID();

		if (FROZEN_PLAYERS.containsKey(playerID)) {
			FrozenPlayerData oldData = FROZEN_PLAYERS.remove(playerID);
			oldData.release();
		}

		FrozenPlayerData data = new FrozenPlayerData(player, durationTicks, targetYaw);
		FROZEN_PLAYERS.put(playerID, data);

		data.apply();
	}

	public static void releasePlayer(ServerPlayerEntity player) {
		UUID playerID = player.getUUID();
		FrozenPlayerData data = FROZEN_PLAYERS.remove(playerID);
		if (data != null) {
			data.release();
		}
	}

	public static boolean isPlayerFrozen(UUID playerID) {
		return FROZEN_PLAYERS.containsKey(playerID);
	}

	public static void tickAll() {
		Iterator<Map.Entry<UUID, FrozenPlayerData>> iterator = FROZEN_PLAYERS.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, FrozenPlayerData> entry = iterator.next();
			FrozenPlayerData data = entry.getValue();

			if (!data.player.isAlive() || data.player.hasDisconnected() || data.tick()) {
				iterator.remove();
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.player;
			UUID playerID = player.getUUID();

			if (isPlayerFrozen(playerID)) {
				FrozenPlayerData data = FROZEN_PLAYERS.get(playerID);

				player.setDeltaMovement(0, 0, 0);
				player.xxa = 0;
				player.zza = 0;
				player.yya = 0;
				player.fallDistance = 0;

				Vector3d currentPos = player.position();
				if (data.initialPosition.distanceTo(currentPos) > data.POSITION_TOLERANCE) {
					player.teleportTo(data.initialPosition.x, data.initialPosition.y, data.initialPosition.z);
				}

				player.yRot = data.targetYaw;
				player.yRotO = data.targetYaw;

				if (!player.isOnGround()) {
					Vector3d pos = player.position();
					player.setPos(pos.x, pos.y - 0.1, pos.z);
				}

				player.hurtMarked = true;
			}
		}
	}
}
