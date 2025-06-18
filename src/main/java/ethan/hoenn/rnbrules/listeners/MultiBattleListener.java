package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import ethan.hoenn.rnbrules.utils.managers.MultiBattleManager;
import java.util.List;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MultiBattleListener {

	//order of checks -> Dependencies, Gauntlets, Dialogue, Clauses, Multi Battle
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBattleStart(BattleStartedEvent.Pre event) {
		List<BattleParticipant> bps = event.getBattleController().participants;

		PlayerParticipant pp = null;
		TrainerParticipant tp = null;

		for (BattleParticipant bp : bps) {
			if (bp instanceof TrainerParticipant) {
				tp = (TrainerParticipant) bp;
			} else if (bp instanceof PlayerParticipant) {
				pp = (PlayerParticipant) bp;
			}
		}

		if (tp == null || pp == null) {
			return;
		}

		if (tp.trainer.getPersistentData().hasUUID("LeagueTrainer")) {
			return;
		}

		if (MultiBattleManager.isBattleOngoing(pp.player.getUUID())) {
			return;
		}
		event.setCanceled(true);
		MultiBattleManager.startTrainerBattle(pp, tp);
	}
}
