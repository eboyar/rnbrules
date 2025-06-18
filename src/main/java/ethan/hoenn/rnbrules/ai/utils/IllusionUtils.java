package ethan.hoenn.rnbrules.ai.utils;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Illusion;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.ai.MoveChoice;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import java.util.ArrayList;
import java.util.List;

public class IllusionUtils {

	public static boolean shouldAIKnowAboutIllusion(PixelmonWrapper pw, PixelmonWrapper target, boolean debugMode) {
		if (pw.lastAttack != null && !pw.isFirstTurn()) {
			int basePower = pw.lastAttack.getActualMove().getBasePower();

			if (basePower > 0) {
				debugLog(
					"AI used damaging move " + pw.lastAttack.getActualMove().getAttackName() + " (base power: " + basePower + ") - should know about illusion on " + target.getSpecies().getLocalizedName(),
					debugMode
				);
				return true;
			} else {
				debugLog(
					"AI used non-damaging move " +
					pw.lastAttack.getActualMove().getAttackName() +
					" (base power: " +
					basePower +
					") - remaining oblivious to illusion on " +
					target.getSpecies().getLocalizedName(),
					debugMode
				);
				return false;
			}
		}

		debugLog("No previous attack recorded - AI doesn't know about illusion on " + target.getSpecies().getLocalizedName(), debugMode);
		return false;
	}

	public static IllusionData checkForIllusion(PixelmonWrapper pw, MoveChoice choice, boolean debugMode) {
		for (PixelmonWrapper target : choice.targets) {
			AbstractAbility ability = (AbstractAbility) target.getBattleAbility();
			if (ability instanceof Illusion) {
				Illusion illusion = (Illusion) ability;
				if (illusion.disguisedPokemon != null) {
					boolean shouldKnowIllusion = shouldAIKnowAboutIllusion(pw, target, debugMode);

					if (!shouldKnowIllusion) {
						BattleParticipant targetParticipant = target.getParticipant();
						for (PixelmonWrapper partyMember : targetParticipant.allPokemon) {
							if (partyMember != target && partyMember.pokemon.getSpecies().equals(illusion.disguisedPokemon)) {
								return new IllusionData(target, partyMember, false);
							}
						}
					} else {
						debugLog("AI knows about illusion on " + target.getSpecies().getLocalizedName() + " - evaluating normally", debugMode);
						return new IllusionData(target, null, true);
					}
					break;
				}
			}
		}
		return null;
	}

	public static void evaluateChoiceWithIllusionSwitch(
		PixelmonWrapper pw,
		MoveChoice choice,
		PixelmonWrapper targetWithIllusion,
		PixelmonWrapper actualDisguisedPokemon,
		BattleController bc,
		Runnable normalEvaluation,
		boolean debugMode
	) {
		List<PixelmonWrapper> originalTargets = new ArrayList<>(choice.targets);
		choice.targets.clear();
		choice.targets.add(actualDisguisedPokemon);

		try {
			SwitchUtils.executeWithTemporarySwitch(
				targetWithIllusion,
				actualDisguisedPokemon,
				bc,
				targetWithIllusion.getStatuses(),
				() -> {
					normalEvaluation.run();

					debugLog("Evaluated against disguised Pokémon " + actualDisguisedPokemon.getSpecies().getLocalizedName() + " with score: " + choice.weight, debugMode);
				},
				debugMode
			);
		} finally {
			choice.targets.clear();
			choice.targets.addAll(originalTargets);
		}
	}

	private static void debugLog(String message, boolean debugMode) {
		if (debugMode) {
			System.out.println("[IllusionUtils] " + message);
		}
	}

	public static class IllusionData {

		public final PixelmonWrapper targetWithIllusion;
		public final PixelmonWrapper actualDisguisedPokemon;
		public final boolean aiKnowsIllusion;

		public IllusionData(PixelmonWrapper targetWithIllusion, PixelmonWrapper actualDisguisedPokemon, boolean aiKnowsIllusion) {
			this.targetWithIllusion = targetWithIllusion;
			this.actualDisguisedPokemon = actualDisguisedPokemon;
			this.aiKnowsIllusion = aiKnowsIllusion;
		}

		public boolean shouldDoTemporarySwitch() {
			return !aiKnowsIllusion && actualDisguisedPokemon != null;
		}
	}
}
