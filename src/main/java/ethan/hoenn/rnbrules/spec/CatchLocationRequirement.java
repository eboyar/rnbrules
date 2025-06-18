package ethan.hoenn.rnbrules.spec;

import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.requirement.AbstractStringPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.util.Set;

public class CatchLocationRequirement extends AbstractStringPokemonRequirement {

	private static final Set<String> KEYS = Sets.newHashSet(new String[] { "cl", "catchlocation" });
	private static final String DEFAULT_VALUE = "NONE";
	private static final String CATCH_LOCATION_TAG = "CatchLocation";

	public CatchLocationRequirement() {
		super(KEYS, DEFAULT_VALUE);
	}

	public CatchLocationRequirement(String value) {
		super(KEYS, DEFAULT_VALUE, value);
	}

	@Override
	public Requirement<Pokemon, PixelmonEntity, String> createInstance(String s) {
		return new CatchLocationRequirement(s);
	}

	@Override
	public boolean isDataMatch(Pokemon pokemon) {
		if (pokemon != null && pokemon.getPersistentData() != null) {
			if (pokemon.getPersistentData().contains(CATCH_LOCATION_TAG)) {
				String storedLocation = pokemon.getPersistentData().getString(CATCH_LOCATION_TAG);
				return storedLocation.equals(this.value);
			}
		}
		return false;
	}

	@Override
	public void applyData(Pokemon pokemon) {
		if (pokemon != null && this.value != null && !this.value.equals(DEFAULT_VALUE)) {
			pokemon.getPersistentData().putString(CATCH_LOCATION_TAG, this.value);
		}
	}
}
