package ethan.hoenn.rnbrules.utils.enums;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;

public enum FlightDestination {
	LITTLEROOT("Littleroot Town", Items.OAK_SAPLING, TextFormatting.GREEN, "A town that can't be shaded any hue."),
	OLDALE("Oldale Town", Items.WHEAT, TextFormatting.GREEN, "Where things start off scarce."),
	PETALBURG("Petalburg City", Items.LILY_PAD, TextFormatting.YELLOW, "Where people mingle with nature."),
	DEWFORD("Dewford Town", Items.PRISMARINE_SHARD, TextFormatting.BLUE, "A tiny island in the blue sea."),
	RUSTBORO("Rustboro City", Items.STONE_BRICKS, TextFormatting.YELLOW, "The city probing the integration of nature and science."),
	SLATEPORT("Slateport City", Items.LIGHT_BLUE_BANNER, TextFormatting.AQUA, "The beating heart of Hoenn."),
	MAUVILLE("Mauville City", Items.REDSTONE_LAMP, TextFormatting.YELLOW, "The city of legendary innovation."),
	PACIFIDLOG("Pacifidlog Town", Items.WATER_BUCKET, TextFormatting.GREEN, "Where the morning sun smiles upon the waters."),
	VERDANTURF("Verdanturf Town", Items.PINK_TULIP, TextFormatting.GREEN, "The windswept highlands with the sweet fragrance of grass."),
	FALLARBOR("Fallarbor Town", Items.CLAY, TextFormatting.GRAY, "A farm community with small gardens."),
	LAVARIDGE("Lavaridge Town", Items.LAVA_BUCKET, TextFormatting.RED, "An old town where the past meets the present."),
	FORTREE("Fortree City", Items.JUNGLE_LOG, TextFormatting.YELLOW, "The treetop city that frolics with nature."),
	LILYCOVE("Lilycove City", Items.BLUE_DYE, TextFormatting.AQUA, "Where the land ends and the sea begins."),
	MOSSDEEP("Mossdeep City", Items.SEA_LANTERN, TextFormatting.BLUE, "Our slogan: Cherish Pokémon!"),
	SOOTOPOLIS("Sootopolis City", Items.PACKED_ICE, TextFormatting.BLUE, "The mystical city where history slumbers.");

	private final String displayName;
	private final Item representativeItem;
	private final TextFormatting textColor;
	private final String description;

	FlightDestination(String displayName, Item representativeItem, TextFormatting textColor, String description) {
		this.displayName = displayName;
		this.representativeItem = representativeItem;
		this.textColor = textColor;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Item getRepresentativeItem() {
		return representativeItem;
	}

	public TextFormatting getTextColor() {
		return textColor;
	}

	public String getDescription() {
		return description;
	}

	public String getDestinationId() {
		return name().toLowerCase();
	}

	public static FlightDestination fromString(String destinationId) {
		try {
			return valueOf(destinationId.toUpperCase());
		} catch (IllegalArgumentException e) {
			for (FlightDestination destination : values()) {
				if (destination.getDestinationId().equalsIgnoreCase(destinationId)) {
					return destination;
				}
			}
			return null;
		}
	}
}
