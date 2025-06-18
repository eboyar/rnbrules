package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class DialogueActionManager {

	private static final DialogueActionManager INSTANCE = new DialogueActionManager();

	private final Map<String, DialogueActionProcessor> actionProcessors = new HashMap<>();

	private DialogueActionManager() {
		registerDefaultProcessors();
	}

	public static DialogueActionManager getInstance() {
		return INSTANCE;
	}

	private void registerDefaultProcessors() {
		registerActionProcessor(new ChatMessageActionProcessor());
		registerActionProcessor(new CommandActionProcessor());
		registerActionProcessor(new BattleDependencyActionProcessor());
		registerActionProcessor(new CompleteDialogueActionProcessor());
		registerActionProcessor(new CompleteLeagueActionProcessor());
		registerActionProcessor(new StartBattleActionProcessor());
		registerActionProcessor(new OTPurchaseActionProcessor());
		registerActionProcessor(new OTRewardActionProcessor());
	}

	public void registerActionProcessor(DialogueActionProcessor processor) {
		actionProcessors.put(processor.getActionType().toUpperCase(), processor);
	}

	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (action == null || action.getType() == null) {
			return;
		}

		try {
			String actionType = action.getType().toUpperCase();
			DialogueActionProcessor processor = actionProcessors.get(actionType);

			if (processor != null) {
				processor.processAction(action, player);
			} else {
				if (player.getServer() != null) {
					player
						.getServer()
						.execute(() -> {
							player.sendMessage(new StringTextComponent("§cError: Unknown dialogue action type: " + action.getType()), player.getUUID());
						});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (player.getServer() != null) {
				player
					.getServer()
					.execute(() -> {
						player.sendMessage(new StringTextComponent("§cError processing dialogue action. See server logs."), player.getUUID());
					});
			}
		}
	}
}
