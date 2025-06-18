package ethan.hoenn.rnbrules.ai.scorers;

import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import ethan.hoenn.rnbrules.ai.handlers.*;

public class SpecializedMoveScorer {

	private final SetupMoveHandler setupMoveHandler;
	private final StatusMoveHandler statusMoveHandler;
	private final HazardMoveHandler hazardMoveHandler;
	private final UtilityMoveHandler utilityMoveHandler;
	private final SpecificMoveHandler specificMoveHandler;

	private final boolean debugMode;

	private static final float SCORE_DEFAULT_NON_DAMAGING = 6.0f;

	public SpecializedMoveScorer(
		SetupMoveHandler setupMoveHandler,
		StatusMoveHandler statusMoveHandler,
		HazardMoveHandler hazardMoveHandler,
		UtilityMoveHandler utilityMoveHandler,
		SpecificMoveHandler specificMoveHandler,
		boolean debugMode
	) {
		this.setupMoveHandler = setupMoveHandler;
		this.statusMoveHandler = statusMoveHandler;
		this.hazardMoveHandler = hazardMoveHandler;
		this.utilityMoveHandler = utilityMoveHandler;
		this.specificMoveHandler = specificMoveHandler;
		this.debugMode = debugMode;
	}

	public boolean scoreSpecializedMove(PixelmonWrapper pw, MoveChoice choice) {
		Attack attack = choice.attack;
		boolean handled = false;

		if (setupMoveHandler.shouldHandle(attack)) {
			handled = setupMoveHandler.handleMove(pw, choice);
		}

		if (statusMoveHandler.shouldHandle(attack)) {
			handled = statusMoveHandler.handleMove(pw, choice);
		}

		if (!handled && hazardMoveHandler.shouldHandle(attack)) {
			handled = hazardMoveHandler.handleMove(pw, choice);
		}

		if (!handled && utilityMoveHandler.shouldHandle(attack)) {
			handled = utilityMoveHandler.handleMove(pw, choice, pw.bc.globalStatusController);
		}

		if (!handled && specificMoveHandler.shouldHandle(attack)) {
			handled = specificMoveHandler.handleMove(pw, choice);
		}

		if (!handled) {
			choice.weight = SCORE_DEFAULT_NON_DAMAGING;
			debugLog("Moving to default scoring for " + attack.getActualMove().getAttackName() + ": " + SCORE_DEFAULT_NON_DAMAGING);
			return false;
		}

		return true;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[SpecializedMoveScorer] " + message);
		}
	}
}
