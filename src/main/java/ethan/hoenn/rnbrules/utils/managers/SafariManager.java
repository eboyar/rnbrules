package ethan.hoenn.rnbrules.utils.managers;

import ethan.hoenn.rnbrules.utils.notifications.LocationNotifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class SafariManager extends WorldSavedData implements ResetableManager {

	public static SafariManager instance;
	private static final String DATA_TAG = "safari";
	private static final String PLAYERS_TAG = "players";
	private static final String SAFARI_OBJECTIVE = "safari_info";

	public static final int MAX_STEPS = 1250;

	private final Map<UUID, SafariPlayerData> playerDataMap = new HashMap<>();

	public SafariManager() {
		super(DATA_TAG);
	}

	public static SafariManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(SafariManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerDataMap.remove(playerUUID) != null;
		if (hadData) {
			setDirty();
		}
		return hadData;
	}

	public SafariPlayerData getPlayerData(UUID playerUUID) {
		return playerDataMap.computeIfAbsent(playerUUID, uuid -> new SafariPlayerData());
	}

	public boolean isPlayerInSafari(UUID playerUUID) {
		return playerDataMap.containsKey(playerUUID) && playerDataMap.get(playerUUID).isActive();
	}

	public void startSafari(UUID playerUUID, int catches) {
		SafariPlayerData data = new SafariPlayerData(catches);
		playerDataMap.put(playerUUID, data);
		this.setDirty();
	}

	public void endSafari(UUID playerUUID) {
		playerDataMap.remove(playerUUID);
		this.setDirty();
	}

	public Collection<UUID> getActiveSafariPlayers() {
		return playerDataMap.keySet();
	}

	public void enableScoreboard(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		if (!isPlayerInSafari(playerUUID)) {
			return;
		}
		LocationNotifier.disableScoreboard(player);

		ServerScoreboard scoreboard = player.getServer().getScoreboard();

		ScoreObjective objective = scoreboard.getObjective(SAFARI_OBJECTIVE);
		if (objective != null) {
			scoreboard.removeObjective(objective);
		}

		objective = scoreboard.addObjective(
			SAFARI_OBJECTIVE,
			ScoreCriteria.DUMMY,
			new StringTextComponent(TextFormatting.GREEN + "" + TextFormatting.BOLD + "Safari Zone"),
			ScoreCriteria.RenderType.INTEGER
		);

		scoreboard.setDisplayObjective(1, objective);

		updateScoreboard(player);
	}

	public void updateScoreboard(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		if (!isPlayerInSafari(playerUUID)) {
			return;
		}

		SafariPlayerData data = getPlayerData(playerUUID);
		ServerScoreboard scoreboard = player.getServer().getScoreboard();
		ScoreObjective objective = scoreboard.getObjective(SAFARI_OBJECTIVE);

		if (objective == null) {
			enableScoreboard(player);
			objective = scoreboard.getObjective(SAFARI_OBJECTIVE);
		}

		Collection<Score> scores = scoreboard.getPlayerScores(objective);
		for (Score score : scores) {
			scoreboard.resetPlayerScore(score.getOwner(), objective);
		}

		addScoreboardLine(scoreboard, objective, TextFormatting.DARK_GREEN + "---------------", 8);

		int stepsRemaining = MAX_STEPS - data.getStepsTaken();
		TextFormatting stepColor = getColorForRatio(stepsRemaining);

		addScoreboardLine(scoreboard, objective, TextFormatting.GOLD + "Steps: ", 7);
		addScoreboardLine(scoreboard, objective, stepColor + String.format("%d/%d", stepsRemaining, MAX_STEPS), 6);

		addScoreboardLine(scoreboard, objective, "", 5);

		int catches = data.getRemainingCatches();
		int maxCatches = data.getMaxCatches();

		addScoreboardLine(scoreboard, objective, TextFormatting.GOLD + "Catches: ", 4);
		addScoreboardLine(scoreboard, objective, TextFormatting.GREEN + String.format("%d/%d", catches, maxCatches), 3);

		addScoreboardLine(scoreboard, objective, " ", 2);

		int timeElapsed = data.getTimePlayed();
		int minutes = timeElapsed / 60;
		int seconds = timeElapsed % 60;

		addScoreboardLine(scoreboard, objective, TextFormatting.YELLOW + "Time: ", 1);
		addScoreboardLine(scoreboard, objective, TextFormatting.AQUA + String.format("%d:%02d", minutes, seconds), 0);

		addScoreboardLine(scoreboard, objective, TextFormatting.DARK_GREEN + "---------------" + TextFormatting.RESET, -1);
	}

	private TextFormatting getColorForRatio(int remaining) {
		double ratio = (double) remaining / SafariManager.MAX_STEPS;

		if (ratio > 0.66) {
			return TextFormatting.GREEN;
		} else if (ratio > 0.33) {
			return TextFormatting.YELLOW;
		} else {
			return TextFormatting.RED;
		}
	}

	private void addScoreboardLine(ServerScoreboard scoreboard, ScoreObjective objective, String text, int score) {
		scoreboard.getOrCreatePlayerScore(text, objective).setScore(score);
	}

	public void disableScoreboard(ServerPlayerEntity player) {
		if (player.getServer() != null) {
			ServerScoreboard scoreboard = player.getServer().getScoreboard();
			ScoreObjective objective = scoreboard.getObjective(SAFARI_OBJECTIVE);

			if (objective != null) {
				scoreboard.removeObjective(objective);
			}
			LocationManager locationManager = LocationManager.get(player.getLevel());
			String currentLocation = locationManager.getPlayerCurrentLocation(player.getUUID());

			if (currentLocation != null) {
				LocationNotifier.enableScoreboard(player, currentLocation);
			}
		}
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerDataMap.clear();

		if (nbt.contains(PLAYERS_TAG)) {
			ListNBT playersList = nbt.getList(PLAYERS_TAG, 10);

			for (int i = 0; i < playersList.size(); i++) {
				CompoundNBT playerNBT = playersList.getCompound(i);
				UUID playerUUID = playerNBT.getUUID("UUID");

				SafariPlayerData playerData = new SafariPlayerData();
				playerData.deserializeNBT(playerNBT.getCompound("Data"));

				playerDataMap.put(playerUUID, playerData);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT playersList = new ListNBT();

		for (Map.Entry<UUID, SafariPlayerData> entry : playerDataMap.entrySet()) {
			CompoundNBT playerNBT = new CompoundNBT();
			playerNBT.putUUID("UUID", entry.getKey());
			playerNBT.put("Data", entry.getValue().serializeNBT());

			playersList.add(playerNBT);
		}

		nbt.put(PLAYERS_TAG, playersList);
		return nbt;
	}

	public static class SafariPlayerData {

		private int remainingCatches;
		private int maxCatches;
		private int stepsTaken;
		private int timePlayed;
		private boolean active;

		public SafariPlayerData() {
			this.remainingCatches = 0;
			this.maxCatches = 0;
			this.stepsTaken = 0;
			this.timePlayed = 0;
			this.active = false;
		}

		public SafariPlayerData(int catches) {
			this.remainingCatches = catches;
			this.maxCatches = catches;
			this.stepsTaken = 0;
			this.timePlayed = 0;
			this.active = true;
		}

		public int getTimePlayed() {
			return timePlayed;
		}

		public void incrementTimePlayed() {
			timePlayed++;
		}

		public int getRemainingCatches() {
			return remainingCatches;
		}

		public int getMaxCatches() {
			return maxCatches;
		}

		public void useCatch() {
			if (remainingCatches > 0) {
				remainingCatches--;
			}
		}

		public int getStepsTaken() {
			return stepsTaken;
		}

		public void incrementSteps() {
			stepsTaken++;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("RemainingCatches", remainingCatches);
			nbt.putInt("MaxCatches", maxCatches);
			nbt.putInt("StepsTaken", stepsTaken);
			nbt.putInt("TimePlayed", timePlayed);
			nbt.putBoolean("Active", active);
			return nbt;
		}

		public void deserializeNBT(CompoundNBT nbt) {
			remainingCatches = nbt.contains("RemainingCatches") ? nbt.getInt("RemainingCatches") : nbt.getInt("RemainingBalls");

			maxCatches = nbt.contains("MaxCatches") ? nbt.getInt("MaxCatches") : remainingCatches;
			stepsTaken = nbt.getInt("StepsTaken");
			timePlayed = nbt.contains("TimePlayed") ? nbt.getInt("TimePlayed") : 0;
			active = nbt.getBoolean("Active");

			if (nbt.contains("RemainingTime")) {}
		}
	}
}
