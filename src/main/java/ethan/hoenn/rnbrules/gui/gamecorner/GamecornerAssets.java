package ethan.hoenn.rnbrules.gui.gamecorner;

import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GamecornerAssets {

	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean initialized = false;

	public static ItemStack BALANCE_BADGE_ITEM;
	public static ItemStack HEAT_BADGE_ITEM;
	public static ItemStack FEATHER_BADGE_ITEM;
	public static ItemStack MIND_BADGE_ITEM;
	public static ItemStack RAIN_BADGE_ITEM;

	private static Pokemon createPokemon(RegistryValue<Species> speciesValue, String form) {
		Species species = speciesValue.getValueUnsafe();
		if (species == null) {
			LOGGER.error("Failed to create Pokemon: Species registry value '{}' resolved to null. Skipping this Pokemon.", speciesValue.getKey());
			return null;
		}
		Pokemon pokemon = PokemonFactory.create(species);
		if (form != null && !form.equals("base") && species.hasForm(form)) {
			pokemon.setForm(form);
		} else if (form != null && !form.equals("base")) {
			LOGGER.warn("Form '{}' not found for species '{}'. Using base form.", form, species.getName());
		}
		return pokemon;
	}

	private static List<Pokemon> BALANCE_BADGE_POKEMON;
	private static List<Pokemon> HEAT_BADGE_POKEMON;
	private static List<Pokemon> FEATHER_BADGE_POKEMON;
	private static List<Pokemon> MIND_BADGE_POKEMON;
	private static List<Pokemon> RAIN_BADGE_POKEMON;

	public static Map<ItemStack, List<Pokemon>> BADGE_TO_POKEMON_POOL;

	private static Map<ItemStack, List<Pokemon>> createBadgeMap() {
		Map<ItemStack, List<Pokemon>> map = new LinkedHashMap<>();
		map.put(BALANCE_BADGE_ITEM, BALANCE_BADGE_POKEMON);
		map.put(HEAT_BADGE_ITEM, HEAT_BADGE_POKEMON);
		map.put(FEATHER_BADGE_ITEM, FEATHER_BADGE_POKEMON);
		map.put(MIND_BADGE_ITEM, MIND_BADGE_POKEMON);
		map.put(RAIN_BADGE_ITEM, RAIN_BADGE_POKEMON);
		return Collections.unmodifiableMap(map);
	}

	public static void initializeAssets() {
		if (initialized) {
			return;
		}
		//LOGGER.info("Initializing GamecornerAssets...");

		BALANCE_BADGE_ITEM = new ItemStack(PixelmonItems.balance_badge);
		HEAT_BADGE_ITEM = new ItemStack(PixelmonItems.heat_badge);
		FEATHER_BADGE_ITEM = new ItemStack(PixelmonItems.feather_badge);
		MIND_BADGE_ITEM = new ItemStack(PixelmonItems.mind_badge);
		RAIN_BADGE_ITEM = new ItemStack(PixelmonItems.rain_badge);

		BALANCE_BADGE_POKEMON = Collections.unmodifiableList(
			Arrays.asList(
				createPokemon(PixelmonSpecies.VAPOREON, "base"),
				createPokemon(PixelmonSpecies.JOLTEON, "base"),
				createPokemon(PixelmonSpecies.FLAREON, "base"),
				createPokemon(PixelmonSpecies.ESPEON, "base"),
				createPokemon(PixelmonSpecies.UMBREON, "base"),
				createPokemon(PixelmonSpecies.LEAFEON, "base"),
				createPokemon(PixelmonSpecies.GLACEON, "base"),
				createPokemon(PixelmonSpecies.SYLVEON, "base")
			)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);

		HEAT_BADGE_POKEMON = Collections.unmodifiableList(
			Arrays.asList(
				createPokemon(PixelmonSpecies.ROTOM, "base"),
				createPokemon(PixelmonSpecies.ROTOM, "fan"),
				createPokemon(PixelmonSpecies.ROTOM, "wash"),
				createPokemon(PixelmonSpecies.ROTOM, "mow"),
				createPokemon(PixelmonSpecies.ROTOM, "heat"),
				createPokemon(PixelmonSpecies.ROTOM, "frost")
			)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);

		FEATHER_BADGE_POKEMON = Collections.unmodifiableList(
			Arrays.asList(createPokemon(PixelmonSpecies.SLOWKING, "galarian"), createPokemon(PixelmonSpecies.SLOWBRO, "galarian")).stream().filter(Objects::nonNull).collect(Collectors.toList())
		);

		MIND_BADGE_POKEMON = Collections.unmodifiableList(
			Arrays.asList(
				createPokemon(PixelmonSpecies.MEW, "base"),
				createPokemon(PixelmonSpecies.CELEBI, "base"),
				createPokemon(PixelmonSpecies.JIRACHI, "base"),
				createPokemon(PixelmonSpecies.VICTINI, "base")
			)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);

		RAIN_BADGE_POKEMON = Collections.unmodifiableList(
			Arrays.asList(
				createPokemon(RegistryValue.of(Species.class, "TAPUKOKO"), "base"),
				createPokemon(RegistryValue.of(Species.class, "TAPULELE"), "base"),
				createPokemon(RegistryValue.of(Species.class, "TAPUBULU"), "base"),
				createPokemon(RegistryValue.of(Species.class, "TAPUFINI"), "base")
			)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);

		BADGE_TO_POKEMON_POOL = createBadgeMap();

		initialized = true;
		//LOGGER.info("GamecornerAssets initialized successfully.");
	}

	public static ItemStack getPokemonPhoto(Pokemon pokemon) {
		return SpriteItemHelper.getPhoto(pokemon);
	}

	private GamecornerAssets() {}
}
