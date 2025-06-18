package ethan.hoenn.rnbrules.ai.scorers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;

import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import ethan.hoenn.rnbrules.ai.utils.SwitchUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SwitchMoveScorer {

	private final Random random;
	private final boolean debugMode;

	private static final float SWITCH_SCORE_FAST_OHKO = 5.0f;
	private static final float SWITCH_SCORE_SLOW_OHKO_SAFE = 4.0f;
	private static final float SWITCH_SCORE_FAST_DAMAGE_ADVANTAGE = 3.0f;
	private static final float SWITCH_SCORE_SLOW_DAMAGE_ADVANTAGE = 2.0f;
	private static final float SWITCH_SCORE_SPEED_ADVANTAGE = 1.0f;
	private static final float SWITCH_SCORE_DEFAULT = 0.0f;
	private static final float SWITCH_SCORE_OHKO_VULNERABLE = -1.0f;

	private static final float SWITCH_SCORE_DITTO = 2.0f;
	private static final float SWITCH_SCORE_COUNTER_MONS = 2.0f;

	public SwitchMoveScorer(boolean debugMode) {
		this.random = new Random();
		this.debugMode = debugMode;
	}

	public UUID shouldSwitch(PixelmonWrapper pw, List<UUID> choices, List<MoveChoice> attackChoices) {
		if (choices == null || choices.isEmpty()) {
			return null;
		}

		if (isDoubleBattle(pw.bc)) {
			return checkPerishSongSwitch(pw, choices);
		}

		UUID perishSwitch = checkPerishSongSwitch(pw, choices);
		if (perishSwitch != null) {
			return perishSwitch;
		}

		if (pw.getHealthPercent() < 0.5f) {
			debugLog("AI health below 50%, not switching");
			return null;
		}

		boolean allMovesNegative = true;
		for (MoveChoice choice : attackChoices) {
			if (choice.weight >= 0.0f) {
				allMovesNegative = false;
				break;
			}
		}

		if (!allMovesNegative) {
			debugLog("AI has moves with non-negative scores, not switching");
			return null;
		}

		List<PixelmonWrapper> opponents = pw.getOpponentPokemon();
		if (opponents.isEmpty()) {
			return null;
		}
		PixelmonWrapper opponent = opponents.get(0);

		List<UUID> validCandidates = new ArrayList<>();

		for (UUID partyUUID : choices) {
			if (isValidSwitchCandidate(pw, partyUUID, opponent)) {
				validCandidates.add(partyUUID);
			}
		}

		if (validCandidates.isEmpty()) {
			debugLog("No valid switch candidates found");
			return null;
		}

		if (random.nextFloat() < 0.5f) {
			debugLog("Hard switch triggered - all moves negative, valid candidates available");

			return mustSwitch(pw, validCandidates);
		}

		debugLog("Hard switch not triggered (random roll failed)");
		return null;
	}

	public UUID mustSwitch(PixelmonWrapper pw, List<UUID> choices) {
		if (choices == null || choices.isEmpty()) {
			return null;
		}

		UUID bestChoice = null;
		float bestScore = Float.NEGATIVE_INFINITY;

		int controlledIndex = pw.getControlledIndex();
		BattleParticipant bp = pw.getParticipant();
		BattleController bc = pw.bc;

		List<PixelmonWrapper> opponents = pw.getOpponentPokemon();
		if (opponents.isEmpty()) {
			return choices.get(random.nextInt(choices.size()));
		}

		for (UUID partyUUID : choices) {
			bc.simulateMode = true;

			try {
				pw.newPokemonUUID = partyUUID;
				PixelmonWrapper switchPokemon = pw.doSwitch();

				if (this.validateSwitch(switchPokemon)) {
					SwitchUtils.applyStatModifications(switchPokemon, pw.getStatuses(), bc, debugMode);

					float matchupScore;
					if (opponents.size() > 1) {
						matchupScore = 0.0f;
						for (PixelmonWrapper opponent : opponents) {
							if (opponent != null && opponent.getHealth() > 0) {
								float score = evaluateSingleMatchup(switchPokemon, opponent, pw);
								matchupScore = Math.max(matchupScore, score);

								debugLog("Double battle - evaluating " + switchPokemon.getSpecies().getLocalizedName() + " vs " + opponent.getSpecies().getLocalizedName() + ": Score=" + score);
							}
						}
					} else {
						matchupScore = evaluateSingleMatchup(switchPokemon, opponents.get(0), pw);
					}

					if (matchupScore > bestScore) {
						bestScore = matchupScore;
						bestChoice = partyUUID;
					}

					debugLog("Final evaluation for " + switchPokemon.getSpecies().getLocalizedName() + ": Best score=" + matchupScore);

					switchPokemon.getBattleStats().setBattleStatsForCurrentForm();
				}
			} finally {
				this.resetSwitchSimulation(pw, controlledIndex, pw, bp);
				bc.simulateMode = false;
			}
		}

		if (bestChoice != null) {
			return bestChoice;
		}

		return choices.get(random.nextInt(choices.size()));
	}

	public float evaluateSingleMatchup(PixelmonWrapper switchPokemon, PixelmonWrapper opponent, PixelmonWrapper preSwitchPokemon) {
		int maxAIDamage = calculateMaxDamage(switchPokemon, opponent);
		int maxOpponentDamage = calculateMaxDamage(opponent, switchPokemon);

		maxOpponentDamage = applyDamageReductionStatues(maxOpponentDamage, preSwitchPokemon.getStatuses(), isDoubleBattle(preSwitchPokemon.bc));

		float aiDamagePercent = opponent.getMaxHealth() > 0 ? (maxAIDamage * 100.0f) / opponent.getMaxHealth() : 0;
		float opponentDamagePercent = switchPokemon.getMaxHealth() > 0 ? (maxOpponentDamage * 100.0f) / switchPokemon.getMaxHealth() : 0;

		boolean aiCanOHKO = maxAIDamage >= opponent.getHealth();
		boolean opponentCanOHKO = maxOpponentDamage >= switchPokemon.getHealth();
		boolean aiDamageAdvantage = aiDamagePercent > opponentDamagePercent;
		boolean aiIsFaster = isPokemonFaster(switchPokemon, opponent);

		float matchupScore;

		if (aiCanOHKO && aiIsFaster) {
			matchupScore = SWITCH_SCORE_FAST_OHKO;
		} else if (aiCanOHKO && !opponentCanOHKO) {
			matchupScore = SWITCH_SCORE_SLOW_OHKO_SAFE;
		} else if (aiDamageAdvantage && aiIsFaster) {
			matchupScore = SWITCH_SCORE_FAST_DAMAGE_ADVANTAGE;
		} else if (aiDamageAdvantage) {
			matchupScore = SWITCH_SCORE_SLOW_DAMAGE_ADVANTAGE;
		} else if (switchPokemon.pokemon.getSpecies().is(PixelmonSpecies.DITTO)) {
			matchupScore = SWITCH_SCORE_DITTO;
		} else if ((switchPokemon.pokemon.getSpecies().is(PixelmonSpecies.WOBBUFFET) || switchPokemon.pokemon.getSpecies().is(PixelmonSpecies.WYNAUT)) && !opponentCanOHKO) {
			matchupScore = SWITCH_SCORE_COUNTER_MONS;
		} else if (aiIsFaster) {
			matchupScore = SWITCH_SCORE_SPEED_ADVANTAGE;
		} else if (opponentCanOHKO) {
			matchupScore = SWITCH_SCORE_OHKO_VULNERABLE;
		} else {
			matchupScore = SWITCH_SCORE_DEFAULT;
		}

		debugLog(
			"Matchup " +
			switchPokemon.getSpecies().getLocalizedName() +
			" vs " +
			opponent.getSpecies().getLocalizedName() +
			": AI damage=" +
			maxAIDamage +
			" (" +
			aiDamagePercent +
			"%), " +
			"Opp damage=" +
			maxOpponentDamage +
			" (" +
			opponentDamagePercent +
			"%), " +
			"AI HP=" +
			switchPokemon.getHealth() +
			"/" +
			switchPokemon.getMaxHealth() +
			", " +
			"Opp HP=" +
			opponent.getHealth() +
			"/" +
			opponent.getMaxHealth() +
			", " +
			"Score=" +
			matchupScore
		);

		return matchupScore;
	}

	private int calculateMaxDamage(PixelmonWrapper attacker, PixelmonWrapper defender) {
		int maxDamage = 0;

		for (Attack attack : attacker.getMoveset()) {
			if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
				int damage = simulateAttackDamage(attacker, defender, attack);
				if (damage > maxDamage) {
					maxDamage = damage;
				}
			}
		}

		return maxDamage;
	}

	private int simulateAttackDamage(PixelmonWrapper attacker, PixelmonWrapper defender, Attack attack) {
		boolean originalSimulateMode = defender.bc.simulateMode;
		defender.bc.simulateMode = true;
		int damage;

		Attack savedAttack = attacker.attack;
		List<PixelmonWrapper> savedTargets = attacker.targets;

		try {
			List<PixelmonWrapper> targets = new ArrayList<>();
			targets.add(defender);
			attacker.setAttack(attack, targets, false);

			MoveResults result = new MoveResults(defender);
			attack.saveAttack();
			attack.use(attacker, defender, result);
			damage = result.damage;
			attack.restoreAttack();
		} finally {
			attacker.attack = savedAttack;
			attacker.targets = savedTargets;
			defender.bc.simulateMode = originalSimulateMode;
		}

		return damage;
	}

	private int applyDamageReductionStatues(int damage, List<StatusBase> teamStatuses, boolean isDouble) {
		int reducedDamage = damage;
		if (teamStatuses.stream().anyMatch(status -> status instanceof AuroraVeil)) {
			if (isDouble) {
				reducedDamage = (reducedDamage * 2732) / 4096;
			} else reducedDamage /= 2;
		}
		return reducedDamage;
	}

	public boolean validateSwitch(PixelmonWrapper nextPokemon) {
		return true;
	}

	private void resetSwitchSimulation(PixelmonWrapper current, int controlledIndex, PixelmonWrapper simulated, BattleParticipant bp) {
		bp.controlledPokemon.set(controlledIndex, current);
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[SwitchMoveScorer] " + message);
		}
	}

	private UUID checkPerishSongSwitch(PixelmonWrapper pw, List<UUID> choices) {
		Perish perishStatus = null;
		for (StatusBase status : pw.getStatuses()) {
			if (status instanceof Perish) {
				perishStatus = (Perish) status;
				break;
			}
		}

		if (perishStatus != null) {
			debugLog("Perish Song detected with " + perishStatus.effectTurns + " turns remaining");

			if (perishStatus.effectTurns <= 1) {
				debugLog("Perish Song switch triggered (1 turn or less remaining)");
				return mustSwitch(pw, choices);
			}
		}

		return null;
	}

	private boolean isValidSwitchCandidate(PixelmonWrapper currentPokemon, UUID candidateUUID, PixelmonWrapper opponent) {
		BattleController bc = currentPokemon.bc;
		boolean originalSimulateMode = bc.simulateMode;
		bc.simulateMode = true;

		try {
			currentPokemon.newPokemonUUID = candidateUUID;
			PixelmonWrapper candidate = currentPokemon.doSwitch();

			if (!validateSwitch(candidate)) {
				return false;
			}

			SwitchUtils.applyStatModifications(candidate, currentPokemon.getStatuses(), bc, debugMode);

			boolean candidateIsFaster = isPokemonFaster(candidate, opponent);
			int opponentDamageToCandidate = calculateMaxDamage(opponent, candidate);

			if (candidateIsFaster) {
				boolean wouldBeOHKO = opponentDamageToCandidate >= candidate.getHealth();
				debugLog("Candidate " + candidate.getSpecies().getLocalizedName() + " is faster. OHKO check: " + wouldBeOHKO + " (damage: " + opponentDamageToCandidate + "/" + candidate.getHealth() + ")");
				return !wouldBeOHKO;
			} else {
				boolean wouldBe2HKO = opponentDamageToCandidate * 2 >= candidate.getHealth();
				debugLog(
					"Candidate " + candidate.getSpecies().getLocalizedName() + " is slower. 2HKO check: " + wouldBe2HKO + " (2x damage: " + (opponentDamageToCandidate * 2) + "/" + candidate.getHealth() + ")"
				);
				return !wouldBe2HKO;
			}
		} finally {
			this.resetSwitchSimulation(currentPokemon, currentPokemon.getControlledIndex(), currentPokemon, currentPokemon.getParticipant());
			bc.simulateMode = originalSimulateMode;
		}
	}

	public boolean shouldConsiderSwitching(PixelmonWrapper pw, List<UUID> choices, List<MoveChoice> attackChoices) {
		if (choices == null || choices.isEmpty()) {
			return false;
		}

		if (isDoubleBattle(pw.bc)) {
			return hasPerishSongSwitch(pw, choices);
		}

		if (hasPerishSongSwitch(pw, choices)) {
			return true;
		}

		if (pw.getHealthPercent() < 0.5f) {
			return false;
		}

		boolean allMovesNegative = true;
		for (MoveChoice choice : attackChoices) {
			if (choice.weight >= 0.0f) {
				allMovesNegative = false;
				break;
			}
		}

		if (!allMovesNegative) {
			return false;
		}

		List<UUID> validCandidates = getValidSwitchCandidates(pw, choices);
		return !validCandidates.isEmpty();
	}

	public List<UUID> getValidSwitchCandidates(PixelmonWrapper pw, List<UUID> choices) {
		List<UUID> validCandidates = new ArrayList<>();

		List<PixelmonWrapper> opponents = pw.getOpponentPokemon();
		if (opponents.isEmpty()) {
			return validCandidates;
		}
		PixelmonWrapper opponent = opponents.get(0);

		for (UUID partyUUID : choices) {
			if (isValidSwitchCandidate(pw, partyUUID, opponent)) {
				validCandidates.add(partyUUID);
			}
		}

		return validCandidates;
	}

	private boolean hasPerishSongSwitch(PixelmonWrapper pw, List<UUID> choices) {
		return checkPerishSongSwitch(pw, choices) != null;
	}
}
