package ethan.hoenn.rnbrules.dialogue.actions;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class BattleDependencyActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "ADD_BATTLEDEP";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (action.getBattledeps() == null) {
			return;
		}

		List<String> battleDeps = new ArrayList<>(action.getBattledeps());

		if (player.getServer() != null) {
			player
				.getServer()
				.execute(() -> {
					BattleDependencyManager manager = BattleDependencyManager.get((ServerWorld) player.level);

					for (String depId : battleDeps) {
						manager.addPlayerDependency(player.getUUID(), depId);
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
