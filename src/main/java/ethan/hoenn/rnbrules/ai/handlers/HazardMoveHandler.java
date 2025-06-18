package ethan.hoenn.rnbrules.ai.handlers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import java.util.Random;

public class HazardMoveHandler {

	private final Random random;
	private final boolean debugMode;

	private static final float SCORE_HAZARD_FIRST_TURN_LOW = 8.0f;
	private static final float SCORE_HAZARD_FIRST_TURN_HIGH = 9.0f;
	private static final float SCORE_HAZARD_LATER_TURN_LOW = 6.0f;
	private static final float SCORE_HAZARD_LATER_TURN_HIGH = 7.0f;
	private static final float HAZARD_LOW_CHANCE = 0.25f;

	private static final float SCORE_STICKY_WEB_FIRST_TURN_LOW = 9.0f;
	private static final float SCORE_STICKY_WEB_FIRST_TURN_HIGH = 12.0f;
	private static final float SCORE_STICKY_WEB_LATER_TURN_LOW = 6.0f;
	private static final float SCORE_STICKY_WEB_LATER_TURN_HIGH = 9.0f;

	private static final float SCORE_HAZARD_ALREADY_EXISTS_PENALTY = -1.0f;

	private static final float SCORE_HAZARD_INEFFECTIVE_PENALTY = -1.0f;

	private static final int MAX_SPIKES_LAYERS = 3;
	private static final int MAX_TOXIC_SPIKES_LAYERS = 2;

	public HazardMoveHandler(boolean debugMode) {
		this.random = new Random();
		this.debugMode = debugMode;
	}

	public boolean shouldHandle(Attack attack) {
		return isHazardMove(attack);
	}

	public boolean handleMove(PixelmonWrapper pw, MoveChoice choice) {
		Attack attack = choice.attack;

		if (attack.getActualMove().isAttack(AttackRegistry.STEALTH_ROCK)) {
			return handleStealthRockScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.SPIKES)) {
			return handleSpikesScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.TOXIC_SPIKES)) {
			return handleToxicSpikesScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.STICKY_WEB)) {
			return handleStickyWebScoring(pw, choice);
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleStealthRockScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean hasExistingHazard = doesOpponentHaveStatus(pw, StealthRock.class);

		if (hasExistingHazard) {
			choice.weight = -20.0f;
			debugLog(moveName + " already active on opponent's side: -20.0");
			return true;
		}

		boolean opponentTeamWeakToRock = isOpponentTeamWeakToRock(pw);

		boolean isFirstTurn = isFirstTurn(pw);

		float score;
		if (isFirstTurn) {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_FIRST_TURN_LOW : SCORE_HAZARD_FIRST_TURN_HIGH;
			debugLog(moveName + " first turn score: " + score);
		} else {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_LATER_TURN_LOW : SCORE_HAZARD_LATER_TURN_HIGH;
			debugLog(moveName + " later turn score: " + score);
		}

		if (opponentTeamWeakToRock) {
			score += 1.0f;
			debugLog(moveName + " opponent team weak to Rock: +1.0");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSpikesScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean mostlyGroundedOpponents = hasOpponentTeamMostlyGrounded(pw);

		if (!mostlyGroundedOpponents) {
			choice.weight = SCORE_HAZARD_INEFFECTIVE_PENALTY;
			debugLog(moveName + " ineffective (opponent has many Flying/Levitating Pokémon): " + SCORE_HAZARD_INEFFECTIVE_PENALTY);
			return true;
		}

		int existingLayers = getOpponentSpikeLayer(pw);

		if (existingLayers >= MAX_SPIKES_LAYERS) {
			choice.weight = -20.0f;
			debugLog(moveName + " max layers already applied: -20.0");
			return true;
		}

		boolean isFirstTurn = isFirstTurn(pw);

		float score;
		if (isFirstTurn) {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_FIRST_TURN_LOW : SCORE_HAZARD_FIRST_TURN_HIGH;
			debugLog(moveName + " first turn score: " + score);
		} else {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_LATER_TURN_LOW : SCORE_HAZARD_LATER_TURN_HIGH;
			debugLog(moveName + " later turn score: " + score);
		}

		if (existingLayers > 0) {
			score += SCORE_HAZARD_ALREADY_EXISTS_PENALTY;
			debugLog(moveName + " already " + existingLayers + " layers, penalty: " + SCORE_HAZARD_ALREADY_EXISTS_PENALTY);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleToxicSpikesScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean mostlyGroundedOpponents = hasOpponentTeamMostlyGrounded(pw);

		boolean mostlyPoisonOrSteelOpponents = hasOpponentTeamMostlyPoisonOrSteel(pw);

		if (!mostlyGroundedOpponents || mostlyPoisonOrSteelOpponents) {
			choice.weight = SCORE_HAZARD_INEFFECTIVE_PENALTY;
			debugLog(moveName + " ineffective (opponent has many Flying/Levitating or Poison/Steel Pokémon): " + SCORE_HAZARD_INEFFECTIVE_PENALTY);
			return true;
		}

		int existingLayers = getOpponentToxicSpikeLayer(pw);

		if (existingLayers >= MAX_TOXIC_SPIKES_LAYERS) {
			choice.weight = -20.0f;
			debugLog(moveName + " max layers already applied: -20.0");
			return true;
		}

		boolean isFirstTurn = isFirstTurn(pw);

		float score;
		if (isFirstTurn) {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_FIRST_TURN_LOW : SCORE_HAZARD_FIRST_TURN_HIGH;
			debugLog(moveName + " first turn score: " + score);
		} else {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_HAZARD_LATER_TURN_LOW : SCORE_HAZARD_LATER_TURN_HIGH;
			debugLog(moveName + " later turn score: " + score);
		}

		if (existingLayers > 0) {
			score += SCORE_HAZARD_ALREADY_EXISTS_PENALTY;
			debugLog(moveName + " already " + existingLayers + " layers, penalty: " + SCORE_HAZARD_ALREADY_EXISTS_PENALTY);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleStickyWebScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean hasExistingHazard = doesOpponentHaveStatus(pw, StickyWeb.class);

		if (hasExistingHazard) {
			choice.weight = -20.0f;
			debugLog(moveName + " already active on opponent's side: -20.0");
			return true;
		}

		boolean mostlyGroundedOpponents = hasOpponentTeamMostlyGrounded(pw);

		if (!mostlyGroundedOpponents) {
			choice.weight = SCORE_HAZARD_INEFFECTIVE_PENALTY;
			debugLog(moveName + " ineffective (opponent has many Flying/Levitating Pokémon): " + SCORE_HAZARD_INEFFECTIVE_PENALTY);
			return true;
		}

		boolean isFirstTurn = isFirstTurn(pw);

		float score;
		if (isFirstTurn) {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_STICKY_WEB_FIRST_TURN_LOW : SCORE_STICKY_WEB_FIRST_TURN_HIGH;
			debugLog(moveName + " first turn score: " + score);
		} else {
			score = (random.nextFloat() < HAZARD_LOW_CHANCE) ? SCORE_STICKY_WEB_LATER_TURN_LOW : SCORE_STICKY_WEB_LATER_TURN_HIGH;
			debugLog(moveName + " later turn score: " + score);
		}

		if (hasOpponentTeamHighSpeed(pw)) {
			score += 1.0f;
			debugLog(moveName + " opponent team has high speed: +1.0");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[HazardMoveHandler] " + message);
		}
	}
}
