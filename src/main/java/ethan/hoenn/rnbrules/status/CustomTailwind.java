package ethan.hoenn.rnbrules.status;

import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.AttackResult;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import java.util.List;

public class CustomTailwind extends StatusBase {

	transient int turnsToGo = 200;

	public CustomTailwind() {
		super(StatusType.Tailwind);
	}

	public void applyEffect(PixelmonWrapper user, PixelmonWrapper target) {
		if (user.targetIndex == 0 || user.bc.simulateMode) if (user.hasStatus(new StatusType[] { this.type })) {
			user.bc.sendToAll("pixelmon.status.tailwindalready", new Object[0]);
			user.attack.moveResult.result = AttackResult.failed;
		} else {
			user.addTeamStatus(new CustomTailwind(), user);
			user.bc.sendToAll("pixelmon.status.tailwindstarted", new Object[] { user.getNickname() });
		}
	}

	public boolean isTeamStatus() {
		return true;
	}

	public int[] modifyStats(PixelmonWrapper user, int[] stats) {
		stats[BattleStatsType.SPEED.getStatIndex()] = stats[BattleStatsType.SPEED.getStatIndex()] * 2;
		return stats;
	}

	public void applyRepeatedEffect(PixelmonWrapper pw) {
		if (--this.turnsToGo == 0) {
			pw.removeStatus(this);
			pw.bc.sendToAll("pixelmon.status.tailwindfaded", new Object[] { pw.getNickname() });
		}
	}

	public int getRemainingTurns() {
		return this.turnsToGo;
	}

	public StatusBase copy() {
		CustomTailwind copy = new CustomTailwind();
		copy.turnsToGo = this.turnsToGo;
		return copy;
	}

	public void weightEffect(
		PixelmonWrapper pw,
		MoveChoice userChoice,
		List<MoveChoice> userChoices,
		List<MoveChoice> bestUserChoices,
		List<MoveChoice> opponentChoices,
		List<MoveChoice> bestOpponentChoices
	) {
		if (!MoveChoice.hasPriority(new List[] { bestOpponentChoices }) && MoveChoice.canOutspeed(bestOpponentChoices, pw, bestUserChoices)) userChoice.raiseWeight(75.0F);
	}
}
