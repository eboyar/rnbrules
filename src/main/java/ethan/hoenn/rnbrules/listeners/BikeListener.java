package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.bikes.BikeEntity;
import com.pixelmonmod.pixelmon.enums.EnumBike;
import com.pixelmonmod.pixelmon.items.BikeItem;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BikeListener {

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity player = event.getPlayer();
		World world = event.getWorld();

		if (world.isClientSide) return;

		ItemStack stack = event.getItemStack();

		if (stack.getItem() instanceof BikeItem) {
			player
				.getServer()
				.execute(() -> {
					List<BikeEntity> nearbyBikes = world.getEntitiesOfClass(BikeEntity.class, player.getBoundingBox().inflate(6));
					BikeEntity closest = null;
					double minDistance = Double.MAX_VALUE;

					for (BikeEntity bike : nearbyBikes) {
						double dist = bike.distanceToSqr(player);
						if (dist < minDistance) {
							minDistance = dist;
							closest = bike;
						}
					}
					if (closest == null) {} else {
						if (!closest.isPassenger() && closest.getPassengers().isEmpty()) {
							player.startRiding(closest);
						}
					}
				});
		}
	}

	@SubscribeEvent
	public static void onDismount(EntityMountEvent event) {
		if (!event.isMounting() && event.getEntity() instanceof PlayerEntity && event.getEntityBeingMounted() instanceof BikeEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityMounting();
			BikeEntity bike = (BikeEntity) event.getEntityBeingMounted();

			if (!player.level.isClientSide) {
				if (bike.getBikeType().equals(EnumBike.Acro)) {
					player.addItem(new ItemStack(PixelmonItems.acro_bike));
				} else {
					player.addItem(new ItemStack(PixelmonItems.mach_bike));
				}
				bike.remove();
			}
		}
	}
}
