package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "COMMAND";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (action.getCommands() == null) {
			return;
		}

		List<String> commands = new ArrayList<>(action.getCommands());

		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					net.minecraft.command.CommandSource source = player.getServer().createCommandSourceStack().withEntity(player).withPosition(player.position()).withPermission(4);

					for (String command : commands) {
						String parsedCommand = command.replace("@p", player.getName().getString());
						player.getServer().getCommands().performCommand(source, parsedCommand);
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
