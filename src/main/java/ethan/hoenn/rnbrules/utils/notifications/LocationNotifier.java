package ethan.hoenn.rnbrules.utils.notifications;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import ethan.hoenn.rnbrules.network.LocationPopupPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.network.PlayerInfoUpdatePacket;
import ethan.hoenn.rnbrules.utils.enums.BoardType;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class LocationNotifier {

	private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fk-or])(.*)");
	private static final Map<UUID, Boolean> overlayDisabled = new HashMap<>();
	private static final Map<UUID, PlayerInfoCache> playerInfoCache = new HashMap<>();
	private static int updateTicker = 0;
	private static final int UPDATE_FREQUENCY = 20;

	private static class PlayerInfoCache {
		String location;
		int levelCap;
		int balance;
		boolean enabled;

		PlayerInfoCache(String location, int levelCap, int balance, boolean enabled) {
			this.location = location;
			this.levelCap = levelCap;
			this.balance = balance;
			this.enabled = enabled;
		}

		boolean hasChanged(String newLocation, int newLevelCap, int newBalance, boolean newEnabled) {
			return !this.location.equals(newLocation) || 
				   this.levelCap != newLevelCap || 
				   this.balance != newBalance || 
				   this.enabled != newEnabled;
		}
	}

	public static void notifyLocationChange(ServerPlayerEntity player, String locationName, String[] direction) {
		BoardType boardType = LocationManager.determineBoardType(locationName);

		String cleanLocationName = locationName;
		Matcher matcher = COLOR_CODE_PATTERN.matcher(locationName);
		if (matcher.matches()) {
			cleanLocationName = matcher.group(2);
		}

		LocationPopupPacket popupPacket = new LocationPopupPacket(cleanLocationName, boardType);
		PacketHandler.INSTANCE.sendTo(popupPacket, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

		updatePlayerInfoOverlay(player, locationName);
	}

	private static void updatePlayerInfoOverlay(ServerPlayerEntity player, String locationName) {
		UUID playerUUID = player.getUUID();
		if (overlayDisabled.getOrDefault(playerUUID, false)) return;

		int levelCap = LevelCapManager.get(player.getLevel()).getLevelCap(playerUUID);
		BankAccount account = BankAccountProxy.getBankAccount(player).orElse(null);
		int balance = account != null ? account.getBalance().intValue() : 0;

		String displayLocation = locationName;
		Matcher matcher = COLOR_CODE_PATTERN.matcher(locationName);
		if (matcher.matches()) {
			displayLocation = matcher.group(2);
		}

		PlayerInfoCache cached = playerInfoCache.get(playerUUID);
		if (cached == null || cached.hasChanged(displayLocation, levelCap, balance, true)) {
			playerInfoCache.put(playerUUID, new PlayerInfoCache(displayLocation, levelCap, balance, true));
			PlayerInfoUpdatePacket infoPacket = new PlayerInfoUpdatePacket(displayLocation, levelCap, balance, true);
			PacketHandler.INSTANCE.sendTo(infoPacket, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void enableOverlay(ServerPlayerEntity player) {
		overlayDisabled.put(player.getUUID(), false);

		LocationManager locationManager = LocationManager.get(player.getLevel());
		String locationName = locationManager.getPlayerCurrentLocation(player.getUUID());
		if (locationName != null) {
			updatePlayerInfoOverlay(player, locationName);
		}
	}

	public static void disableOverlay(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		overlayDisabled.put(playerUUID, true);

		PlayerInfoCache cached = playerInfoCache.get(playerUUID);
		if (cached == null || cached.enabled) {
			playerInfoCache.put(playerUUID, new PlayerInfoCache("", 0, 0, false));
			
			PlayerInfoUpdatePacket disablePacket = new PlayerInfoUpdatePacket("", 0, 0, false);
			PacketHandler.INSTANCE.sendTo(disablePacket, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void enableOverlay(ServerPlayerEntity player, String locationName) {
		overlayDisabled.put(player.getUUID(), false);
		updatePlayerInfoOverlay(player, locationName);
	}

	public static void updateAllOverlays(MinecraftServer server) {
		if (server == null) return;

		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			UUID playerUUID = player.getUUID();
			if (overlayDisabled.getOrDefault(playerUUID, false)) continue;

			LocationManager locationManager = LocationManager.get(player.getLevel());
			String locationName = locationManager.getPlayerCurrentLocation(playerUUID);
			if (locationName != null) {
				updatePlayerInfoOverlay(player, locationName);
			}
		}
	}

	public static void tickUpdate() {
		updateTicker++;
		if (updateTicker >= UPDATE_FREQUENCY) {
			updateTicker = 0;
			updateAllOverlays(ServerLifecycleHooks.getCurrentServer());
		}
	}

	public static void enableScoreboard(ServerPlayerEntity player) {
		enableOverlay(player);
	}

	public static void disableScoreboard(ServerPlayerEntity player) {
		disableOverlay(player);
	}

	public static void enableScoreboard(ServerPlayerEntity player, String locationName) {
		enableOverlay(player, locationName);
	}

	public static void updateAllScoreboards(MinecraftServer server) {
		updateAllOverlays(server);
	}

	public static void cleanupPlayerCache(UUID playerUUID) {
		overlayDisabled.remove(playerUUID);
		playerInfoCache.remove(playerUUID);
	}
}
