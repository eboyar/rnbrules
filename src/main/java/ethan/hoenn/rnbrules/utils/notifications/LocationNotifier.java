package ethan.hoenn.rnbrules.utils.notifications;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import ethan.hoenn.rnbrules.network.LocationPopupPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.enums.BoardType;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class LocationNotifier {

	private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fk-or])(.*)");
	private static final Map<UUID, Boolean> scoreboardDisabled = new HashMap<>();
	private static final Map<UUID, Map<Integer, String>> playerLastDynamicLineTexts = new HashMap<>();
	private static int updateTicker = 0;
	private static final int UPDATE_FREQUENCY = 20;

	public static void notifyLocationChange(ServerPlayerEntity player, String locationName, String[] direction) {
		BoardType boardType = LocationManager.determineBoardType(locationName);

		String cleanLocationName = locationName;
		Matcher matcher = COLOR_CODE_PATTERN.matcher(locationName);
		if (matcher.matches()) {
			cleanLocationName = matcher.group(2);
		}

		LocationPopupPacket packet = new LocationPopupPacket(cleanLocationName, boardType);
		PacketHandler.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

		updatePlayerScoreboard(player, locationName);
	}

	private static TextFormatting getFormattingFromCode(String code) {
		switch (code) {
			case "0":
				return TextFormatting.BLACK;
			case "1":
				return TextFormatting.DARK_BLUE;
			case "2":
				return TextFormatting.DARK_GREEN;
			case "3":
				return TextFormatting.DARK_AQUA;
			case "4":
				return TextFormatting.DARK_RED;
			case "5":
				return TextFormatting.DARK_PURPLE;
			case "6":
				return TextFormatting.GOLD;
			case "7":
				return TextFormatting.GRAY;
			case "8":
				return TextFormatting.DARK_GRAY;
			case "9":
				return TextFormatting.BLUE;
			case "a":
				return TextFormatting.GREEN;
			case "b":
				return TextFormatting.AQUA;
			case "c":
				return TextFormatting.RED;
			case "d":
				return TextFormatting.LIGHT_PURPLE;
			case "e":
				return TextFormatting.YELLOW;
			case "f":
				return TextFormatting.WHITE;
			case "k":
				return TextFormatting.OBFUSCATED;
			case "l":
				return TextFormatting.BOLD;
			case "m":
				return TextFormatting.STRIKETHROUGH;
			case "n":
				return TextFormatting.UNDERLINE;
			case "o":
				return TextFormatting.ITALIC;
			case "r":
				return TextFormatting.RESET;
			default:
				return TextFormatting.GREEN;
		}
	}

	private static void updatePlayerScoreboard(ServerPlayerEntity player, String locationName) {
		if (scoreboardDisabled.getOrDefault(player.getUUID(), false)) return;

		Scoreboard scoreboard = player.getLevel().getScoreboard();
		String objectiveName = "PixelmonRNB";
		ScoreObjective objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			ITextComponent title = new StringTextComponent("Pixelmon ")
				.withStyle(style -> style.withItalic(true).withBold(true).withColor(TextFormatting.WHITE))
				.append(new StringTextComponent("Run & Bun").withStyle(style -> style.withItalic(true).withBold(true).withColor(TextFormatting.GREEN)));

			objective = scoreboard.addObjective(objectiveName, ScoreCriteria.DUMMY, title, ScoreCriteria.RenderType.INTEGER);
			scoreboard.setDisplayObjective(1, objective);
		}

		Map<Integer, String> lastTexts = playerLastDynamicLineTexts.computeIfAbsent(player.getUUID(), k -> new HashMap<>());

		int levelCap = LevelCapManager.get(player.getLevel()).getLevelCap(player.getUUID());
		BankAccount account = BankAccountProxy.getBankAccount(player).orElse(null);
		int balance = account != null ? account.getBalance().intValue() : 0;

		String displayLocation = locationName;
		TextFormatting locationColor = TextFormatting.GREEN;
		Matcher matcher = COLOR_CODE_PATTERN.matcher(locationName);
		if (matcher.matches()) {
			displayLocation = matcher.group(2);
			locationColor = getFormattingFromCode(matcher.group(1));
		}

		addScoreboardLine(scoreboard, objective, TextFormatting.GRAY + "-----------------", 7);

		String newLocationLineText = locationColor + displayLocation;
		String oldLocationLineText = lastTexts.get(6);
		if (oldLocationLineText != null && !oldLocationLineText.equals(newLocationLineText)) {
			scoreboard.resetPlayerScore(oldLocationLineText, objective);
		}
		addScoreboardLine(scoreboard, objective, newLocationLineText, 6);
		lastTexts.put(6, newLocationLineText);

		addScoreboardLine(scoreboard, objective, "", 5);

		String newLevelCapLineText = TextFormatting.WHITE + "Level Cap: " + TextFormatting.GOLD + levelCap;
		String oldLevelCapLineText = lastTexts.get(4);
		if (oldLevelCapLineText != null && !oldLevelCapLineText.equals(newLevelCapLineText)) {
			scoreboard.resetPlayerScore(oldLevelCapLineText, objective);
		}
		addScoreboardLine(scoreboard, objective, newLevelCapLineText, 4);
		lastTexts.put(4, newLevelCapLineText);

		addScoreboardLine(scoreboard, objective, " ", 3);

		String newBalanceLineText = TextFormatting.WHITE + "Balance: " + TextFormatting.GOLD + "$" + balance;
		String oldBalanceLineText = lastTexts.get(2);
		if (oldBalanceLineText != null && !oldBalanceLineText.equals(newBalanceLineText)) {
			scoreboard.resetPlayerScore(oldBalanceLineText, objective);
		}
		addScoreboardLine(scoreboard, objective, newBalanceLineText, 2);
		lastTexts.put(2, newBalanceLineText);

		addScoreboardLine(scoreboard, objective, TextFormatting.GRAY + "----------------- ", 1);
	}

	private static void addScoreboardLine(Scoreboard scoreboard, ScoreObjective objective, String text, int score) {
		scoreboard.getOrCreatePlayerScore(text, objective).setScore(score);
	}

	public static void enableScoreboard(ServerPlayerEntity player) {
		scoreboardDisabled.put(player.getUUID(), false);
	}

	public static void disableScoreboard(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		scoreboardDisabled.put(playerUUID, true);
		playerLastDynamicLineTexts.remove(playerUUID);

		Scoreboard scoreboard = player.getLevel().getScoreboard();
		ScoreObjective objective = scoreboard.getObjective("PixelmonRNB");
		if (objective != null) {
			scoreboard.removeObjective(objective);
		}
	}

	public static void enableScoreboard(ServerPlayerEntity player, String locationName) {
		scoreboardDisabled.put(player.getUUID(), false);
		updatePlayerScoreboard(player, locationName);
	}

	public static void updateAllScoreboards(MinecraftServer server) {
		if (server == null) return;

		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			UUID playerUUID = player.getUUID();
			if (scoreboardDisabled.getOrDefault(playerUUID, false)) continue;

			LocationManager locationManager = LocationManager.get(player.getLevel());
			String locationName = locationManager.getPlayerCurrentLocation(playerUUID);
			if (locationName != null) {
				updatePlayerScoreboard(player, locationName);
			}
		}
	}

	public static void tickUpdate() {
		updateTicker++;
		if (updateTicker >= UPDATE_FREQUENCY) {
			updateTicker = 0;
			updateAllScoreboards(ServerLifecycleHooks.getCurrentServer());
		}
	}
}
