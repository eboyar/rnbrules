package ethan.hoenn.rnbrules.utils.managers;

import ethan.hoenn.rnbrules.network.EnvironmentSyncPacket;
import ethan.hoenn.rnbrules.network.LocationSyncPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.enums.BoardType;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.network.NetworkDirection;

public class LocationManager extends WorldSavedData {

	private static final String DATA_TAG = "location_manager";
	private static LocationManager instance;
	private final Map<UUID, String> lastPlayerLocations = new HashMap<>();
	private final Map<UUID, String> currentPlayerLocations = new HashMap<>();
	private final Map<UUID, String[]> playerMovementDirections = new HashMap<>();
	private final Map<String, Environment> locationEnvironments = new HashMap<>();

	public LocationManager() {
		super(DATA_TAG);
	}

	public static LocationManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(LocationManager::new, DATA_TAG);
		}
		return instance;
	}

	public static String normalizeLocationName(String locationName) {
		if (locationName == null) {
			return null;
		}

		String normalized = locationName.replaceAll("&[0-9a-fk-or]", "");
		normalized = normalized.replaceAll("[.\\s]+", "").toLowerCase();

		return normalized;
	}

	public static boolean isWaterLocation(String normalizedLocationName) {
		if (normalizedLocationName == null || normalizedLocationName.isEmpty()) {
			return false;
		}

		String[] waterRoutes = {
			"route105",
			"route108",
			"route109",
			"route122",
			"route124",
			"route125",
			"route126",
			"route127",
			"route128",
			"route129",
			"route130",
			"route131",
			"route132",
			"route133",
			"route134",
		};

		for (String route : waterRoutes) {
			if (normalizedLocationName.contains(route)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isCaveLocation(String normalizedLocationName) {
		if (normalizedLocationName == null || normalizedLocationName.isEmpty()) {
			return false;
		}

		String[] caveLocations = {
			"seafloorcavern",
			"chamberoforigin",
			"fierypath",
			"granitecave",
			"rusturftunnel",
			"alteringcave",
			"meteorfalls",
			"newmauville",
			"teammagmahideout",
			"aquahideout",
			"mtchimneycore",
			"victoryroad",
			"scorchedslab"
		};

		for (String cave : caveLocations) {
			if (normalizedLocationName.contains(cave)) {
				return true;
			}
		}

		return false;
	}

	public static BoardType determineBoardType(String locationName) {
		String normalizedLocation = normalizeLocationName(locationName);

		if (isWaterLocation(normalizedLocation)) {
			return BoardType.WATER;
		} else if (isCaveLocation(normalizedLocation)) {
			return BoardType.CAVE;
		} else {
			return BoardType.WOOD;
		}
	}

	public boolean updatePlayerLocation(ServerPlayerEntity player, String locationName) {
		UUID playerUUID = player.getUUID();
		String currentLocation = currentPlayerLocations.get(playerUUID);

		if (locationName.equals(currentLocation)) {
			return false;
		}

		if (currentLocation != null) {
			lastPlayerLocations.put(playerUUID, currentLocation);
		}

		currentPlayerLocations.put(playerUUID, locationName);

		if (currentLocation != null) {
			playerMovementDirections.put(playerUUID, new String[] { currentLocation, locationName });
		}

		sendLocationUpdateToClient(player, locationName);
		Environment env = getLocationEnvironment(locationName);
		if (env != null) {
			sendEnvironmentUpdateToClient(player, locationName, env);
		}

		this.setDirty();

		return true;
	}

	private void sendLocationUpdateToClient(ServerPlayerEntity player, String locationName) {
		LocationSyncPacket packet = new LocationSyncPacket(player.getUUID(), locationName);
		PacketHandler.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	private void sendEnvironmentUpdateToClient(ServerPlayerEntity player, String locationName, Environment environment) {
		EnvironmentSyncPacket packet = new EnvironmentSyncPacket(locationName, environment.getId());
		PacketHandler.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	public String getPlayerLastLocation(UUID playerUUID) {
		return lastPlayerLocations.get(playerUUID);
	}

	public String getPlayerCurrentLocation(UUID playerUUID) {
		return currentPlayerLocations.get(playerUUID);
	}

	public String[] getPlayerMovementDirection(UUID playerUUID) {
		return playerMovementDirections.get(playerUUID);
	}

	public String addLocationFormatting(String baseLocation) {
		String lower = baseLocation.toLowerCase();

		if ((lower.contains("city") || lower.contains("town")) && !baseLocation.equalsIgnoreCase("ever grande city")) {
			return "&e" + baseLocation;
		} else if (lower.contains("route")) {
			return "&a" + baseLocation;
		} else if (baseLocation.equalsIgnoreCase("ever grande city")) {
			return "&d" + baseLocation;
		} else {
			return baseLocation;
		}
	}

	public boolean setLocationEnvironment(String locationName, Environment environment) {
		if (locationName == null || locationName.isEmpty()) {
			return false;
		}

		if (environment.equals(Environment.NONE)) {
			locationEnvironments.remove(locationName);
		} else {
			locationEnvironments.put(locationName, environment);
		}

		this.setDirty();
		return true;
	}

	public Environment getLocationEnvironment(String locationName) {
		if (locationName == null || locationName.isEmpty()) {
			return null;
		}

		return locationEnvironments.get(locationName);
	}

	public Map<String, Environment> getLocationEnvironments() {
		return locationEnvironments;
	}

	@Override
	public void load(CompoundNBT nbt) {
		lastPlayerLocations.clear();
		currentPlayerLocations.clear();
		playerMovementDirections.clear();
		locationEnvironments.clear();

		if (nbt.contains("LastLocations")) {
			CompoundNBT lastLocNBT = nbt.getCompound("LastLocations");
			for (String uuidStr : lastLocNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				lastPlayerLocations.put(uuid, lastLocNBT.getString(uuidStr));
			}
		}

		if (nbt.contains("CurrentLocations")) {
			CompoundNBT currentLocNBT = nbt.getCompound("CurrentLocations");
			for (String uuidStr : currentLocNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				currentPlayerLocations.put(uuid, currentLocNBT.getString(uuidStr));
			}
		}

		if (nbt.contains("MovementDirections")) {
			CompoundNBT directionNBT = nbt.getCompound("MovementDirections");
			for (String uuidStr : directionNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				ListNBT dirList = directionNBT.getList(uuidStr, 8);
				if (dirList.size() == 2) {
					String[] direction = new String[] { dirList.getString(0), dirList.getString(1) };
					playerMovementDirections.put(uuid, direction);
				}
			}
		}

		if (nbt.contains("LocationEnvironments")) {
			CompoundNBT envNBT = nbt.getCompound("LocationEnvironments");
			for (String location : envNBT.getAllKeys()) {
				String environmentId = envNBT.getString(location);
				Environment environment = Environment.fromString(environmentId);
				if (environment != null) {
					locationEnvironments.put(location, environment);
				}
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT lastLocNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : lastPlayerLocations.entrySet()) {
			lastLocNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("LastLocations", lastLocNBT);

		CompoundNBT currentLocNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : currentPlayerLocations.entrySet()) {
			currentLocNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("CurrentLocations", currentLocNBT);

		CompoundNBT directionNBT = new CompoundNBT();
		for (Map.Entry<UUID, String[]> entry : playerMovementDirections.entrySet()) {
			String[] dir = entry.getValue();
			if (dir != null && dir.length == 2) {
				ListNBT dirList = new ListNBT();
				dirList.add(StringNBT.valueOf(dir[0]));
				dirList.add(StringNBT.valueOf(dir[1]));
				directionNBT.put(entry.getKey().toString(), dirList);
			}
		}
		nbt.put("MovementDirections", directionNBT);

		CompoundNBT envNBT = new CompoundNBT();
		for (Map.Entry<String, Environment> entry : locationEnvironments.entrySet()) {
			envNBT.putString(entry.getKey(), entry.getValue().getId());
		}
		nbt.put("LocationEnvironments", envNBT);

		return nbt;
	}
}
