package ethan.hoenn.rnbrules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

public class RNBConfig {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final String CONFIG_PATH = "config/rnbrules.json";

	public static ConfigData configData = new ConfigData();

	public static void loadConfig() {
		File file = Paths.get(CONFIG_PATH).toFile();
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				Type type = new TypeToken<ConfigData>() {}.getType();
				configData = gson.fromJson(reader, type);
				if (configData == null) {
					configData = getDefaultConfig();
				}
			} catch (IOException e) {
				System.err.println("Error loading config/rnbrules.json, using default values");
				configData = getDefaultConfig();
			}
		} else {
			configData = getDefaultConfig();
			saveConfig();
		}
	}

	public static void saveConfig() {
		File file = Paths.get(CONFIG_PATH).toFile();
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(configData, writer);
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error saving config/rnbrules.json");
		}
	}

	private static ConfigData getDefaultConfig() {
		ConfigData defaultConfig = new ConfigData();

		defaultConfig.badgeRequirements.put("pixelmon:razor_fang", 3);
		defaultConfig.badgeRequirements.put("pixelmon:great_ball", 1);
		defaultConfig.badgeRequirements.put("pixelmon:ultra_ball", 3);

		defaultConfig.gauntlets.put("gauntlet_1", Arrays.asList("UUID1", "UUID2"));
		defaultConfig.gauntlets.put("gauntlet_2", Arrays.asList("UUID3", "UUID4"));

		defaultConfig.deathlessCommands.put("gauntlet_1", "give @pl minecraft:diamond 1");
		defaultConfig.deathlessCommands.put("gauntlet_2", "xp add @pl 100 levels");

		defaultConfig.safariEntryPoint = new TeleportLocation(0, 64, 0);
		defaultConfig.safariExitPoint = new TeleportLocation(0, 64, 0);

		defaultConfig.townLocations.put("LITTLEROOT", new TeleportLocation(100, 64, 100));
		defaultConfig.townLocations.put("OLDALE", new TeleportLocation(200, 64, 200));
		defaultConfig.townLocations.put("PETALBURG", new TeleportLocation(300, 64, 300));
		defaultConfig.townLocations.put("RUSTBORO", new TeleportLocation(400, 64, 400));
		defaultConfig.townLocations.put("DEWFORD", new TeleportLocation(500, 64, 500));
		defaultConfig.townLocations.put("SLATEPORT", new TeleportLocation(600, 64, 600));
		defaultConfig.townLocations.put("MAUVILLE", new TeleportLocation(700, 64, 700));
		defaultConfig.townLocations.put("VERDANTURF", new TeleportLocation(800, 64, 800));
		defaultConfig.townLocations.put("FALLARBOR", new TeleportLocation(900, 64, 900));
		defaultConfig.townLocations.put("LAVARIDGE", new TeleportLocation(1000, 64, 1000));
		defaultConfig.townLocations.put("FORTREE", new TeleportLocation(1100, 64, 1100));
		defaultConfig.townLocations.put("LILYCOVE", new TeleportLocation(1200, 64, 1200));
		defaultConfig.townLocations.put("MOSSDEEP", new TeleportLocation(1300, 64, 1300));
		defaultConfig.townLocations.put("SOOTOPOLIS", new TeleportLocation(1400, 64, 1400));
		defaultConfig.townLocations.put("PACIFIDLOG", new TeleportLocation(1500, 64, 1500));

		defaultConfig.ferryLocations.put("PETALBURG", new TeleportLocation(305, 65, 305));
		defaultConfig.ferryLocations.put("DEWFORD", new TeleportLocation(505, 65, 505));
		defaultConfig.ferryLocations.put("LITTLEROOT", new TeleportLocation(105, 65, 105));
		defaultConfig.ferryLocations.put("ROUTE_109", new TeleportLocation(550, 65, 650));
		defaultConfig.ferryLocations.put("SLATEPORT", new TeleportLocation(605, 65, 605));
		defaultConfig.ferryLocations.put("PACIFIDLOG", new TeleportLocation(1505, 65, 1505));
		defaultConfig.ferryLocations.put("SOOTOPOLIS", new TeleportLocation(1405, 65, 1405));
		defaultConfig.ferryLocations.put("MOSSDEEP", new TeleportLocation(1305, 65, 1305));
		defaultConfig.ferryLocations.put("LILYCOVE", new TeleportLocation(1205, 65, 1205));
		defaultConfig.ferryLocations.put("EVER_GRANDE", new TeleportLocation(1600, 65, 1600));

		defaultConfig.arenaPlayerPositions.put("1", new TeleportLocation(100.5, 64, 100.5));
		defaultConfig.arenaPlayerPositions.put("2", new TeleportLocation(200.5, 64, 200.5));
		defaultConfig.arenaPlayerPositions.put("3", new TeleportLocation(300.5, 64, 300.5));
		defaultConfig.arenaPlayerPositions.put("4", new TeleportLocation(400.5, 64, 400.5));
		defaultConfig.arenaPlayerPositions.put("5", new TeleportLocation(500.5, 64, 500.5));

		defaultConfig.arenaOpponentPositions.put("1", new TeleportLocation(110.5, 64, 100.5));
		defaultConfig.arenaOpponentPositions.put("2", new TeleportLocation(210.5, 64, 200.5));
		defaultConfig.arenaOpponentPositions.put("3", new TeleportLocation(310.5, 64, 300.5));
		defaultConfig.arenaOpponentPositions.put("4", new TeleportLocation(410.5, 64, 400.5));
		defaultConfig.arenaOpponentPositions.put("5", new TeleportLocation(510.5, 64, 500.5));

		defaultConfig.firstJoinCommands.add("give @pl pixelmon:poke_ball 5");

		return defaultConfig;
	}

	public static void addNewGauntlet(String gauntletID) {
		if (!configData.gauntlets.containsKey(gauntletID)) {
			configData.gauntlets.put(gauntletID, new ArrayList<>());
			saveConfig();
			System.out.println("Gauntlet " + gauntletID + " added successfully.");
		} else {
			System.out.println("Gauntlet " + gauntletID + " already exists.");
		}
	}

	public static void addNPCToGauntlet(String UUID, String gauntletID) {
		if (!configData.gauntlets.containsKey(gauntletID)) {
			System.out.println("Gauntlet " + gauntletID + " does not exist.");
		}

		List<String> npcList = configData.gauntlets.get(gauntletID);

		if (!npcList.contains(UUID)) {
			npcList.add(UUID);
			saveConfig();
			System.out.println("NPC " + UUID + " added to Gauntlet " + gauntletID + ".");
		} else {
			System.out.println("NPC " + UUID + " is already in Gauntlet " + gauntletID + ".");
		}
	}

	public static void removeNPCFromGauntlet(String UUID, String gauntletID) {
		if (!configData.gauntlets.containsKey(gauntletID)) {
			System.out.println("Gauntlet " + gauntletID + " does not exist.");
			return;
		}

		List<String> npcList = configData.gauntlets.get(gauntletID);
		if (npcList.contains(UUID)) {
			npcList.remove(UUID);
			saveConfig();
			System.out.println("NPC " + UUID + " removed from Gauntlet " + gauntletID + ".");
		} else {
			System.out.println("NPC " + UUID + " is not in Gauntlet " + gauntletID + ".");
		}
	}

	public static Map<String, Integer> getBadgeRequirements() {
		return configData.badgeRequirements;
	}

	public static Map<String, List<String>> getGauntlets() {
		return configData.gauntlets;
	}

	public static String getDeathlessCommand(String gauntletId) {
		return configData.deathlessCommands.getOrDefault(gauntletId, "");
	}

	public static void setDeathlessCommand(String gauntletId, String command) {
		configData.deathlessCommands.put(gauntletId, command);
		saveConfig();
	}

	public static String getNextGauntlet(String gauntletId) {
		return configData.nextGauntlets.getOrDefault(gauntletId, "");
	}

	public static void setNextGauntlet(String gauntletId, String nextGauntletId) {
		configData.nextGauntlets.put(gauntletId, nextGauntletId);
		saveConfig();
	}

	public static String findParentGauntlet(String gauntletId) {
		for (Map.Entry<String, String> entry : configData.nextGauntlets.entrySet()) {
			if (gauntletId.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static class ConfigData {

		public Map<String, Integer> badgeRequirements = new HashMap<>();
		public Map<String, List<String>> gauntlets = new HashMap<>();
		public Map<String, TeleportLocation> townLocations = new HashMap<>();
		public Map<String, TeleportLocation> ferryLocations = new HashMap<>();
		public Map<String, String> deathlessCommands = new HashMap<>();
		public Map<String, String> nextGauntlets = new HashMap<>();
		public TeleportLocation safariEntryPoint = new TeleportLocation(0, 64, 0);
		public TeleportLocation safariExitPoint = new TeleportLocation(0, 64, 0);
		public String safariCompletionCommand = "";
		public String gamecornerCompletionCommand = "";
		public String fossilCompletionCommand = "";
		public List<String> firstJoinCommands = new ArrayList<>();

		public String SidneyS = "";
		public String SidneyD = "";
		public String PhoebeS = "";
		public String PhoebeD = "";
		public String GlaciaS = "";
		public String GlaciaD = "";
		public String DrakeS = "";
		public String DrakeD = "";
		public String Wallace = "";

		public Map<String, TeleportLocation> arenaPlayerPositions = new HashMap<>();
		public Map<String, TeleportLocation> arenaOpponentPositions = new HashMap<>();
	}

	public static class TeleportLocation {

		public double x;
		public double y;
		public double z;

		public TeleportLocation(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public TeleportLocation() {
			this(0, 70, 0);
		}
	}

	public static TeleportLocation getTownLocation(String townName) {
		if (configData.townLocations.containsKey(townName)) {
			return configData.townLocations.get(townName);
		} else {
			System.out.println("Warning: Town location for " + townName + " not found in config");
			return new TeleportLocation(0, 64, 0);
		}
	}

	public static TeleportLocation getFerryLocation(String locationName) {
		if (configData.ferryLocations.containsKey(locationName)) {
			return configData.ferryLocations.get(locationName);
		} else {
			System.out.println("Warning: Ferry location for " + locationName + " not found in config");
			return new TeleportLocation(0, 64, 0);
		}
	}

	public static void setTownLocation(String townName, TeleportLocation location) {
		configData.townLocations.put(townName, location);
		saveConfig();
	}

	public static void setFerryLocation(String locationName, TeleportLocation location) {
		configData.ferryLocations.put(locationName, location);
		saveConfig();
	}

	public static TeleportLocation getSafariEntryPoint() {
		return configData.safariEntryPoint;
	}

	public static TeleportLocation getSafariExitPoint() {
		return configData.safariExitPoint;
	}

	public static void setSafariEntryPoint(TeleportLocation location) {
		configData.safariEntryPoint = location;
		saveConfig();
	}

	public static void setSafariExitPoint(TeleportLocation location) {
		configData.safariExitPoint = location;
		saveConfig();
	}

	public static String getSafariCompletionCommand() {
		return configData.safariCompletionCommand;
	}

	public static void setSafariCompletionCommand(String command) {
		configData.safariCompletionCommand = command;
		saveConfig();
	}

	public static String getGamecornerCompletionCommand() {
		return configData.gamecornerCompletionCommand;
	}

	public static void setGamecornerCompletionCommand(String command) {
		configData.gamecornerCompletionCommand = command;
		saveConfig();
	}

	public static String getFossilCompletionCommand() {
		return configData.fossilCompletionCommand;
	}

	public static void setFossilCompletionCommand(String command) {
		configData.fossilCompletionCommand = command;
		saveConfig();
	}

	public static UUID getSidneySinglesUUID() {
		try {
			return configData.SidneyS != null && !configData.SidneyS.isEmpty() ? UUID.fromString(configData.SidneyS) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Sidney Singles: " + configData.SidneyS);
			return null;
		}
	}

	public static UUID getSidneyDoublesUUID() {
		try {
			return configData.SidneyD != null && !configData.SidneyD.isEmpty() ? UUID.fromString(configData.SidneyD) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Sidney Doubles: " + configData.SidneyD);
			return null;
		}
	}

	public static UUID getPhoebeSinglesUUID() {
		try {
			return configData.PhoebeS != null && !configData.PhoebeS.isEmpty() ? UUID.fromString(configData.PhoebeS) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Phoebe Singles: " + configData.PhoebeS);
			return null;
		}
	}

	public static UUID getPhoebeDoublesUUID() {
		try {
			return configData.PhoebeD != null && !configData.PhoebeD.isEmpty() ? UUID.fromString(configData.PhoebeD) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Phoebe Doubles: " + configData.PhoebeD);
			return null;
		}
	}

	public static UUID getGlaciaSinglesUUID() {
		try {
			return configData.GlaciaS != null && !configData.GlaciaS.isEmpty() ? UUID.fromString(configData.GlaciaS) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Glacia Singles: " + configData.GlaciaS);
			return null;
		}
	}

	public static UUID getGlaciaDoublesUUID() {
		try {
			return configData.GlaciaD != null && !configData.GlaciaD.isEmpty() ? UUID.fromString(configData.GlaciaD) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Glacia Doubles: " + configData.GlaciaD);
			return null;
		}
	}

	public static UUID getDrakeSinglesUUID() {
		try {
			return configData.DrakeS != null && !configData.DrakeS.isEmpty() ? UUID.fromString(configData.DrakeS) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Drake Singles: " + configData.DrakeS);
			return null;
		}
	}

	public static UUID getDrakeDoublesUUID() {
		try {
			return configData.DrakeD != null && !configData.DrakeD.isEmpty() ? UUID.fromString(configData.DrakeD) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Drake Doubles: " + configData.DrakeD);
			return null;
		}
	}

	public static UUID getWallaceUUID() {
		try {
			return configData.Wallace != null && !configData.Wallace.isEmpty() ? UUID.fromString(configData.Wallace) : null;
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid UUID format for Wallace: " + configData.Wallace);
			return null;
		}
	}

	public static TeleportLocation getLeaguePlayerPosition(int arenaNumber) {
		String arenaKey = String.valueOf(arenaNumber);
		if (configData.arenaPlayerPositions.containsKey(arenaKey)) {
			return configData.arenaPlayerPositions.get(arenaKey);
		} else {
			System.out.println("Warning: Player position for arena " + arenaNumber + " not found in config");
			return new TeleportLocation(0, 64, 0);
		}
	}

	public static TeleportLocation getLeagueOpponentPosition(int arenaNumber) {
		String arenaKey = String.valueOf(arenaNumber);
		if (configData.arenaOpponentPositions.containsKey(arenaKey)) {
			return configData.arenaOpponentPositions.get(arenaKey);
		} else {
			System.out.println("Warning: Opponent position for arena " + arenaNumber + " not found in config");
			return new TeleportLocation(0, 64, 0);
		}
	}

	public static void setLeaguePlayerPosition(int arenaNumber, TeleportLocation location) {
		String arenaKey = String.valueOf(arenaNumber);
		configData.arenaPlayerPositions.put(arenaKey, location);
		saveConfig();
	}

	public static void setLeagueOpponentPosition(int arenaNumber, TeleportLocation location) {
		String arenaKey = String.valueOf(arenaNumber);
		configData.arenaOpponentPositions.put(arenaKey, location);
		saveConfig();
	}

	public static List<String> getFirstJoinCommands() {
		return new ArrayList<>(configData.firstJoinCommands);
	}

	public static void setFirstJoinCommands(List<String> commands) {
		configData.firstJoinCommands = new ArrayList<>(commands);
		saveConfig();
	}

	public static void addFirstJoinCommand(String command) {
		configData.firstJoinCommands.add(command);
		saveConfig();
	}

	public static boolean removeFirstJoinCommand(int index) {
		if (index >= 0 && index < configData.firstJoinCommands.size()) {
			configData.firstJoinCommands.remove(index);
			saveConfig();
			return true;
		}
		return false;
	}

	public static void clearFirstJoinCommands() {
		configData.firstJoinCommands.clear();
		saveConfig();
	}
}
