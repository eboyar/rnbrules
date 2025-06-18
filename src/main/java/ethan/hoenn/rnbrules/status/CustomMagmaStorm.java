package ethan.hoenn.rnbrules.status;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.*;
import com.pixelmonmod.pixelmon.battles.attacks.DamageTypeEnum;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.AuroraVeil;
import com.pixelmonmod.pixelmon.battles.status.GlobalStatusBase;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.List;

public class CustomMagmaStorm extends GlobalStatusBase {

	public transient ImmutableAttack variant;
	private transient int turnsToGo;
	private transient PixelmonWrapper trapper;

	public CustomMagmaStorm() {
		this(200);
	}

	public CustomMagmaStorm(int turnsToGo) {
		super(StatusType.PartialTrap);
	}

	public boolean stopsSwitching() {
		return true;
	}

	public boolean isImmune(PixelmonWrapper p) {
		if (p.getTrainerOwner() != null) {
			return true;
		} else return (p.hasType(new Element[] { Element.GHOST }) || p.getUsableHeldItem().getHeldItemType() == EnumHeldItems.safetyGoggles);
	}

	public void applyRepeatedEffect(GlobalStatusController gsc) {
		BattleController bc = gsc.bc;
		for (PixelmonWrapper p : bc.getDefaultTurnOrder()) {
			if (!this.isImmune(p)) {
				p.bc.sendToAll("The intense heat burns.");
				p.doBattleDamage(p, (float) p.getPercentMaxHealth(12.5F), DamageTypeEnum.STATUS);
			}
		}
	}

	public int getRemainingTurns() {
		return this.turnsToGo;
	}

	public void weightEffect(
		PixelmonWrapper pw,
		MoveChoice userChoice,
		List<MoveChoice> userChoices,
		List<MoveChoice> bestUserChoices,
		List<MoveChoice> opponentChoices,
		List<MoveChoice> bestOpponentChoices
	) {
		if (!userChoice.hitsAlly()) {
			for (PixelmonWrapper target : userChoice.targets) {
				if (target.getBattleAbility() instanceof MagicGuard) {
					return;
				}

				float weight = 12.5F;
				if (pw.getUsableHeldItem().getHeldItemType() == EnumHeldItems.bindingBand) {
					weight *= 2.0F;
				}

				userChoice.raiseWeight(weight);
			}
		}
	}
}
