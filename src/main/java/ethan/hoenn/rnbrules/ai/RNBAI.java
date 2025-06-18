package ethan.hoenn.rnbrules.ai;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.isDoubleBattle;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.ai.BattleAIBase;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import ethan.hoenn.rnbrules.ai.handlers.HazardMoveHandler;
import ethan.hoenn.rnbrules.ai.handlers.SetupMoveHandler;
import ethan.hoenn.rnbrules.ai.handlers.SpecificMoveHandler;
import ethan.hoenn.rnbrules.ai.handlers.StatusMoveHandler;
import ethan.hoenn.rnbrules.ai.handlers.UtilityMoveHandler;
import ethan.hoenn.rnbrules.ai.scorers.GeneralMoveScorer;
import ethan.hoenn.rnbrules.ai.scorers.SpecializedMoveScorer;
import ethan.hoenn.rnbrules.ai.scorers.SwitchMoveScorer;
import ethan.hoenn.rnbrules.ai.utils.IllusionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class RNBAI extends BattleAIBase {

	private final BattleParticipant participant;
	private final BattleController bc;
	private final GlobalStatusController gsc;
	private final Random random;
	private final boolean debugMode;

	private UUID voluntarySwitchTarget = null;

	private final GeneralMoveScorer generalMoveScorer;
	private final SpecializedMoveScorer specializedMoveScorer;
	private final SwitchMoveScorer switchMoveScorer;
	private final SetupMoveHandler setupMoveHandler;
	private final StatusMoveHandler statusMoveHandler;
	private final HazardMoveHandler hazardMoveHandler;
	private final UtilityMoveHandler utilityMoveHandler;
	private final SpecificMoveHandler specificMoveHandler;

	public RNBAI(BattleParticipant participant) {
		super(participant);
		this.participant = participant;
		this.bc = participant.bc;
		this.gsc = bc.globalStatusController;
		this.random = new Random();

		this.debugMode = false;

		this.setupMoveHandler = new SetupMoveHandler(debugMode);
		this.statusMoveHandler = new StatusMoveHandler(gsc, debugMode);
		this.hazardMoveHandler = new HazardMoveHandler(debugMode);
		this.utilityMoveHandler = new UtilityMoveHandler(debugMode);
		this.specificMoveHandler = new SpecificMoveHandler(debugMode, bc);

		this.generalMoveScorer = new GeneralMoveScorer(bc, debugMode);
		this.specializedMoveScorer = new SpecializedMoveScorer(setupMoveHandler, statusMoveHandler, hazardMoveHandler, utilityMoveHandler, specificMoveHandler, debugMode);
		this.switchMoveScorer = new SwitchMoveScorer(debugMode);
	}

	@Override
	public MoveChoice getNextMove(PixelmonWrapper pw) {
		List<MoveChoice> attackChoices = getAttackChoices(pw);

		List<UUID> possibleSwitches = getPossibleSwitchIDs();
		voluntarySwitchTarget = switchMoveScorer.shouldSwitch(pw, possibleSwitches, attackChoices);

		if (voluntarySwitchTarget != null) {
			debugLog("Voluntary switch triggered to: " + voluntarySwitchTarget);

			return null;
		}

		scoreAllMoves(pw, attackChoices);
		List<MoveChoice> bestMoves = getBestScoringMoves(attackChoices);

		if (bestMoves.size() > 1) {
			return bestMoves.get(random.nextInt(bestMoves.size()));
		}

		return bestMoves.isEmpty() ? attackChoices.get(0) : bestMoves.get(0);
	}

	private void scoreAllMoves(PixelmonWrapper pw, List<MoveChoice> choices) {
		boolean originalSimulateMode = bc.simulateMode;
		bc.simulateMode = true;

		try {
			List<MoveChoice> evaluatedChoices = new ArrayList<>();

			for (MoveChoice originalChoice : choices) {
				if (isDoubleBattle(bc) && originalChoice.targets.size() > 1) {
					float bestScore = 0.0f;
					PixelmonWrapper bestTarget = null;

					for (PixelmonWrapper target : originalChoice.targets) {
						List<PixelmonWrapper> singleTargetList = new ArrayList<>();
						singleTargetList.add(target);

						MoveChoice singleTargetChoice = new MoveChoice(originalChoice.user, originalChoice.attack, singleTargetList);
						singleTargetChoice.weight = 0.0f;

						evaluateSingleChoice(pw, singleTargetChoice);

						if (singleTargetChoice.weight > bestScore) {
							bestScore = singleTargetChoice.weight;
							bestTarget = target;
						}
					}

					if (bestTarget != null) {
						originalChoice.weight = bestScore;
						originalChoice.targets.clear();
						originalChoice.targets.add(bestTarget);

						debugLog(originalChoice.attack.getActualMove().getAttackName() + " best against " + bestTarget.getSpecies().getLocalizedName() + ": " + bestScore);
					}
				} else {
					evaluateSingleChoice(pw, originalChoice);
				}

				evaluatedChoices.add(originalChoice);
			}

			choices.clear();
			choices.addAll(evaluatedChoices);
		} finally {
			bc.simulateMode = originalSimulateMode;
		}
	}

	private void evaluateSingleChoice(PixelmonWrapper pw, MoveChoice choice) {
		choice.weight = 0.0f;

		IllusionUtils.IllusionData illusionData = IllusionUtils.checkForIllusion(pw, choice, debugMode);

		if (illusionData != null && illusionData.shouldDoTemporarySwitch()) {
			IllusionUtils.evaluateChoiceWithIllusionSwitch(
				pw,
				choice,
				illusionData.targetWithIllusion,
				illusionData.actualDisguisedPokemon,
				bc,
				() -> {
					boolean wasHandledBySpecialized = specializedMoveScorer.scoreSpecializedMove(pw, choice);
					if (!wasHandledBySpecialized) {
						generalMoveScorer.scoreGeneralMove(pw, choice);
					}
				},
				debugMode
			);
		} else {
			boolean wasHandledBySpecialized = specializedMoveScorer.scoreSpecializedMove(pw, choice);

			if (!wasHandledBySpecialized) {
				generalMoveScorer.scoreGeneralMove(pw, choice);
			}
		}

		debugLog(
			"Score for " + choice.attack.getActualMove().getAttackName() + (!choice.targets.isEmpty() ? " against " + choice.targets.get(0).getSpecies().getLocalizedName() : "") + ": " + choice.weight
		);
	}

	private List<MoveChoice> getBestScoringMoves(List<MoveChoice> choices) {
		if (choices.isEmpty()) {
			return choices;
		}

		List<MoveChoice> bestChoices = new ArrayList<>();
		float bestScore = -Float.MAX_VALUE;

		for (MoveChoice choice : choices) {
			if (choice.weight > bestScore) {
				bestScore = choice.weight;
				bestChoices.clear();
				bestChoices.add(choice);
			} else if (Math.abs(choice.weight - bestScore) < 0.001f) {
				bestChoices.add(choice);
			}
		}

		debugLog("BEST SCORE: " + bestScore + ", MATCHING MOVES: " + bestChoices.size());
		return bestChoices;
	}

	@Override
	public UUID getNextSwitch(PixelmonWrapper pw) {
		if (voluntarySwitchTarget != null) {
			UUID target = voluntarySwitchTarget;
			voluntarySwitchTarget = null;
			debugLog("Executing voluntary switch to: " + target);
			return target;
		}

		List<UUID> choices = getPossibleSwitchIDs();

		if (!choices.isEmpty()) return switchMoveScorer.mustSwitch(pw, choices);
		else return null;
	}

	@Override
	public UUID getFaintedPokemonToRevive() {
		return null;
	}

	@Override
	protected List<MoveChoice> getAttackChoices(PixelmonWrapper pw) {
		List<MoveChoice> choices = new ArrayList<>();

		for (Attack a : this.getMoveset(pw)) {
			if (a != null && a.pp > 0 && !a.getDisabled()) {
				a.createMoveChoices(pw, choices, true);
			}
		}

		if (choices.isEmpty()) {
			Attack struggle = new Attack(AttackRegistry.STRUGGLE);
			struggle.createMoveChoices(pw, choices, false);
		}

		return choices;
	}

	protected List<MoveChoice> getAttackChoicesOpponentOnly(PixelmonWrapper pw) {
		List<MoveChoice> choices = this.getAttackChoices(pw);
		List<PixelmonWrapper> allies = this.bc.getTeamPokemonExcludeSelf(pw);

		if (!allies.isEmpty()) {
			for (int i = 0; i < choices.size(); ++i) {
				MoveChoice choice = choices.get(i);
				if (choice.targets.size() == 1 && allies.contains(choice.targets.get(0))) {
					choices.remove(i--);
				}
			}
		}

		return choices;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[RNB AI] " + message);
		}
	}
}
