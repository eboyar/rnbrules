package ethan.hoenn.rnbrules.spec;

import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.requirement.AbstractBooleanPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.Flags;
import java.util.Set;

public class NeedsWaterfallRequirement extends AbstractBooleanPokemonRequirement {

	private static final Set<String> KEYS = Sets.newHashSet("waterfall");

	public NeedsWaterfallRequirement() {
		super(KEYS);
	}

	public NeedsWaterfallRequirement(boolean value) {
		super(KEYS, value);
	}

	@Override
	public Requirement<Pokemon, PixelmonEntity, Boolean> createInstance(Boolean val) {
		return new NeedsWaterfallRequirement(val);
	}

	@Override
	public boolean isDataMatch(Pokemon pokemon) {
		return this.value && pokemon.hasFlag(Flags.NEEDS_WATERFALL);
	}

	@Override
	public void applyData(Pokemon pokemon) {
		if (this.value) {
			pokemon.addFlag(Flags.NEEDS_WATERFALL);
		}
	}
}
