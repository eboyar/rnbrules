package ethan.hoenn.rnbrules.utils.enums;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;

public enum FerryDestination {
	PETALBURG("Petalburg City", Items.LILY_PAD, TextFormatting.YELLOW, "Where people mingle with nature.", FerryRoute.WESTERN_HOENN),
	DEWFORD("Dewford Town", Items.PRISMARINE_CRYSTALS, TextFormatting.BLUE, "A tiny island in the blue sea.", FerryRoute.WESTERN_HOENN),
	LITTLEROOT("Littleroot Town", Items.OAK_BOAT, TextFormatting.GREEN, "A town that can't be shaded any hue.", FerryRoute.WESTERN_HOENN),
	ROUTE_109("Route 109", Items.SAND, TextFormatting.GOLD, "Slateport's beautiful beach, with the finest sand.", FerryRoute.WESTERN_HOENN),

	SLATEPORT("Slateport City", Items.LIGHT_BLUE_BANNER, TextFormatting.AQUA, "The port where people and Pokémon cross paths with nature.", FerryRoute.EASTERN_HOENN),
	PACIFIDLOG("Pacifidlog Town", Items.WATER_BUCKET, TextFormatting.GREEN, "Where the morning sun smiles upon the waters.", FerryRoute.EASTERN_HOENN),
	SOOTOPOLIS("Sootopolis City", Items.PACKED_ICE, TextFormatting.BLUE, "The mystical city where history slumbers.", FerryRoute.EASTERN_HOENN),
	MOSSDEEP("Mossdeep City", Items.SEA_LANTERN, TextFormatting.BLUE, "Our slogan: Cherish Pokémon!", FerryRoute.EASTERN_HOENN),
	LILYCOVE("Lilycove City", Items.BLUE_DYE, TextFormatting.AQUA, "Where the land ends and the sea begins.", FerryRoute.EASTERN_HOENN),
	EVER_GRANDE("Ever Grande City", Items.EMERALD_BLOCK, TextFormatting.GREEN, "The end of the journey begins here.", FerryRoute.EASTERN_HOENN);

	private final String displayName;
	private final Item representativeItem;
	private final TextFormatting textColor;
	private final String description;
	private final FerryRoute route;

	FerryDestination(String displayName, Item representativeItem, TextFormatting textColor, String description, FerryRoute route) {
		this.displayName = displayName;
		this.representativeItem = representativeItem;
		this.textColor = textColor;
		this.description = description;
		this.route = route;
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

	public FerryRoute getRoute() {
		return route;
	}

	public String getDestinationId() {
		return name().toLowerCase();
	}

	public static FerryDestination fromString(String destinationId) {
		try {
			return valueOf(destinationId.toUpperCase());
		} catch (IllegalArgumentException e) {
			for (FerryDestination destination : values()) {
				if (destination.getDestinationId().equalsIgnoreCase(destinationId)) {
					return destination;
				}
			}
			return null;
		}
	}
}
