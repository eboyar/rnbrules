package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;

public class NPCRemoveDialogue {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremovedialogue")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeDialogueFromNPC(context.getSource(), EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int removeDialogueFromNPC(CommandSource source, net.minecraft.entity.Entity entity) {
		boolean success = DialogueNPCManager.get().removeDialogueFromNPC(source, entity);
		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
