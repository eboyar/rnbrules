package ethan.hoenn.rnbrules.mixins.moves;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.Defog;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.SpecialAttackBase;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Defog.class)
public class CustomDefog extends SpecialAttackBase {

	public CustomDefog() {}

	/**
	 * @author ethan
	 * @reason remove terrain removal portion
	 */
	@Overwrite(remap = false)
	public void applyEffect(PixelmonWrapper user, PixelmonWrapper target) {
		boolean clearedHazards = false;

		for (PixelmonWrapper pokemon : new PixelmonWrapper[] { user, (PixelmonWrapper) user.getOpponentPokemon().get(0) }) {
			if (pokemon.removeTeamStatus(new StatusType[] { StatusType.Spikes, StatusType.StealthRock, StatusType.ToxicSpikes, StatusType.StickyWeb })) {
				clearedHazards = true;
			}
		}

		if (clearedHazards) {
			user.bc.sendToAll("pixelmon.effect.clearspikes", new Object[] { user.getNickname() });
		}

		if (target.removeTeamStatus(new StatusType[] { StatusType.LightScreen })) {
			user.bc.sendToAll("pixelmon.status.lightscreenoff", new Object[] { target.getNickname() });
		}

		if (target.removeTeamStatus(new StatusType[] { StatusType.Reflect })) {
			user.bc.sendToAll("pixelmon.status.reflectoff", new Object[] { target.getNickname() });
		}

		if (target.removeTeamStatus(new StatusType[] { StatusType.Mist })) {
			user.bc.sendToAll("pixelmon.status.mistoff", new Object[] { target.getNickname() });
		}

		if (target.removeTeamStatus(new StatusType[] { StatusType.SafeGuard })) {
			user.bc.sendToAll("pixelmon.status.safeguardoff", new Object[] { target.getNickname() });
		}

		if (target.removeTeamStatus(new StatusType[] { StatusType.AuroraVeil })) {
			user.bc.sendToAll("pixelmon.status.auroraveil.woreoff", new Object[] { target.getNickname() });
		}
	}

	public void applyMissEffect(PixelmonWrapper user, PixelmonWrapper target) {
		if (
			target.hasStatus(new StatusType[] { StatusType.Substitute }) &&
			!target.attack.isAttack(
				new Optional[] { AttackRegistry.BANEFUL_BUNKER, AttackRegistry.CRAFTY_SHIELD, AttackRegistry.DETECT, AttackRegistry.MAX_GUARD, AttackRegistry.PROTECT, AttackRegistry.SPIKY_SHIELD }
			)
		) {
			this.applyEffect(user, target);
		}
	}

	public void weightEffect(
		PixelmonWrapper pw,
		MoveChoice userChoice,
		List<MoveChoice> userChoices,
		List<MoveChoice> bestUserChoices,
		List<MoveChoice> opponentChoices,
		List<MoveChoice> bestOpponentChoices
	) {
		int total = 0;

		for (PixelmonWrapper target : userChoice.targets) {
			total += target.countStatuses(new StatusType[] { StatusType.LightScreen, StatusType.Reflect, StatusType.Mist, StatusType.SafeGuard, StatusType.AuroraVeil });
		}

		if (userChoice.hitsAlly()) {
			total = -total;
		}

		StatusType[] entryHazards = new StatusType[] { StatusType.Spikes, StatusType.StealthRock, StatusType.ToxicSpikes, StatusType.StickyWeb };
		total += pw.countStatuses(entryHazards);
		total -= ((PixelmonWrapper) pw.getOpponentPokemon().get(0)).countStatuses(entryHazards);
		userChoice.raiseWeight((float) (30 * total));
	}
}
