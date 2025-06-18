package ethan.hoenn.rnbrules.ai.scorers;

import static com.pixelmonmod.pixelmon.battles.controller.BattlePriorityHelper.calculatePriority;
import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.PokemonUtils.*;

import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.AttackResult;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import java.util.List;
import java.util.Random;

public class GeneralMoveScorer {

	private final BattleController bc;
	private final Random random;
	private final boolean debugMode;

	// Constants for move scoring
	private static final float SCORE_HIGHEST_DAMAGING_NORMAL = 6.0f;
	private static final float SCORE_HIGHEST_DAMAGING_BOOSTED = 8.0f;

	// Kill bonus constants
	private static final float SCORE_FAST_KILL_BONUS = 6.0f;
	private static final float SCORE_SLOW_KILL_BONUS = 3.0f;
	private static final float SCORE_KILL_BOOST_ABILITY_BONUS = 1.0f;

	// Crit & type effectiveness
	private static final float SCORE_CRIT_SUPER_EFFECTIVE_BONUS = 1.0f;
	private static final float CRIT_BONUS_CHANCE = 0.5f;

	// Priority move when at risk bonus
	private static final float SCORE_PRIORITY_WHEN_AT_RISK_BONUS = 11.0f;

	// Ally targeting penalties
	private static final float SCORE_KO_ALLY_PENALTY = -20.0f;
	private static final float SCORE_DAMAGE_ALLY_PENALTY = -5.0f;

	// Chance for boosted scores
	private static final float BOOST_CHANCE = 0.2f;

	public GeneralMoveScorer(BattleController bc, boolean debugMode) {
		this.bc = bc;
		this.random = new Random();
		this.debugMode = debugMode;
	}

	public void scoreGeneralMove(PixelmonWrapper pw, MoveChoice choice) {
		List<PixelmonWrapper> allies = pw.getTeamPokemonExcludeSelf();

		Attack saveAttack = pw.attack;
		List<PixelmonWrapper> saveTargets = pw.targets;
		boolean originalSimulateMode = bc.simulateMode;

		bc.simulateMode = true;

		try {
			pw.setAttack(choice.attack, choice.targets, false);

			for (int j = 0; j < choice.targets.size(); ++j) {
				PixelmonWrapper target = choice.targets.get(j);
				MoveResults result = new MoveResults(target);
				result.priority = calculatePriority(pw);
				choice.result = result;

				choice.attack.saveAttack();
				choice.attack.use(pw, target, result);
				if (result.result == AttackResult.charging) {
					choice.attack.use(pw, target, result);
				}

				if (!choice.attack.isMax) {
					choice.attack.restoreAttack();
				}

				if (allies.contains(target)) {
					handleAllyTarget(choice, target, result);
				} else {
					handleOpponentTarget(pw, choice, target, result);
				}
			}
		} finally {
			pw.attack = saveAttack;
			pw.targets = saveTargets;
			bc.simulateMode = originalSimulateMode;
		}
	}

	private void handleAllyTarget(MoveChoice choice, PixelmonWrapper target, MoveResults result) {
		if (result.damage >= target.getHealth()) {
			choice.weight = SCORE_KO_ALLY_PENALTY;
			debugLog("Move " + choice.attack.getActualMove().getAttackName() + " would KO ally: " + choice.weight);
		} else {
			float damagePercent = target.getHealthPercent((float) result.damage);
			choice.weight = -SCORE_DAMAGE_ALLY_PENALTY;
			debugLog("Move " + choice.attack.getActualMove().getAttackName() + " would damage ally for " + damagePercent + "%, score: " + choice.weight);
		}
	}

	private void handleOpponentTarget(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target, MoveResults result) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isSpecialCase = isSpecialCaseDamagingMove(choice.attack);
		boolean boosted = random.nextFloat() < BOOST_CHANCE;

		choice.weight = 0.0f;

		int damage = getMaxDamageRoll(result);
		boolean wouldKO = damage >= target.getHealth();

		if (!isSpecialCase && damage > 0) {
			choice.weight = boosted ? SCORE_HIGHEST_DAMAGING_BOOSTED : SCORE_HIGHEST_DAMAGING_NORMAL;
			debugLog("Base score for " + moveName + (boosted ? " (boosted)" : "") + ": " + choice.weight);
		}

		if (wouldKO) {
			boolean hasPriority = choice.attack.getActualMove().getPriority(pw) > 0;
			boolean isFaster = isPokemonFaster(pw, target);
			boolean hasKillBoostAbility = hasKillBoostAbility(pw);

			if (isFaster || hasPriority) {
				choice.weight += SCORE_FAST_KILL_BONUS;
				debugLog("Fast kill bonus for " + moveName + ": +" + SCORE_FAST_KILL_BONUS);
			} else {
				choice.weight += SCORE_SLOW_KILL_BONUS;
				debugLog("Slow kill bonus for " + moveName + ": +" + SCORE_SLOW_KILL_BONUS);
			}

			if (hasKillBoostAbility) {
				choice.weight += SCORE_KILL_BOOST_ABILITY_BONUS;
				debugLog("Kill boost ability bonus for " + moveName + ": +" + SCORE_KILL_BOOST_ABILITY_BONUS);
			}
		}

		float typeEffectiveness = (float) choice.attack.getTypeEffectiveness(pw, target);
		if (typeEffectiveness > 1.0f && hasHighCritChance(choice.attack)) {
			if (random.nextFloat() < CRIT_BONUS_CHANCE) {
				choice.weight += SCORE_CRIT_SUPER_EFFECTIVE_BONUS;
				debugLog("High crit + super effective bonus for " + moveName + ": +" + SCORE_CRIT_SUPER_EFFECTIVE_BONUS);
			}
		}

		if (isPriorityMove(choice.attack, pw) && isAtRiskOfBeingKOd(pw, target, bc)) {
			choice.weight += SCORE_PRIORITY_WHEN_AT_RISK_BONUS;
			debugLog("Priority move defensive bonus when at risk of KO: +" + SCORE_PRIORITY_WHEN_AT_RISK_BONUS);
		}

		debugLog("Final score for " + moveName + ": " + choice.weight);
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[GeneralMoveScorer] " + message);
		}
	}
}
