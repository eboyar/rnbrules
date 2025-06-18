package ethan.hoenn.rnbrules.dialogue.actions;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class CompleteLeagueActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "COMPLETE_LEAGUE";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					try {
						DialogueNPCManager dialogueManager = DialogueNPCManager.get();
						ServerWorld world = (ServerWorld) player.level;
						LeagueManager leagueManager = LeagueManager.get(world);

						DialogueNPCManager.ActiveDialogueData activeDialogue = dialogueManager.getActiveDialogue(player.getUUID());
						if (activeDialogue != null) {
							UUID npcUUID = activeDialogue.getNpcUUID();

							UUID originalUUID = leagueManager.getOriginalTrainerUUID(npcUUID);
							if (originalUUID != null && leagueManager.isLeagueMember(originalUUID)) {
								leagueManager.markLeagueDialogueCompleted(player.getUUID(), originalUUID);

								boolean completed = dialogueManager.completeDialogue(player);

								LeagueManager.LeagueMember leagueMember = LeagueManager.getLeagueMemberFromUUID(originalUUID);

								if (leagueMember != null) {
									Entity entity = world.getEntity(npcUUID);
									if (entity instanceof NPCTrainer) {
										leagueManager.startLeagueBattle(player, (NPCTrainer) entity);
									}
								}

								if (!completed) {
									System.err.println("Failed to complete regular dialogue for player " + player.getName().getString() + " with league member " + originalUUID);
								}
							} else {
								System.err.println("COMPLETE_LEAGUE action used on non-league member NPC: " + npcUUID + " for player " + player.getName().getString());

								dialogueManager.completeDialogue(player);
							}
						} else {
							System.err.println("No active dialogue found for COMPLETE_LEAGUE action for player " + player.getName().getString());
						}
					} catch (Exception e) {
						System.err.println("Error processing COMPLETE_LEAGUE action for player " + player.getName().getString() + ": " + e.getMessage());
						e.printStackTrace();
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
