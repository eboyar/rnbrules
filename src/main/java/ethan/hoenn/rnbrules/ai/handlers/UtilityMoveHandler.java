package ethan.hoenn.rnbrules.ai.handlers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.PokemonUtils.*;

import com.pixelmonmod.pixelmon.api.battles.AttackCategory;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Sturdy;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UtilityMoveHandler {

	private final Random random;
	private final boolean debugMode;

	// Screens
	private static final float SCORE_SCREENS_BASE = 6.0f;
	private static final float SCORE_SCREENS_LIGHT_CLAY_BONUS = 1.0f;
	private static final float SCORE_SCREENS_RELEVANT_MOVES_BONUS = 1.0f;
	private static final float SCREENS_BONUS_CHANCE = 0.5f;
	private static final float SCORE_SCREENS_ALREADY_ACTIVE = -20.0f;
	
	// Aurora Veil 
	private static final float SCORE_AURORA_VEIL_WITH_HAIL = 8.0f;
	private static final float SCORE_AURORA_VEIL_WITHOUT_HAIL = -20.0f;
	private static final float SCORE_AURORA_VEIL_BOTH_MOVES_BONUS = 2.0f;

	// Terrain moves
	private static final float SCORE_TERRAIN_WITH_EXTENDER = 9.0f;
	private static final float SCORE_TERRAIN_DEFAULT = 8.0f;
	private static final float SCORE_TERRAIN_SAME_ACTIVE = -20f;

	// Tailwind
	private static final float SCORE_TAILWIND_WITH_SPEED_DISADVANTAGE = 9.0f;
	private static final float SCORE_TAILWIND_DEFAULT = 5.0f;
	private static final float SCORE_TAILWIND_ALREADY_UP = -20f;

	// Trick Room
	private static final float SCORE_TRICK_ROOM_WITH_SPEED_DISADVANTAGE = 10.0f;
	private static final float SCORE_TRICK_ROOM_DEFAULT = 5.0f;
	private static final float SCORE_TRICK_ROOM_ALREADY_UP = -20.0f;

	// Helping Hand/Follow Me
	private static final float SCORE_HELPING_HAND_FOLLOW_ME_DEFAULT = 6.0f;
	private static final float SCORE_HELPING_HAND_FOLLOW_ME_PARTNER_CONFLICT = -20.0f;

	// Recovery
	private static final float SCORE_RECOVERY_SHOULD_RECOVER = 7.0f;
	private static final float SCORE_RECOVERY_SHOULD_NOT_RECOVER = 5.0f;
	private static final float SCORE_RECOVERY_FULL_HP = -20.0f;
	private static final float SCORE_RECOVERY_HIGH_HP = -6.0f;
	private static final float SCORE_SUN_RECOVERY_WITH_SUN = 7.0f;
	private static final float SCORE_REST_WITH_COUNTERMEASURE = 8.0f;
	private static final float SCORE_REST_WITHOUT_COUNTERMEASURE = 7.0f;
	private static final float HP_HIGH_THRESHOLD = 0.85f;
	private static final float HP_MEDIUM_THRESHOLD = 0.66f;
	private static final float HP_LOW_THRESHOLD = 0.40f;
	private static final float HP_CRITICAL_THRESHOLD = 0.50f;

	// Counter/Mirror Coat
	private static final float SCORE_COUNTER_BASE = 6.0f;
	private static final float SCORE_COUNTER_SURVIVAL_BONUS = 2.0f;
	private static final float SCORE_COUNTER_MATCHING_MOVES_BONUS = 2.0f;
	private static final float SCORE_COUNTER_FASTER_PENALTY = -1.0f;
	private static final float SCORE_COUNTER_STATUS_MOVES_PENALTY = -1.0f;
	private static final float CHANCE_COUNTER_FASTER_PENALTY = 0.25f;
	private static final float CHANCE_COUNTER_STATUS_PENALTY = 0.25f;
	private static final float CHANCE_COUNTER_MATCHING_BONUS = 0.8f;

	// Encore
	private static final float SCORE_ENCORE_AI_FASTER = 7.0f;
	private static final float SCORE_ENCORE_AI_SLOWER_HIGH = 6.0f;
	private static final float SCORE_ENCORE_AI_SLOWER_LOW = 5.0f;
	private static final float SCORE_ENCORE_INVALID_TARGET = -20.0f;

	public UtilityMoveHandler(boolean debugMode) {
		this.random = new Random();
		this.debugMode = debugMode;
	}

	public boolean shouldHandle(Attack attack) {
		return (
			isScreenMove(attack) ||
			isTerrainSettingMove(attack) ||
			isStandardRecoveryMove(attack) ||
			isSunBasedRecoveryMove(attack) ||
			isDrainingMove(attack) ||
			isCounterMove(attack) ||
			attack.getActualMove().isAttack(AttackRegistry.TAILWIND) ||
			attack.getActualMove().isAttack(AttackRegistry.TRICK_ROOM) ||
			attack.getActualMove().isAttack(AttackRegistry.HELPING_HAND) ||
			attack.getActualMove().isAttack(AttackRegistry.FOLLOW_ME) ||
			attack.getActualMove().isAttack(AttackRegistry.REST)
		);
	}

	public boolean handleMove(PixelmonWrapper pw, MoveChoice choice, GlobalStatusController gsc) {
		Attack attack = choice.attack;

		if (isScreenMove(attack)) {
			return handleScreenMoveScoring(pw, choice);
		}

		if (isTerrainSettingMove(attack)) {
			return handleTerrainMoveScoring(pw, choice, gsc);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.TAILWIND)) {
			return handleTailwindScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.TRICK_ROOM)) {
			return handleTrickRoomScoring(pw, choice, gsc);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.HELPING_HAND) || attack.getActualMove().isAttack(AttackRegistry.FOLLOW_ME)) {
			return handleSupportiveMoveScoring(pw, choice);
		}

		if (isStandardRecoveryMove(attack)) {
			return handleStandardRecoveryScoring(pw, choice);
		}

		if (isSunBasedRecoveryMove(attack)) {
			return handleSunBasedRecoveryScoring(pw, choice, gsc);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.REST)) {
			return handleRestScoring(pw, choice);
		}

		if (isCounterMove(attack)) {
			return handleCounterMoveScoring(pw, choice);
		}

		if (isDrainingMove(attack)) {
			return handleDrainingMoveScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.ENCORE)) {
			return handleEncoreScoring(pw, choice);
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleScreenMoveScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isReflect = choice.attack.isAttack(AttackRegistry.REFLECT);
		boolean isLightScreen = choice.attack.isAttack(AttackRegistry.LIGHT_SCREEN);
		boolean isAuroraVeil = choice.attack.isAttack(AttackRegistry.AURORA_VEIL);
		float score = SCORE_SCREENS_BASE;

		// Check if the screen is already active
		if (isReflect && pw.getStatuses().stream().anyMatch(status -> status instanceof Reflect)) {
			choice.weight = SCORE_SCREENS_ALREADY_ACTIVE;
			debugLog(moveName + " scoring (already active): " + SCORE_SCREENS_ALREADY_ACTIVE);
			return true;
		} else if (isLightScreen && pw.getStatuses().stream().anyMatch(status -> status instanceof LightScreen)) {
			choice.weight = SCORE_SCREENS_ALREADY_ACTIVE;
			debugLog(moveName + " scoring (already active): " + SCORE_SCREENS_ALREADY_ACTIVE);
			return true;
		} else if (isAuroraVeil && pw.getStatuses().stream().anyMatch(status -> status instanceof AuroraVeil)) {
			choice.weight = SCORE_SCREENS_ALREADY_ACTIVE;
			debugLog(moveName + " scoring (already active): " + SCORE_SCREENS_ALREADY_ACTIVE);
			return true;
		}

		// Aurora Veil special handling - requires hail/snow
		if (isAuroraVeil) {
			return handleAuroraVeilScoring(pw, choice);
		}

		// Check for relevant opponent moves
		boolean hasRelevantMoves = false;
		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (isReflect && hasPhysicalAttackingMoves(opponent)) {
				hasRelevantMoves = true;
				break;
			} else if (isLightScreen && hasSpecialAttackingMoves(opponent)) {
				hasRelevantMoves = true;
				break;
			}
		}

		if (hasRelevantMoves) {
			score += SCORE_SCREENS_RELEVANT_MOVES_BONUS;
			debugLog(moveName + " bonus for relevant opponent moves: +" + SCORE_SCREENS_RELEVANT_MOVES_BONUS);
		}

		if (pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.lightClay) {
			score += SCORE_SCREENS_LIGHT_CLAY_BONUS;
			debugLog(moveName + " Light Clay bonus: +" + SCORE_SCREENS_LIGHT_CLAY_BONUS);
		}

		if (random.nextFloat() < SCREENS_BONUS_CHANCE) {
			score += 1.0f;
			debugLog(moveName + " random additional bonus: +1.0");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleAuroraVeilScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		
		boolean isHailOrSnow = pw.bc.globalStatusController.getWeather() instanceof Hail || 
				pw.bc.globalStatusController.getWeather() instanceof Snow;
		
		if (!isHailOrSnow) {
			choice.weight = SCORE_AURORA_VEIL_WITHOUT_HAIL;
			debugLog(moveName + " scoring (no hail/snow): " + SCORE_AURORA_VEIL_WITHOUT_HAIL);
			return true;
		}

		float score = SCORE_AURORA_VEIL_WITH_HAIL;
		debugLog(moveName + " base score with hail/snow: " + SCORE_AURORA_VEIL_WITH_HAIL);

		boolean hasPhysicalMoves = false;
		boolean hasSpecialMoves = false;
		
		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (hasPhysicalAttackingMoves(opponent)) {
				hasPhysicalMoves = true;
			}
			if (hasSpecialAttackingMoves(opponent)) {
				hasSpecialMoves = true;
			}
		}

		if (hasPhysicalMoves && hasSpecialMoves) {
			score += SCORE_AURORA_VEIL_BOTH_MOVES_BONUS;
			debugLog(moveName + " bonus for both physical and special opponent moves: +" + SCORE_AURORA_VEIL_BOTH_MOVES_BONUS);
		} else if (hasPhysicalMoves || hasSpecialMoves) {
			score += SCORE_SCREENS_RELEVANT_MOVES_BONUS;
			debugLog(moveName + " bonus for relevant opponent moves: +" + SCORE_SCREENS_RELEVANT_MOVES_BONUS);
		}

		if (pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.lightClay) {
			score += SCORE_SCREENS_LIGHT_CLAY_BONUS;
			debugLog(moveName + " Light Clay bonus: +" + SCORE_SCREENS_LIGHT_CLAY_BONUS);
		}

		if (random.nextFloat() < SCREENS_BONUS_CHANCE) {
			score += 1.0f;
			debugLog(moveName + " random additional bonus: +1.0");
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleTerrainMoveScoring(PixelmonWrapper pw, MoveChoice choice, GlobalStatusController gsc) {
		Attack attack = choice.attack;
		String moveName = attack.getActualMove().getAttackName();

		if (attack.getActualMove().isAttack(AttackRegistry.ELECTRIC_TERRAIN) && gsc.getTerrain() instanceof ElectricTerrain) {
			choice.weight = SCORE_TERRAIN_SAME_ACTIVE;
			debugLog(moveName + " scoring (same already active): " + SCORE_TERRAIN_SAME_ACTIVE);
			return true;
		} else if (attack.getActualMove().isAttack(AttackRegistry.MISTY_TERRAIN) && gsc.getTerrain() instanceof MistyTerrain) {
			choice.weight = SCORE_TERRAIN_SAME_ACTIVE;
			debugLog(moveName + " scoring (same already active): " + SCORE_TERRAIN_SAME_ACTIVE);
			return true;
		} else if (attack.getActualMove().isAttack(AttackRegistry.GRASSY_TERRAIN) && gsc.getTerrain() instanceof GrassyTerrain) {
			choice.weight = SCORE_TERRAIN_SAME_ACTIVE;
			debugLog(moveName + " scoring (same already active): " + SCORE_TERRAIN_SAME_ACTIVE);
			return true;
		} else if (attack.getActualMove().isAttack(AttackRegistry.PSYCHIC_TERRAIN) && gsc.getTerrain() instanceof PsychicTerrain) {
			choice.weight = SCORE_TERRAIN_SAME_ACTIVE;
			debugLog(moveName + " scoring (same already active): " + SCORE_TERRAIN_SAME_ACTIVE);
			return true;
		}

		boolean hasTerrainExtender = pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.terrainExtender;

		if (hasTerrainExtender) {
			choice.weight = SCORE_TERRAIN_WITH_EXTENDER;
			debugLog(moveName + " scoring (with Terrain Extender): " + SCORE_TERRAIN_WITH_EXTENDER);
			return true;
		} else {
			choice.weight = SCORE_TERRAIN_DEFAULT;
			debugLog(moveName + " scoring (default): " + SCORE_TERRAIN_DEFAULT);
		}

		return true;
	}

	private boolean handleTailwindScoring(PixelmonWrapper pw, MoveChoice choice) {
		List<PixelmonWrapper> allPokemon = new ArrayList<>(pw.bc.getActivePokemon());
		List<PixelmonWrapper> allyTeam = new ArrayList<>();
		List<PixelmonWrapper> enemyTeam = new ArrayList<>();

		for (PixelmonWrapper wrapper : allPokemon) {
			if (wrapper.isAlly(pw)) allyTeam.add(wrapper);
			else enemyTeam.add(wrapper);
		}

		boolean hasSpeedDisadvantage = false;

		for (PixelmonWrapper ally : allyTeam) {
			for (PixelmonWrapper enemy : enemyTeam) {
				if (isPokemonFaster(enemy, ally)) {
					hasSpeedDisadvantage = true;
					debugLog("Tailwind: " + ally.getPokemonName() + " is slower than " + enemy.getPokemonName());
					break;
				}
			}
			if (hasSpeedDisadvantage) {
				break;
			}
		}

		if (hasSpeedDisadvantage) {
			choice.weight = SCORE_TAILWIND_WITH_SPEED_DISADVANTAGE;
			debugLog("Tailwind scoring (speed disadvantage): " + SCORE_TAILWIND_WITH_SPEED_DISADVANTAGE);
		} else if (pw.getStatuses().stream().anyMatch(status -> status instanceof Tailwind)) {
			choice.weight = SCORE_TAILWIND_ALREADY_UP;
			debugLog("Tailwind scoring (already up): " + SCORE_TAILWIND_ALREADY_UP);
		} else {
			choice.weight = SCORE_TAILWIND_DEFAULT;
			debugLog("Tailwind scoring (no speed disadvantage): " + SCORE_TAILWIND_DEFAULT);
		}

		return true;
	}

	private boolean handleTrickRoomScoring(PixelmonWrapper pw, MoveChoice choice, GlobalStatusController gsc) {
		List<PixelmonWrapper> allPokemon = new ArrayList<>(pw.bc.getActivePokemon());
		List<PixelmonWrapper> allyTeam = new ArrayList<>();
		List<PixelmonWrapper> enemyTeam = new ArrayList<>();

		for (PixelmonWrapper wrapper : allPokemon) {
			if (wrapper.isAlly(pw)) allyTeam.add(wrapper);
			else enemyTeam.add(wrapper);
		}

		boolean hasSpeedDisadvantage = false;

		for (PixelmonWrapper ally : allyTeam) {
			for (PixelmonWrapper enemy : enemyTeam) {
				if (isPokemonFaster(enemy, ally)) {
					hasSpeedDisadvantage = true;
					debugLog("Trick Room: " + ally.getPokemonName() + " is slower than " + enemy.getPokemonName());
					break;
				}
			}
			if (hasSpeedDisadvantage) {
				break;
			}
		}

		if (hasSpeedDisadvantage) {
			choice.weight = SCORE_TRICK_ROOM_WITH_SPEED_DISADVANTAGE;
			debugLog("Trick Room scoring (speed disadvantage): " + SCORE_TRICK_ROOM_WITH_SPEED_DISADVANTAGE);
		} else if (gsc.getGlobalStatuses().stream().anyMatch(status -> status instanceof TrickRoom)) {
			choice.weight = SCORE_TRICK_ROOM_ALREADY_UP;
			debugLog("Trick Room scoring (already up): " + SCORE_TRICK_ROOM_ALREADY_UP);
		} else {
			choice.weight = SCORE_TRICK_ROOM_DEFAULT;
			debugLog("Trick Room scoring (no speed disadvantage): " + SCORE_TRICK_ROOM_DEFAULT);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSupportiveMoveScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!isDoubleBattle(pw.bc)) {
			choice.weight = 0.0f;
			debugLog(moveName + " scoring (not a double battle): 0.0");
			return true;
		}

		List<PixelmonWrapper> adjacent = pw.bc.getAdjacentPokemon(pw);
		PixelmonWrapper ally = adjacent.isEmpty() ? null : adjacent.get(0);

		if (ally == null) {
			choice.weight = 0.0f;
			debugLog(moveName + " scoring (no ally present): 0.0");
			return true;
		}

		boolean allyUsingConflictingMove = false;

		if (ally.selectedAttack != null) {
			boolean allyUsingHelpingHand = ally.selectedAttack.getActualMove().isAttack(AttackRegistry.HELPING_HAND);
			boolean allyUsingFollowMe = ally.selectedAttack.getActualMove().isAttack(AttackRegistry.FOLLOW_ME);
			boolean allyUsingStatusMove = ally.selectedAttack.getActualMove().getAttackCategory() == com.pixelmonmod.pixelmon.api.battles.AttackCategory.STATUS;

			allyUsingConflictingMove = allyUsingHelpingHand || allyUsingFollowMe || allyUsingStatusMove;
		}

		if (allyUsingConflictingMove) {
			choice.weight = SCORE_HELPING_HAND_FOLLOW_ME_PARTNER_CONFLICT;
			debugLog(moveName + " scoring (partner using conflicting move): " + SCORE_HELPING_HAND_FOLLOW_ME_PARTNER_CONFLICT);
		} else {
			choice.weight = SCORE_HELPING_HAND_FOLLOW_ME_DEFAULT;
			debugLog(moveName + " scoring (default): " + SCORE_HELPING_HAND_FOLLOW_ME_DEFAULT);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleStandardRecoveryScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float currentHpPercent = pw.getHealthPercent();

		if (currentHpPercent >= 1.0f) {
			choice.weight = SCORE_RECOVERY_FULL_HP;
			debugLog(moveName + " scoring (full HP): " + SCORE_RECOVERY_FULL_HP);
			return true;
		}

		if (currentHpPercent >= HP_HIGH_THRESHOLD) {
			choice.weight = SCORE_RECOVERY_HIGH_HP;
			debugLog(moveName + " scoring (high HP): " + SCORE_RECOVERY_HIGH_HP);
			return true;
		}

		boolean shouldRecover = shouldAIRecover(pw, (float) 0.5);

		if (shouldRecover) {
			choice.weight = SCORE_RECOVERY_SHOULD_RECOVER;
			debugLog(moveName + " scoring (should recover): " + SCORE_RECOVERY_SHOULD_RECOVER);
		} else {
			choice.weight = SCORE_RECOVERY_SHOULD_NOT_RECOVER;
			debugLog(moveName + " scoring (should not recover): " + SCORE_RECOVERY_SHOULD_NOT_RECOVER);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSunBasedRecoveryScoring(PixelmonWrapper pw, MoveChoice choice, GlobalStatusController gsc) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float currentHpPercent = pw.getHealthPercent();

		if (currentHpPercent >= 1.0f) {
			choice.weight = SCORE_RECOVERY_FULL_HP;
			debugLog(moveName + " scoring (full HP): " + SCORE_RECOVERY_FULL_HP);
			return true;
		}

		if (currentHpPercent >= HP_HIGH_THRESHOLD) {
			choice.weight = SCORE_RECOVERY_HIGH_HP;
			debugLog(moveName + " scoring (high HP): " + SCORE_RECOVERY_HIGH_HP);
			return true;
		}

		boolean isSunny = gsc.getWeather() instanceof Sunny;
		float recoveryPercentage = isSunny ? 0.67f : 0.5f;

		boolean shouldRecover = shouldAIRecover(pw, recoveryPercentage);

		if (isSunny && shouldRecover) {
			choice.weight = SCORE_SUN_RECOVERY_WITH_SUN;
			debugLog(moveName + " scoring (sun active and should recover): " + SCORE_SUN_RECOVERY_WITH_SUN);
		} else {
			boolean shouldRecoverWithStandardHealing = shouldAIRecover(pw, 0.5f);

			if (shouldRecoverWithStandardHealing) {
				choice.weight = SCORE_RECOVERY_SHOULD_RECOVER;
				debugLog(moveName + " scoring (should recover with standard healing): " + SCORE_RECOVERY_SHOULD_RECOVER);
			} else {
				choice.weight = SCORE_RECOVERY_SHOULD_NOT_RECOVER;
				debugLog(moveName + " scoring (should not recover with standard healing): " + SCORE_RECOVERY_SHOULD_NOT_RECOVER);
			}
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleRestScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float currentHpPercent = pw.getHealthPercent();

		if (currentHpPercent >= 1.0f) {
			choice.weight = SCORE_RECOVERY_FULL_HP;
			debugLog(moveName + " scoring (full HP): " + SCORE_RECOVERY_FULL_HP);
			return true;
		}

		if (currentHpPercent >= HP_HIGH_THRESHOLD) {
			choice.weight = SCORE_RECOVERY_HIGH_HP;
			debugLog(moveName + " scoring (high HP): " + SCORE_RECOVERY_HIGH_HP);
			return true;
		}

		boolean shouldRecover = shouldAIRecover(pw, 1.0f);

		if (shouldRecover) {
			boolean hasSleepCounter = hasSleepCountermeasures(pw);

			if (hasSleepCounter) {
				choice.weight = SCORE_REST_WITH_COUNTERMEASURE;
				debugLog(moveName + " scoring (should recover with sleep counter): " + SCORE_REST_WITH_COUNTERMEASURE);
			} else {
				choice.weight = SCORE_REST_WITHOUT_COUNTERMEASURE;
				debugLog(moveName + " scoring (should recover without sleep counter): " + SCORE_REST_WITHOUT_COUNTERMEASURE);
			}
		} else {
			choice.weight = SCORE_RECOVERY_SHOULD_NOT_RECOVER;
			debugLog(moveName + " scoring (should not recover): " + SCORE_RECOVERY_SHOULD_NOT_RECOVER);
		}

		return true;
	}

	private boolean shouldAIRecover(PixelmonWrapper pw, float recoveryPercentage) {
		if (pw.getStatuses().stream().anyMatch(status -> status instanceof PoisonBadly)) {
			debugLog("AI is badly poisoned, should not recover");
			return false;
		}

		float currentHpPercent = pw.getHealthPercent();
		int maxHealth = pw.getMaxHealth();
		int currentHealth = pw.getHealth();
		int healAmount = (int) (maxHealth * recoveryPercentage);

		List<PixelmonWrapper> opponents = pw.getOpponentPokemon();
		if (opponents.isEmpty()) return false;
		PixelmonWrapper opponent = opponents.get(0);

		int maxDamage = estimateMaxDamage(opponent, pw);
		if (maxDamage >= healAmount) {
			debugLog("Opponent can deal " + maxDamage + " damage, which exceeds healing amount " + healAmount);
			return false;
		}

		boolean isFaster = isPokemonFaster(pw, opponent);

		if (isFaster) {
			if (currentHealth <= maxDamage && (currentHealth + healAmount) > maxDamage) {
				debugLog("Recovery would prevent a KO");
				return true;
			}

			if (currentHpPercent < HP_MEDIUM_THRESHOLD && currentHpPercent > HP_LOW_THRESHOLD) {
				boolean shouldRecover = random.nextBoolean();
				debugLog("Health between " + HP_LOW_THRESHOLD + " and " + HP_MEDIUM_THRESHOLD + ", recovery decision: " + shouldRecover);
				return shouldRecover;
			}

			if (currentHpPercent < HP_LOW_THRESHOLD) {
				debugLog("Health below " + HP_LOW_THRESHOLD + ", should recover");
				return true;
			}
		} else {
			if (currentHpPercent < 0.7f) {
				boolean shouldRecover = random.nextFloat() < 0.75f;
				debugLog("AI is slower, health below 70%, recovery decision (75% chance): " + shouldRecover);
				return shouldRecover;
			}

			if (currentHpPercent < HP_CRITICAL_THRESHOLD) {
				debugLog("AI is slower, health below " + HP_CRITICAL_THRESHOLD + ", should recover");
				return true;
			}
		}

		debugLog("No recovery condition met, defaulting to false");
		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleDrainingMoveScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		float currentHpPercent = pw.getHealthPercent();

		if (choice.targets == null || choice.targets.isEmpty()) {
			choice.weight = 0.0f;
			debugLog(moveName + " scoring (no valid target): 0.0");
			return false;
		}

		PixelmonWrapper target = choice.targets.get(0);

		if (moveName.equals("Dream Eater")) {
			if (target.getStatuses().stream().noneMatch(status -> status instanceof Sleep)) {
				choice.weight = 0.0f;
				debugLog(moveName + " scoring (target not asleep): 0.0");
				return false;
			}
		}

		boolean isFaster = isPokemonFaster(pw, target);
		debugLog(moveName + " speed check: AI " + (isFaster ? "is faster" : "is slower") + " than target");

		int estimatedDamage = estimateMaxDamageOfMove(choice.attack, pw, target);
		float healingFraction = getDrainHealingFraction(choice.attack);
		int estimatedHealing = (int) (estimatedDamage * healingFraction);

		debugLog(moveName + " estimated damage: " + estimatedDamage + ", estimated healing: " + estimatedHealing);

		if (currentHpPercent >= 1.0f) {
			choice.weight = SCORE_RECOVERY_FULL_HP;
			debugLog(moveName + " scoring (full HP): " + SCORE_RECOVERY_FULL_HP);
			return false;
		}

		int opponentMaxDamage = estimateMaxDamage(target, pw);
		debugLog(moveName + " opponent max damage potential: " + opponentMaxDamage);

		if (!isFaster && opponentMaxDamage >= pw.getHealth()) {
			debugLog(moveName + " opponent is faster and can KO, draining move useless for recovery");

			choice.weight = SCORE_RECOVERY_SHOULD_NOT_RECOVER;
			return false;
		}

		boolean shouldUseForHealing = shouldAIUseDrainingMove(pw, target, estimatedHealing, isFaster);

		if (shouldUseForHealing) {
			choice.weight = SCORE_RECOVERY_SHOULD_RECOVER;
			debugLog(moveName + " scoring (should use for healing): " + SCORE_RECOVERY_SHOULD_RECOVER);
			return true;
		} else if (currentHpPercent >= HP_HIGH_THRESHOLD) {
			choice.weight = SCORE_RECOVERY_HIGH_HP;
			debugLog(moveName + " scoring (high HP): " + SCORE_RECOVERY_HIGH_HP);
			return true;
		} else {
			choice.weight = SCORE_RECOVERY_SHOULD_NOT_RECOVER;
			debugLog(moveName + " scoring (using as normal attack): " + SCORE_RECOVERY_SHOULD_NOT_RECOVER);
			return false;
		}
	}

	private boolean shouldAIUseDrainingMove(PixelmonWrapper pw, PixelmonWrapper target, int estimatedHealing, boolean isFaster) {
		float currentHpPercent = pw.getHealthPercent();
		int currentHealth = pw.getHealth();
		int opponentMaxDamage = estimateMaxDamage(target, pw);

		if (currentHpPercent < HP_LOW_THRESHOLD) {
			if (isFaster || currentHealth > opponentMaxDamage) {
				debugLog("Health below " + HP_LOW_THRESHOLD + ", should use draining move");
				return true;
			} else {
				debugLog("Health below " + HP_LOW_THRESHOLD + ", but opponent is faster and can deal significant damage");
				return false;
			}
		}

		if (isFaster && currentHealth <= opponentMaxDamage && (currentHealth + estimatedHealing) > opponentMaxDamage) {
			debugLog("Draining move would prevent a KO");
			return true;
		}

		if (currentHpPercent < HP_MEDIUM_THRESHOLD && currentHpPercent >= HP_LOW_THRESHOLD) {
			float probability = isFaster ? 0.6f : 0.4f;
			boolean shouldUse = random.nextFloat() < probability;
			debugLog("Health between " + HP_LOW_THRESHOLD + " and " + HP_MEDIUM_THRESHOLD + ", draining move decision (" + (probability * 100) + "% chance): " + shouldUse);
			return shouldUse;
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleCounterMoveScoring(PixelmonWrapper pw, MoveChoice choice) {
		boolean isCounter = choice.attack.getActualMove().isAttack(AttackRegistry.COUNTER);
		String moveName = isCounter ? "Counter" : "Mirror Coat";
		AttackCategory targetCategory = isCounter ? com.pixelmonmod.pixelmon.api.battles.AttackCategory.PHYSICAL : com.pixelmonmod.pixelmon.api.battles.AttackCategory.SPECIAL;

		float score = SCORE_COUNTER_BASE;
		debugLog(moveName + " base scoring: " + SCORE_COUNTER_BASE);

		if (pw.getOpponentPokemon().isEmpty()) {
			choice.weight = score;
			return true;
		}

		PixelmonWrapper opponent = pw.getOpponentPokemon().get(0);

		boolean opponentCanKO = wouldBeKOdThisTurn(pw);
		boolean hasSurvivalMechanism = pw.getAbility() instanceof Sturdy || (pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == EnumHeldItems.focussash && pw.getHealthPercent() == 1.0f);

		boolean opponentOnlyHasMatchingMoves = true;
		boolean opponentHasStatusMoves = false;

		for (Attack opponentAttack : opponent.getMoveset()) {
			if (opponentAttack != null) {
				if (opponentAttack.getActualMove().getAttackCategory() != targetCategory) {
					if (opponentAttack.getActualMove().getAttackCategory() == com.pixelmonmod.pixelmon.api.battles.AttackCategory.STATUS) {
						opponentHasStatusMoves = true;
					} else {
						opponentOnlyHasMatchingMoves = false;
					}
				}
			}
		}

		if (opponentCanKO && hasSurvivalMechanism && opponentOnlyHasMatchingMoves) {
			score += SCORE_COUNTER_SURVIVAL_BONUS;
			debugLog(moveName + " survival bonus: +" + SCORE_COUNTER_SURVIVAL_BONUS);
		}

		if (opponentOnlyHasMatchingMoves && !opponentCanKO) {
			if (random.nextFloat() < CHANCE_COUNTER_MATCHING_BONUS) {
				score += SCORE_COUNTER_MATCHING_MOVES_BONUS;
				debugLog(moveName + " matching moves bonus: +" + SCORE_COUNTER_MATCHING_MOVES_BONUS);
			} else {
				debugLog(moveName + " matching moves bonus skipped (random)");
			}
		}

		if (isPokemonFaster(pw, opponent) && random.nextFloat() < CHANCE_COUNTER_FASTER_PENALTY) {
			score += SCORE_COUNTER_FASTER_PENALTY;
			debugLog(moveName + " faster penalty: " + SCORE_COUNTER_FASTER_PENALTY);
		}

		if (opponentHasStatusMoves && random.nextFloat() < CHANCE_COUNTER_STATUS_PENALTY) {
			score += SCORE_COUNTER_STATUS_MOVES_PENALTY;
			debugLog(moveName + " status moves penalty: " + SCORE_COUNTER_STATUS_MOVES_PENALTY);
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleEncoreScoring(PixelmonWrapper pw, MoveChoice choice) {
		if (choice.targets == null || choice.targets.isEmpty()) {
			choice.weight = SCORE_ENCORE_INVALID_TARGET;
			debugLog("Encore scoring (no target): " + SCORE_ENCORE_INVALID_TARGET);
			return true;
		}

		PixelmonWrapper target = choice.targets.get(0);

		if (isFirstTurn(target) || target.getStatuses().stream().anyMatch(status -> status instanceof com.pixelmonmod.pixelmon.battles.status.Encore)) {
			choice.weight = SCORE_ENCORE_INVALID_TARGET;
			debugLog("Encore scoring (first turn or already encored): " + SCORE_ENCORE_INVALID_TARGET);
			return true;
		}

		if (target.lastAttack != null && target.lastAttack.getActualMove().getBasePower() <= 0) {
			boolean isFaster = isPokemonFaster(pw, target);

			if (isFaster) {
				choice.weight = SCORE_ENCORE_AI_FASTER;
				debugLog("Encore scoring (faster AI, non-damaging move): " + SCORE_ENCORE_AI_FASTER);
			} else {
				if (random.nextBoolean()) {
					choice.weight = SCORE_ENCORE_AI_SLOWER_HIGH;
					debugLog("Encore scoring (slower AI, non-damaging move, high): " + SCORE_ENCORE_AI_SLOWER_HIGH);
				} else {
					choice.weight = SCORE_ENCORE_AI_SLOWER_LOW;
					debugLog("Encore scoring (slower AI, non-damaging move, low): " + SCORE_ENCORE_AI_SLOWER_LOW);
				}
			}
		} else {
			choice.weight = SCORE_ENCORE_INVALID_TARGET;
			debugLog("Encore scoring (damaging or null last move): " + SCORE_ENCORE_INVALID_TARGET);
		}

		return true;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[UtilityMoveHandler] " + message);
		}
	}
}
