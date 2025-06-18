package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.GrassPelt;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Mimicry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.GrassyTerrain;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import com.pixelmonmod.pixelmon.battles.status.Terrain;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GrassyTerrain.class)
public class CustomGrassyTerrain extends Terrain {

	public CustomGrassyTerrain(boolean extended) {
		super(StatusType.GrassyTerrain, "pixelmon.status.grassyterrain", "pixelmon.status.grassyterrainend", extended);
	}

	public CustomGrassyTerrain(int turnsToGo) {
		super(StatusType.GrassyTerrain, turnsToGo, "pixelmon.status.grassyterrain", "pixelmon.status.grassyterrainend");
	}

	public Terrain getNewInstance() {
		return new GrassyTerrain(false);
	}

	@Nonnull
	public com.pixelmonmod.pixelmon.api.battles.Terrain getTerrainType() {
		return com.pixelmonmod.pixelmon.api.battles.Terrain.GRASSY;
	}

	public Element getTypingForTerrain() {
		return Element.GRASS;
	}

	/**
	 * @author ethan
	 * @reason 30 -> 50
	 */
	@Overwrite(remap = false)
	public int[] modifyPowerAndAccuracyTarget(int power, int accuracy, PixelmonWrapper user, PixelmonWrapper target, Attack a) {
		if (this.affectsPokemon(user) && a.getType() == Element.GRASS) {
			power = (int) ((double) power * 1.5);
		}

		if (a.isAttack(new Optional[] { AttackRegistry.BULLDOZE, AttackRegistry.EARTHQUAKE, AttackRegistry.MAGNITUDE })) {
			power = (int) ((double) power * (double) 0.5F);
		}

		return new int[] { power, accuracy };
	}

	public void applyRepeatedEffect(GlobalStatusController gsc) {
		super.applyRepeatedEffect(gsc);
		if (gsc.hasStatus(this.type)) {
			for (PixelmonWrapper p : gsc.bc.getDefaultTurnOrder()) {
				if (!p.hasFullHealth() && !p.isFainted() && !p.isAirborne() && !p.hasStatus(new StatusType[] { StatusType.HealBlock }) && p.healByPercent(6.25F) > 0) {
					p.bc.sendToAll("pixelmon.effect.restorehealth", new Object[] { p.getNickname() });
				}
			}
		}
	}

	protected int countBenefits(PixelmonWrapper user, PixelmonWrapper target) {
		int benefits = 0;
		List<Attack> moveset = user.getBattleAI().getMoveset(target);
		if (this.affectsPokemon(target)) {
			if (Attack.hasOffensiveAttackType(moveset, Element.GRASS)) {
				++benefits;
			}

			for (Attack move : moveset) {
				if (
					move.isAttack(new Optional[] { AttackRegistry.NATURE_POWER }) ||
					move.isAttack(new Optional[] { AttackRegistry.SECRET_POWER }) ||
					move.isAttack(new Optional[] { AttackRegistry.CAMOUFLAGE }) ||
					move.isAttack(new Optional[] { AttackRegistry.GRASSY_GLIDE }) ||
					move.isAttack(new Optional[] { AttackRegistry.TERRAIN_PULSE }) ||
					move.isAttack(new Optional[] { AttackRegistry.FLORAL_HEALING })
				) {
					++benefits;
				}
			}

			if (target.getBattleAbility().isAbility(GrassPelt.class)) {
				++benefits;
			}

			if (target.getBattleAbility().isAbility(Mimicry.class)) {
				++benefits;
			}

			if ((target.hasHeldItem() && target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainExtender) || target.getHeldItem().getHeldItemType() == EnumHeldItems.terrainSeed) {
				++benefits;
			}

			++benefits;
		}

		if (Attack.hasAttack(moveset, new Optional[] { AttackRegistry.BULLDOZE, AttackRegistry.EARTHQUAKE, AttackRegistry.MAGNITUDE })) {
			--benefits;
		}

		return benefits;
	}

	public StatusBase copy() {
		return new GrassyTerrain(this.turnsToGo);
	}
}
