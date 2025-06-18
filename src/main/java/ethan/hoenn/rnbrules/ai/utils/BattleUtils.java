package ethan.hoenn.rnbrules.ai.utils;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Sturdy;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Truant;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.GlobalStatusController;
import com.pixelmonmod.pixelmon.battles.controller.log.MoveResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.*;
import java.util.ArrayList;
import java.util.List;

public class BattleUtils {

	private final boolean debugMode;

	public BattleUtils(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public static boolean isPokemonFaster(PixelmonWrapper pw, PixelmonWrapper target) {
		if (pw.bc.globalStatusController.getGlobalStatuses().stream().anyMatch(status -> status instanceof TrickRoom)) {
			return (pw.getBattleStats().getStatWithMod(BattleStatsType.SPEED) <= target.getBattleStats().getStatWithMod(BattleStatsType.SPEED));
		} else return (pw.getBattleStats().getStatWithMod(BattleStatsType.SPEED) >= target.getBattleStats().getStatWithMod(BattleStatsType.SPEED));
	}

	public static boolean isLastPokemonAlive(PixelmonWrapper pw, BattleParticipant participant) {
		int alivePokemon = 0;
		for (PixelmonWrapper teammate : participant.controlledPokemon) {
			if (teammate != null && !teammate.isFainted()) {
				alivePokemon++;
			}
		}
		return alivePokemon <= 1;
	}

	public static boolean isIncapacitated(PixelmonWrapper pw) {
		boolean isFrozen = pw.getStatuses().stream().anyMatch(status -> status instanceof Freeze);
		boolean hasFrozenCure = pw
			.getMoveset()
			.stream()
			.anyMatch(
				attack ->
					attack != null &&
					(attack.getActualMove().isAttack(AttackRegistry.FLAME_WHEEL) ||
						attack.getActualMove().isAttack(AttackRegistry.SACRED_FIRE) ||
						attack.getActualMove().isAttack(AttackRegistry.FLARE_BLITZ) ||
						attack.getActualMove().isAttack(AttackRegistry.FUSION_FLARE) ||
						attack.getActualMove().isAttack(AttackRegistry.SCALD) ||
						attack.getActualMove().isAttack(AttackRegistry.STEAM_ERUPTION))
			);

		if (isFrozen && !hasFrozenCure) {
			return true;
		}

		boolean isAsleep = pw.getStatuses().stream().anyMatch(status -> status instanceof Sleep);

		boolean isRecharging = pw.getStatuses().stream().anyMatch(status -> status instanceof Recharge);

		boolean hasTruant = pw.getAbility() instanceof Truant;

		return isAsleep || isRecharging || hasTruant;
	}

	public static boolean wouldBeTwoHitKOd(PixelmonWrapper defender, PixelmonWrapper attacker) {
		int maxDamagePerHit = 0;

		for (Attack attack : attacker.getMoveset()) {
			if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
				boolean originalSimulateMode = defender.bc.simulateMode;
				defender.bc.simulateMode = true;

				Attack savedAttack = attacker.attack;
				List<PixelmonWrapper> savedTargets = attacker.targets;

				try {
					List<PixelmonWrapper> targets = new ArrayList<>();
					targets.add(defender);
					attacker.setAttack(attack, targets, false);

					MoveResults result = new MoveResults(defender);
					attack.saveAttack();
					attack.use(attacker, defender, result);

					int maxRollDamage = getMaxDamageRoll(result);

					if (maxRollDamage > maxDamagePerHit) {
						maxDamagePerHit = maxRollDamage;
					}

					attack.restoreAttack();
				} finally {
					attacker.attack = savedAttack;
					attacker.targets = savedTargets;
					defender.bc.simulateMode = originalSimulateMode;
				}
			}
		}
		return maxDamagePerHit >= defender.getHealth() / 2;
	}

	public static boolean wouldBeKOdThisTurn(PixelmonWrapper pw) {
		boolean hasSturdy = pw.getAbility() instanceof Sturdy;
		boolean hasFocusSash = pw.hasHeldItem() && pw.getHeldItem().getHeldItemType() == com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems.focussash && pw.getHealthPercent() == 1.0f;

		if (hasSturdy || hasFocusSash) {
			return false;
		}

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			for (Attack attack : opponent.getMoveset()) {
				if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
					boolean originalSimulateMode = pw.bc.simulateMode;
					pw.bc.simulateMode = true;
					boolean wouldKill = false;

					Attack savedAttack = opponent.attack;
					List<PixelmonWrapper> savedTargets = opponent.targets;

					try {
						List<PixelmonWrapper> targets = new ArrayList<>();
						targets.add(pw);
						opponent.setAttack(attack, targets, false);

						MoveResults result = new MoveResults(pw);
						attack.saveAttack();
						attack.use(opponent, pw, result);

						wouldKill = couldKnockOut(result, pw);

						attack.restoreAttack();
					} finally {
						opponent.attack = savedAttack;
						opponent.targets = savedTargets;
						pw.bc.simulateMode = originalSimulateMode;
					}

					if (wouldKill) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isAtRiskOfBeingKOd(PixelmonWrapper pw, PixelmonWrapper opponent, BattleController bc) {
		if (isPokemonFaster(pw, opponent)) {
			return false;
		}

		boolean originalSimulateMode = bc.simulateMode;
		bc.simulateMode = true;
		boolean atRisk = false;

		try {
			for (Attack attack : opponent.getMoveset()) {
				if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
					Attack savedAttack = opponent.attack;
					List<PixelmonWrapper> savedTargets = opponent.targets;

					try {
						List<PixelmonWrapper> targets = new ArrayList<>();
						targets.add(pw);
						opponent.setAttack(attack, targets, false);

						MoveResults result = new MoveResults(pw);
						attack.saveAttack();
						attack.use(opponent, pw, result);

						atRisk = couldKnockOut(result, pw);

						attack.restoreAttack();
					} finally {
						opponent.attack = savedAttack;
						opponent.targets = savedTargets;
					}
				}
			}
		} finally {
			bc.simulateMode = originalSimulateMode;
		}

		return atRisk;
	}

	public static boolean wouldDieToSecondaryDamage(PixelmonWrapper pokemon, GlobalStatusController gsc) {
		int health = pokemon.getHealth();
		int totalDamage = 0;
		int maxHealth = pokemon.getMaxHealth();

		List<StatusBase> statuses = pokemon.getStatuses();

		if (statuses.stream().anyMatch(status -> status instanceof Burn)) {
			totalDamage += Math.max(1, maxHealth / 16);
		}

		boolean isPoisoned = statuses.stream().anyMatch(status -> status instanceof Poison);
		boolean isBadlyPoisoned = statuses.stream().anyMatch(status -> status instanceof PoisonBadly);

		if (isPoisoned || isBadlyPoisoned) {
			totalDamage += Math.max(1, maxHealth / 8);
		}

		if (statuses.stream().anyMatch(status -> status instanceof Leech)) {
			totalDamage += Math.max(1, maxHealth / 8);
		}

		if (statuses.stream().anyMatch(status -> status instanceof Cursed) && !pokemon.type.contains(Element.GHOST)) {
			totalDamage += Math.max(1, maxHealth / 4);
		}

		Weather currentWeather = gsc.getWeather();
		if ((currentWeather instanceof Sandstorm && !currentWeather.isImmune(pokemon)) || (currentWeather instanceof Hail && !currentWeather.isImmune(pokemon))) {
			totalDamage += Math.max(1, maxHealth / 16);
		}

		return totalDamage >= health;
	}

	public static int estimateMaxDamage(PixelmonWrapper attacker, PixelmonWrapper defender) {
		int maxDamage = 0;

		boolean originalSimulateMode = defender.bc.simulateMode;
		defender.bc.simulateMode = true;

		try {
			for (Attack attack : attacker.getMoveset()) {
				if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
					Attack savedAttack = attacker.attack;
					List<PixelmonWrapper> savedTargets = attacker.targets;

					try {
						List<PixelmonWrapper> targets = new ArrayList<>();
						targets.add(defender);
						attacker.setAttack(attack, targets, false);

						MoveResults result = new MoveResults(defender);
						attack.saveAttack();
						attack.use(attacker, defender, result);

						int maxRollDamage = getMaxDamageRoll(result);

						if (maxRollDamage > maxDamage) {
							maxDamage = maxRollDamage;
						}

						attack.restoreAttack();
					} finally {
						attacker.attack = savedAttack;
						attacker.targets = savedTargets;
					}
				}
			}
		} finally {
			defender.bc.simulateMode = originalSimulateMode;
		}

		return maxDamage;
	}

	public static int estimateMaxDamageOfMove(Attack attack, PixelmonWrapper attacker, PixelmonWrapper defender) {
		int maxDamage = 0;

		boolean originalSimulateMode = defender.bc.simulateMode;
		defender.bc.simulateMode = true;

		try {
			if (attack != null && attack.pp > 0 && !attack.getDisabled()) {
				Attack savedAttack = attacker.attack;
				List<PixelmonWrapper> savedTargets = attacker.targets;

				try {
					List<PixelmonWrapper> targets = new ArrayList<>();
					targets.add(defender);
					attacker.setAttack(attack, targets, false);

					MoveResults result = new MoveResults(defender);
					attack.saveAttack();
					attack.use(attacker, defender, result);

					maxDamage = getMaxDamageRoll(result);

					attack.restoreAttack();
				} finally {
					attacker.attack = savedAttack;
					attacker.targets = savedTargets;
				}
			}
		} finally {
			defender.bc.simulateMode = originalSimulateMode;
		}

		return maxDamage;
	}

	public static boolean isHighestDamagingMove(PixelmonWrapper pw, Attack attack) {
		int maxDamage = 0;
		int attackDamage = 0;

		List<PixelmonWrapper> opponents = pw.bc.getOpponentPokemon(pw);
		if (opponents.isEmpty()) return false;

		PixelmonWrapper opponent = opponents.get(0);
		boolean originalSimulateMode = pw.bc.simulateMode;
		pw.bc.simulateMode = true;

		try {
			for (Attack moveToCheck : pw.getMoveset()) {
				if (moveToCheck != null && moveToCheck.pp > 0 && !moveToCheck.getDisabled()) {
					Attack savedAttack = pw.attack;
					List<PixelmonWrapper> savedTargets = pw.targets;

					try {
						List<PixelmonWrapper> targets = new ArrayList<>();
						targets.add(opponent);
						pw.setAttack(moveToCheck, targets, false);

						MoveResults result = new MoveResults(opponent);
						moveToCheck.saveAttack();
						moveToCheck.use(pw, opponent, result);

						if (moveToCheck == attack) {
							attackDamage = result.damage;
						}

						if (result.damage > maxDamage) {
							maxDamage = result.damage;
						}

						moveToCheck.restoreAttack();
					} finally {
						pw.attack = savedAttack;
						pw.targets = savedTargets;
					}
				}
			}
		} finally {
			pw.bc.simulateMode = originalSimulateMode;
		}

		return attackDamage >= maxDamage;
	}

	public static boolean doesOpponentHaveStatus(PixelmonWrapper pw, Class<? extends StatusBase> statusClass) {
		if (pw.getOpponentPokemon().isEmpty()) {
			return false;
		}

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			for (StatusBase status : opponent.getStatuses()) {
				if (statusClass.isInstance(status)) {
					return true;
				}
			}
		}
		return false;
	}

	public static int getOpponentSpikeLayer(PixelmonWrapper pw) {
		if (pw.getOpponentPokemon().isEmpty()) {
			return 0;
		}

		PixelmonWrapper opponent = pw.getOpponentPokemon().get(0);
		for (StatusBase status : opponent.getStatuses()) {
			if (status instanceof Spikes) {
				return ((Spikes) status).getNumLayers();
			}
		}
		return 0;
	}

	public static int getOpponentToxicSpikeLayer(PixelmonWrapper pw) {
		if (pw.getOpponentPokemon().isEmpty()) {
			return 0;
		}

		PixelmonWrapper opponent = pw.getOpponentPokemon().get(0);
		for (StatusBase status : opponent.getStatuses()) {
			if (status instanceof ToxicSpikes) {
				return ((ToxicSpikes) status).getNumLayers();
			}
		}
		return 0;
	}

	public static boolean hasOpponentTeamMostlyGrounded(PixelmonWrapper pw) {
		int groundedCount = 0;
		int totalCount = 0;

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (opponent != null) {
				totalCount++;
				if (!isImmuneToGroundHazards(opponent)) {
					groundedCount++;
				}
			}
		}

		return groundedCount > (totalCount / 2);
	}

	public static boolean hasOpponentTeamMostlyPoisonOrSteel(PixelmonWrapper pw) {
		int poisonOrSteelCount = 0;
		int totalCount = 0;

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (opponent != null) {
				totalCount++;
				if (opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.POISON) || opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.STEEL)) {
					poisonOrSteelCount++;
				}
			}
		}

		return poisonOrSteelCount > (totalCount / 2);
	}

	public static boolean isOpponentTeamWeakToRock(PixelmonWrapper pw) {
		int rockWeakCount = 0;
		int totalCount = 0;

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (opponent != null) {
				totalCount++;

				if (
					opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.BUG) ||
					opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.FIRE) ||
					opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.FLYING) ||
					opponent.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.ICE)
				) {
					rockWeakCount++;
				}
			}
		}

		return rockWeakCount > (totalCount / 3);
	}

	public static boolean checkSpeedAdvantageAfterParalysis(PixelmonWrapper pw, PixelmonWrapper target) {
		int aiSpeed = pw.getBattleStats().getStatWithMod(com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType.SPEED);
		int targetSpeed = target.getBattleStats().getStatWithMod(com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType.SPEED);

		if (targetSpeed > aiSpeed) {
			int targetSpeedAfterParalysis = targetSpeed / 4;

			return aiSpeed > targetSpeedAfterParalysis;
		}

		return false;
	}

	public static boolean hasOpponentTeamHighSpeed(PixelmonWrapper pw) {
		int highSpeedCount = 0;
		int totalCount = 0;

		int userSpeed = pw.getBattleStats().getStatWithMod(com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType.SPEED);

		for (PixelmonWrapper opponent : pw.getOpponentPokemon()) {
			if (opponent != null) {
				totalCount++;
				int opponentSpeed = opponent.getBattleStats().getStatWithMod(com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType.SPEED);

				if (opponentSpeed > userSpeed) {
					highSpeedCount++;
				}
			}
		}

		return highSpeedCount > (totalCount / 2);
	}

	public static boolean isImmuneToGroundHazards(PixelmonWrapper pw) {
		return (pw.type.contains(com.pixelmonmod.pixelmon.api.pokemon.Element.FLYING) || pw.getAbility().getLocalizedName().equals("Levitate"));
	}

	public static boolean isFirstTurn(PixelmonWrapper pw) {
		return pw.isFirstTurn();
	}

	public static boolean isDoubleBattle(BattleController bc) {
		return bc.participants.size() > 2;
	}

	public static int getMaxDamageRoll(MoveResults result) {
		return (int) Math.ceil(result.damage * (1.0f / 0.925f));
	}

	public static boolean couldKnockOut(MoveResults result, PixelmonWrapper target) {
		int maxPossibleDamage = getMaxDamageRoll(result);
		return maxPossibleDamage >= target.getHealth();
	}

	public void debugLog(String message) {
		if (debugMode) {
			System.out.println("[BattleUtils] " + message);
		}
	}
}
