package ethan.hoenn.rnbrules.gui.league;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import ethan.hoenn.rnbrules.utils.misc.PlayerFreezeTracker;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class LeagueBattleCountdown {

	private static final Map<UUID, LeagueBattleCountdown> ACTIVE_COUNTDOWNS = new HashMap<>();

	private final ServerPlayerEntity player;
	private final NPCTrainer opponent;
	private int ticksRemaining;
	private int lastDisplayedSecond;

	private LeagueBattleCountdown(ServerPlayerEntity player, NPCTrainer opponent, LeagueManager.LeagueMember leagueMember) {
		this.player = player;
		this.opponent = opponent;
		this.ticksRemaining = 100;
		this.lastDisplayedSecond = 5;
		StringTextComponent battleWithText = getStringTextComponent(leagueMember);

		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, battleWithText, 10, 130, 20));

		PlayerFreezeTracker.freezePlayer(player, 100, -90.0f);
	}

	private void releasePlayer() {
		PlayerFreezeTracker.releasePlayer(player);
	}

	private StringTextComponent getStringTextComponent(LeagueManager.LeagueMember leagueMember) {
		StringTextComponent battleWithText = new StringTextComponent("Battle with ");
		battleWithText.withStyle(style -> style.withColor(TextFormatting.GREEN).withItalic(false));

		StringTextComponent nameText = new StringTextComponent(getLeagueMemberName(leagueMember));
		nameText.withStyle(style -> style.withColor(TextFormatting.GOLD).withItalic(false));

		battleWithText.append(nameText);
		return battleWithText;
	}

	public static void start(ServerPlayerEntity player, NPCTrainer opponent, LeagueManager.LeagueMember leagueMember) {
		UUID playerID = player.getUUID();

		ACTIVE_COUNTDOWNS.remove(playerID);

		LeagueBattleCountdown countdown = new LeagueBattleCountdown(player, opponent, leagueMember);
		ACTIVE_COUNTDOWNS.put(playerID, countdown);

		countdown.updateSubtitle(5);
		countdown.playStartSound();
	}

	public static void tickAll() {
		Iterator<Map.Entry<UUID, LeagueBattleCountdown>> iterator = ACTIVE_COUNTDOWNS.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, LeagueBattleCountdown> entry = iterator.next();
			LeagueBattleCountdown countdown = entry.getValue();

			boolean completed = countdown.tick();

			if (completed) {
				iterator.remove();
			}
		}
	}

	private boolean tick() {
		if (!player.isAlive() || player.hasDisconnected()) {
			return true;
		}

		ticksRemaining--;

		int currentSecond = (ticksRemaining + 19) / 20;

		if (currentSecond < lastDisplayedSecond) {
			lastDisplayedSecond = currentSecond;

			if (currentSecond > 0) {
				updateSubtitle(currentSecond);
				playTickSound(currentSecond);
			} else {
				startBattle();
				playBattleStartSound();
				return true;
			}
		}

		return false;
	}

	private void updateSubtitle(int count) {
		String subtitle = "Battle starting in " + count + "...";

		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(subtitle).withStyle(style -> style.withColor(TextFormatting.WHITE).withItalic(false)), 10, 60, 20));
	}

	private void playStartSound() {
		player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0F, 0.8F);
	}

	private void playTickSound(int count) {
		float pitch = 0.8F + ((5 - count) * 0.1F);
		player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0F, pitch);
	}

	private void playBattleStartSound() {
		player.playNotifySound(SoundEvents.ANVIL_LAND, SoundCategory.PLAYERS, 0.7F, 1.0F);
	}

	private void startBattle() {
		releasePlayer();

		UUID templateUUID = LeagueManager.get(player.getLevel()).getOriginalTrainerUUID(opponent.getUUID());
		LeagueManager.LeagueMember leagueMember = LeagueManager.getLeagueMemberFromUUID(templateUUID);
		LeagueManager leagueManager = LeagueManager.get(player.getLevel());

		if (leagueMember != null && leagueManager.shouldStartLeagueDialogue(player.getUUID(), templateUUID)) {
			String dialogueId = LeagueManager.getDialogueIdForLeagueMember(leagueMember);
			if (dialogueId != null) {
				leagueManager.startLeagueDialogue(player, dialogueId, opponent);
				return;
			}
		}

		leagueManager.startLeagueBattle(player, opponent);
	}

	private String getLeagueMemberName(LeagueManager.LeagueMember member) {
		switch (member) {
			case SIDNEY:
				return "Elite Four Sidney";
			case PHOEBE:
				return "Elite Four Phoebe";
			case GLACIA:
				return "Elite Four Glacia";
			case DRAKE:
				return "Elite Four Drake";
			case WALLACE:
				return "Champion Wallace";
			default:
				return "Elite Four";
		}
	}
}
