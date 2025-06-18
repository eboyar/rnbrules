package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CompleteDialogueActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "COMPLETE";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					DialogueNPCManager manager = DialogueNPCManager.get();

					boolean completed = manager.completeDialogue(player);

					if (!completed) {
						System.err.println("Failed to complete dialogue for player " + player.getName().getString());
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
