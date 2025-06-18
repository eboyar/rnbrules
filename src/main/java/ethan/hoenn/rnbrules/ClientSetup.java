package ethan.hoenn.rnbrules;

import ethan.hoenn.rnbrules.gui.battleinfo.BattleInfoOverlay;
import ethan.hoenn.rnbrules.music.event.EventRegistry;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

	@OnlyIn(Dist.CLIENT)
	public static void init(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new BattleInfoOverlay());
		GuiRegistry.registerScreens(event);
		new EventRegistry();
	}
}
