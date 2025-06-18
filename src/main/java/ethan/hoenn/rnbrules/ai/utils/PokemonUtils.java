package ethan.hoenn.rnbrules.ai.utils;

import com.pixelmonmod.pixelmon.api.battles.AttackCategory;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.*;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.List;

public class PokemonUtils {

	private final boolean debugMode;

	public PokemonUtils(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public static boolean hasKillBoostAbility(PixelmonWrapper pw) {
		return (pw.getAbility() instanceof Moxie || pw.getAbility() instanceof BeastBoost || pw.getAbility() instanceof ChillingNeigh || pw.getAbility() instanceof GrimNeigh);
	}

	public static boolean hasReceiverAbility(PixelmonWrapper pw) {
		return pw.getAbility() instanceof Receiver;
	}

	public static boolean isMeloettaBaseForm(PixelmonWrapper pw) {
		return pw.pokemon.getSpecies().is(PixelmonSpecies.MELOETTA) && pw.pokemon.getForm().isForm(("base"));
	}

	public static boolean isMeloettaPirouetteForm(PixelmonWrapper pw) {
		return pw.pokemon.getSpecies().is(PixelmonSpecies.MELOETTA) && pw.pokemon.getForm().isForm(("pirouette"));
	}

	public static boolean isDesirableRolePlayAbility(Ability ability) {
		return (ability instanceof HugePower || ability instanceof PurePower || ability instanceof Protean || ability instanceof ToughClaws);
	}

	public static boolean isImmuneToGroundMoves(PixelmonWrapper pw) {
		if (pw.type.contains(Element.FLYING)) {
			return true;
		}

		if (pw.getAbility() instanceof Levitate) {
			return true;
		}

		if (pw.getStatuses().stream().anyMatch(status -> status instanceof MagnetRise)) {
			return true;
		}

		return pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.airBalloon;
	}

	public static boolean hasHarmfulStatusCondition(PixelmonWrapper pw) {
		return pw
			.getStatuses()
			.stream()
			.anyMatch(
				status ->
					status instanceof Poison ||
					status instanceof PoisonBadly ||
					status instanceof Burn ||
					status instanceof Cursed ||
					status instanceof Infatuated ||
					status instanceof Perish ||
					status instanceof Leech ||
					status instanceof Yawn
			);
	}

	public static boolean hasMajorStatus(PixelmonWrapper target) {
		return target
			.getStatuses()
			.stream()
			.anyMatch(status -> status instanceof Paralysis || status instanceof Burn || status instanceof Poison || status instanceof PoisonBadly || status instanceof Freeze || status instanceof Sleep);
	}

	public static boolean hasSubstitute(PixelmonWrapper pw) {
		return pw.getStatuses().stream().anyMatch(status -> status instanceof Substitute);
	}

	public static boolean hasPositiveStatChanges(PixelmonWrapper pw) {
		return (
			pw.getBattleStats().getStage(BattleStatsType.ATTACK) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.DEFENSE) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.SPECIAL_ATTACK) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.SPECIAL_DEFENSE) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.SPEED) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.EVASION) > 0 ||
			pw.getBattleStats().getStage(BattleStatsType.ACCURACY) > 0
		);
	}

	public static boolean isAtMaxAttackStage(PixelmonWrapper pw) {
		return pw.getBattleStats().getStage(BattleStatsType.ATTACK) >= 6;
	}

	public static boolean hasCritImmunity(PixelmonWrapper pw) {
		return (pw.getAbility().getLocalizedName().equals("Shell Armor") || pw.getAbility().getLocalizedName().equals("Battle Armor"));
	}

	public static boolean hasPhysicalAttackingMoves(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && attack.getActualMove().getAttackCategory().equals(AttackCategory.PHYSICAL)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasSpecialAttackingMoves(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && attack.getActualMove().getAttackCategory().equals(AttackCategory.SPECIAL)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasAnyDamagingMoves(PixelmonWrapper target) {
		for (Attack attack : target.getMoveset()) {
			if (attack != null && (attack.getActualMove().getAttackCategory() == AttackCategory.PHYSICAL || attack.getActualMove().getAttackCategory() == AttackCategory.SPECIAL)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasAnySoundBasedMove(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && MoveUtils.isSoundBasedMove(attack.getActualMove())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasHexMove(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && MoveUtils.isHexMove(attack)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasPartnerWithHex(PixelmonWrapper pw) {
		List<PixelmonWrapper> allies = pw.getTeamPokemonExcludeSelf();
		if (!allies.isEmpty()) {
			for (PixelmonWrapper ally : allies) {
				if (hasHexMove(ally)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean opponentHasUnaware(PixelmonWrapper pw) {
		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (opponent.getAbility() instanceof com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Unaware) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasAllyWithHugePowerAbility(PixelmonWrapper pw, List<PixelmonWrapper> adjacentPokemon) {
		if (adjacentPokemon != null && !adjacentPokemon.isEmpty()) {
			for (PixelmonWrapper ally : adjacentPokemon) {
				if (ally.isAlly(pw)) {
					Ability allyAbility = ally.getAbility();
					if (allyAbility instanceof HugePower || allyAbility instanceof PurePower) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean hasMoveThatRequiresSleep(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && (MoveUtils.isDreamEaterMove(attack) || MoveUtils.isNightmareMove(attack))) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasSleepCounterMoves(PixelmonWrapper target) {
		for (Attack attack : target.getMoveset()) {
			if (attack != null && (MoveUtils.isSnoreMove(attack) || MoveUtils.isSleepTalkMove(attack))) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasStatReductionImmunity(PixelmonWrapper pw) {
		return (pw.getAbility() instanceof Contrary || pw.getAbility() instanceof ClearBody || pw.getAbility() instanceof WhiteSmoke || pw.getAbility() instanceof FullMetalBody);
	}

	public static boolean hasImmunityToSleep(PixelmonWrapper target, StatusBase terrain) {
		boolean hasImmunityAbility = target.getAbility() instanceof Insomnia || target.getAbility() instanceof VitalSpirit || target.getAbility() instanceof Comatose;

		boolean isGrounded =
			!target.type.contains(Element.FLYING) && !(target.getAbility() instanceof Levitate) && !(target.hasHeldItem() && target.getHeldItem().getHeldItemType() == EnumHeldItems.airBalloon);

		boolean hasTerrainImmunity = false;
		if (isGrounded) {
			hasTerrainImmunity = terrain instanceof ElectricTerrain || terrain instanceof MistyTerrain;
		}

		boolean isGrassTypeWithPowderMove = target.type.contains(Element.GRASS) && ((MoveUtils.isSporeMove(target.selectedAttack) || MoveUtils.isPowderMove(target.selectedAttack)));

		return hasImmunityAbility || hasTerrainImmunity || isGrassTypeWithPowderMove;
	}

	public static boolean hasHexOrFlinchMove(PixelmonWrapper pw) {
		for (Attack attack : pw.getMoveset()) {
			if (attack != null) {
				if (MoveUtils.isHexMove(attack) || MoveUtils.canCauseFlinch(attack.getActualMove())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean hasCommonMoves(PixelmonWrapper aiPokemon, PixelmonWrapper opponentPokemon) {
		for (Attack aiAttack : aiPokemon.getMoveset()) {
			if (aiAttack == null) continue;

			for (Attack opponentAttack : opponentPokemon.getMoveset()) {
				if (opponentAttack == null) continue;

				if (aiAttack.getActualMove().getAttackName().equals(opponentAttack.getActualMove().getAttackName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isLastMoveUsed(PixelmonWrapper pw, ImmutableAttack moveType) {
		return pw.lastAttack != null && pw.lastAttack.isAttack(moveType);
	}

	public static boolean isGoodItem(EnumHeldItems item) {
		return (
			item == EnumHeldItems.lifeorb ||
			item == EnumHeldItems.choiceItem ||
			item == EnumHeldItems.expertBelt ||
			item == EnumHeldItems.focussash ||
			item == EnumHeldItems.leftovers ||
			item == EnumHeldItems.assaultVest ||
			item == EnumHeldItems.weaknessPolicy
		);
	}

	public static boolean isHarmfulItem(EnumHeldItems item) {
		return (isHarmfulOrbItem(item) || item == EnumHeldItems.ironBall || item == EnumHeldItems.laggingTail || item == EnumHeldItems.stickyBarb);
	}

	public static boolean isHarmfulOrbItem(EnumHeldItems item) {
		return item == EnumHeldItems.toxicOrb || item == EnumHeldItems.flameOrb || item == EnumHeldItems.blackSludge;
	}

	public void debugLog(String message) {
		if (debugMode) {
			System.out.println("[PokemonUtils] " + message);
		}
	}
}
