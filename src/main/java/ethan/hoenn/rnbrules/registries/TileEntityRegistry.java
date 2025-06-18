package ethan.hoenn.rnbrules.registries;

import ethan.hoenn.rnbrules.blocks.LocationBlockTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegistry {

	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "rnbrules");

	public static final RegistryObject<TileEntityType<LocationBlockTileEntity>> LOCATION_BLOCK = TILE_ENTITIES.register("location_block", () ->
		TileEntityType.Builder.of(LocationBlockTileEntity::new, BlockRegistry.LOCATION_BLOCK.get()).build(null)
	);

	public static void register(IEventBus eventBus) {
		TILE_ENTITIES.register(eventBus);
	}
}
