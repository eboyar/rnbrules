package ethan.hoenn.rnbrules.gui.battleoverlays;

import static net.minecraftforge.api.distmarker.Dist.CLIENT;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = CLIENT)
public class OverlayEvents {

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
			LocationPopupOverlay.tick();
		}
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			LocationPopupOverlay.render(event.getMatrixStack());
		}
	}
}
