package ethan.hoenn.rnbrules.ai.handlers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.PokemonUtils.*;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StatusMoveHandler {

	private final Random random;
	private final boolean debugMode;
	private final GlobalStatusController gsc;

	// Paralysis-inducing
	private static final float SCORE_PARALYSIS_SPEED_ADVANTAGE = 8.0f;
	private static final float SCORE_PARALYSIS_DEFAULT = 7.0f;
	private static final float PARALYSIS_RANDOM_PENALTY_CHANCE = 0.5f;
	private static final float PARALYSIS_RANDOM_PENALTY = -1.0f;

	// Will-o-wisp
	private static final float SCORE_WILL_O_WISP_BASE = 6.0f;
	private static final float SCORE_WILL_O_WISP_PHYSICAL_BONUS = 1.0f;
	private static final float SCORE_WILL_O_WISP_HEX_BONUS = 1.0f;
	private static final float WILL_O_WISP_BONUS_CHANCE = 0.37f;

	// Sleep-inducing
	private static final float SCORE_SLEEP_MOVE_BASE = 6.0f;
	private static final float SCORE_SLEEP_VALID_TARGET_BONUS = 1.0f;
	private static final float SCORE_SLEEP_DREAM_EATER_SYNERGY_BONUS = 1.0f;
	private static final float SCORE_SLEEP_HEX_SYNERGY_BONUS = 1.0f;
	private static final float SLEEP_BONUS_CHECK_CHANCE = 0.25f;

	// Poisoning
	private static final float SCORE_POISON_MOVE_BASE = 6.0f;
	private static final float SCORE_POISON_SYNERGY_BONUS = 2.0f;
	private static final float SCORE_POISON_TOXIC_BONUS_SETUP = 2.0f;
	private static final float POISON_BONUS_CHECK_CHANCE = 0.38f;

	public StatusMoveHandler(GlobalStatusController gsc, boolean debugMode) {
		this.random = new Random();
		this.debugMode = debugMode;
		this.gsc = gsc;
	}

	public boolean shouldHandle(Attack attack) {
		return (isParalysisMove(attack) || attack.getActualMove().isAttack(AttackRegistry.WILL_O_WISP) || isSleepInducingMove(attack) || isPoisonInducingMove(attack));
	}

	public boolean handleMove(PixelmonWrapper pw, MoveChoice choice) {
		Attack attack = choice.attack;
		PixelmonWrapper target = !choice.targets.isEmpty() ? choice.targets.get(0) : null;

		if (target == null || target.isAlly(pw)) {
			return false;
		}

		if (isParalysisMove(attack)) {
			return handleParalysisMoveScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.WILL_O_WISP)) {
			return handleWillOWispScoring(pw, choice, target);
		}

		if (isSleepInducingMove(attack)) {
			return handleSleepMoveScoring(pw, choice, target);
		}

		if (isPoisonInducingMove(attack)) {
			return handlePoisonMoveScoring(pw, choice, target);
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleParalysisMoveScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean targetHasStatus = hasMajorStatus(target);

		if (targetHasStatus) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target already has status): -20.0");
			return true;
		}

		if ((choice.attack.getActualMove().isAttack(AttackRegistry.THUNDER_WAVE)) && (target.type.contains(Element.ELECTRIC) || target.type.contains(Element.GROUND))) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target is immune): -20.0");
			return true;
		}

		if (choice.attack.getActualMove().isAttack(AttackRegistry.STUN_SPORE) && target.type.contains(Element.GRASS)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (Grass-type immune to spores): -20.0");
			return true;
		}

		float score = SCORE_PARALYSIS_DEFAULT;

		boolean speedAdvantageAfterParalysis = checkSpeedAdvantageAfterParalysis(pw, target);
		boolean aiHasHexOrFlinch = hasHexOrFlinchMove(pw);
		boolean targetIsConfusedOrInfatuated = target.getStatuses().stream().anyMatch(status -> status instanceof Confusion || status instanceof Infatuated);

		if (speedAdvantageAfterParalysis || aiHasHexOrFlinch || targetIsConfusedOrInfatuated) {
			score = SCORE_PARALYSIS_SPEED_ADVANTAGE;

			String reasons = "";
			if (speedAdvantageAfterParalysis) reasons += "speed advantage after paralysis";
			if (aiHasHexOrFlinch) {
				if (!reasons.isEmpty()) reasons += ", ";
				reasons += "has Hex/flinch move";
			}
			if (targetIsConfusedOrInfatuated) {
				if (!reasons.isEmpty()) reasons += ", ";
				reasons += "target confused/infatuated";
			}

			debugLog(moveName + " meets special conditions (" + reasons + "): " + score);
		} else {
			debugLog(moveName + " default score (no special conditions): " + score);
		}

		if (random.nextFloat() < PARALYSIS_RANDOM_PENALTY_CHANCE) {
			score += PARALYSIS_RANDOM_PENALTY;
			debugLog(moveName + " random penalty applied (50% chance): " + PARALYSIS_RANDOM_PENALTY);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + choice.weight);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleWillOWispScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean targetHasStatus = hasMajorStatus(target);

		if (targetHasStatus) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target already has status): -20.0");
			return true;
		}

		if (target.type.contains(Element.FIRE)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (Fire-type is immune): -20.0");
			return true;
		}

		float score = SCORE_WILL_O_WISP_BASE;
		debugLog(moveName + " base score: " + score);

		if (random.nextFloat() < WILL_O_WISP_BONUS_CHANCE) {
			if (hasPhysicalAttackingMoves(target)) {
				score += SCORE_WILL_O_WISP_PHYSICAL_BONUS;
				debugLog(moveName + " physical attacker bonus: +" + SCORE_WILL_O_WISP_PHYSICAL_BONUS);
			}

			if (hasHexMove(pw) || (isDoubleBattle(pw.bc) && hasPartnerWithHex(pw))) {
				score += SCORE_WILL_O_WISP_HEX_BONUS;
				debugLog(moveName + " Hex synergy bonus: +" + SCORE_WILL_O_WISP_HEX_BONUS);
			}
		} else {
			debugLog(moveName + " no additional bonuses (~63% chance)");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + choice.weight);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSleepMoveScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SLEEP_MOVE_BASE;

		if (hasMajorStatus(target)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target already has status): -20.0");
			return true;
		}

		if (choice.attack.getActualMove().isAttack(AttackRegistry.DARK_VOID) && !pw.pokemon.getSpecies().is(com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies.DARKRAI)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (can only be used by Darkrai): -20.0");
			return true;
		}

		if (hasImmunityToSleep(target, gsc.getTerrain())) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target is immune to sleep): -20.0");
			return true;
		}

		if (random.nextFloat() < SLEEP_BONUS_CHECK_CHANCE) {
			debugLog(moveName + " checking for additional bonuses (25% chance triggered)");

			score += SCORE_SLEEP_VALID_TARGET_BONUS;
			debugLog(moveName + " valid sleep target bonus: +" + SCORE_SLEEP_VALID_TARGET_BONUS);

			if (hasMoveThatRequiresSleep(pw) && !hasSleepCounterMoves(target)) {
				score += SCORE_SLEEP_DREAM_EATER_SYNERGY_BONUS;
				debugLog(moveName + " Dream Eater/Nightmare synergy bonus: +" + SCORE_SLEEP_DREAM_EATER_SYNERGY_BONUS);
			}

			if (hasHexMove(pw) || (isDoubleBattle(pw.bc) && hasPartnerWithHex(pw))) {
				score += SCORE_SLEEP_HEX_SYNERGY_BONUS;
				debugLog(moveName + " Hex synergy bonus: +" + SCORE_SLEEP_HEX_SYNERGY_BONUS);
			}
		} else {
			debugLog(moveName + " no additional bonus checks (75% chance)");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handlePoisonMoveScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_POISON_MOVE_BASE;

		if (hasMajorStatus(target)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target already has status): -20.0");
			return true;
		}

		if (target.type.contains(Element.STEEL) || target.type.contains(Element.POISON)) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (target is immune): -20.0");
			return true;
		}

		boolean originalSimulateMode = pw.bc.simulateMode;
		pw.bc.simulateMode = true;
		boolean wouldKO = false;

		Attack savedAttack = pw.attack;
		List<PixelmonWrapper> savedTargets = pw.targets;

		try {
			List<PixelmonWrapper> targets = new ArrayList<>();
			targets.add(target);
			pw.setAttack(choice.attack, targets, false);

			MoveResults result = new MoveResults(target);
			choice.attack.saveAttack();
			choice.attack.use(pw, target, result);
			wouldKO = result.damage >= target.getHealth();
			choice.attack.restoreAttack();
		} finally {
			pw.attack = savedAttack;
			pw.targets = savedTargets;
			pw.bc.simulateMode = originalSimulateMode;
		}

		if (wouldKO) {
			debugLog(moveName + " would KO, using standard damage scoring");
			return false;
		}

		if (choice.attack.isAttack(AttackRegistry.TOXIC) && target.getHealthPercent() > 0.9f && hasPositiveStatChanges(target)) {
			score += SCORE_POISON_TOXIC_BONUS_SETUP;
			debugLog(moveName + " against setup sweeper bonus: +" + SCORE_POISON_TOXIC_BONUS_SETUP);
		}

		if (random.nextFloat() < POISON_BONUS_CHECK_CHANCE) {
			if (target.getHealthPercent() > 0.2f) {
				boolean hasHexMove = hasHexMove(pw);
				boolean hasVenomDrenchMove = pw.getMoveset().stream().anyMatch(attack -> attack != null && attack.getActualMove().isAttack(AttackRegistry.VENOM_DRENCH));
				boolean hasVenoshockMove = pw.getMoveset().stream().anyMatch(attack -> attack != null && attack.getActualMove().isAttack(AttackRegistry.VENOSHOCK));
				boolean hasMercilessAbility = pw.getAbility() instanceof com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Merciless;

				if (hasHexMove || hasVenomDrenchMove || hasVenoshockMove || hasMercilessAbility) {
					if (!hasAnyDamagingMoves(target)) {
						score += SCORE_POISON_SYNERGY_BONUS;
						debugLog(moveName + " synergy bonus (no damaging moves): +" + SCORE_POISON_SYNERGY_BONUS);
					}
				}
			} else {
				debugLog(moveName + " target below 20% HP, no extra bonus");
			}
		} else {
			debugLog(moveName + " no additional bonus checks (~62% chance)");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[StatusMoveHandler] " + message);
		}
	}
}
