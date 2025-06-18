package ethan.hoenn.rnbrules.utils.misc;

import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import java.util.*;

public class GrowthHelper {

	public static final Map<RegistryValue<Species>, List<EnumGrowth>> RESTRICTED_GROWTHS = new HashMap<>();

	static {
		RESTRICTED_GROWTHS.put(PixelmonSpecies.WAILORD, Arrays.asList(EnumGrowth.Microscopic, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.TURTONATOR, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Small));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.STEELIX, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.WALREIN, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.ONIX, Arrays.asList(EnumGrowth.Pygmy, EnumGrowth.Microscopic));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.BEWEAR, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.AGGRON, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.SALAMENCE, Arrays.asList(EnumGrowth.Small, EnumGrowth.Ordinary));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.COPPERAJAH, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GYARADOS, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.CONKELDURR, Collections.singletonList(EnumGrowth.Small));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.MILOTIC, Arrays.asList(EnumGrowth.Small, EnumGrowth.Ordinary));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.DRAGAPULT, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GOODRA, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.KOMMOO, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.RHYPERIOR, Collections.singletonList(EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.AVALUGG, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GIGALITH, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GARCHOMP, Arrays.asList(EnumGrowth.Small, EnumGrowth.Ordinary));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.CENTISKORCH, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.OVERQWIL, Arrays.asList(EnumGrowth.Pygmy, EnumGrowth.Microscopic));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.DRAGONITE, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.WEEZING, Collections.singletonList(EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.CHARIZARD, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.ARCANINE, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.VENUSAUR, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.CLEFABLE, Arrays.asList(EnumGrowth.Pygmy, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.TORTERRA, Arrays.asList(EnumGrowth.Small, EnumGrowth.Ordinary));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.MUDSDALE, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.URSALUNA, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GRIMMSNARL, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GENGAR, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.JELLICENT, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.NOIVERN, Arrays.asList(EnumGrowth.Small, EnumGrowth.Runt));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.ARAQUANID, Collections.singletonList(EnumGrowth.Microscopic));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.MAGMORTAR, Collections.singletonList(EnumGrowth.Small));
		RESTRICTED_GROWTHS.put(PixelmonSpecies.GRENINJA, Arrays.asList(EnumGrowth.Runt, EnumGrowth.Pygmy));

		Map<RegistryValue<Species>, List<EnumGrowth>> preEvoAdditions = new HashMap<>();
		List<Map.Entry<RegistryValue<Species>, List<EnumGrowth>>> initialEntries = new ArrayList<>(RESTRICTED_GROWTHS.entrySet());

		for (Map.Entry<RegistryValue<Species>, List<EnumGrowth>> entry : initialEntries) {
			RegistryValue<Species> evolvedSpeciesRV = entry.getKey();
			List<EnumGrowth> restrictedGrowths = entry.getValue();

			Species evolvedSpecies = evolvedSpeciesRV.getValueUnsafe();
			if (evolvedSpecies != null) {
				List<Species> allPreEvolutions = evolvedSpecies.getDefaultForm().getPreEvolutions();
				for (Species preEvoSpecies : allPreEvolutions) {
					if (preEvoSpecies != null) {
						preEvoAdditions.put(preEvoSpecies.getRegistryValue(), restrictedGrowths);
					}
				}
			}
		}
		RESTRICTED_GROWTHS.putAll(preEvoAdditions);
	}

	public static Optional<EnumGrowth> getRestrictedGrowth(Species species) {
		if (species == null) {
			return Optional.empty();
		}
		List<EnumGrowth> allowedGrowths = RESTRICTED_GROWTHS.get(species.getRegistryValue());
		if (allowedGrowths != null && !allowedGrowths.isEmpty()) {
			return Optional.of(allowedGrowths.get(RandomHelper.rand.nextInt(allowedGrowths.size())));
		}
		return Optional.empty();
	}

	public static EnumGrowth getDefaultWeightedGrowth() {
		int chance = RandomHelper.rand.nextInt(100);
		if (chance < 60) {
			return EnumGrowth.Ordinary;
		} else if (chance < 80) {
			return EnumGrowth.Huge;
		} else {
			return EnumGrowth.Small;
		}
	}
}
