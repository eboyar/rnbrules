package ethan.hoenn.rnbrules.listeners;

import ethan.hoenn.rnbrules.blocks.LocationBlockTileEntity;
import ethan.hoenn.rnbrules.utils.managers.EncounterManager;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import ethan.hoenn.rnbrules.utils.notifications.LocationNotifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LocationListener {

	private static final Map<UUID, BlockPos> lastCheckedPositions = new HashMap<>();
	private static final Map<UUID, TileEntity> nearbyLocationBlocks = new HashMap<>();

	private static final Map<UUID, Integer> playerStepCount = new HashMap<>();
	private static final int ROAMER_CHECK_STEPS = 500;

	private static final int TICK_GROUPS = 5;
	private static final List<List<UUID>> playerTickGroups = new ArrayList<>(TICK_GROUPS);
	private static final Map<UUID, Integer> playerToGroupMap = new HashMap<>();

	private static final int SIDE_RADIUS = 1;
	private static final int BACKWARD_RADIUS = 5;

	private static int tickCounter = 0;
	private static final int CHECK_FREQUENCY = 5;

	private static boolean initialPlayerDistributionDone = false;

	static {
		for (int i = 0; i < TICK_GROUPS; i++) {
			playerTickGroups.add(new ArrayList<>());
		}
	}

	private static void addPlayerToDistribution(UUID playerUUID) {
		int minGroupSize = Integer.MAX_VALUE;
		int targetGroup = 0;

		for (int i = 0; i < TICK_GROUPS; i++) {
			int groupSize = playerTickGroups.get(i).size();
			if (groupSize < minGroupSize) {
				minGroupSize = groupSize;
				targetGroup = i;
			}
		}

		playerTickGroups.get(targetGroup).add(playerUUID);
		playerToGroupMap.put(playerUUID, targetGroup);
	}

	private static void removePlayerFromDistribution(UUID playerUUID) {
		Integer group = playerToGroupMap.remove(playerUUID);
		if (group != null) {
			playerTickGroups.get(group).remove(playerUUID);
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (!initialPlayerDistributionDone && event.phase == TickEvent.Phase.END) {
			initialPlayerDistributionDone = true;

			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

			if (server != null) {
				for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
					if (!playerToGroupMap.containsKey(player.getUUID())) {
						addPlayerToDistribution(player.getUUID());
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
			return;
		}

		tickCounter++;
		if (tickCounter % CHECK_FREQUENCY != 0) {
			return;
		}

		if (!(event.player instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.player;
		UUID playerUUID = player.getUUID();

		Integer playerGroup = playerToGroupMap.get(playerUUID);
		int currentGroup = tickCounter % TICK_GROUPS;

		if (playerGroup == null || playerGroup != currentGroup) {
			return;
		}

		ServerWorld world = (ServerWorld) player.level;
		BlockPos playerPos = player.blockPosition();

		BlockPos lastCheckedPos = lastCheckedPositions.get(playerUUID);

		if (lastCheckedPos != null && playerPos.distSqr(lastCheckedPos) < 2.0) {
			return;
		}

		lastCheckedPositions.put(playerUUID, playerPos);

		int steps = playerStepCount.getOrDefault(playerUUID, 0) + 1;
		playerStepCount.put(playerUUID, steps);

		if (steps >= ROAMER_CHECK_STEPS) {
			triggerRoamerEncounter(player);
			playerStepCount.put(playerUUID, 0);
		}

		TileEntity nearestLocationBlock = findNearestLocationBlock(world, playerPos, lastCheckedPos != null ? lastCheckedPos : playerPos, player);

		if (nearestLocationBlock instanceof LocationBlockTileEntity) {
			String locationName = ((LocationBlockTileEntity) nearestLocationBlock).getLocationName();

			TileEntity previousNearbyBlock = nearbyLocationBlocks.put(playerUUID, nearestLocationBlock);

			if (previousNearbyBlock != nearestLocationBlock) {
				LocationManager locationManager = LocationManager.get(world);
				boolean changedLocation = locationManager.updatePlayerLocation(player, locationName);

				if (changedLocation) {
					String[] direction = locationManager.getPlayerMovementDirection(playerUUID);

					LocationNotifier.notifyLocationChange(player, locationName, direction);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			UUID playerUUID = player.getUUID();

			lastCheckedPositions.remove(playerUUID);
			nearbyLocationBlocks.remove(playerUUID);
			playerStepCount.remove(playerUUID);
			addPlayerToDistribution(playerUUID);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			UUID playerUUID = event.getPlayer().getUUID();

			lastCheckedPositions.remove(playerUUID);
			nearbyLocationBlocks.remove(playerUUID);
			playerStepCount.remove(playerUUID);
			removePlayerFromDistribution(playerUUID);
		}
	}

	private static TileEntity findNearestLocationBlock(ServerWorld world, BlockPos center, BlockPos lastPos, ServerPlayerEntity player) {
		BlockPos searchCenter = center;

		boolean isInWater = player.isInWater() || (player.getVehicle() != null && player.getVehicle().isInWater());

		if (isInWater) {
			BlockPos.Mutable mutablePos = new BlockPos.Mutable(center.getX(), center.getY(), center.getZ());
			while (mutablePos.getY() > 0) {
				mutablePos.move(0, -1, 0);
				BlockState blockState = world.getBlockState(mutablePos);
				if (blockState.getMaterial().isSolid() && blockState.getMaterial() != Material.WATER) {
					searchCenter = mutablePos.immutable();
					break;
				}
			}

			if (mutablePos.getY() == 0 && !world.getBlockState(mutablePos).getMaterial().isSolid()) {
				searchCenter = center;
			}
		}

		TileEntity currentTile = world.getBlockEntity(searchCenter);
		if (currentTile instanceof LocationBlockTileEntity) {
			return currentTile;
		}

		TileEntity nearestBlock = null;
		int nearestDistanceSq = Integer.MAX_VALUE;

		int dx = searchCenter.getX() - lastPos.getX();
		int dz = searchCenter.getZ() - lastPos.getZ();

		int forwardX = 0, forwardZ = -1;
		if (Math.abs(dx) > Math.abs(dz)) {
			forwardX = Integer.signum(dx);
			forwardZ = 0;
		} else if (dz != 0) {
			forwardZ = Integer.signum(dz);
		} else if (dx != 0) {
			forwardX = Integer.signum(dx);
			forwardZ = 0;
		}

		int backwardX = -forwardX;
		int backwardZ = -forwardZ;

		int sideX = forwardZ;
		int sideZ = -forwardX;

		for (int b = 0; b <= BACKWARD_RADIUS; b++) {
			for (int s = -SIDE_RADIUS; s <= SIDE_RADIUS; s++) {
				for (int y = 0; y >= -3; y--) {
					int x = searchCenter.getX() + (b * backwardX) + (s * sideX);
					int yPos = searchCenter.getY() + y;
					int z = searchCenter.getZ() + (b * backwardZ) + (s * sideZ);

					BlockPos checkPos = new BlockPos(x, yPos, z);
					TileEntity tileEntity = world.getBlockEntity(checkPos);

					if (tileEntity instanceof LocationBlockTileEntity) {
						int distSq = (int) checkPos.distSqr(searchCenter);
						if (distSq < nearestDistanceSq) {
							nearestDistanceSq = distSq;
							nearestBlock = tileEntity;
						}
					}
				}
			}
		}

		return nearestBlock;
	}

	public static Map<Integer, Integer> getPlayerDistribution() {
		Map<Integer, Integer> distribution = new HashMap<>();

		for (int i = 0; i < TICK_GROUPS; i++) {
			distribution.put(i, playerTickGroups.get(i).size());
		}

		return distribution;
	}

	private static void triggerRoamerEncounter(ServerPlayerEntity player) {
		ServerWorld world = (ServerWorld) player.level;
		EncounterManager.get(world).tryTriggerRoamerEncounter(player);
	}
}
