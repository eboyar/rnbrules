package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;

public class NPCClearCompletedDialogue {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcclearcompleteddialogue")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> clearCompletedDialogues(context.getSource(), EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int clearCompletedDialogues(CommandSource source, net.minecraft.entity.Entity entity) {
		boolean success = DialogueNPCManager.get().clearCompletedDialogues(source, entity);
		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
