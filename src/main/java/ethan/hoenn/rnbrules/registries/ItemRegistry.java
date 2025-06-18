package ethan.hoenn.rnbrules.registries;

import com.pixelmonmod.pixelmon.items.group.PixelmonItemGroups;
import ethan.hoenn.rnbrules.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "rnbrules");

	public static final RegistryObject<Item> ENDLESS_CANDY = ITEMS.register("endlesscandy", () -> new EndlessCandy(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_RESTORATION)));
	public static final RegistryObject<Item> MAX_PARTY_RESTORE = ITEMS.register("maxpartyrestore", () -> new MaxPartyRestore(new Item.Properties().durability(16).tab(PixelmonItemGroups.TAB_RESTORATION))
	);
	public static final RegistryObject<Item> PARTY_RESTORE = ITEMS.register("partyrestore", () -> new PartyRestore(new Item.Properties().durability(8).tab(PixelmonItemGroups.TAB_RESTORATION)));
	public static final RegistryObject<Item> POKELINK = ITEMS.register("pokelink", () -> new PokeLink(new Item.Properties().durability(50).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> BATTERY = ITEMS.register("battery", () -> new Battery(new Item.Properties().stacksTo(16).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> BIG_BACKPACK = ITEMS.register("bigbackpack", () -> new BigBackpack(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_UTILITY)));
	public static final RegistryObject<Item> SCROLL_OF_WATER = ITEMS.register("scrollofwater", () -> new GenericUpgradeItem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> SCROLL_OF_DARKNESS = ITEMS.register("scrollofdarkness", () -> new GenericUpgradeItem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT))
	);
	public static final RegistryObject<Item> MEGA_TRANSCEIVER = ITEMS.register("megatransceiver", () ->
		new GenericUpgradeComponentItem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT))
	);
	public static final RegistryObject<Item> MAX_CASING = ITEMS.register("maxcasing", () -> new GenericUpgradeComponentItem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> MAX_HEALING_SERUM = ITEMS.register("maxhealingserum", () -> new MaxHealingSerum(new Item.Properties().stacksTo(16).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> MAX_AEROSOLIZER = ITEMS.register("maxaerosolizer", () ->
		new GenericUpgradeComponentItem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT))
	);
	public static final RegistryObject<Item> MEGA_GEM = ITEMS.register("megagem", () -> new MegaGem(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT)));
	public static final RegistryObject<Item> SAFARI_PASS = ITEMS.register("safaripass", () -> new SafariPass(new Item.Properties().stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT)));

	public static final RegistryObject<Item> LOCATION_WAND = ITEMS.register("locationwand", () -> new LocationWand(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS)));

	public static final RegistryObject<Item> LOCATION_BLOCK = ITEMS.register("location_block", () ->
		new LocationBlockItem(BlockRegistry.LOCATION_BLOCK.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS))
	);

	public static final RegistryObject<Item> SIDNEY = ITEMS.register("sidney", () -> new GenericGUIItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));
	public static final RegistryObject<Item> PHOEBE = ITEMS.register("phoebe", () -> new GenericGUIItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));
	public static final RegistryObject<Item> GLACIA = ITEMS.register("glacia", () -> new GenericGUIItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));
	public static final RegistryObject<Item> DRAKE = ITEMS.register("drake", () -> new GenericGUIItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));
	public static final RegistryObject<Item> WALLACE = ITEMS.register("wallace", () -> new GenericGUIItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
