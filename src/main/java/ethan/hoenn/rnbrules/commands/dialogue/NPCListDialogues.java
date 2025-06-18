package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;

public class NPCListDialogues {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npclistdialogues")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> listDialoguesForNPC(context.getSource(), EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int listDialoguesForNPC(CommandSource source, Entity entity) {
		return DialogueNPCManager.get().listDialoguesForNPC(source, entity);
	}
}
