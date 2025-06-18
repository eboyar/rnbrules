package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class ChatMessageActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "CHAT_MESSAGE";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (action.getMessages() == null) {
			return;
		}

		List<String> messages = new ArrayList<>(action.getMessages());

		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					for (String message : messages) {
						player.sendMessage(new StringTextComponent(message), player.getUUID());
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
