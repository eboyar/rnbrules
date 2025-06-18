package ethan.hoenn.rnbrules.spec;

import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.requirement.AbstractBooleanPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.Flags;
import java.util.Set;

public class NeedsSurfRequirement extends AbstractBooleanPokemonRequirement {

	private static final Set<String> KEYS = Sets.newHashSet("surf");

	public NeedsSurfRequirement() {
		super(KEYS);
	}

	public NeedsSurfRequirement(boolean value) {
		super(KEYS, value);
	}

	@Override
	public Requirement<Pokemon, PixelmonEntity, Boolean> createInstance(Boolean val) {
		return new NeedsSurfRequirement(val);
	}

	@Override
	public boolean isDataMatch(Pokemon pokemon) {
		return this.value && pokemon.hasFlag(Flags.NEEDS_SURF);
	}

	@Override
	public void applyData(Pokemon pokemon) {
		if (this.value) {
			pokemon.addFlag(Flags.NEEDS_SURF);
		}
	}
}
