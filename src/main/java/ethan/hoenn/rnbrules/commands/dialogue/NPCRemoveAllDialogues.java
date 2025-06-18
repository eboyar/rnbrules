package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;

public class NPCRemoveAllDialogues {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremovealldialogues")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeAllDialoguesFromNPC(context.getSource(), EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int removeAllDialoguesFromNPC(CommandSource source, Entity entity) {
		boolean success = DialogueNPCManager.get().removeAllDialoguesFromNPC(source, entity);
		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
