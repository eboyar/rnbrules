package ethan.hoenn.rnbrules.utils.managers;

import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;

public class ClientLocationManager {

	private static ClientLocationManager instance;
	private final Map<UUID, String> playerCurrentLocations = new HashMap<>();
	private final Map<String, Environment> locationEnvironments = new HashMap<>();
	private String currentPlayerLocation;

	public static ClientLocationManager getInstance() {
		if (instance == null) {
			instance = new ClientLocationManager();
		}
		return instance;
	}

	public void updateClientPlayerLocation(UUID playerUuid, String locationName) {
		playerCurrentLocations.put(playerUuid, locationName);

		if (playerUuid.equals(getClientPlayerUUID())) {
			currentPlayerLocation = locationName;
		}
	}

	public void updateLocationEnvironment(String locationName, Environment environment) {
		if (environment == Environment.NONE) {
			locationEnvironments.remove(locationName);
		} else {
			locationEnvironments.put(locationName, environment);
		}
	}

	public UUID getClientPlayerUUID() {
		if (Minecraft.getInstance().player != null) {
			return Minecraft.getInstance().player.getUUID();
		}
		return null;
	}

	public String getPlayerLocation(UUID playerUuid) {
		return playerCurrentLocations.get(playerUuid);
	}

	public String getCurrentPlayerLocation() {
		return currentPlayerLocation;
	}

	public Environment getLocationEnvironment(String locationName) {
		return locationEnvironments.get(locationName);
	}

	public void clear() {
		playerCurrentLocations.clear();
		locationEnvironments.clear();
		currentPlayerLocation = null;
	}
}
