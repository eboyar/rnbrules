package ethan.hoenn.rnbrules.gui.fossils;

import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.enums.items.EnumFossils;
import java.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FossilAssets {

	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean initialized = false;

	private static final Map<Integer, ItemStack> FOSSIL_PREVIEWS = new HashMap<>();

	private static final List<ItemStack> ALL_FOSSIL_PREVIEWS = new ArrayList<>();

	public static void initializeAssets() {
		if (initialized) {
			return;
		}

		LOGGER.info("Initializing FossilAssets...");

		addFossilPreview(0, PixelmonSpecies.OMANYTE);
		addFossilPreview(1, PixelmonSpecies.KABUTO);
		addFossilPreview(2, PixelmonSpecies.AERODACTYL);
		addFossilPreview(3, PixelmonSpecies.LILEEP);
		addFossilPreview(4, PixelmonSpecies.ANORITH);
		addFossilPreview(5, PixelmonSpecies.CRANIDOS);
		addFossilPreview(6, PixelmonSpecies.SHIELDON);
		addFossilPreview(7, PixelmonSpecies.TIRTOUGA);
		addFossilPreview(8, PixelmonSpecies.ARCHEN);
		addFossilPreview(9, PixelmonSpecies.TYRUNT);
		addFossilPreview(10, PixelmonSpecies.AMAURA);

		for (int i = 0; i <= 10; i++) {
			ALL_FOSSIL_PREVIEWS.add(FOSSIL_PREVIEWS.get(i));
		}

		initialized = true;
		LOGGER.info("FossilAssets initialized successfully.");
	}

	private static void addFossilPreview(int index, RegistryValue<Species> species) {
		Species speciesValue = species.getValueUnsafe();

		try {
			Pokemon pokemon = PokemonFactory.create(speciesValue);
			if (pokemon != null) {
				ItemStack photoItem = SpriteItemHelper.getPhoto(pokemon);
				photoItem.setHoverName(new StringTextComponent(pokemon.getSpecies().getLocalizedName()).withStyle(style -> style.withBold(true).withItalic(false)));
				FOSSIL_PREVIEWS.put(index, photoItem);
			} else {
				LOGGER.error("Failed to create Pokemon for fossil preview: {}", speciesValue.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error creating fossil preview for " + speciesValue.getName(), e);
		}
	}

	public static ItemStack getPreviewForFossil(EnumFossils fossil) {
		if (fossil == null) {
			return ItemStack.EMPTY;
		}

		if (fossil.getIndex() == 14) {
			return ItemStack.EMPTY;
		}

		return FOSSIL_PREVIEWS.getOrDefault(fossil.getIndex(), ItemStack.EMPTY);
	}

	public static List<ItemStack> getAllPreviews() {
		List<ItemStack> shuffled = new ArrayList<>(ALL_FOSSIL_PREVIEWS);
		Collections.shuffle(shuffled);
		return shuffled;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	private FossilAssets() {}
}
