/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

public class PokemonCondition extends Condition<PixelmonEntity> {

	public PokemonSpecification spec;
	public Boolean wild;
	public boolean invert;

	@Override
	public boolean conditionMet(PixelmonEntity pokemon) {
		if (wild != null && ((pokemon.getPokemon().getOriginalTrainer() == null) != wild)) return invert;
		if (spec != null && !spec.matches(pokemon.getPokemon())) return invert;

		return !invert;
	}

	@Override
	public PixelmonEntity itemFromPixelmon(PixelmonEntity entity) {
		return entity;
	}

	@Override
	public String toString() {
		return "PokemonCondition{" + "spec='" + spec + '\'' + ", wild=" + wild + ", invert=" + invert + '}';
	}
}
