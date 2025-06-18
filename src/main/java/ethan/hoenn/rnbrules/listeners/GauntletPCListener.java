package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.blocks.machines.PCBlock;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import java.util.HashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GauntletPCListener {

	private static final HashSet<PlayerEntity> notifiedPlayers = new HashSet<>();
	private static int tickCounter = 0;

	@SubscribeEvent
	public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
		if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof PCBlock) {
			PlayerEntity player = event.getPlayer();

			if (player instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				GauntletManager gm = GauntletManager.get(serverPlayer.getLevel());

				if (gm.isPartOfAnyGauntlet(player.getUUID())) {
					if (!notifiedPlayers.contains(player)) {
						notifiedPlayers.add(player);
						player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to change your Pokemon."), player.getUUID());
					}
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		tickCounter++;

		if (tickCounter >= 10) {
			notifiedPlayers.clear();
			tickCounter = 0;
		}
	}
}
