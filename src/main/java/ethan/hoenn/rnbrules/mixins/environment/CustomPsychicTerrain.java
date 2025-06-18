package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.battles.AttackCategory;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.GaleWings;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Mimicry;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Prankster;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Triage;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.EffectBase;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.*;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PsychicTerrain.class)
public class CustomPsychicTerrain extends Terrain {

	public CustomPsychicTerrain(boolean extended) {
		super(StatusType.PsychicTerrain, "pixelmon.status.psychicterrain", "pixelmon.status.psychicterrainend", extended);
	}

	public CustomPsychicTerrain(int turnsToGo) {
		super(StatusType.PsychicTerrain, turnsToGo, "pixelmon.status.psychicterrain", "pixelmon.status.psychicterrainend");
	}

	public Terrain getNewInstance() {
		return new PsychicTerrain(false);
	}

	@Nonnull
	public com.pixelmonmod.pixelmon.api.battles.Terrain getTerrainType() {
		return com.pixelmonmod.pixelmon.api.battles.Terrain.PSYCHIC;
	}

	public Element getTypingForTerrain() {
		return Element.PSYCHIC;
	}

	public boolean stopsIncomingAttack(PixelmonWrapper pokemon, PixelmonWrapper user) {
		if (this.affectsPokemon(pokemon) && Math.round(user.priority) > 0) {
			if (user.attack.isAttack(new Optional[] { AttackRegistry.PERISH_SONG, AttackRegistry.FLOWER_SHIELD, AttackRegistry.ROTOTILLER, AttackRegistry.PURSUIT })) {
				return false;
			} else if (!user.attack.getMove().getTargetingInfo().hitsAll && !user.attack.isAttack(EntryHazard.ENTRY_HAZARDS)) {
				boolean targetsOwnTeam = true;

				for (PixelmonWrapper target : user.targets) {
					if (user.isOpponent(target)) {
						targetsOwnTeam = false;
					}
				}

				if (targetsOwnTeam) {
					return false;
				} else {
					user.bc.sendToAll("pixelmon.battletext.movefailed", new Object[0]);
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @author ethan
	 * @reason 30 -> 50
	 */
	@Overwrite(remap = false)
	public int[] modifyPowerAndAccuracyTarget(int power, int accuracy, PixelmonWrapper user, PixelmonWrapper target, Attack a) {
		if (this.affectsPokemon(user) && a.getType() == Element.PSYCHIC) {
			power = (int) ((double) power * 1.5);
		}

		return new int[] { power, accuracy };
	}

	protected int countBenefits(PixelmonWrapper user, PixelmonWrapper target) {
		int benefits = 0;
		if (this.affectsPokemon(target)) {
			if (Attack.hasOffensiveAttackType(user.getMoveset(), Element.PSYCHIC)) {
				++benefits;
			}

			for (Attack a : target.getMoveset()) {
				if (a == null) {
					break;
				}

				if (
					a.getMove().getPriority(target) > 0 ||
					(a.getAttackCategory() == AttackCategory.STATUS && target.getAbility() instanceof Prankster) ||
					(a.getType().equals(Element.FLYING) && target.getAbility() instanceof GaleWings)
				) {
					--benefits;
				}

				if (
					a.isAttack(new Optional[] { AttackRegistry.NATURE_POWER }) ||
					a.isAttack(new Optional[] { AttackRegistry.SECRET_POWER }) ||
					a.isAttack(new Optional[] { AttackRegistry.CAMOUFLAGE }) ||
					a.isAttack(new Optional[] { AttackRegistry.EXPANDING_FORCE }) ||
					a.isAttack(new Optional[] { AttackRegistry.TERRAIN_PULSE })
				) {
					++benefits;
				}

				if (target.getBattleAbility().isAbility(Triage.class)) {
					for (EffectBase effect : a.getMove().effects) {
						if (
							effect instanceof Recover ||
							effect instanceof Drain ||
							effect instanceof Rest ||
							effect instanceof Synthesis ||
							effect instanceof HealingWish ||
							effect instanceof LunarDance ||
							effect instanceof Purify ||
							effect instanceof ShoreUp ||
							effect instanceof StrengthSap ||
							effect instanceof Swallow ||
							effect instanceof Wish
						) {
							--benefits;
						}
					}
				}
			}

			if (target.getBattleAbility().isAbility(Mimicry.class)) {
				++benefits;
			}

			if ((target.hasHeldItem() && target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainExtender) || target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainSeed) {
				++benefits;
			}
		}

		return benefits;
	}

	public StatusBase copy() {
		return new PsychicTerrain(this.turnsToGo);
	}
}
