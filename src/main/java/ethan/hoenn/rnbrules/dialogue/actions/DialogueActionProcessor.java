package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface DialogueActionProcessor {
	void processAction(DialogueActionData action, ServerPlayerEntity player);

	String getActionType();
}
