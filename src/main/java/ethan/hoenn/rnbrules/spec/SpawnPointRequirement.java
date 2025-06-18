package ethan.hoenn.rnbrules.spec;

import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.requirement.AbstractIntegerPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.SpawnPointHelper;
import java.util.Set;

public class SpawnPointRequirement extends AbstractIntegerPokemonRequirement {

	private static final Set<String> KEYS = Sets.newHashSet("spawnpoint", "spoint");
	public static final int DEFAULT_RADIUS_SQ = 20;
	public static final String NEEDS_SPAWN_TAG = "NeedsSpawnPoint";

	public SpawnPointRequirement() {
		super(KEYS, DEFAULT_RADIUS_SQ);
	}

	public SpawnPointRequirement(int radius) {
		super(KEYS, DEFAULT_RADIUS_SQ, radius);
	}

	@Override
	public Requirement<Pokemon, PixelmonEntity, Integer> createInstance(Integer value) {
		return new SpawnPointRequirement(value);
	}

	@Override
	public boolean isDataMatch(Pokemon pokemon) {
		if (SpawnPointHelper.hasSpawnPoint(pokemon)) {
			return pokemon.getPersistentData().getInt(NEEDS_SPAWN_TAG) == this.value;
		}
		return false;
	}

	@Override
	public void applyData(Pokemon pokemon) {
		if (this.value != 0) {
			pokemon.getPersistentData().putInt(NEEDS_SPAWN_TAG, this.value);
		}
	}
}
