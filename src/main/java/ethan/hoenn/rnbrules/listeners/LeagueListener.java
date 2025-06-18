package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.events.BeatTrainerEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LeagueListener {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBeatTrainer(BeatTrainerEvent event) {
		ServerPlayerEntity player = event.player;
		LeagueManager lm = LeagueManager.get(player.getLevel());

		if (!event.trainer.getPersistentData().hasUUID("LeagueTrainer")) {
			return;
		}

		UUID originalTrainerUUID = event.trainer.getPersistentData().getUUID("LeagueTrainer");

		LeagueManager.LeagueMember leagueMember = LeagueManager.getLeagueMemberFromUUID(originalTrainerUUID);
		if (leagueMember == null) {
			return;
		}

		boolean isDoubles = event.trainer.getBattleType().equals(BattleType.DOUBLE);

		int memberIndex = leagueMember.ordinal();
		LeagueManager.Progress newProgress;

		if (leagueMember == LeagueManager.LeagueMember.WALLACE) {
			newProgress = LeagueManager.Progress.CHAMPION;
		} else {
			newProgress = isDoubles ? LeagueManager.Progress.DOUBLES : LeagueManager.Progress.SINGLES;
		}

		lm.setPlayerProgress(player.getUUID(), memberIndex, newProgress);

		lm.queueCleanupLeagueBattle(player.getUUID());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBattleEnd(BattleEndEvent event) {
		List<BattleParticipant> participants = event.getBattleController().participants;
		PlayerParticipant pp = null;
		TrainerParticipant tp = null;

		for (BattleParticipant participant : participants) {
			if (participant instanceof PlayerParticipant) {
				pp = (PlayerParticipant) participant;
			} else if (participant instanceof TrainerParticipant) {
				tp = (TrainerParticipant) participant;
			}
		}

		if (tp == null || pp == null) {
			return;
		}

		if (tp.trainer == null) {
			return;
		}

		if (!tp.trainer.getPersistentData().hasUUID("LeagueTrainer")) {
			return;
		}

		BattleResults playerResult = event.getResults().get(pp);

		if (playerResult == BattleResults.VICTORY) {
			return;
		}

		ServerPlayerEntity player = pp.player;
		LeagueManager lm = LeagueManager.get(player.getLevel());

		lm.resetPlayerLeagueProgress(player.getUUID());

		player.sendMessage(new StringTextComponent("§cYou have lost to §6" + tp.trainer.getName("en_us") + "§c. You must restart the Hoenn League."), player.getUUID());

		lm.queueCleanupLeagueBattle(player.getUUID());
	}
}
