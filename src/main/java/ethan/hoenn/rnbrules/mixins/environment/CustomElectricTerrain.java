package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.battles.AttackCategory;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.SurgeSurfer;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.EffectBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ElectricTerrain.class)
public class CustomElectricTerrain extends Terrain {

	public CustomElectricTerrain(boolean extended) {
		super(StatusType.ElectricTerrain, "pixelmon.status.electricterrain", "pixelmon.status.electricterrainend", extended);
	}

	public CustomElectricTerrain(int turnsToGo) {
		super(StatusType.ElectricTerrain, turnsToGo, "pixelmon.status.electricterrain", "pixelmon.status.electricterrainend");
	}

	public Terrain getNewInstance() {
		return new ElectricTerrain(false);
	}

	@Nonnull
	public com.pixelmonmod.pixelmon.api.battles.Terrain getTerrainType() {
		return com.pixelmonmod.pixelmon.api.battles.Terrain.ELECTRIC;
	}

	public Element getTypingForTerrain() {
		return Element.ELECTRIC;
	}

	/**
	 * @author ethan
	 * @reason 30 -> 50
	 */
	@Overwrite(remap = false)
	public int[] modifyPowerAndAccuracyTarget(int power, int accuracy, PixelmonWrapper user, PixelmonWrapper target, Attack a) {
		if (this.affectsPokemon(user)) {
			if (a.isAttack(new Optional[] { AttackRegistry.RISING_VOLTAGE })) {
				power = (int) ((double) power * (double) 2.0F);
			} else if (a.getType() == Element.ELECTRIC) {
				power = (int) ((double) power * 1.5);
			}
		}

		return new int[] { power, accuracy };
	}

	public boolean stopsStatusChange(StatusType t, PixelmonWrapper target, PixelmonWrapper user) {
		if (this.affectsPokemon(target) && (t == StatusType.Sleep || t == StatusType.Yawn)) {
			if (user != target && user.attack.getAttackCategory() == AttackCategory.STATUS) {
				target.bc.sendToAll("pixelmon.effect.effectfailed", new Object[0]);
			}

			return true;
		} else {
			return false;
		}
	}

	protected int countBenefits(PixelmonWrapper user, PixelmonWrapper target) {
		int benefits = 0;
		if (this.affectsPokemon(target)) {
			List<Attack> moveset = user.getBattleAI().getMoveset(target);
			if (Attack.hasOffensiveAttackType(moveset, Element.ELECTRIC)) {
				++benefits;
			}

			if (target.hasStatus(new StatusType[] { StatusType.Yawn })) {
				++benefits;
			}

			if (Attack.hasAttack(moveset, new Optional[] { AttackRegistry.REST })) {
				--benefits;
			}

			for (Attack move : moveset) {
				for (EffectBase e : move.getMove().effects) {
					if (e instanceof Sleep || e instanceof Yawn) {
						--benefits;
						break;
					}
				}

				if (
					move.isAttack(new Optional[] { AttackRegistry.NATURE_POWER }) ||
					move.isAttack(new Optional[] { AttackRegistry.SECRET_POWER }) ||
					move.isAttack(new Optional[] { AttackRegistry.CAMOUFLAGE }) ||
					move.isAttack(new Optional[] { AttackRegistry.RISING_VOLTAGE }) ||
					move.isAttack(new Optional[] { AttackRegistry.TERRAIN_PULSE })
				) {
					++benefits;
				}
			}

			if (target.getBattleAbility().isAbility(SurgeSurfer.class)) {
				++benefits;
			}

			if (target.hasHeldItem() && (target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainExtender || target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainSeed)) {
				++benefits;
			}
		}

		return benefits;
	}

	public StatusBase copy() {
		return new ElectricTerrain(this.turnsToGo);
	}
}
