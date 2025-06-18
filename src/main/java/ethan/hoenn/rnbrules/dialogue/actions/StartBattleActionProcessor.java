package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;

public class StartBattleActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "START_BATTLE";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					UUID playerUUID = player.getUUID();

					DialogueNPCManager dialogueManager = DialogueNPCManager.get();
					if (dialogueManager.hasActiveTrainerDialogue(playerUUID)) {
						dialogueManager.completeTrainerDialogue(player);
						dialogueManager.startPendingBattleAfterDialogue(player);
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
