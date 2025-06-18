package ethan.hoenn.rnbrules.registries;

import ethan.hoenn.rnbrules.blocks.LocationBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "rnbrules");

	public static final RegistryObject<Block> LOCATION_BLOCK = BLOCKS.register("location_block", () -> new LocationBlock());

	public static void registerBlockItems(DeferredRegister<Item> items) {
		BLOCKS.getEntries()
			.forEach(block -> {
				items.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties().tab(ItemGroup.TAB_DECORATIONS)));
			});
	}

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
}
