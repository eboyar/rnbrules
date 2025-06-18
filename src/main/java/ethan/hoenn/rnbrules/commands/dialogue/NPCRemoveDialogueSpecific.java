package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;

public class NPCRemoveDialogueSpecific {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremovedialog")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("entity", EntityArgument.entity())
						.executes(context -> listDialoguesForNPC(context, EntityArgument.getEntity(context, "entity")))
						.then(
							Commands.argument("dialogOrder", IntegerArgumentType.integer(1)).executes(context ->
								removeDialogueFromNPC(context.getSource(), EntityArgument.getEntity(context, "entity"), IntegerArgumentType.getInteger(context, "dialogOrder"))
							)
						)
				)
		);
	}

	private static int listDialoguesForNPC(CommandContext<CommandSource> context, Entity entity) {
		return DialogueNPCManager.get().listDialoguesForNPC(context.getSource(), entity);
	}

	private static int removeDialogueFromNPC(CommandSource source, Entity entity, int dialogOrder) {
		boolean success = DialogueNPCManager.get().removeSpecificDialogueFromNPC(source, entity, dialogOrder);
		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
