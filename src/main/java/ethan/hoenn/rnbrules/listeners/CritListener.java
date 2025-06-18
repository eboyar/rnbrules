package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.AttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CritListener {

	@SubscribeEvent
	public void onCrit (AttackEvent.CriticalHit event) {
		event.critMultiplier = 1.5;
	}
}
