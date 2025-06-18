package ethan.hoenn.rnbrules.ai.utils;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.EarlyBird;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Hydration;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.ShedSkin;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Rainy;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import java.util.Objects;

public class MoveUtils {

	private final boolean debugMode;

	public MoveUtils(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public static boolean isTrappingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.BIND) ||
			attack.getActualMove().isAttack(AttackRegistry.CLAMP) ||
			attack.getActualMove().isAttack(AttackRegistry.FIRE_SPIN) ||
			attack.getActualMove().isAttack(AttackRegistry.INFESTATION) ||
			attack.getActualMove().isAttack(AttackRegistry.MAGMA_STORM) ||
			attack.getActualMove().isAttack(AttackRegistry.SAND_TOMB) ||
			attack.getActualMove().isAttack(AttackRegistry.WHIRLPOOL) ||
			attack.getActualMove().isAttack(AttackRegistry.WRAP)
		);
	}

	public static boolean isTerrainSettingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.ELECTRIC_TERRAIN) ||
			attack.getActualMove().isAttack(AttackRegistry.PSYCHIC_TERRAIN) ||
			attack.getActualMove().isAttack(AttackRegistry.GRASSY_TERRAIN) ||
			attack.getActualMove().isAttack(AttackRegistry.MISTY_TERRAIN)
		);
	}

	public static boolean isSpecialCaseDamagingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.EXPLOSION) ||
			attack.getActualMove().isAttack(AttackRegistry.FINAL_GAMBIT) ||
			attack.getActualMove().isAttack(AttackRegistry.RELIC_SONG) ||
			attack.getActualMove().isAttack(AttackRegistry.ROLLOUT) ||
			attack.getActualMove().isAttack(AttackRegistry.METEOR_BEAM) ||
			attack.getActualMove().isAttack(AttackRegistry.WHIRLPOOL) ||
			attack.getActualMove().isAttack(AttackRegistry.FIRE_SPIN) ||
			attack.getActualMove().isAttack(AttackRegistry.MAGMA_STORM) ||
			attack.getActualMove().isAttack(AttackRegistry.SAND_TOMB) ||
			attack.getActualMove().isAttack(AttackRegistry.CLAMP) ||
			attack.getActualMove().isAttack(AttackRegistry.THUNDER_CAGE) ||
			attack.getActualMove().isAttack(AttackRegistry.SNAP_TRAP) ||
			attack.getActualMove().isAttack(AttackRegistry.WRAP) ||
			attack.getActualMove().isAttack(AttackRegistry.BIND) ||
			attack.getActualMove().isAttack(AttackRegistry.INFESTATION) ||
			attack.getActualMove().isAttack(AttackRegistry.FUTURE_SIGHT) ||
			attack.getActualMove().isAttack(AttackRegistry.DOOM_DESIRE)
		);
	}

	public static boolean isSpeedReductionMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.BULLDOZE) ||
			attack.getActualMove().isAttack(AttackRegistry.ELECTROWEB) ||
			attack.getActualMove().isAttack(AttackRegistry.GLACIATE) ||
			attack.getActualMove().isAttack(AttackRegistry.ICY_WIND) ||
			attack.getActualMove().isAttack(AttackRegistry.LOW_SWEEP) ||
			attack.getActualMove().isAttack(AttackRegistry.MUD_SHOT) ||
			attack.getActualMove().isAttack(AttackRegistry.ROCK_TOMB) ||
			attack.getActualMove().isAttack(AttackRegistry.SCORCHING_SANDS)
		);
	}

	public static boolean isSpreadSpeedReductionMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.ICY_WIND) || attack.getActualMove().isAttack(AttackRegistry.ELECTROWEB));
	}

	public static boolean isAttackReductionMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.CHILLING_WATER) || attack.getActualMove().isAttack(AttackRegistry.LUNGE) || attack.getActualMove().isAttack(AttackRegistry.TROP_KICK));
	}

	public static boolean isSpecialAttackReductionMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.MYSTICAL_FIRE) || attack.getActualMove().isAttack(AttackRegistry.SKITTER_SMACK) || attack.getActualMove().isAttack(AttackRegistry.SPIRIT_BREAK)
		);
	}

	public static boolean isSpreadStatReductionMove(Attack attack) {
		return false;
	}

	public static boolean isAcidSprayLike(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.ACID_SPRAY);
	}

	public static boolean isDrainingMove(Attack attack) {
		String moveName = attack.getActualMove().getAttackName();
		return (
			moveName.equals("Absorb") ||
			moveName.equals("Mega Drain") ||
			moveName.equals("Giga Drain") ||
			moveName.equals("Leech Life") ||
			moveName.equals("Horn Leech") ||
			moveName.equals("Drain Punch") ||
			moveName.equals("Draining Kiss") ||
			moveName.equals("Parabolic Charge") ||
			moveName.equals("Oblivion Wing") ||
			moveName.equals("Dream Eater")
		);
	}

	public static float getDrainHealingFraction(Attack attack) {
		ImmutableAttack move = attack.getActualMove();

		if (move.isAttack(AttackRegistry.DRAINING_KISS) || move.isAttack(AttackRegistry.OBLIVION_WING)) {
			return 0.75f;
		}

		if (
			move.isAttack(AttackRegistry.GIGA_DRAIN) ||
			move.isAttack(AttackRegistry.DRAIN_PUNCH) ||
			move.isAttack(AttackRegistry.HORN_LEECH) ||
			move.isAttack(AttackRegistry.PARABOLIC_CHARGE) ||
			move.isAttack(AttackRegistry.ABSORB) ||
			move.isAttack(AttackRegistry.MEGA_DRAIN) ||
			move.isAttack(AttackRegistry.LEECH_LIFE) ||
			move.isAttack(AttackRegistry.DREAM_EATER)
		) {
			return 0.5f;
		}

		return 0.5f;
	}

	public static boolean isParalysisMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.THUNDER_WAVE) ||
			attack.getActualMove().isAttack(AttackRegistry.STUN_SPORE) ||
			attack.getActualMove().isAttack(AttackRegistry.GLARE) ||
			attack.getActualMove().isAttack(AttackRegistry.NUZZLE)
		);
	}

	public static boolean isSleepInducingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.YAWN) ||
			attack.getActualMove().isAttack(AttackRegistry.DARK_VOID) ||
			attack.getActualMove().isAttack(AttackRegistry.GRASS_WHISTLE) ||
			attack.getActualMove().isAttack(AttackRegistry.HYPNOSIS) ||
			attack.getActualMove().isAttack(AttackRegistry.LOVELY_KISS) ||
			attack.getActualMove().isAttack(AttackRegistry.SING) ||
			attack.getActualMove().isAttack(AttackRegistry.SLEEP_POWDER) ||
			attack.getActualMove().isAttack(AttackRegistry.SPORE)
		);
	}

	public static boolean isPoisonInducingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.TOXIC) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_POWDER) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_GAS) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_JAB) ||
			attack.getActualMove().isAttack(AttackRegistry.CROSS_POISON) ||
			attack.getActualMove().isAttack(AttackRegistry.GUNK_SHOT) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_TAIL) ||
			attack.getActualMove().isAttack(AttackRegistry.SLUDGE) ||
			attack.getActualMove().isAttack(AttackRegistry.SLUDGE_BOMB) ||
			attack.getActualMove().isAttack(AttackRegistry.SLUDGE_WAVE) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_FANG)
		);
	}

	public static boolean isSporeMove(Attack attack) {
		if (attack == null) return false;
		return (
			attack.getActualMove().isAttack(AttackRegistry.SPORE) ||
			attack.getActualMove().isAttack(AttackRegistry.SLEEP_POWDER) ||
			attack.getActualMove().isAttack(AttackRegistry.STUN_SPORE) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_POWDER) ||
			attack.getActualMove().isAttack(AttackRegistry.RAGE_POWDER) ||
			attack.getActualMove().isAttack(AttackRegistry.COTTON_SPORE)
		);
	}

	public static boolean isPowderMove(Attack attack) {
		if (attack == null) return false;
		return isSporeMove(attack) || attack.getActualMove().isAttack(AttackRegistry.POWDER);
	}

	public static boolean isSoundBasedMove(ImmutableAttack attack) {
		return (
			attack.isAttack(AttackRegistry.BOOMBURST) ||
			attack.isAttack(AttackRegistry.BUG_BUZZ) ||
			attack.isAttack(AttackRegistry.CHATTER) ||
			attack.isAttack(AttackRegistry.CLANGING_SCALES) ||
			attack.isAttack(AttackRegistry.CLANGOROUS_SOUL) ||
			attack.isAttack(AttackRegistry.CONFIDE) ||
			attack.isAttack(AttackRegistry.DISARMING_VOICE) ||
			attack.isAttack(AttackRegistry.ECHOED_VOICE) ||
			attack.isAttack(AttackRegistry.EERIE_SPELL) ||
			attack.isAttack(AttackRegistry.GROWL) ||
			attack.isAttack(AttackRegistry.HEAL_BELL) ||
			attack.isAttack(AttackRegistry.HOWL) ||
			attack.isAttack(AttackRegistry.HYPER_VOICE) ||
			attack.isAttack(AttackRegistry.METAL_SOUND) ||
			attack.isAttack(AttackRegistry.NOBLE_ROAR) ||
			attack.isAttack(AttackRegistry.OVERDRIVE) ||
			attack.isAttack(AttackRegistry.PARTING_SHOT) ||
			attack.isAttack(AttackRegistry.PERISH_SONG) ||
			attack.isAttack(AttackRegistry.RELIC_SONG) ||
			attack.isAttack(AttackRegistry.ROAR) ||
			attack.isAttack(AttackRegistry.ROUND) ||
			attack.isAttack(AttackRegistry.SCREECH) ||
			attack.isAttack(AttackRegistry.SING) ||
			attack.isAttack(AttackRegistry.SNARL) ||
			attack.isAttack(AttackRegistry.SNORE) ||
			attack.isAttack(AttackRegistry.SPARKLY_SWIRL) ||
			attack.isAttack(AttackRegistry.SUPERSONIC) ||
			attack.isAttack(AttackRegistry.UPROAR)
		);
	}

	public static boolean canCauseFlinch(ImmutableAttack attack) {
		return (
			attack.isAttack(AttackRegistry.FAKE_OUT) ||
			attack.isAttack(AttackRegistry.BITE) ||
			attack.isAttack(AttackRegistry.HEADBUTT) ||
			attack.isAttack(AttackRegistry.IRON_HEAD) ||
			attack.isAttack(AttackRegistry.ROCK_SLIDE) ||
			attack.isAttack(AttackRegistry.AIR_SLASH) ||
			attack.isAttack(AttackRegistry.ZING_ZAP) ||
			attack.isAttack(AttackRegistry.ASTONISH) ||
			attack.isAttack(AttackRegistry.DARK_PULSE) ||
			attack.isAttack(AttackRegistry.DRAGON_RUSH) ||
			attack.isAttack(AttackRegistry.EXTRASENSORY) ||
			attack.isAttack(AttackRegistry.HYPER_FANG) ||
			attack.isAttack(AttackRegistry.ICE_FANG) ||
			attack.isAttack(AttackRegistry.FIRE_FANG) ||
			attack.isAttack(AttackRegistry.THUNDER_FANG) ||
			attack.isAttack(AttackRegistry.NEEDLE_ARM) ||
			attack.isAttack(AttackRegistry.SECRET_POWER) ||
			attack.isAttack(AttackRegistry.SKY_ATTACK) ||
			attack.isAttack(AttackRegistry.SNORE) ||
			attack.isAttack(AttackRegistry.WATERFALL) ||
			attack.isAttack(AttackRegistry.ZEN_HEADBUTT) ||
			attack.isAttack(AttackRegistry.ICICLE_CRASH)
		);
	}

	public static boolean hasHighCritChance(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.AEROBLAST) ||
			attack.getActualMove().isAttack(AttackRegistry.AIR_CUTTER) ||
			attack.getActualMove().isAttack(AttackRegistry.ATTACK_ORDER) ||
			attack.getActualMove().isAttack(AttackRegistry.BLAZE_KICK) ||
			attack.getActualMove().isAttack(AttackRegistry.CRABHAMMER) ||
			attack.getActualMove().isAttack(AttackRegistry.CROSS_CHOP) ||
			attack.getActualMove().isAttack(AttackRegistry.CROSS_POISON) ||
			attack.getActualMove().isAttack(AttackRegistry.DRILL_RUN) ||
			attack.getActualMove().isAttack(AttackRegistry.KARATE_CHOP) ||
			attack.getActualMove().isAttack(AttackRegistry.LEAF_BLADE) ||
			attack.getActualMove().isAttack(AttackRegistry.NIGHT_SLASH) ||
			attack.getActualMove().isAttack(AttackRegistry.POISON_TAIL) ||
			attack.getActualMove().isAttack(AttackRegistry.PSYCHO_CUT) ||
			attack.getActualMove().isAttack(AttackRegistry.RAZOR_LEAF) ||
			attack.getActualMove().isAttack(AttackRegistry.RAZOR_WIND) ||
			attack.getActualMove().isAttack(AttackRegistry.SHADOW_CLAW) ||
			attack.getActualMove().isAttack(AttackRegistry.SLASH) ||
			attack.getActualMove().isAttack(AttackRegistry.SPACIAL_REND) ||
			attack.getActualMove().isAttack(AttackRegistry.STONE_EDGE)
		);
	}

	public static boolean isDreamEaterMove(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.DREAM_EATER);
	}

	public static boolean isNightmareMove(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.NIGHTMARE);
	}

	public static boolean isSnoreMove(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.SNORE);
	}

	public static boolean isSleepTalkMove(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.SLEEP_TALK);
	}

	public static boolean isHexMove(Attack attack) {
		return attack.getActualMove().isAttack(AttackRegistry.HEX);
	}

	public static boolean isHazardMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.STEALTH_ROCK) ||
			attack.getActualMove().isAttack(AttackRegistry.SPIKES) ||
			attack.getActualMove().isAttack(AttackRegistry.TOXIC_SPIKES) ||
			attack.getActualMove().isAttack(AttackRegistry.STICKY_WEB)
		);
	}

	public static boolean isPriorityMove(Attack attack, PixelmonWrapper user) {
		return attack.getActualMove().getPriority(user) > 0;
	}

	public static boolean isScreenMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.REFLECT) || attack.getActualMove().isAttack(AttackRegistry.LIGHT_SCREEN) || attack.getActualMove().isAttack(AttackRegistry.AURORA_VEIL));
	}

	public static boolean isTypedPriorityMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.SHADOW_SNEAK) ||
			attack.getActualMove().isAttack(AttackRegistry.AQUA_JET) ||
			attack.getActualMove().isAttack(AttackRegistry.ICE_SHARD) ||
			attack.getActualMove().isAttack(AttackRegistry.MACH_PUNCH) ||
			attack.getActualMove().isAttack(AttackRegistry.BULLET_PUNCH) ||
			attack.getActualMove().isAttack(AttackRegistry.QUICK_ATTACK) ||
			attack.getActualMove().isAttack(AttackRegistry.VACUUM_WAVE) ||
			attack.getActualMove().isAttack(AttackRegistry.WATER_SHURIKEN)
		);
	}

	public static boolean isProtectMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.PROTECT) ||
			attack.getActualMove().isAttack(AttackRegistry.DETECT) ||
			attack.getActualMove().isAttack(AttackRegistry.KINGS_SHIELD) ||
			attack.getActualMove().isAttack(AttackRegistry.BANEFUL_BUNKER) ||
			attack.getActualMove().isAttack(AttackRegistry.OBSTRUCT) ||
			attack.getActualMove().isAttack(AttackRegistry.SPIKY_SHIELD)
		);
	}

	public static boolean isCounterMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.COUNTER) || attack.getActualMove().isAttack(AttackRegistry.MIRROR_COAT));
	}

	public static boolean isBoomMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.EXPLOSION) || attack.getActualMove().isAttack(AttackRegistry.SELF_DESTRUCT) || attack.getActualMove().isAttack(AttackRegistry.MISTY_EXPLOSION)
		);
	}

	public static boolean isWeatherSettingMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.RAIN_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.SUNNY_DAY) ||
			attack.getActualMove().isAttack(AttackRegistry.SANDSTORM) ||
			attack.getActualMove().isAttack(AttackRegistry.HAIL) ||
			attack.getActualMove().isAttack(AttackRegistry.SNOWSCAPE)
		);
	}

	public static boolean isContrarySetupMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.OVERHEAT) ||
			attack.getActualMove().isAttack(AttackRegistry.LEAF_STORM) ||
			attack.getActualMove().isAttack(AttackRegistry.DRACO_METEOR) ||
			attack.getActualMove().isAttack(AttackRegistry.SUPERPOWER) ||
			attack.getActualMove().isAttack(AttackRegistry.CLOSE_COMBAT) ||
			attack.getActualMove().isAttack(AttackRegistry.PSYCHO_BOOST) ||
			attack.getActualMove().isAttack(AttackRegistry.HAMMER_ARM) ||
			attack.getActualMove().isAttack(AttackRegistry.ICE_HAMMER) ||
			attack.getActualMove().isAttack(AttackRegistry.FLEUR_CANNON) ||
			attack.getActualMove().isAttack(AttackRegistry.CLANGING_SCALES)
		);
	}

	public static boolean isMixedSetupMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.COIL) ||
			attack.getActualMove().isAttack(AttackRegistry.BULK_UP) ||
			attack.getActualMove().isAttack(AttackRegistry.CALM_MIND) ||
			attack.getActualMove().isAttack(AttackRegistry.QUIVER_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.NO_RETREAT)
		);
	}

	public static boolean isOffensiveSetupMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.DRAGON_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.SHIFT_GEAR) ||
			attack.getActualMove().isAttack(AttackRegistry.SWORDS_DANCE) ||
			attack.getActualMove().isAttack(AttackRegistry.HOWL) ||
			attack.getActualMove().isAttack(AttackRegistry.SHARPEN) ||
			attack.getActualMove().isAttack(AttackRegistry.MEDITATE) ||
			attack.getActualMove().isAttack(AttackRegistry.HONE_CLAWS)
		);
	}

	public static boolean isDefensiveSetupMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.ACID_ARMOR) ||
			attack.getActualMove().isAttack(AttackRegistry.BARRIER) ||
			attack.getActualMove().isAttack(AttackRegistry.COTTON_GUARD) ||
			attack.getActualMove().isAttack(AttackRegistry.HARDEN) ||
			attack.getActualMove().isAttack(AttackRegistry.IRON_DEFENSE) ||
			attack.getActualMove().isAttack(AttackRegistry.STOCKPILE) ||
			attack.getActualMove().isAttack(AttackRegistry.COSMIC_POWER)
		);
	}

	public static boolean isSpeedSetupMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.AGILITY) || attack.getActualMove().isAttack(AttackRegistry.ROCK_POLISH) || attack.getActualMove().isAttack(AttackRegistry.AUTOTOMIZE));
	}

	public static boolean isStandardRecoveryMove(Attack attack) {
		return (
			attack.getActualMove().isAttack(AttackRegistry.RECOVER) ||
			attack.getActualMove().isAttack(AttackRegistry.SLACK_OFF) ||
			attack.getActualMove().isAttack(AttackRegistry.HEAL_ORDER) ||
			attack.getActualMove().isAttack(AttackRegistry.SOFT_BOILED) ||
			attack.getActualMove().isAttack(AttackRegistry.ROOST) ||
			attack.getActualMove().isAttack(AttackRegistry.STRENGTH_SAP)
		);
	}

	public static boolean isSunBasedRecoveryMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.MORNING_SUN) || attack.getActualMove().isAttack(AttackRegistry.SYNTHESIS) || attack.getActualMove().isAttack(AttackRegistry.MOONLIGHT));
	}

	public static boolean hasFakeOut(PixelmonWrapper pokemon) {
		if (pokemon != null && pokemon.getMoveset() != null) {
			for (Attack attack : pokemon.getMoveset()) {
				if (attack != null && attack.getActualMove().isAttack(AttackRegistry.FAKE_OUT)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean hasSleepCountermeasures(PixelmonWrapper pw) {
		boolean hasSleepCounterMoves = false;
		for (Attack attack : pw.getMoveset()) {
			if (attack != null && (isSleepTalkMove(attack) || isSnoreMove(attack))) {
				hasSleepCounterMoves = true;
				break;
			}
		}

		boolean hasSleepCuringItem =
			pw.hasHeldItem() &&
			(Objects.requireNonNull(pw.getHeldItem().getItem().getRegistryName()).toString().equals("pixelmon:lum_berry") ||
				Objects.requireNonNull(pw.getHeldItem().getItem().getRegistryName()).toString().equals("pixelmon:chesto_berry"));

		boolean hasSleepCuringAbility = pw.getAbility() instanceof ShedSkin || pw.getAbility() instanceof EarlyBird;

		boolean hasHydrationInRain = pw.getAbility() instanceof Hydration && pw.bc.globalStatusController.getWeather() instanceof Rainy;

		return hasSleepCounterMoves || hasSleepCuringItem || hasSleepCuringAbility || hasHydrationInRain;
	}

	public static boolean isSpecialAttackSetupMove(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.TAIL_GLOW) || attack.getActualMove().isAttack(AttackRegistry.NASTY_PLOT) || attack.getActualMove().isAttack(AttackRegistry.WORK_UP));
	}

	public static boolean hasSpecialBurnInteraction(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.FACADE) || attack.getActualMove().isAttack(AttackRegistry.FLARE_BLITZ));
	}

	public static boolean isBlockedBySubstitute(Attack attack) {
		return (
			!isSoundBasedMove(attack.getActualMove()) &&
			!attack.getActualMove().isAttack(AttackRegistry.TRANSFORM) &&
			!attack.getActualMove().isAttack(AttackRegistry.SKY_DROP) &&
			!attack.getActualMove().isAttack(AttackRegistry.AROMATHERAPY)
		);
	}

	public static boolean isUnaffectedByUnaware(Attack attack) {
		return (attack.getActualMove().isAttack(AttackRegistry.POWER_UP_PUNCH) || attack.getActualMove().isAttack(AttackRegistry.SWORDS_DANCE) || attack.getActualMove().isAttack(AttackRegistry.HOWL));
	}

	public void debugLog(String message) {
		if (debugMode) {
			System.out.println("[MoveUtils] " + message);
		}
	}
}
