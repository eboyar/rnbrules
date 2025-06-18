package ethan.hoenn.rnbrules.ai.utils;

import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.battles.status.StickyWeb;
import com.pixelmonmod.pixelmon.battles.status.Tailwind;
import java.util.List;

public class SwitchUtils {

	public static void executeWithTemporarySwitch(
		PixelmonWrapper originalPokemon,
		PixelmonWrapper switchToPokemon,
		BattleController bc,
		List<StatusBase> statusesToApply,
		Runnable logic,
		boolean debugMode
	) {
		int controlledIndex = originalPokemon.getControlledIndex();
		BattleParticipant participant = originalPokemon.getParticipant();

		debugLog("Temporarily switching " + originalPokemon.getSpecies().getLocalizedName() + " to " + switchToPokemon.getSpecies().getLocalizedName(), debugMode);

		try {
			participant.controlledPokemon.set(controlledIndex, switchToPokemon);

			applyStatModifications(switchToPokemon, statusesToApply, bc, debugMode);

			logic.run();
		} finally {
			participant.controlledPokemon.set(controlledIndex, originalPokemon);
			switchToPokemon.getBattleStats().setBattleStatsForCurrentForm();
			debugLog("Restored original Pokémon " + originalPokemon.getSpecies().getLocalizedName(), debugMode);
		}
	}

	public static void applyStatModifications(PixelmonWrapper pokemon, List<StatusBase> statusesToApply, BattleController bc, boolean debugMode) {
		bc.modifyStats();
		bc.modifyStatsCancellable(pokemon);
		pokemon.getBattleStats().setBattleStatsForCurrentForm();

		if (statusesToApply != null) {
			applyStatusEffects(pokemon, statusesToApply, debugMode);
		}

		applyHeldItemEffects(pokemon, debugMode);
	}

	private static void applyStatusEffects(PixelmonWrapper pokemon, List<StatusBase> statuses, boolean debugMode) {
		for (StatusBase status : statuses) {
			if (status instanceof StickyWeb) {
				pokemon.getBattleStats().speedStat = (pokemon.getBattleStats().speedStat * 67) / 100;
				debugLog("Applied Sticky Web: Speed reduced to " + pokemon.getBattleStats().speedStat, debugMode);
			} else if (status instanceof Tailwind) {
				pokemon.getBattleStats().speedStat *= 2;
				debugLog("Applied Tailwind: Speed doubled to " + pokemon.getBattleStats().speedStat, debugMode);
			}
		}
	}

	private static void applyHeldItemEffects(PixelmonWrapper pokemon, boolean debugMode) {
		String heldItemName = pokemon.getHeldItem().getLocalizedName().toLowerCase();

		switch (heldItemName) {
			case "choice specs":
				pokemon.getBattleStats().specialAttackStat = (pokemon.getBattleStats().specialAttackStat * 3) / 2;
				debugLog("Applied Choice Specs: Special Attack boosted to " + pokemon.getBattleStats().specialAttackStat, debugMode);
				break;
			case "choice band":
				pokemon.getBattleStats().attackStat = (pokemon.getBattleStats().attackStat * 3) / 2;
				debugLog("Applied Choice Band: Attack boosted to " + pokemon.getBattleStats().attackStat, debugMode);
				break;
			case "choice scarf":
				pokemon.getBattleStats().speedStat = (pokemon.getBattleStats().speedStat * 3) / 2;
				debugLog("Applied Choice Scarf: Speed boosted to " + pokemon.getBattleStats().speedStat, debugMode);
				break;
			case "soul dew":
				pokemon.getBattleStats().specialAttackStat = (pokemon.getBattleStats().specialAttackStat * 3) / 2;
				pokemon.getBattleStats().specialDefenseStat = (pokemon.getBattleStats().specialDefenseStat * 3) / 2;
				debugLog("Applied Soul Dew: Special Attack and Defense boosted", debugMode);
				break;
			default:
				break;
		}
	}

	public static void executeTemporarySwitchEvaluation(PixelmonWrapper currentPokemon, PixelmonWrapper switchCandidate, BattleController bc, Runnable evaluation, boolean debugMode) {
		List<StatusBase> currentStatuses = currentPokemon.getStatuses();

		executeWithTemporarySwitch(currentPokemon, switchCandidate, bc, currentStatuses, evaluation, debugMode);
	}

	private static void debugLog(String message, boolean debugMode) {
		if (debugMode) {
			System.out.println("[SwitchUtils] " + message);
		}
	}
}
