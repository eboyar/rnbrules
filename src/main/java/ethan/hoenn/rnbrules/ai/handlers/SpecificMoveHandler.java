package ethan.hoenn.rnbrules.ai.handlers;

import static ethan.hoenn.rnbrules.ai.utils.BattleUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.MoveUtils.*;
import static ethan.hoenn.rnbrules.ai.utils.PokemonUtils.*;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.ClearBody;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.FullMetalBody;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Infiltrator;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.WhiteSmoke;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpecificMoveHandler {

	private final BattleController bc;
	private final Random random;
	private final boolean debugMode;

	// Trapping
	private static final float SCORE_TRAPPING_MOVE_NORMAL = 6.0f;
	private static final float SCORE_TRAPPING_MOVE_BOOSTED = 8.0f;

	// Damaging stat-change
	private static final float SCORE_SPEED_REDUCTION_SLOWER = 6.0f;
	private static final float SCORE_SPEED_REDUCTION_OTHER = 5.0f;
	private static final float SCORE_SPEED_REDUCTION_DOUBLE_BATTLE_BONUS = 1.0f;
	private static final float SCORE_STAT_REDUCTION_RELEVANT = 6.0f;
	private static final float SCORE_STAT_REDUCTION_OTHER = 5.0f;
	private static final float SCORE_STAT_REDUCTION_SPREAD_BONUS = 1.0f;
	private static final float SCORE_ACID_SPRAY_BONUS = 6.0f;

	// Protect-like
	private static final float SCORE_PROTECT_BASE = 6.0f;
	private static final float SCORE_PROTECT_AI_STATUS_PENALTY = -2.0f;
	private static final float SCORE_PROTECT_PLAYER_STATUS_BONUS = 1.0f;
	private static final float SCORE_PROTECT_FIRST_TURN_PENALTY = -1.0f;
	private static final float SCORE_PROTECT_USED_LAST_TURN_PENALTY = -20.0f;
	private static final float SCORE_PROTECT_RECEIVER_HUGE_POWER_BONUS = 8.0f;

	// Rollout
	private static final float SCORE_ROLLOUT = 7.0f;

	// EQ in doubles
	private static final float SCORE_EQ_SAFE_ALLY_BONUS = 2.0f;
	private static final float SCORE_EQ_VULNERABLE_ALLY_PENALTY = -10.0f;
	private static final float SCORE_EQ_ALLY_PENALTY = -3.0f;

	// Future Sight
	private static final float SCORE_FUTURE_SIGHT_FASTER_AND_KOD = 8.0f;
	private static final float SCORE_FUTURE_SIGHT_DEFAULT = 6.0f;

	// Relic Song
	private static final float SCORE_RELIC_SONG_BASE_FORM = 10.0f;
	private static final float SCORE_RELIC_SONG_PIROUETTE_FORM = -20.0f;

	// Sucker Punch
	private static final float SCORE_SUCKER_PUNCH_PENALTY = -20.0f;
	private static final float SUCKER_PUNCH_PENALTY_CHANCE = 0.5f;

	// Fake Out
	private static final float SCORE_FAKE_OUT_FIRST_TURN = 9.0f;
	private static final float SCORE_FAKE_OUT_NOT_FIRST_TURN = -20.0f;

	// Baton Pass
	private static final float SCORE_BATON_PASS_WITH_ADVANTAGE = 14.0f;
	private static final float SCORE_BATON_PASS_LAST_MON = -20.0f;
	private static final float SCORE_BATON_PASS_NO_ADVANTAGE = 0.0f;

	// Trick
	private static final float SCORE_TRICK_WITH_HARMFUL_ORB_LOW = 6.0f;
	private static final float SCORE_TRICK_WITH_HARMFUL_ORB_HIGH = 7.0f;
	private static final float SCORE_TRICK_WITH_HARMFUL_ITEM = 7.0f;
	private static final float SCORE_TRICK_DEFAULT = 5.0f;
	private static final float TRICK_HARMFUL_ORB_HIGH_CHANCE = 0.5f;

	// Substitute
	private static final float SCORE_SUBSTITUTE_BASE = 6.0f;
	private static final float SCORE_SUBSTITUTE_ASLEEP_BONUS = 2.0f;
	private static final float SCORE_SUBSTITUTE_LEECH_SEED_BONUS = 2.0f;
	private static final float SCORE_SUBSTITUTE_RANDOM_PENALTY = -1.0f;
	private static final float SCORE_SUBSTITUTE_SOUND_MOVE_PENALTY = -8.0f;
	private static final float SCORE_SUBSTITUTE_LOW_HP_PENALTY = -20.0f;
	private static final float SUBSTITUTE_RANDOM_PENALTY_CHANCE = 0.5f;

	// BOOM
	private static final float SCORE_BOOM_MOVE_VERY_LOW_HP = 10.0f;
	private static final float SCORE_BOOM_MOVE_LOW_HP = 8.0f;
	private static final float SCORE_BOOM_MOVE_MID_HP = 7.0f;
	private static final float SCORE_BOOM_MOVE_LAST_MON_VS_LAST_MON = -1.0f;
	private static final float BOOM_MOVE_LOW_HP_NO_BONUS_CHANCE = 0.3f;
	private static final float BOOM_MOVE_MID_HP_NO_BONUS_CHANCE = 0.5f;
	private static final float BOOM_MOVE_HIGH_HP_BONUS_CHANCE = 0.05f;

	// Memento
	private static final float SCORE_MEMENTO_VERY_LOW_HP = 16.0f;
	private static final float SCORE_MEMENTO_LOW_HP = 14.0f;
	private static final float SCORE_MEMENTO_MID_HP = 13.0f;
	private static final float SCORE_MEMENTO_DEFAULT = 6.0f;
	private static final float MEMENTO_LOW_HP_NO_BONUS_CHANCE = 0.3f;
	private static final float MEMENTO_MID_HP_NO_BONUS_CHANCE = 0.5f;
	private static final float MEMENTO_HIGH_HP_BONUS_CHANCE = 0.05f;

	// Pursuit
	private static final float SCORE_PURSUIT_KO = 10.0f;
	private static final float SCORE_PURSUIT_LOW_HP = 10.0f;
	private static final float SCORE_PURSUIT_MED_HP = 8.0f;
	private static final float SCORE_PURSUIT_FASTER_BONUS = 3.0f;
	private static final float PURSUIT_MED_HP_CHANCE = 0.5f;

	// Final Gambit
	private static final float SCORE_FINAL_GAMBIT_HIGH_HP_FASTER = 8.0f;
	private static final float SCORE_FINAL_GAMBIT_DIES_ANYWAY_FASTER = 7.0f;
	private static final float SCORE_FINAL_GAMBIT_DEFAULT = 6.0f;

	// Fell Stinger
	private static final float SCORE_FELL_STINGER_KO_FASTER_NORMAL = 21.0f;
	private static final float SCORE_FELL_STINGER_KO_FASTER_BOOSTED = 23.0f;
	private static final float SCORE_FELL_STINGER_KO_SLOWER_NORMAL = 15.0f;
	private static final float SCORE_FELL_STINGER_KO_SLOWER_BOOSTED = 17.0f;

	// Fling
	private static final float SCORE_FLING_SPEED_BOOST_WITH_WP_AND_SE = 12.0f;
	private static final float SCORE_FLING_SPEED_BOOST_DEFAULT = 9.0f;

	// Role Play
	private static final float SCORE_ROLE_PLAY_GOOD_ABILITY = 9.0f;
	private static final float SCORE_ROLE_PLAY_BAD_CASE = -20.0f;

	// Imprison
	private static final float SCORE_IMPRISON_HAS_COMMON_MOVES = 9.0f;
	private static final float SCORE_IMPRISON_NO_COMMON_MOVES = -20.0f;

	// Priority moves
	private static final float SCORE_PRIORITY_ALLY_WP_SE = 12.0f;

	public SpecificMoveHandler(boolean debugMode, BattleController bc) {
		this.random = new Random();
		this.bc = bc;
		this.debugMode = debugMode;
	}

	public boolean shouldHandle(Attack attack) {
		return (
			isProtectMove(attack) ||
			isBoomMove(attack) ||
			isTrappingMove(attack) ||
			isSpeedReductionMove(attack) ||
			isAttackReductionMove(attack) ||
			isSpecialAttackReductionMove(attack) ||
			isAcidSprayLike(attack) ||
			attack.getActualMove().isAttack(AttackRegistry.ROLLOUT) ||
			attack.getActualMove().isAttack(AttackRegistry.EARTHQUAKE) ||
			attack.getActualMove().isAttack(AttackRegistry.MAGNITUDE) ||
			attack.getActualMove().isAttack(AttackRegistry.FUTURE_SIGHT) ||
			attack.getActualMove().isAttack(AttackRegistry.RELIC_SONG) ||
			attack.getActualMove().isAttack(AttackRegistry.SUCKER_PUNCH) ||
			attack.getActualMove().isAttack(AttackRegistry.FAKE_OUT) ||
			attack.getActualMove().isAttack(AttackRegistry.BATON_PASS) ||
			attack.getActualMove().isAttack(AttackRegistry.TRICK) ||
			attack.getActualMove().isAttack(AttackRegistry.SWITCHEROO) ||
			attack.getActualMove().isAttack(AttackRegistry.SUBSTITUTE) ||
			attack.getActualMove().isAttack(AttackRegistry.MEMENTO) ||
			attack.getActualMove().isAttack(AttackRegistry.PURSUIT) ||
			attack.getActualMove().isAttack(AttackRegistry.FINAL_GAMBIT) ||
			attack.getActualMove().isAttack(AttackRegistry.FELL_STINGER) ||
			attack.getActualMove().isAttack(AttackRegistry.FLING) ||
			attack.getActualMove().isAttack(AttackRegistry.ROLE_PLAY) ||
			attack.getActualMove().isAttack(AttackRegistry.IMPRISON) ||
			isTypedPriorityMove(attack)
		);
	}

	public boolean handleMove(PixelmonWrapper pw, MoveChoice choice) {
		Attack attack = choice.attack;
		PixelmonWrapper target = !choice.targets.isEmpty() ? choice.targets.get(0) : null;
		boolean boosted = random.nextFloat() < 0.2f;

		if (isTrappingMove(attack)) {
			return handleTrappingMoveScoring(pw, choice, boosted);
		}

		if (isSpeedReductionMove(attack) && target != null) {
			return handleSpeedReductionScoring(pw, choice, target);
		}

		if ((isAttackReductionMove(attack) || isSpecialAttackReductionMove(attack)) && target != null) {
			return handleAttackReductionScoring(pw, choice, target);
		}

		if (isAcidSprayLike(attack)) {
			return handleAcidSprayScoring(pw, choice);
		}

		if (isProtectMove(attack)) {
			return handleProtectMoveScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.ROLLOUT)) {
			choice.weight = SCORE_ROLLOUT;
			debugLog("Rollout base score: " + choice.weight);
			return true;
		}

		if ((attack.getActualMove().isAttack(AttackRegistry.EARTHQUAKE) || attack.getActualMove().isAttack(AttackRegistry.MAGNITUDE)) && isDoubleBattle(pw.bc) && target != null) {
			return handleEarthquakeScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FUTURE_SIGHT) && target != null) {
			return handleFutureSightScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.RELIC_SONG)) {
			return handleRelicSongScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.SUCKER_PUNCH)) {
			return handleSuckerPunchScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FAKE_OUT)) {
			return handleFakeOutScoring(pw, choice);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.BATON_PASS)) {
			return handleBatonPassScoring(pw, choice);
		}

		if ((attack.getActualMove().isAttack(AttackRegistry.TRICK) || attack.getActualMove().isAttack(AttackRegistry.SWITCHEROO)) && target != null) {
			return handleTrickScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.SUBSTITUTE)) {
			return handleSubstituteScoring(pw, choice, target);
		}

		if (isBoomMove(attack)) {
			return handleBoomMoveScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.MEMENTO)) {
			return handleMementoScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.PURSUIT) && target != null) {
			return handlePursuitScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FINAL_GAMBIT) && target != null) {
			return handleFinalGambitScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FELL_STINGER) && target != null) {
			return handleFellStingerScoring(pw, choice, target, boosted);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.FLING) && target != null) {
			return handleFlingScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.ROLE_PLAY) && target != null) {
			return handleRolePlayScoring(pw, choice, target);
		}

		if (attack.getActualMove().isAttack(AttackRegistry.IMPRISON) && target != null) {
			return handleImprisonScoring(pw, choice, target);
		}

		if (isTypedPriorityMove(attack) && target != null) {
			return handleTypedPriorityMoveScoring(pw, choice, target);
		}

		return false;
	}

	private boolean handleTrappingMoveScoring(PixelmonWrapper pw, MoveChoice choice, boolean boosted) {
		String moveName = choice.attack.getActualMove().getAttackName();

		choice.weight = boosted ? SCORE_TRAPPING_MOVE_BOOSTED : SCORE_TRAPPING_MOVE_NORMAL;
		debugLog(moveName + " trapping move base score: " + choice.weight);

		return true;
	}

	private boolean handleSpeedReductionScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!hasStatReductionImmunity(target) && !isPokemonFaster(pw, target)) {
			choice.weight = SCORE_SPEED_REDUCTION_SLOWER;
			debugLog(moveName + " speed reduction move (AI slower) score: " + SCORE_SPEED_REDUCTION_SLOWER);
		} else {
			choice.weight = SCORE_SPEED_REDUCTION_OTHER;
			debugLog(moveName + " speed reduction move score: " + SCORE_SPEED_REDUCTION_OTHER);
		}

		if (isDoubleBattle(pw.bc) && isSpreadSpeedReductionMove(choice.attack)) {
			choice.weight += SCORE_SPEED_REDUCTION_DOUBLE_BATTLE_BONUS;
			debugLog(moveName + " spread speed reduction bonus in double battle: +" + SCORE_SPEED_REDUCTION_DOUBLE_BATTLE_BONUS);
		}

		return true;
	}

	private boolean handleAttackReductionScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isAttackMove = isAttackReductionMove(choice.attack);

		boolean relevantStat = (isAttackMove && hasPhysicalAttackingMoves(target)) || (!isAttackMove && hasSpecialAttackingMoves(target));

		if (!hasStatReductionImmunity(target) && relevantStat) {
			choice.weight = SCORE_STAT_REDUCTION_RELEVANT;
			debugLog(moveName + " stat reduction move (relevant) score: " + SCORE_STAT_REDUCTION_RELEVANT);
		} else {
			choice.weight = SCORE_STAT_REDUCTION_OTHER;
			debugLog(moveName + " stat reduction move score: " + SCORE_STAT_REDUCTION_OTHER);
		}

		if (isDoubleBattle(pw.bc) && isSpreadStatReductionMove(choice.attack)) {
			choice.weight += SCORE_STAT_REDUCTION_SPREAD_BONUS;
			debugLog(moveName + " spread stat reduction bonus in double battle: +" + SCORE_STAT_REDUCTION_SPREAD_BONUS);
		}

		return true;
	}

	private boolean handleAcidSprayScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		choice.weight += SCORE_ACID_SPRAY_BONUS;
		debugLog(moveName + " acid spray bonus: +" + SCORE_ACID_SPRAY_BONUS);

		return false;
	}

	private boolean handleProtectMoveScoring(PixelmonWrapper pw, MoveChoice choice) {
		float score = SCORE_PROTECT_BASE;
		String moveName = choice.attack.getActualMove().getAttackName();
		debugLog(moveName + " base score: " + score);

		if (hasHarmfulStatusCondition(pw)) {
			score += SCORE_PROTECT_AI_STATUS_PENALTY;
			debugLog(moveName + " AI has harmful status, applying penalty: " + SCORE_PROTECT_AI_STATUS_PENALTY);
		}

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (hasHarmfulStatusCondition(opponent)) {
				score += SCORE_PROTECT_PLAYER_STATUS_BONUS;
				debugLog(moveName + " opponent has harmful status, applying bonus: " + SCORE_PROTECT_PLAYER_STATUS_BONUS);
				break;
			}
		}

		if (isFirstTurn(pw) && !isDoubleBattle(pw.bc)) {
			score += SCORE_PROTECT_FIRST_TURN_PENALTY;
			debugLog(moveName + " first turn penalty (not double battle): " + SCORE_PROTECT_FIRST_TURN_PENALTY);
		}

		if (wouldDieToSecondaryDamage(pw, pw.bc.globalStatusController)) {
			debugLog(moveName + " AI would die to secondary damage, using prevent flag");
			return false;
		}

		if (pw.protectsInARow > 0) {
			score += SCORE_PROTECT_USED_LAST_TURN_PENALTY;
			debugLog(moveName + " used last turn, applying large penalty: " + SCORE_PROTECT_USED_LAST_TURN_PENALTY);
		}

		if (hasReceiverAbility(pw)) {
			if (!pw.bc.getAdjacentPokemon(pw).isEmpty()) {
				if (hasAllyWithHugePowerAbility(pw, pw.bc.getAdjacentPokemon(pw))) {
					score += SCORE_PROTECT_RECEIVER_HUGE_POWER_BONUS;
					debugLog(moveName + " Receiver + Huge Power bonus: " + SCORE_PROTECT_RECEIVER_HUGE_POWER_BONUS);
				}
			}
		}

		choice.weight = score;
		debugLog(moveName + " final score: " + score);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleEarthquakeScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (target.isAlly(pw)) {
			if (isImmuneToGroundMoves(target)) {
				choice.weight += SCORE_EQ_SAFE_ALLY_BONUS;
				debugLog(moveName + " bonus: ally is immune: " + SCORE_EQ_SAFE_ALLY_BONUS);
				return true;
			}

			if ((target.selectedAttack != null && target.selectedAttack.getActualMove().isAttack(AttackRegistry.MAGNET_RISE) && isPokemonFaster(target, pw))) {
				choice.weight += SCORE_EQ_SAFE_ALLY_BONUS;
				debugLog(moveName + " bonus: ally using Magnet Rise and is faster: " + SCORE_EQ_SAFE_ALLY_BONUS);
				return true;
			}

			if (target.type.contains(Element.FIRE) || target.type.contains(Element.POISON) || target.type.contains(Element.ELECTRIC) || target.type.contains(Element.ROCK)) {
				choice.weight += SCORE_EQ_VULNERABLE_ALLY_PENALTY;
				debugLog(moveName + " penalty: ally is vulnerable type: " + SCORE_EQ_VULNERABLE_ALLY_PENALTY);
			} else {
				choice.weight += SCORE_EQ_ALLY_PENALTY;
				debugLog(moveName + " penalty: ally will be hit: " + SCORE_EQ_ALLY_PENALTY);
			}
			return true;
		}
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleFutureSightScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isFaster = isPokemonFaster(pw, target);
		boolean wouldBeKOd = isAtRiskOfBeingKOd(pw, target, pw.bc);

		if (isFaster && wouldBeKOd) {
			choice.weight = SCORE_FUTURE_SIGHT_FASTER_AND_KOD;
			debugLog(moveName + " scoring (faster & would be KO'd): " + choice.weight);
		} else {
			choice.weight = SCORE_FUTURE_SIGHT_DEFAULT;
			debugLog(moveName + " default scoring: " + choice.weight);
		}
		return true;
	}

	private boolean handleRolePlayScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		if (!target.isAlly(pw)) {
			choice.weight = SCORE_ROLE_PLAY_BAD_CASE;
			return true;
		}

		Ability allyAbility = target.getAbility();
		Ability myAbility = pw.getAbility();

		if (isDesirableRolePlayAbility(allyAbility)) {
			if (!isDesirableRolePlayAbility(myAbility)) {
				choice.weight = SCORE_ROLE_PLAY_GOOD_ABILITY;
				debugLog("Role Play targeting ally with desirable ability: " + choice.weight);
				return true;
			}
		}

		choice.weight = SCORE_ROLE_PLAY_BAD_CASE;
		debugLog("Role Play with no good target: " + choice.weight);
		return false;
	}

	private boolean handleSuckerPunchScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		if (isLastMoveUsed(pw, choice.attack.getActualMove()) && random.nextFloat() < SUCKER_PUNCH_PENALTY_CHANCE) {
			choice.weight = SCORE_SUCKER_PUNCH_PENALTY;
			debugLog(moveName + " used last turn (50% penalty): " + choice.weight);
			return true;
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleFakeOutScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isFirstTurn = isFirstTurn(pw);

		if (!isFirstTurn) {
			choice.weight = SCORE_FAKE_OUT_NOT_FIRST_TURN;
			debugLog(moveName + " scoring (not first turn): " + SCORE_FAKE_OUT_NOT_FIRST_TURN);
			return true;
		}

		PixelmonWrapper target = !choice.targets.isEmpty() ? choice.targets.get(0) : null;
		if (target == null || target.isAlly(pw)) {
			choice.weight = 0.0f;
			debugLog(moveName + " scoring (no valid target or targeting ally): 0.0");
			return true;
		}

		boolean hasImmunity =
			target.getAbility() instanceof com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.ShieldDust ||
			target.getAbility() instanceof com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.InnerFocus;

		if (hasImmunity) {
			choice.weight = 0.0f;
			debugLog(moveName + " scoring (target immune to flinch): 0.0");
			return true;
		}

		boolean aiIsFaster = isPokemonFaster(pw, target);
		boolean aiCanKOTarget = isAtRiskOfBeingKOd(target, pw, bc);
		boolean targetCanKOAI = isAtRiskOfBeingKOd(pw, target, bc);

		if (!isPokemonFaster(target, pw) && targetCanKOAI) {
			choice.weight = 0.0f;
			debugLog(moveName + " Player mon slower and would die to AI, preferring attack over Fake Out");
			return true;
		}

		if (isPokemonFaster(target, pw) && targetCanKOAI) {
			if (random.nextFloat() < 0.5f) {
				choice.weight = SCORE_FAKE_OUT_FIRST_TURN;
				debugLog(moveName + " Player mon faster but would die, 50% chance to use Fake Out: " + choice.weight);
			} else {
				choice.weight = 0.0f;
				debugLog(moveName + " Player mon faster but would die, 50% chance to prefer attacking move");
			}
			return true;
		}

		if (isDoubleBattle(pw.bc)) {
			List<PixelmonWrapper> playerMons = new ArrayList<>();
			for (com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant p : pw.bc.participants) {
				if (p != pw.getParticipant()) {
					for (PixelmonWrapper playerMon : p.controlledPokemon) {
						if (playerMon != null && !playerMon.isFainted()) {
							playerMons.add(playerMon);
						}
					}
				}
			}

			if (playerMons.size() >= 2) {
				PixelmonWrapper otherPlayerMon = null;
				for (PixelmonWrapper playerMon : playerMons) {
					if (playerMon != target) {
						otherPlayerMon = playerMon;
						break;
					}
				}

				if (otherPlayerMon != null) {
					boolean onePlayerMonFaster = isPokemonFaster(target, pw) || isPokemonFaster(otherPlayerMon, pw);
					boolean onePlayerMonSlower = !isPokemonFaster(target, pw) || !isPokemonFaster(otherPlayerMon, pw);
					boolean canKOSlowerMon = false;

					if (onePlayerMonFaster && onePlayerMonSlower) {
						if (!isPokemonFaster(target, pw)) {
							choice.weight = 0.0f;
							debugLog(moveName + " One player mon faster, one slower; preferring attack on slower target");
							return true;
						} else if (otherPlayerMon != null && !isPokemonFaster(otherPlayerMon, pw)) {
							choice.weight = 0.0f;
							debugLog(moveName + " Current target is faster, but there's a slower target; don't use Fake Out here");
							return true;
						}
					}

					if (isPokemonFaster(target, pw) && aiCanKOTarget && hasFakeOut(pw)) {
						choice.weight = SCORE_FAKE_OUT_FIRST_TURN;
						debugLog(moveName + " Player can outspeed and KO this AI Fake Out user, using Fake Out now: " + choice.weight);
						return true;
					}
				}
			}
		}

		choice.weight = SCORE_FAKE_OUT_FIRST_TURN;
		debugLog(moveName + " scoring (first turn, default): " + SCORE_FAKE_OUT_FIRST_TURN);
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleBatonPassScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean isLastPokemon = isLastPokemonAlive(pw, pw.getParticipant());

		if (isLastPokemon) {
			choice.weight = SCORE_BATON_PASS_LAST_MON;
			debugLog(moveName + " scoring: last Pokémon alive (never use): " + SCORE_BATON_PASS_LAST_MON);
			return true;
		}

		boolean hasSub = hasSubstitute(pw);
		boolean hasStatBoosts = hasPositiveStatChanges(pw);

		if (hasSub || hasStatBoosts) {
			choice.weight = SCORE_BATON_PASS_WITH_ADVANTAGE;
			String advantages = (hasSub ? "Substitute" : "") + ((hasSub && hasStatBoosts) ? " and " : "") + (hasStatBoosts ? "stat boosts" : "");
			debugLog(moveName + " scoring: has advantages to pass (" + advantages + "): " + SCORE_BATON_PASS_WITH_ADVANTAGE);
		} else {
			choice.weight = SCORE_BATON_PASS_NO_ADVANTAGE;
			debugLog(moveName + " scoring: no advantages to pass: " + SCORE_BATON_PASS_NO_ADVANTAGE);
		}
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleTrickScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!pw.hasHeldItem()) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (no held item): -20.0");
			return true;
		}

		if (target.hasHeldItem()) {
			boolean targetHasGoodItem = isGoodItem(target.getHeldItem().getHeldItemType());
			boolean aiHasHarmfulItem = isHarmfulItem(pw.getHeldItem().getHeldItemType());

			if (targetHasGoodItem && !aiHasHarmfulItem) {
				choice.weight = 0.0f;
				debugLog(moveName + " scoring (target has good item & AI doesn't have harmful item): 0.0");
				return true;
			}
		}

		EnumHeldItems aiItem = pw.getHeldItem().getHeldItemType();

		if (isHarmfulOrbItem(aiItem)) {
			float score = random.nextFloat() < TRICK_HARMFUL_ORB_HIGH_CHANCE ? SCORE_TRICK_WITH_HARMFUL_ORB_HIGH : SCORE_TRICK_WITH_HARMFUL_ORB_LOW;
			choice.weight = score;
			debugLog(moveName + " scoring (AI has harmful orb): " + score);
		} else if (isHarmfulItem(aiItem)) {
			choice.weight = SCORE_TRICK_WITH_HARMFUL_ITEM;
			debugLog(moveName + " scoring (AI has harmful item): " + SCORE_TRICK_WITH_HARMFUL_ITEM);
		} else {
			choice.weight = SCORE_TRICK_DEFAULT;
			debugLog(moveName + " scoring (default): " + SCORE_TRICK_DEFAULT);
		}

		return true;
	}

	private boolean handleRelicSongScoring(PixelmonWrapper pw, MoveChoice choice) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (isMeloettaBaseForm(pw)) {
			choice.weight = SCORE_RELIC_SONG_BASE_FORM;
			debugLog(moveName + " in base form: " + choice.weight);
		} else if (isMeloettaPirouetteForm(pw)) {
			choice.weight = SCORE_RELIC_SONG_PIROUETTE_FORM;
			debugLog(moveName + " in pirouette form: " + choice.weight);
		}
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleSubstituteScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		if (pw.getStatuses().stream().anyMatch(status -> status instanceof Substitute)) {
			choice.weight = -20.0f;
			debugLog("Substitute scoring (already active): -20.0");
			return true;
		}

		if (pw.getHealthPercent() <= 0.25f) {
			choice.weight = SCORE_SUBSTITUTE_LOW_HP_PENALTY;
			debugLog("Substitute scoring (≤ 25% HP): " + SCORE_SUBSTITUTE_LOW_HP_PENALTY);
			return true;
		}

		float score = SCORE_SUBSTITUTE_BASE;
		debugLog("Substitute base score: " + score);

		if (target != null && !target.isAlly(pw)) {
			if (target.getAbility() instanceof Infiltrator) {
				choice.weight = SCORE_SUBSTITUTE_LOW_HP_PENALTY;
				debugLog("Substitute scoring (opponent has Infiltrator): " + SCORE_SUBSTITUTE_LOW_HP_PENALTY);
				return true;
			}

			boolean isTargetAsleep = target.getStatuses().stream().anyMatch(status -> status instanceof Sleep);

			if (isTargetAsleep) {
				score += SCORE_SUBSTITUTE_ASLEEP_BONUS;
				debugLog("Substitute opponent asleep bonus: +" + SCORE_SUBSTITUTE_ASLEEP_BONUS);
			}

			boolean isLeechSeeded = pw.getStatuses().stream().anyMatch(status -> status instanceof Leech);
			boolean aiIsFaster = isPokemonFaster(pw, target);

			if (isLeechSeeded && aiIsFaster) {
				score += SCORE_SUBSTITUTE_LEECH_SEED_BONUS;
				debugLog("Substitute AI leech seeded and faster bonus: +" + SCORE_SUBSTITUTE_LEECH_SEED_BONUS);
			}

			if (hasAnySoundBasedMove(target)) {
				score += SCORE_SUBSTITUTE_SOUND_MOVE_PENALTY;
				debugLog("Substitute opponent has sound-based move penalty: " + SCORE_SUBSTITUTE_SOUND_MOVE_PENALTY);
			}
		}

		if (random.nextFloat() < SUBSTITUTE_RANDOM_PENALTY_CHANCE) {
			score += SCORE_SUBSTITUTE_RANDOM_PENALTY;
			debugLog("Substitute random penalty (50% chance): " + SCORE_SUBSTITUTE_RANDOM_PENALTY);
		}

		choice.weight = score;
		debugLog("Substitute final score: " + score);

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleBoomMoveScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (target != null && !target.isAlly(pw)) {
			boolean isNormalTypeMove = choice.attack.getActualMove().isAttack(AttackRegistry.EXPLOSION) || choice.attack.getActualMove().isAttack(AttackRegistry.SELF_DESTRUCT);
			boolean isFairyTypeMove = choice.attack.getActualMove().isAttack(AttackRegistry.MISTY_EXPLOSION);
			boolean immuneNormal = target.type.contains(Element.GHOST);
			boolean resistFairy = target.type.contains(Element.STEEL) || target.type.contains(Element.POISON);

			if ((isNormalTypeMove && immuneNormal) || (isFairyTypeMove && resistFairy)) {
				choice.weight = -20.0f;
				debugLog(moveName + " scoring (target is immune): -20.0");
				return true;
			}
		}

		boolean isLastPokemon = isLastPokemonAlive(pw, pw.getParticipant());

		if (isLastPokemon) {
			int playerPokemonLeft = 0;
			for (com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant p : pw.bc.participants) {
				if (p != pw.getParticipant()) {
					for (PixelmonWrapper playerMon : p.controlledPokemon) {
						if (playerMon != null && !playerMon.isFainted()) {
							playerPokemonLeft++;
						}
					}
				}
			}

			if (playerPokemonLeft > 1) {
				choice.weight = -20.0f;
				debugLog(moveName + " scoring (last Pokemon vs multiple opponent Pokemon): -20.0");
				return true;
			}

			if (playerPokemonLeft == 1) {
				choice.weight += SCORE_BOOM_MOVE_LAST_MON_VS_LAST_MON;
				debugLog(moveName + " last mon vs last mon penalty: " + SCORE_BOOM_MOVE_LAST_MON_VS_LAST_MON);
			}
		}

		float hpPercent = pw.getHealthPercent();
		float bonusScore = 0.0f;

		if (hpPercent < 0.1f) {
			bonusScore = SCORE_BOOM_MOVE_VERY_LOW_HP;
			debugLog(moveName + " very low HP (<10%) bonus: +" + SCORE_BOOM_MOVE_VERY_LOW_HP);
		} else if (hpPercent < 0.33f) {
			if (random.nextFloat() >= BOOM_MOVE_LOW_HP_NO_BONUS_CHANCE) {
				bonusScore = SCORE_BOOM_MOVE_LOW_HP;
				debugLog(moveName + " low HP (<33%) bonus (70% chance): +" + SCORE_BOOM_MOVE_LOW_HP);
			} else {
				debugLog(moveName + " low HP (<33%) but no bonus (30% chance)");
			}
		} else if (hpPercent < 0.66f) {
			if (random.nextFloat() >= BOOM_MOVE_MID_HP_NO_BONUS_CHANCE) {
				bonusScore = SCORE_BOOM_MOVE_MID_HP;
				debugLog(moveName + " medium HP (<66%) bonus (50% chance): +" + SCORE_BOOM_MOVE_MID_HP);
			} else {
				debugLog(moveName + " medium HP (<66%) but no bonus (50% chance)");
			}
		} else {
			if (random.nextFloat() < BOOM_MOVE_HIGH_HP_BONUS_CHANCE) {
				bonusScore = SCORE_BOOM_MOVE_MID_HP;
				debugLog(moveName + " high HP (>66%) bonus (rare 5% chance): +" + SCORE_BOOM_MOVE_MID_HP);
			} else {
				debugLog(moveName + " high HP (>66%) with no bonus (95% chance)");
			}
		}

		choice.weight += bonusScore;
		debugLog(moveName + " final score: " + choice.weight);

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleMementoScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (isLastPokemonAlive(pw, pw.getParticipant())) {
			choice.weight = -20.0f;
			debugLog(moveName + " scoring (last Pokemon alive): -20.0");
			return true;
		}

		float hpPercent = pw.getHealthPercent();
		float baseScore = SCORE_MEMENTO_DEFAULT;

		if (hpPercent < 0.1f) {
			baseScore = SCORE_MEMENTO_VERY_LOW_HP;
			debugLog(moveName + " very low HP (<10%) bonus: " + SCORE_MEMENTO_VERY_LOW_HP);
		} else if (hpPercent < 0.33f) {
			if (random.nextFloat() >= MEMENTO_LOW_HP_NO_BONUS_CHANCE) {
				baseScore = SCORE_MEMENTO_LOW_HP;
				debugLog(moveName + " low HP (<33%) bonus (70% chance): " + SCORE_MEMENTO_LOW_HP);
			} else {
				debugLog(moveName + " low HP (<33%) but no bonus (30% chance): " + SCORE_MEMENTO_DEFAULT);
			}
		} else if (hpPercent < 0.66f) {
			if (random.nextFloat() >= MEMENTO_MID_HP_NO_BONUS_CHANCE) {
				baseScore = SCORE_MEMENTO_MID_HP;
				debugLog(moveName + " medium HP (<66%) bonus (50% chance): " + SCORE_MEMENTO_MID_HP);
			} else {
				debugLog(moveName + " medium HP (<66%) but no bonus (50% chance): " + SCORE_MEMENTO_DEFAULT);
			}
		} else {
			if (random.nextFloat() < MEMENTO_HIGH_HP_BONUS_CHANCE) {
				baseScore = SCORE_MEMENTO_MID_HP;
				debugLog(moveName + " high HP (>66%) bonus (rare 5% chance): " + SCORE_MEMENTO_MID_HP);
			} else {
				debugLog(moveName + " high HP (>66%) with default score (95% chance): " + SCORE_MEMENTO_DEFAULT);
			}
		}

		if (target != null && !target.isAlly(pw)) {
			if (target.getAbility() instanceof ClearBody || target.getAbility() instanceof WhiteSmoke || target.getAbility() instanceof FullMetalBody) {
				baseScore = 0.0f;
				debugLog(moveName + " target immune to stat drops, score: 0.0");
			}
		}

		choice.weight = baseScore;
		debugLog(moveName + " final score: " + choice.weight);

		return true;
	}

	private boolean handlePursuitScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();
		boolean wouldKO;
		float healthPercent;

		boolean originalSimulateMode = bc.simulateMode;
		bc.simulateMode = true;

		Attack savedAttack = pw.attack;
		List<PixelmonWrapper> savedTargets = pw.targets;

		try {
			List<PixelmonWrapper> targets = new ArrayList<>();
			targets.add(target);
			pw.setAttack(choice.attack, targets, false);

			MoveResults result = new MoveResults(target);
			choice.attack.saveAttack();
			choice.attack.use(pw, target, result);
			wouldKO = getMaxDamageRoll(result) >= target.getHealth();
			healthPercent = target.getHealthPercent();
			choice.attack.restoreAttack();
		} finally {
			pw.attack = savedAttack;
			pw.targets = savedTargets;
			bc.simulateMode = originalSimulateMode;
		}

		boolean isFaster = isPokemonFaster(pw, target);

		if (wouldKO) {
			choice.weight = SCORE_PURSUIT_KO;
			debugLog(moveName + " would KO: " + choice.weight);
		} else if (healthPercent < 0.2f) {
			choice.weight = SCORE_PURSUIT_LOW_HP;
			debugLog(moveName + " target below 20% HP: " + choice.weight);
		} else if (healthPercent < 0.4f && random.nextFloat() < PURSUIT_MED_HP_CHANCE) {
			choice.weight = SCORE_PURSUIT_MED_HP;
			debugLog(moveName + " target below 40% HP (50% chance): " + choice.weight);
		}

		if (isFaster) {
			choice.weight += SCORE_PURSUIT_FASTER_BONUS;
			debugLog(moveName + " faster bonus: +" + SCORE_PURSUIT_FASTER_BONUS);
		}

		return true;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleFinalGambitScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (target.isAlly(pw)) {
			choice.weight = 0f;
			debugLog(moveName + " targeting ally: 0.0");
			return true;
		}

		boolean isFaster = isPokemonFaster(pw, target);

		int aiHP = pw.getHealth();
		int targetHP = target.getHealth();

		boolean hasHigherHP = aiHP > targetHP;

		boolean wouldDieAnyway = isAtRiskOfBeingKOd(pw, target, bc);

		if (isFaster && hasHigherHP) {
			choice.weight = SCORE_FINAL_GAMBIT_HIGH_HP_FASTER;
			debugLog(moveName + " scoring (faster with higher HP): " + SCORE_FINAL_GAMBIT_HIGH_HP_FASTER);
		} else if (isFaster && wouldDieAnyway) {
			choice.weight = SCORE_FINAL_GAMBIT_DIES_ANYWAY_FASTER;
			debugLog(moveName + " scoring (faster but would die anyway): " + SCORE_FINAL_GAMBIT_DIES_ANYWAY_FASTER);
		} else {
			choice.weight = SCORE_FINAL_GAMBIT_DEFAULT;
			debugLog(moveName + " scoring (default case): " + SCORE_FINAL_GAMBIT_DEFAULT);
		}

		return true;
	}

	private boolean handleFlingScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (pw.hasHeldItem()) {
			boolean raisesSpeed = pw.getHeldItem().getHeldItemType() == EnumHeldItems.berryStatIncrease;

			if (raisesSpeed) {
				boolean targetingAlly = target.isAlly(pw);

				if (targetingAlly) {
					boolean hasWeaknessPolicy = target.hasHeldItem() && target.getHeldItem().getHeldItemType() == EnumHeldItems.weaknessPolicy;

					boolean isSuperEffective = target.type.contains(Element.GHOST) || target.type.contains(Element.PSYCHIC);

					if (hasWeaknessPolicy && isSuperEffective) {
						choice.weight = SCORE_FLING_SPEED_BOOST_WITH_WP_AND_SE;
						debugLog(moveName + " with speed boost to ally with WP, super effective: " + choice.weight);
					} else {
						choice.weight = SCORE_FLING_SPEED_BOOST_DEFAULT;
						debugLog(moveName + " with speed boost to ally (no WP or not SE): " + choice.weight);
					}
					return true;
				}
			}
		}

		return false;
	}

	private boolean handleFellStingerScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target, boolean boosted) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!isAtMaxAttackStage(pw)) {
			boolean originalSimulateMode = bc.simulateMode;
			bc.simulateMode = true;
			boolean wouldKO;

			Attack savedAttack = pw.attack;
			List<PixelmonWrapper> savedTargets = pw.targets;

			try {
				List<PixelmonWrapper> targets = new ArrayList<>();
				targets.add(target);
				pw.setAttack(choice.attack, targets, false);

				MoveResults result = new MoveResults(target);
				choice.attack.saveAttack();
				choice.attack.use(pw, target, result);
				wouldKO = getMaxDamageRoll(result) >= target.getHealth();
				choice.attack.restoreAttack();
			} finally {
				pw.attack = savedAttack;
				pw.targets = savedTargets;
				bc.simulateMode = originalSimulateMode;
			}

			if (wouldKO) {
				boolean isFaster = isPokemonFaster(pw, target);

				if (isFaster) {
					choice.weight = boosted ? SCORE_FELL_STINGER_KO_FASTER_BOOSTED : SCORE_FELL_STINGER_KO_FASTER_NORMAL;
					debugLog(moveName + " would KO and AI is faster: " + choice.weight);
				} else {
					choice.weight = boosted ? SCORE_FELL_STINGER_KO_SLOWER_BOOSTED : SCORE_FELL_STINGER_KO_SLOWER_NORMAL;
					debugLog(moveName + " would KO but AI is slower: " + choice.weight);
				}
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleImprisonScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (!target.isAlly(pw)) {
			if (hasCommonMoves(pw, target)) {
				choice.weight = SCORE_IMPRISON_HAS_COMMON_MOVES;
				debugLog(moveName + " scoring: target has common moves: " + SCORE_IMPRISON_HAS_COMMON_MOVES);
			} else {
				choice.weight = SCORE_IMPRISON_NO_COMMON_MOVES;
				debugLog(moveName + " scoring: target has no common moves: " + SCORE_IMPRISON_NO_COMMON_MOVES);
			}
			return true;
		}

		choice.weight = -20.0f;
		debugLog(moveName + " scoring: targeting ally (never use): -20.0");
		return true;
	}

	private boolean handleTypedPriorityMoveScoring(PixelmonWrapper pw, MoveChoice choice, PixelmonWrapper target) {
		String moveName = choice.attack.getActualMove().getAttackName();

		if (isDoubleBattle(pw.bc)) {
			if (target.isAlly(pw)) {
				boolean hasWeaknessPolicy = target.hasHeldItem() && target.getHeldItem().getHeldItemType() == EnumHeldItems.weaknessPolicy;

				if (hasWeaknessPolicy) {
					boolean isSuperEffective = false;
					Element attackType = choice.attack.getType();
					List<Element> targetTypes = target.type;

					if (attackType == Element.WATER && (targetTypes.contains(Element.FIRE) || targetTypes.contains(Element.GROUND) || targetTypes.contains(Element.ROCK))) {
						isSuperEffective = true;
					} else if (
						attackType == Element.ICE && (targetTypes.contains(Element.GRASS) || targetTypes.contains(Element.GROUND) || targetTypes.contains(Element.FLYING) || targetTypes.contains(Element.DRAGON))
					) {
						isSuperEffective = true;
					} else if (attackType == Element.GHOST && (targetTypes.contains(Element.GHOST) || targetTypes.contains(Element.PSYCHIC))) {
						isSuperEffective = true;
					} else if (
						attackType == Element.FIGHTING &&
						(targetTypes.contains(Element.NORMAL) ||
							targetTypes.contains(Element.ICE) ||
							targetTypes.contains(Element.ROCK) ||
							targetTypes.contains(Element.DARK) ||
							targetTypes.contains(Element.STEEL))
					) {
						isSuperEffective = true;
					}

					if (isSuperEffective) {
						choice.weight = SCORE_PRIORITY_ALLY_WP_SE;
						debugLog(moveName + " targeting ally with Weakness Policy (super effective): " + choice.weight);
						return true;
					}
				}
			}
		}

		return false;
	}

	private void debugLog(String message) {
		if (debugMode) {
			System.out.println("[SpecificMoveHandler] " + message);
		}
	}
}
