package ethan.hoenn.rnbrules.ai.handlers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.PokemonUtils.*;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Contrary;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Sniper;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.SuperLuck;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.List;
import java.util.Random;

public class SetupMoveHandler {

	private final Random random;
	private final boolean debugMode;

	// General
	private static final float SCORE_SETUP_BASE = 6.0f;
	private static final float SCORE_SETUP_COULD_BE_KILLED_PENALTY = -20.0f;
	private static final float SCORE_SETUP_UNAWARE_PENALTY = -20.0f;

	// Generic Offensive Setup
	private static final float SCORE_OFFENSIVE_SETUP_INCAPACITATED_BONUS = 3.0f;
	private static final float SCORE_OFFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY = -5.0f;

	// Generic Defensive Setup
	private static final float SCORE_DEFENSIVE_SETUP_INCAPACITATED_BONUS = 2.0f;
	private static final float SCORE_DEFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY = -5.0f;
	private static final float SCORE_DEFENSIVE_SETUP_DOUBLE_STAT_BONUS = 2.0f;
	private static final float DEFENSIVE_SETUP_CHECKS_CHANCE = 0.95f; // more dexzeh funny rng

	// Mixed (removed for now)
	private static final float SCORE_MIXED_SETUP_INCAPACITATED_BONUS = 3.0f;
	private static final float SCORE_MIXED_SETUP_SLOW_AND_2HKO_PENALTY = -5.0f;

	// Speed Setup
	private static final float SCORE_SPEED_SETUP_BASE_SLOWER = 7.0f;
	private static final float SCORE_SPEED_SETUP_BASE_FASTER = -20.0f;

	// SA Setup
	private static final float SCORE_SPECIAL_ATTACK_SETUP_BASE = 6.0f;
	private static final float SCORE_SPECIAL_ATTACK_SETUP_INCAPACITATED_BONUS = 3.0f;
	private static final float SCORE_SPECIAL_ATTACK_SETUP_CANT_3HKO_BONUS = 1.0f;
	private static final float SCORE_SPECIAL_ATTACK_SETUP_FASTER_BONUS = 1.0f;
	private static final float SCORE_SPECIAL_ATTACK_SETUP_SLOW_AND_2HKO_PENALTY = -5.0f;
	private static final float SCORE_SPECIAL_ATTACK_SETUP_ALREADY_BOOSTED_PENALTY = -1.0f;

	// Shell Smash
	private static final float SCORE_SHELL_SMASH_BASE = 6.0f;
	private static final float SCORE_SHELL_SMASH_INCAPACITATED_BONUS = 3.0f;
	private static final float SCORE_SHELL_SMASH_SURVIVE_BONUS = 2.0f;
	private static final float SCORE_SHELL_SMASH_DEATH_PENALTY = -2.0f;
	private static final float SCORE_SHELL_SMASH_ALREADY_BOOSTED = -20.0f;

	// Belly Drum
	private static final float SCORE_BELLY_DRUM_INCAPACITATED = 9.0f;
	private static final float SCORE_BELLY_DRUM_SURVIVE = 8.0f;
	private static final float SCORE_BELLY_DRUM_BASE = 4.0f;

	// Focus Energy and Laser Focus
	private static final float SCORE_FOCUS_ENERGY_BASE = 6.0f;
	private static final float SCORE_FOCUS_ENERGY_CRIT_SYNERGY = 7.0f;
	private static final float SCORE_FOCUS_ENERGY_ANTI_CRIT_ABILITY = -20.0f;

	// Coaching
	private static final float SCORE_COACHING_BASE = 6.0f;
	private static final float SCORE_COACHING_INVALID = -20.0f;
	private static final float SCORE_COACHING_RANDOM_BONUS = 1.0f;
	private static final float COACHING_RANDOM_BONUS_CHANCE = 0.8f;

	// Contrary
	private static final float SCORE_CONTRARY_SETUP = 6.0f; // this may change in the future

	// Meteor Beam
	private static final float SCORE_METEOR_BEAM_WITH_POWER_HERB = 9.0f;
	private static final float SCORE_METEOR_BEAM_WITHOUT_POWER_HERB = -20.0f;

	// Destiny Bond
	private static final float SCORE_DESTINY_BOND_FASTER_AND_DIES_HIGH = 7.0f;
	private static final float SCORE_DESTINY_BOND_FASTER_AND_DIES_LOW = 6.0f;
	private static final float SCORE_DESTINY_BOND_SLOWER_HIGH = 6.0f;
	private static final float SCORE_DESTINY_BOND_SLOWER_LOW = 5.0f;
	private static final float DESTINY_BOND_FASTER_HIGH_CHANCE = 0.81f;
	private static final float DESTINY_BOND_SLOWER_HIGH_CHANCE = 0.5f;

	public SetupMoveHandler(boolean debugMode) {
		this.random = new Random();
		this.debugMode = debugMode;
	}

	public boolean shouldHandle(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.POWER_UP_PUNCH) ||
			attack.getActualMove().isAttack(AttackRegistry.SWORDS_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.HOWL) ||
			attack.getActualMove().isAttack(AttackRegistry.STUFF_CHEEKS) ||
			attack.getActualMove().isAttack(AttackRegistry.BARRIER) ||
			attack.getActualMove().isAttack(AttackRegistry.ACID_ARMOR) ||
			attack.getActualMove().isAttack(AttackRegistry.IRON_DEFENSE) ||
			attack.getActualMove().isAttack(AttackRegistry.COTTON_GUARD) ||
			attack.getActualMove().isAttack(AttackRegistry.CHARGE_BEAM) ||
			attack.getActualMove().isAttack(AttackRegistry.TAIL_GLOW) ||
			attack.getActualMove().isAttack(AttackRegistry.NASTY_PLOT) ||
			attack.getActualMove().isAttack(AttackRegistry.COSMIC_POWER) ||
			attack.getActualMove().isAttack(AttackRegistry.BULK_UP) ||
			attack.getActualMove().isAttack(AttackRegistry.CALM_MIND) ||
			attack.getActualMove().isAttack(AttackRegistry.DRAGON_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.COIL) ||
			attack.getActualMove().isAttack(AttackRegistry.HONE_CLAWS) ||
			attack.getActualMove().isAttack(AttackRegistry.QUIVER_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.SHIFT_GEAR) ||
			attack.getActualMove().isAttack(AttackRegistry.SHELL_SMASH) ||
			attack.getActualMove().isAttack(AttackRegistry.GROWTH) ||
			attack.getActualMove().isAttack(AttackRegistry.WORK_UP) ||
			attack.getActualMove().isAttack(AttackRegistry.CURSE) ||
			attack.getActualMove().isAttack(AttackRegistry.NO_RETREAT) ||
			attack.getActualMove().isAttack(AttackRegistry.AGILITY) ||
			attack.getActualMove().isAttack(AttackRegistry.ROCK_POLISH) ||
			attack.getActualMove().isAttack(AttackRegistry.AUTOTOMIZE) ||
			attack.getActualMove().isAttack(AttackRegistry.BELLY_DRUM) ||
			attack.getActualMove().isAttack(AttackRegistry.FOCUS_ENERGY) ||
			attack.getActualMove().isAttack(AttackRegistry.LASER_FOCUS) ||
			attack.getActualMove().isAttack(AttackRegistry.COACHING) ||
			attack.getActualMove().isAttack(AttackRegistry.METEOR_BEAM) ||
			attack.getActualMove().isAttack(AttackRegistry.DESTINY_BOND) ||
			(isContrarySetupMove(attack))
		);
	}

	public boolean handleMove(PixelmonWrapper pw, MoveChoice choice) {
		Attack attack = choice.attack;
		String moveName = attack.getActualMove().getAttackName();

		if (wouldBeKOdThisTurn(pw)) {
			choice.weight = SCORE_SETUP_COULD_BE_KILLED_PENALTY;
			debugLog(moveName + " scoring (could be killed this turn): " + SCORE_SETUP_COULD_BE_KILLED_PENALTY);
			return true;
		}

		if (opponentHasUnaware(pw) && !isUnaffectedByUnaware(attack)) {
			choice.weight = SCORE_SETUP_UNAWARE_PENALTY;
			debugLog(moveName + " scoring (opponent has Unaware): " + SCORE_SETUP_UNAWARE_PENALTY);
			return true;
		}

		if (attack.getActualMove().isAttack(AttackRegistry.METEOR_BEAM)) {
			return handleMeteorBeamScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.DESTINY_BOND)) {
			return handleDestinyBondScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.COACHING)) {
			return handleCoachingScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FOCUS_ENERGY) || attack.getActualMove().isAttack(AttackRegistry.LASER_FOCUS)) {
			return handleFocusEnergyScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.BELLY_DRUM)) {
			return handleBellyDrumScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.SHELL_SMASH)) {
			return handleShellSmashScoring(pw, choice);
		}

		if (isSpeedSetupMove(attack)) {
			return handleSpeedSetupScoring(pw, choice);
		}

		if (isSpecialAttackSetupMove(attack)) {
			return handleSpecialAttackSetupScoring(pw, choice);
		}

		if (isMixedSetupMove(attack)) {
			return handleMixedSetupScoring(pw, choice);
		}

		if (isOffensiveSetupMove(attack)) {
			return handleOffensiveSetupScoring(pw, choice);
		}

		if (isDefensiveSetupMove(attack)) {
			return handleDefensiveSetupScoring(pw, choice);
		}

		if (isContrarySetupMove(attack) && pw.getAbility() instanceof Contrary) {
			if (handleContrarySetupMove(pw, choice)) {
				return true;
			}
		}

		choice.weight = SCORE_SETUP_BASE;
		debugLog(moveName + " final score: " + SCORE_SETUP_BASE);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleOffensiveSetupScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SETUP_BASE;
		debugLog(moveName + " base score: " + score);

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);

		for (PixelmonWrapper opponent : opponents) {
			if (isIncapacitated(opponent)) {
				score += SCORE_OFFENSIVE_SETUP_INCAPACITATED_BONUS;
				debugLog(moveName + " target incapacitated bonus: +" + SCORE_OFFENSIVE_SETUP_INCAPACITATED_BONUS);
				break;
			}
		}

		for (PixelmonWrapper opponent : opponents) {
			boolean isSlower = !isPokemonFaster(pw, opponent);
			boolean isTwoHKO = wouldBeTwoHitKOd(pw, opponent);

			if (isSlower && isTwoHKO) {
				score += SCORE_OFFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY;
				debugLog(moveName + " AI slower and 2HKO'd penalty: " + SCORE_OFFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY);
				break;
			}
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleDefensiveSetupScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SETUP_BASE;
		debugLog(moveName + " base score: " + score);

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		for (PixelmonWrapper opponent : opponents) {
			boolean isSlower = !isPokemonFaster(pw, opponent);
			boolean isTwoHKO = wouldBeTwoHitKOd(pw, opponent);

			if (isSlower && isTwoHKO) {
				score += SCORE_DEFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY;
				debugLog(moveName + " AI slower and 2HKO'd penalty: " + SCORE_DEFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY);
				break;
			}
		}

		if (random.nextFloat() < DEFENSIVE_SETUP_CHECKS_CHANCE) {
			for (PixelmonWrapper opponent : opponents) {
				if (isIncapacitated(opponent)) {
					score += SCORE_DEFENSIVE_SETUP_INCAPACITATED_BONUS;
					debugLog(moveName + " opponent incapacitated bonus: +" + SCORE_DEFENSIVE_SETUP_INCAPACITATED_BONUS);
					break;
				}
			}

			boolean boostsBothDefenses = choice.attack.getActualMove().isAttack(AttackRegistry.COSMIC_POWER) || choice.attack.getActualMove().isAttack(AttackRegistry.STOCKPILE);

			if (boostsBothDefenses) {
				int defenseStage = pw.getBattleStats().getStage(BattleStatsType.DEFENSE);
				int spDefenseStage = pw.getBattleStats().getStage(BattleStatsType.SPECIAL_DEFENSE);

				if (defenseStage < 2 || spDefenseStage < 2) {
					score += SCORE_DEFENSIVE_SETUP_DOUBLE_STAT_BONUS;
					debugLog(moveName + " double defense bonus (current stages: Def " + defenseStage + ", SpDef " + spDefenseStage + "): +" + SCORE_DEFENSIVE_SETUP_DOUBLE_STAT_BONUS);
				}
			}
		} else {
			debugLog(moveName + " skipping additional checks (5% chance)");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleMixedSetupScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SETUP_BASE;
		debugLog(moveName + " base score: " + score);

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);

		boolean isPhysicalBooster =
			choice.attack.getActualMove().isAttack(AttackRegistry.COIL) ||
			choice.attack.getActualMove().isAttack(AttackRegistry.BULK_UP) ||
			choice.attack.getActualMove().isAttack(AttackRegistry.NO_RETREAT);

		boolean isSpecialBooster = choice.attack.getActualMove().isAttack(AttackRegistry.CALM_MIND) || choice.attack.getActualMove().isAttack(AttackRegistry.QUIVER_DANCE);

		boolean treatAsDefensive = false;

		for (PixelmonWrapper opponent : opponents) {
			boolean hasPhysicalMoves = hasPhysicalAttackingMoves(opponent);
			boolean hasSpecialMoves = hasSpecialAttackingMoves(opponent);

			if (isPhysicalBooster && hasPhysicalMoves && !hasSpecialMoves) {
				treatAsDefensive = true;
				debugLog(moveName + " treating as defensive setup (opponent has physical moves only)");
				break;
			}

			if (isSpecialBooster && !hasPhysicalMoves && hasSpecialMoves) {
				treatAsDefensive = true;
				debugLog(moveName + " treating as defensive setup (opponent has special moves only)");
				break;
			}
		}

		if (treatAsDefensive) {
			for (PixelmonWrapper opponent : opponents) {
				boolean isSlower = !isPokemonFaster(pw, opponent);
				boolean isTwoHKO = wouldBeTwoHitKOd(pw, opponent);

				if (isSlower && isTwoHKO) {
					score += SCORE_DEFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY;
					debugLog(moveName + " AI slower and 2HKO'd penalty: " + SCORE_DEFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY);
					break;
				}
			}

			if (random.nextFloat() < DEFENSIVE_SETUP_CHECKS_CHANCE) {
				for (PixelmonWrapper opponent : opponents) {
					if (isIncapacitated(opponent)) {
						score += SCORE_DEFENSIVE_SETUP_INCAPACITATED_BONUS;
						debugLog(moveName + " opponent incapacitated bonus: +" + SCORE_DEFENSIVE_SETUP_INCAPACITATED_BONUS);
						break;
					}
				}
			} else {
				debugLog(moveName + " skipping additional checks (5% chance)");
			}
		} else {
			for (PixelmonWrapper opponent : opponents) {
				if (isIncapacitated(opponent)) {
					score += SCORE_OFFENSIVE_SETUP_INCAPACITATED_BONUS;
					debugLog(moveName + " opponent incapacitated bonus: +" + SCORE_OFFENSIVE_SETUP_INCAPACITATED_BONUS);
					break;
				}
			}

			for (PixelmonWrapper opponent : opponents) {
				boolean isSlower = !isPokemonFaster(pw, opponent);
				boolean isTwoHKO = wouldBeTwoHitKOd(pw, opponent);

				if (isSlower && isTwoHKO) {
					score += SCORE_OFFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY;
					debugLog(moveName + " AI slower and 2HKO'd penalty: " + SCORE_OFFENSIVE_SETUP_SLOW_AND_2HKO_PENALTY);
					break;
				}
			}
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSpeedSetupScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		boolean isFasterThanAny = true;

		for (PixelmonWrapper opponent : opponents) {
			if (!isPokemonFaster(pw, opponent)) {
				isFasterThanAny = false;
				break;
			}
		}

		if (isFasterThanAny) {
			choice.weight = SCORE_SPEED_SETUP_BASE_FASTER;
			debugLog(moveName + " AI already faster than all opponents: " + SCORE_SPEED_SETUP_BASE_FASTER);
		} else {
			choice.weight = SCORE_SPEED_SETUP_BASE_SLOWER;
			debugLog(moveName + " AI slower than at least one opponent: " + SCORE_SPEED_SETUP_BASE_SLOWER);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSpecialAttackSetupScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SPECIAL_ATTACK_SETUP_BASE;
		debugLog(moveName + " base score: " + score);

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);

		boolean opponentIncapacitated = false;
		for (PixelmonWrapper opponent : opponents) {
			if (isIncapacitated(opponent)) {
				opponentIncapacitated = true;
				score += SCORE_SPECIAL_ATTACK_SETUP_INCAPACITATED_BONUS;
				debugLog(moveName + " opponent incapacitated bonus: +" + SCORE_SPECIAL_ATTACK_SETUP_INCAPACITATED_BONUS);
				break;
			}
		}

		if (!opponentIncapacitated) {
			boolean cantBeThreeHKOd = true;
			for (PixelmonWrapper opponent : opponents) {
				if (wouldBeTwoHitKOd(pw, opponent)) {
					cantBeThreeHKOd = false;
					break;
				}
			}

			if (cantBeThreeHKOd) {
				score += SCORE_SPECIAL_ATTACK_SETUP_CANT_3HKO_BONUS;
				debugLog(moveName + " cannot be 3HKO'd bonus: +" + SCORE_SPECIAL_ATTACK_SETUP_CANT_3HKO_BONUS);

				boolean isFasterThanAll = true;
				for (PixelmonWrapper opponent : opponents) {
					if (!isPokemonFaster(pw, opponent)) {
						isFasterThanAll = false;
						break;
					}
				}

				if (isFasterThanAll) {
					score += SCORE_SPECIAL_ATTACK_SETUP_FASTER_BONUS;
					debugLog(moveName + " AI faster than all opponents bonus: +" + SCORE_SPECIAL_ATTACK_SETUP_FASTER_BONUS);
				}
			}
		}

		for (PixelmonWrapper opponent : opponents) {
			boolean isSlower = !isPokemonFaster(pw, opponent);
			boolean isTwoHKO = wouldBeTwoHitKOd(pw, opponent);

			if (isSlower && isTwoHKO) {
				score += SCORE_SPECIAL_ATTACK_SETUP_SLOW_AND_2HKO_PENALTY;
				debugLog(moveName + " AI slower and 2HKO'd penalty: " + SCORE_SPECIAL_ATTACK_SETUP_SLOW_AND_2HKO_PENALTY);
				break;
			}
		}

		int specialAttackStage = pw.getBattleStats().getStage(BattleStatsType.SPECIAL_ATTACK);
		if (specialAttackStage >= 2) {
			score += SCORE_SPECIAL_ATTACK_SETUP_ALREADY_BOOSTED_PENALTY;
			debugLog(moveName + " already at +" + specialAttackStage + " SpAtk penalty: " + SCORE_SPECIAL_ATTACK_SETUP_ALREADY_BOOSTED_PENALTY);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleShellSmashScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float score = SCORE_SHELL_SMASH_BASE;
		debugLog(moveName + " base score: " + score);

		int attackStage = pw.getBattleStats().getStage(BattleStatsType.ATTACK);
		int specialAttackStage = pw.getBattleStats().getStage(BattleStatsType.SPECIAL_ATTACK);

		if (specialAttackStage >= 1 || attackStage >= 1) {
			choice.weight = SCORE_SHELL_SMASH_ALREADY_BOOSTED;
			debugLog(moveName + " already boosted, not using: " + SCORE_SHELL_SMASH_ALREADY_BOOSTED);
			return true;
		}

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);

		for (PixelmonWrapper opponent : opponents) {
			if (isIncapacitated(opponent)) {
				score += SCORE_SHELL_SMASH_INCAPACITATED_BONUS;
				debugLog(moveName + " opponent incapacitated bonus: +" + SCORE_SHELL_SMASH_INCAPACITATED_BONUS);
				break;
			}
		}

		boolean hasWhiteHerb = pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.whiteHerb;

		boolean wouldBeKOdAfterShellSmash = false;
		for (PixelmonWrapper opponent : opponents) {
			if (isIncapacitated(opponent)) continue;

			int originalDefense = pw.getBattleStats().getStage(BattleStatsType.DEFENSE);
			int originalSpDefense = pw.getBattleStats().getStage(BattleStatsType.SPECIAL_DEFENSE);

			boolean originalSimulateMode = pw.bc.simulateMode;
			pw.bc.simulateMode = true;
			try {
				if (!hasWhiteHerb) {
					pw.getBattleStats().setStage(BattleStatsType.DEFENSE, originalDefense - 1);
					pw.getBattleStats().setStage(BattleStatsType.SPECIAL_DEFENSE, originalSpDefense - 1);
				}

				boolean isPwFaster = isPokemonFaster(pw, opponent);
				if (!isPwFaster) {
					if (wouldBeKOdThisTurn(pw)) {
						wouldBeKOdAfterShellSmash = true;
					}
				} else {
					int originalSpeed = pw.getBattleStats().getStage(BattleStatsType.SPEED);
					pw.getBattleStats().setStage(BattleStatsType.SPEED, originalSpeed + 2);

					if (wouldBeKOdThisTurn(pw)) {
						wouldBeKOdAfterShellSmash = true;
					}

					pw.getBattleStats().setStage(BattleStatsType.SPEED, originalSpeed);
				}

				pw.getBattleStats().setStage(BattleStatsType.DEFENSE, originalDefense);
				pw.getBattleStats().setStage(BattleStatsType.SPECIAL_DEFENSE, originalSpDefense);
			} finally {
				pw.bc.simulateMode = originalSimulateMode;
			}

			if (wouldBeKOdAfterShellSmash) {
				break;
			}
		}

		if (wouldBeKOdAfterShellSmash) {
			score += SCORE_SHELL_SMASH_DEATH_PENALTY;
			debugLog(moveName + " would be KO'd after using: " + SCORE_SHELL_SMASH_DEATH_PENALTY);
		} else {
			score += SCORE_SHELL_SMASH_SURVIVE_BONUS;
			debugLog(moveName + " would survive after using: +" + SCORE_SHELL_SMASH_SURVIVE_BONUS);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleBellyDrumScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);

		for (PixelmonWrapper opponent : opponents) {
			if (isIncapacitated(opponent)) {
				choice.weight = SCORE_BELLY_DRUM_INCAPACITATED;
				debugLog(moveName + " opponent incapacitated: " + SCORE_BELLY_DRUM_INCAPACITATED);
				return true;
			}
		}

		int currentHealth = pw.getHealth();
		int maxHealth = pw.getMaxHealth();
		int halfHealth = maxHealth / 2;
		int healthAfterDrum = currentHealth - halfHealth;

		boolean hasSitrusBerry = pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.berryRestoreHP;

		if (hasSitrusBerry && healthAfterDrum > 0) {
			healthAfterDrum += maxHealth / 4;
		}

		healthAfterDrum = Math.min(healthAfterDrum, maxHealth);

		if (healthAfterDrum <= 0) {
			choice.weight = -20.0f;
			debugLog(moveName + " would KO self: -20.0");
			return true;
		}

		boolean couldBeKOdAfterDrum = false;

		boolean originalSimulateMode = pw.bc.simulateMode;
		pw.bc.simulateMode = true;
		try {
			int originalHealth = pw.getHealth();

			pw.setHealth(healthAfterDrum);

			for (PixelmonWrapper opponent : opponents) {
				for (Attack attack : opponent.getMoveset()) {
					if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
						MoveResults result = new MoveResults(pw);
						attack.saveAttack();
						attack.use(opponent, pw, result);

						if (result.damage >= healthAfterDrum) {
							couldBeKOdAfterDrum = true;
							attack.restoreAttack();
							break;
						}

						attack.restoreAttack();
					}
				}

				if (couldBeKOdAfterDrum) break;
			}

			pw.setHealth(originalHealth);
		} finally {
			pw.bc.simulateMode = originalSimulateMode;
		}

		if (!couldBeKOdAfterDrum) {
			choice.weight = SCORE_BELLY_DRUM_SURVIVE;
			debugLog(moveName + " could survive after using: " + SCORE_BELLY_DRUM_SURVIVE);
		} else {
			choice.weight = SCORE_BELLY_DRUM_BASE;
			debugLog(moveName + " base score: " + SCORE_BELLY_DRUM_BASE);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleFocusEnergyScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		for (PixelmonWrapper opponent : opponents) {
			if (hasCritImmunity(opponent)) {
				choice.weight = SCORE_FOCUS_ENERGY_ANTI_CRIT_ABILITY;
				debugLog(moveName + " opponent has crit immunity: " + SCORE_FOCUS_ENERGY_ANTI_CRIT_ABILITY);
				return true;
			}
		}

		boolean hasSniper = pw.getAbility() instanceof Sniper;
		boolean hasSuperLuck = pw.getAbility() instanceof SuperLuck;
		boolean hasScopeLens = pw.hasHeldItem() && (pw.getHeldItem().getHeldItemType() == EnumHeldItems.scopeLens || pw.getHeldItem().getHeldItemType() == EnumHeldItems.razorClaw);

		boolean hasHighCritMove = false;
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && hasHighCritChance(attack)) {
				hasHighCritMove = true;
				break;
			}
		}

		if (hasSniper || hasSuperLuck || hasScopeLens || hasHighCritMove) {
			choice.weight = SCORE_FOCUS_ENERGY_CRIT_SYNERGY;
			debugLog(moveName + " has crit synergy: " + SCORE_FOCUS_ENERGY_CRIT_SYNERGY);
		} else {
			choice.weight = SCORE_FOCUS_ENERGY_BASE;
			debugLog(moveName + " base score: " + SCORE_FOCUS_ENERGY_BASE);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleCoachingScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!isDoubleBattle(pw.bc)) {
			choice.weight = SCORE_COACHING_INVALID;
			debugLog(moveName + " not in a double battle: " + SCORE_COACHING_INVALID);
			return true;
		}

		PixelmonWrapper partner = null;
		for (PixelmonWrapper teammate : pw.bc.getTeamPokemon(pw)) {
			if (teammate != pw && !teammate.isFainted()) {
				partner = teammate;
				break;
			}
		}

		if (partner == null) {
			choice.weight = SCORE_COACHING_INVALID;
			debugLog(moveName + " no active partner found: " + SCORE_COACHING_INVALID);
			return true;
		}

		boolean hasContrary = partner.getAbility().getLocalizedName().equals("Contrary");
		if (hasContrary) {
			choice.weight = SCORE_COACHING_INVALID;
			debugLog(moveName + " partner has Contrary ability: " + SCORE_COACHING_INVALID);
			return true;
		}

		float score = SCORE_COACHING_BASE;
		debugLog(moveName + " base score: " + score);

		int attackStage = partner.getBattleStats().getStage(BattleStatsType.ATTACK);
		int defenseStage = partner.getBattleStats().getStage(BattleStatsType.DEFENSE);

		if (attackStage < 2) {
			float attackBonus = 1 - attackStage;
			score += attackBonus;
			debugLog(moveName + " partner Attack stage bonus (stage " + attackStage + "): +" + attackBonus);
		}

		if (defenseStage < 2) {
			float defenseBonus = 1 - defenseStage;
			score += defenseBonus;
			debugLog(moveName + " partner Defense stage bonus (stage " + defenseStage + "): +" + defenseBonus);
		}

		if (random.nextFloat() < COACHING_RANDOM_BONUS_CHANCE) {
			score += SCORE_COACHING_RANDOM_BONUS;
			debugLog(moveName + " random bonus (80% chance): +" + SCORE_COACHING_RANDOM_BONUS);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleContrarySetupMove(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean hasContrary = pw.getAbility().getLocalizedName().equals("Contrary");
		if (!hasContrary) {
			return false;
		}

		debugLog(moveName + " with Contrary ability being evaluated as setup move");

		boolean isHighestDamagingMove = isHighestDamagingMove(pw, choice.attack);
		if (isHighestDamagingMove) {
			debugLog(moveName + " is highest damaging move, treating as normal attack");
			return false;
		}

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		boolean wouldKO = false;

		for (PixelmonWrapper opponent : opponents) {
			boolean originalSimulateMode = pw.bc.simulateMode;
			pw.bc.simulateMode = true;

			try {
				MoveResults result = new MoveResults(opponent);
				choice.attack.saveAttack();
				choice.attack.use(pw, opponent, result);

				if (result.damage >= opponent.getHealth()) {
					wouldKO = true;
					debugLog(moveName + " would KO opponent, treating as normal attack");
				}

				choice.attack.restoreAttack();
			} finally {
				pw.bc.simulateMode = originalSimulateMode;
			}

			if (wouldKO) break;
		}

		if (wouldKO) {
			return false;
		}

		choice.weight = SCORE_CONTRARY_SETUP;
		debugLog(moveName + " with Contrary treated as setup move, score: " + SCORE_CONTRARY_SETUP);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleMeteorBeamScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		boolean hasPowerHerb = pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.powerHerb;

		if (hasPowerHerb) {
			choice.weight = SCORE_METEOR_BEAM_WITH_POWER_HERB;
			debugLog(moveName + " with Power Herb: " + SCORE_METEOR_BEAM_WITH_POWER_HERB);
		} else {
			choice.weight = SCORE_METEOR_BEAM_WITHOUT_POWER_HERB;
			debugLog(moveName + " without Power Herb: " + SCORE_METEOR_BEAM_WITHOUT_POWER_HERB);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleDestinyBondScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		if (opponents.isEmpty()) {
			choice.weight = 0.0f;
			debugLog(moveName + " no opponents found: 0.0");
			return true;
		}

		PixelmonWrapper opponent = opponents.get(0);

		boolean isFaster = isPokemonFaster(pw, opponent);

		boolean wouldDie = false;
		boolean originalSimulateMode = pw.bc.simulateMode;
		pw.bc.simulateMode = true;

		try {
			for (Attack attack : opponent.getMoveset()) {
				if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
					MoveResults result = new MoveResults(pw);
					attack.saveAttack();
					attack.use(opponent, pw, result);

					if (result.damage >= pw.getHealth()) {
						wouldDie = true;
						attack.restoreAttack();
						break;
					}

					attack.restoreAttack();
				}
			}
		} finally {
			pw.bc.simulateMode = originalSimulateMode;
		}

		if (isFaster && wouldDie) {
			if (random.nextFloat() < DESTINY_BOND_FASTER_HIGH_CHANCE) {
				choice.weight = SCORE_DESTINY_BOND_FASTER_AND_DIES_HIGH;
				debugLog(moveName + " faster and would die (high score): " + SCORE_DESTINY_BOND_FASTER_AND_DIES_HIGH);
			} else {
				choice.weight = SCORE_DESTINY_BOND_FASTER_AND_DIES_LOW;
				debugLog(moveName + " faster and would die (low score): " + SCORE_DESTINY_BOND_FASTER_AND_DIES_LOW);
			}
		} else {
			if (random.nextFloat() < DESTINY_BOND_SLOWER_HIGH_CHANCE) {
				choice.weight = SCORE_DESTINY_BOND_SLOWER_HIGH;
				debugLog(moveName + " slower or wouldn't die (high score): " + SCORE_DESTINY_BOND_SLOWER_HIGH);
			} else {
				choice.weight = SCORE_DESTINY_BOND_SLOWER_LOW;
				debugLog(moveName + " slower or wouldn't die (low score): " + SCORE_DESTINY_BOND_SLOWER_LOW);
			}
		}

		return true;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[SetupMoveHandler] " + message);
		}
	}
}
