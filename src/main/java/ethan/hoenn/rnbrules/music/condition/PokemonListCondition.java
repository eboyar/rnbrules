/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.util.List;

public class PokemonListCondition extends Condition<PixelmonEntity> {

	public List<PokemonSpecification> spec;
	public Boolean wild;
	public boolean invert;

	@Override
	public boolean conditionMet(PixelmonEntity pokemon) {
		if (wild != null && ((pokemon.getPokemon().getOriginalTrainer() == null) != wild)) return invert;
		if (spec != null) {
			for (PokemonSpecification s : spec) {
				if (s.matches(pokemon.getPokemon())) return !invert;
			}
			return invert;
		}

		return !invert;
	}

	@Override
	public PixelmonEntity itemFromPixelmon(PixelmonEntity entity) {
		return entity;
	}

	@Override
	public String toString() {
		return "PokemonListCondition{" + "spec='" + spec + '\'' + ", wild=" + wild + ", invert=" + invert + '}';
	}
}
