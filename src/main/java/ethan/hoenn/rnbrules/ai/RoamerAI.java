package ethan.hoenn.rnbrules.ai;

import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.controller.ai.BattleAIBase;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import java.util.UUID;

public class RoamerAI extends BattleAIBase {

	private final BattleParticipant participant;

	public RoamerAI(BattleParticipant participant) {
		super(participant);
		this.participant = participant;
	}

	@Override
	public MoveChoice getNextMove(PixelmonWrapper pixelmonWrapper) {
		if (this.participant.bc == null || pixelmonWrapper == null) {
			return this.getRandomAttackChoice(pixelmonWrapper);
		}

		PixelmonWrapper pw = participant.getActiveUnfaintedPokemon().get(0);
		boolean[] canSwitchOrFlee = BattleParticipant.canSwitch(pw);
		if (canSwitchOrFlee[1]) {
			return null;
		}

		return this.getRandomAttackChoice(pixelmonWrapper);
	}

	@Override
	public UUID getNextSwitch(PixelmonWrapper pixelmonWrapper) {
		if (!this.getPossibleSwitchIDs().isEmpty()) {
			return RandomHelper.getRandomElementFromList(this.getPossibleSwitchIDs());
		}
		return null;
	}

	@Override
	public UUID getFaintedPokemonToRevive() {
		if (!this.getPossibleReviveIDs().isEmpty()) {
			return RandomHelper.getRandomElementFromList(this.getPossibleReviveIDs());
		}
		return null;
	}
}
