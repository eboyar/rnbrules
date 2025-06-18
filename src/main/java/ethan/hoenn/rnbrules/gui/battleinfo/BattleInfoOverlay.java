package ethan.hoenn.rnbrules.gui.battleinfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.api.battles.BattleMode;
import com.pixelmonmod.pixelmon.client.gui.battles.BattleScreen;
import ethan.hoenn.rnbrules.network.BattleInfoPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class BattleInfoOverlay {

	private static final int ENVIRONMENT_COLOR = 0xEEEA90;
	private static final int PLAYER_TRAINER_COLOR = 0x90EE90;
	private static final int TEXT_COLOR = 0xFFFFFF;
	private static final int SHADOW_COLOR = 0x383838;
	private static final int SEPARATOR_COLOR = 0xCCCCCCFF;
	private static final int BACKGROUND_COLOR = 0xA0000000;

	private static final int LINE_HEIGHT = 12;
	private static final int PADDING = 8;
	private static final int LINE_WIDTH = 80;
	private static final int MAX_LINE_LENGTH = 20;
	private static final String SEPARATOR_LINE = "─────────";
	private static final float TEXT_SCALE = 0.9f;
	private static final int POSITION_OFFSET = 190;
	private static final int MAX_NAME_LENGTH = 16;

	private static final List<BattleMode> ACTIVE_BATTLE_MODES = Arrays.asList(
		BattleMode.WAITING,
		BattleMode.MAIN_MENU,
		BattleMode.ENFORCED_SWITCH,
		BattleMode.ENFORCED_REVIVE,
		BattleMode.APPLY_TO_POKEMON,
		BattleMode.CHOOSE_TARGETS,
		BattleMode.CHOOSE_ATTACK
	);

	private static boolean isActive = false;
	private static boolean isDoubleBattle = false;

	private static String enemyStats = "";
	private static String allyStats = "";
	private static boolean enemyIsMega = false;
	private static boolean allyIsMega = false;

	private static List<String> enemyStatsList = new ArrayList<>();
	private static List<Boolean> enemyIsMegaList = new ArrayList<>();
	private static List<String> allyStatsList = new ArrayList<>();
	private static List<Boolean> allyIsMegaList = new ArrayList<>();

	private static String environment = "";
	private static String playerName = "";
	private static String trainerName = "";
	private static String playerStatuses = "";
	private static String trainerStatuses = "";

	public static void enable() {
		isActive = true;
	}

	public static void disable() {
		isActive = false;
		isDoubleBattle = false;
	}

	public static void updateFromPacket(BattleInfoPacket packet) {
		if (!packet.inBattle) {
			isActive = false;
			return;
		}

		isDoubleBattle = packet.isDoubleBattle;
		environment = packet.environment;

		if (isDoubleBattle) {
			enemyStatsList = new ArrayList<>(packet.enemyStatsList);
			enemyIsMegaList = new ArrayList<>(packet.enemyIsMegaList);
			allyStatsList = new ArrayList<>(packet.allyStatsList);
			allyIsMegaList = new ArrayList<>(packet.allyIsMegaList);

			enemyStats = packet.enemyStats;
			allyStats = packet.allyStats;
			enemyIsMega = packet.enemyIsMega;
			allyIsMega = packet.allyIsMega;
		} else {
			enemyStats = packet.enemyStats;
			allyStats = packet.allyStats;
			enemyIsMega = packet.enemyIsMega;
			allyIsMega = packet.allyIsMega;
		}

		playerName = truncateName(packet.playerName);
		trainerName = truncateName(packet.trainerName);
		playerStatuses = packet.playerStatuses;
		trainerStatuses = packet.trainerStatuses;

		isActive = true;
	}

	private static String truncateName(String name) {
		if (name.length() > MAX_NAME_LENGTH) {
			return name.substring(0, MAX_NAME_LENGTH) + "...";
		}
		return name;
	}

	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		BattleMode currentMode = getCurrentBattleMode(minecraft);

		if (currentMode == null || !ACTIVE_BATTLE_MODES.contains(currentMode)) {
			BattleInfoOverlay.disable();
			return;
		}

		if (!isActive || event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
			return;
		}

		MatrixStack matrixStack = event.getMatrixStack();
		int screenWidth = event.getWindow().getGuiScaledWidth();
		int screenHeight = event.getWindow().getGuiScaledHeight();

		renderBattleStats(matrixStack, screenWidth, screenHeight);
		renderTopRightInfo(matrixStack, screenWidth);
	}

	private void renderBattleStats(MatrixStack matrixStack, int screenWidth, int screenHeight) {
		int enemyBoxY = 39;
		int allyBoxY = screenHeight - 130;

		if (isDoubleBattle) {
			renderDoubleBattleStats(matrixStack, screenWidth, enemyBoxY, allyBoxY);
		} else {
			renderSingleBattleStats(matrixStack, screenWidth, enemyBoxY, allyBoxY);
		}
	}

	private void renderSingleBattleStats(MatrixStack matrixStack, int screenWidth, int enemyBoxY, int allyBoxY) {
		renderStats(matrixStack, enemyStats, enemyIsMega, 59, enemyBoxY, false);
		renderStats(matrixStack, allyStats, allyIsMega, screenWidth - 146, allyBoxY, true);
	}

	private void renderDoubleBattleStats(MatrixStack matrixStack, int screenWidth, int enemyBoxY, int allyBoxY) {
		int singleEnemyX = 59;
		int singleAllyX = screenWidth - 146;

		// Position 0 (left) enemy stats
		if (!enemyStatsList.isEmpty() && !enemyStatsList.get(0).isEmpty()) {
			renderStats(matrixStack, enemyStatsList.get(0), enemyIsMegaList.get(0), singleEnemyX, enemyBoxY, false);
		}

		// Position 1 (right) enemy stats
		if (enemyStatsList.size() > 1 && !enemyStatsList.get(1).isEmpty()) {
			renderStats(matrixStack, enemyStatsList.get(1), enemyIsMegaList.get(1), singleEnemyX + POSITION_OFFSET - 11, enemyBoxY, false);
		}

		// Position 0 (left) ally stats
		if (!allyStatsList.isEmpty() && !allyStatsList.get(0).isEmpty()) {
			renderStats(matrixStack, allyStatsList.get(0), allyIsMegaList.get(0), singleAllyX - POSITION_OFFSET - 1, allyBoxY, true);
		}

		// Position 1 (right) ally stats
		if (allyStatsList.size() > 1 && !allyStatsList.get(1).isEmpty()) {
			renderStats(matrixStack, allyStatsList.get(1), allyIsMegaList.get(1), singleAllyX - 7, allyBoxY, true);
		}
	}

	private void renderTopRightInfo(MatrixStack matrixStack, int screenWidth) {
		List<String> lines = new ArrayList<>();
		List<Integer> lineColors = new ArrayList<>();

		int topRightY = 0;

		boolean hasEnvironment = !environment.isEmpty();
		boolean hasPlayerStatus = !playerStatuses.isEmpty();
		boolean hasTrainerStatus = !trainerStatuses.isEmpty();
		boolean hasContent = hasEnvironment || hasPlayerStatus || hasTrainerStatus;

		if (hasContent) {
			addSeparatorLine(lines, lineColors);
		}

		if (hasEnvironment) {
			addEnvironmentInfo(lines, lineColors);

			if (hasPlayerStatus || hasTrainerStatus) {
				addSeparatorLine(lines, lineColors);
			}
		}

		if (hasPlayerStatus) {
			addPlayerInfo(lines, lineColors);

			if (hasTrainerStatus) {
				addSeparatorLine(lines, lineColors);
			}
		}

		if (hasTrainerStatus) {
			addTrainerInfo(lines, lineColors);
		}

		if (hasContent) {
			addSeparatorLine(lines, lineColors);
		}

		renderInfoBox(matrixStack, lines, lineColors, screenWidth, topRightY);
	}

	private void addEnvironmentInfo(List<String> lines, List<Integer> lineColors) {
		lines.add("Environment");
		lineColors.add(ENVIRONMENT_COLOR);

		lines.add(environment);
		lineColors.add(TEXT_COLOR);
	}

	private void addPlayerInfo(List<String> lines, List<Integer> lineColors) {
		if (!playerName.isEmpty()) {
			lines.add(playerName);
			lineColors.add(PLAYER_TRAINER_COLOR);
		} else {
			lines.add("Your Team");
			lineColors.add(PLAYER_TRAINER_COLOR);
		}

		String[] statusEntries = playerStatuses.split(";");
		for (String status : statusEntries) {
			lines.add(status.trim());
			lineColors.add(TEXT_COLOR);
		}
	}

	private void addTrainerInfo(List<String> lines, List<Integer> lineColors) {
		if (!trainerName.isEmpty()) {
			lines.add(trainerName);
			lineColors.add(PLAYER_TRAINER_COLOR);
		} else {
			lines.add("Enemy Team");
			lineColors.add(PLAYER_TRAINER_COLOR);
		}

		String[] statusEntries = trainerStatuses.split(";");
		for (String status : statusEntries) {
			lines.add(status.trim());
			lineColors.add(TEXT_COLOR);
		}
	}

	private void addSeparatorLine(List<String> lines, List<Integer> lineColors) {
		lines.add(SEPARATOR_LINE);
		lineColors.add(SEPARATOR_COLOR);
	}

	private void renderInfoBox(MatrixStack matrixStack, List<String> lines, List<Integer> lineColors, int screenWidth, int topRightY) {
		if (lines.isEmpty()) {
			return;
		}

		int backgroundWidth = (int) (LINE_WIDTH * 1.3);
		int backgroundHeight = (lines.size() * LINE_HEIGHT) + (PADDING * 2);
		int backgroundX = screenWidth - backgroundWidth;

		AbstractGui.fill(matrixStack, backgroundX, topRightY, screenWidth, topRightY + backgroundHeight, BACKGROUND_COLOR);

		int currentY = topRightY + PADDING;
		int contentCenterX = backgroundX + (backgroundWidth / 2);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int color = lineColors.get(i);

			if (line.equals(SEPARATOR_LINE)) {
				renderSeparator(matrixStack, contentCenterX, currentY);
			} else {
				renderTextCentered(matrixStack, line, contentCenterX, currentY, color);
			}

			currentY += LINE_HEIGHT;
		}
	}

	private void renderSeparator(MatrixStack matrixStack, int contentCenterX, int currentY) {
		int separatorHeight = 2;
		int lineX = contentCenterX - (LINE_WIDTH / 2);
		int lineY = currentY + 3;

		AbstractGui.fill(matrixStack, lineX, lineY, lineX + LINE_WIDTH, lineY + separatorHeight, SEPARATOR_COLOR);
	}

	private void renderTextCentered(MatrixStack matrixStack, String text, int centerX, int y, int color) {
		int textWidth = Minecraft.getInstance().font.width(text);
		int textX = centerX - (textWidth / 2);
		drawTextWithShadow(matrixStack, text, textX, y, color, SHADOW_COLOR, 1.0f);
	}

	private void renderStats(MatrixStack matrixStack, String stats, boolean isMega, int x, int y, boolean isAlly) {
		String[] statLines = formatStats(stats);
		int lines = statLines.length;
		int currentY = y;

		if (isAlly) {
			renderAllyStats(matrixStack, statLines, isMega, x, y);
		} else {
			renderEnemyStats(matrixStack, statLines, isMega, x, y);
		}
	}

	private void renderAllyStats(MatrixStack matrixStack, String[] statLines, boolean isMega, int x, int y) {
		int lines = statLines.length;
		int currentY = y - (LINE_HEIGHT * (lines + (isMega ? 1 : 0) - 1));

		if (isMega) {
			drawTextWithShadow(matrixStack, "Mega Evolved", x, currentY, TEXT_COLOR, SHADOW_COLOR, TEXT_SCALE);
			currentY += LINE_HEIGHT;
		}

		// stats in reverse order (bottom to top) to grow correctly
		for (int i = lines - 1; i >= 0; i--) {
			drawTextWithShadow(matrixStack, statLines[i], x, currentY, TEXT_COLOR, SHADOW_COLOR, TEXT_SCALE);
			currentY += LINE_HEIGHT;
		}
	}

	private void renderEnemyStats(MatrixStack matrixStack, String[] statLines, boolean isMega, int x, int y) {
		int currentY = y;

		for (String line : statLines) {
			drawTextWithShadow(matrixStack, line, x, currentY, TEXT_COLOR, SHADOW_COLOR, TEXT_SCALE);
			currentY += LINE_HEIGHT;
		}

		if (isMega) {
			drawTextWithShadow(matrixStack, "Mega Evolved", x, currentY, TEXT_COLOR, SHADOW_COLOR, TEXT_SCALE);
		}
	}

	private void drawTextWithShadow(MatrixStack matrixStack, String text, float x, float y, int color, int shadowColor, float scale) {
		Minecraft.getInstance().font.draw(matrixStack, text, x + scale, y + scale, shadowColor);
		Minecraft.getInstance().font.draw(matrixStack, text, x, y, color);
	}

	private String[] formatStats(String stats) {
		if (stats == null || stats.trim().isEmpty()) {
			return new String[0];
		}

		String[] components = stats.split(" ");
		StringBuilder lineBuilder = new StringBuilder();
		int currentLineLength = 0;
		List<String> finalLines = new ArrayList<>();

		for (String component : components) {
			int componentLength = component.length() + (lineBuilder.length() > 0 ? 1 : 0);
			if (currentLineLength + componentLength > MAX_LINE_LENGTH) {
				finalLines.add(lineBuilder.toString());
				lineBuilder = new StringBuilder();
				currentLineLength = 0;
			}

			if (lineBuilder.length() > 0) {
				lineBuilder.append(" ");
			}
			lineBuilder.append(component);
			currentLineLength += component.length() + 1;
		}

		if (lineBuilder.length() > 0) {
			finalLines.add(lineBuilder.toString());
		}

		return finalLines.toArray(new String[0]);
	}

	public static BattleMode getCurrentBattleMode(Minecraft minecraft) {
		Screen currentScreen = minecraft.screen;

		if (currentScreen instanceof BattleScreen) {
			BattleScreen battleScreen = (BattleScreen) currentScreen;
			return battleScreen.bm.getMode();
		}

		return null;
	}
}
