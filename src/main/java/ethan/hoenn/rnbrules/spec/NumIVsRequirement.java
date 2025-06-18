package ethan.hoenn.rnbrules.spec;

import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.requirement.AbstractIntegerPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.util.*;

public class NumIVsRequirement extends AbstractIntegerPokemonRequirement {

	//stolen from envyware

	private static final Set<String> KEYS = Sets.newHashSet("numivs", "perfectivs");

	public NumIVsRequirement() {
		super(KEYS, 2);
	}

	public NumIVsRequirement(int value) {
		super(KEYS, 2, value);
	}

	@Override
	public Requirement<Pokemon, PixelmonEntity, Integer> createInstance(Integer integer) {
		return new NumIVsRequirement(integer);
	}

	@Override
	public boolean isDataMatch(Pokemon pokemon) {
		return this.getMaxIVCount(pokemon) == this.value;
	}

	private int getMaxIVCount(Pokemon pokemon) {
		int counter = 0;
		for (int i : pokemon.getIVs().getArray()) {
			if (i >= 31) {
				++counter;
			}
		}

		return counter;
	}

	@Override
	public void applyData(Pokemon pokemon) {
		if (this.value < 1) {
			this.value = 1;
		} else if (this.value > 6) {
			this.value = 6;
		}

		List<Integer> ivs = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			ivs.add(0);
		}
		for (int i = 0; i < this.value; i++) {
			ivs.set(i, 31);
		}
		for (int i = this.value; i < ivs.size(); i++) {
			ivs.set(i, 1 + (int) (Math.random() * 31));
		}

		Collections.shuffle(ivs);

		List<BattleStatsType> stats = new ArrayList<>();
		for (BattleStatsType stat : BattleStatsType.values()) {
			stats.add(stat);
		}

		for (int i = 0; i < ivs.size(); i++) {
			BattleStatsType stat = stats.get(i + 1);
			pokemon.getIVs().setStat(stat, ivs.get(i));
		}
	}
}
