package ethan.hoenn.rnbrules.status;

import com.google.common.collect.Sets;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.log.AttackResult;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.Set;

public class CustomAuroraVeil extends Screen {

	transient PixelmonWrapper user;
	private static final Set<String> DIRECT_DAMAGE_MOVES = Sets.newHashSet(
		new String[] {
			"Bide",
			"Counter",
			"Dragon Rage",
			"Endeavor",
			"Final Gambit",
			"Guardian of Alola",
			"Metal Burst",
			"Mirror Coat",
			"Nature's Madness",
			"Night Shade",
			"Psywave",
			"Seismic Toss",
			"Sonic Boom",
			"Super Fang",
		}
	);

	public CustomAuroraVeil() {
		this((PixelmonWrapper) null, 200);
	}

	public CustomAuroraVeil(int turns) {
		this((PixelmonWrapper) null, turns);
	}

	public CustomAuroraVeil(PixelmonWrapper user, int turns) {
		super(StatusType.AuroraVeil, (BattleStatsType) null, turns, "pixelmon.effect.auroraveil.raised", "pixelmon.effect.auroraveil.already", "pixelmon.status.auroraveil.woreoff");
		this.user = null;
		this.user = user;
	}

	public void applyEffect(PixelmonWrapper user, PixelmonWrapper target) {
		if (user.targetIndex == 0 || user.bc.simulateMode) {
			if (user.hasStatus(new StatusType[] { this.type })) {
				user.bc.sendToAll("pixelmon.effect.auroraveil.already", new Object[] { user.getNickname() });
				user.attack.moveResult.result = AttackResult.failed;
			} else if (!user.hasStatus(new StatusType[] { StatusType.Reflect })) {
				user.addTeamStatus(((com.pixelmonmod.pixelmon.battles.status.AuroraVeil) this.getNewInstance(100)).withUser(user), user);
				user.bc.sendToAll("pixelmon.effect.auroraveil.raised", new Object[] { user.getNickname() });
			}
		}
	}

	public CustomAuroraVeil withUser(PixelmonWrapper user) {
		this.user = user;
		return this;
	}

	public boolean shouldReduce(Attack a) {
		return true;
	}

	public float getDamageMultiplier(PixelmonWrapper user, PixelmonWrapper target) {
		if (target == user && user.hasStatus(new StatusType[] { StatusType.Confusion })) {
			return 1.0F;
		} else if (user.attack.didCrit) {
			return 1.0F;
		} else if (DIRECT_DAMAGE_MOVES.contains(user.attack.getMove().getAttackName())) {
			return 1.0F;
		} else {
			return target.bc.rules.getOrDefault(BattleRuleRegistry.BATTLE_TYPE) == BattleType.SINGLE ? 0.5F : 0.6666667F;
		}
	}

	protected Screen getNewInstance(int effectTurns) {
		return new com.pixelmonmod.pixelmon.battles.status.AuroraVeil((PixelmonWrapper) null, effectTurns);
	}
}
