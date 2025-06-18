package ethan.hoenn.rnbrules.commands.globalot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCRemoveGlobalOT {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npcremoveglobalot")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeGlobalOTFromNPC(context, EntityArgument.getEntity(context, "entity"))));

		dispatcher.register(command);
	}

	private static int removeGlobalOTFromNPC(CommandContext<CommandSource> context, Entity entity) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(player.getLevel());

		boolean removed = manager.npcRemoveGlobalOT(entity);

		if (removed) {
			context.getSource().sendSuccess(new StringTextComponent("Removed global OT from entity at " + entity.blockPosition()).withStyle(TextFormatting.GREEN), true);
		} else {
			context.getSource().sendFailure(new StringTextComponent("Failed to remove global OT from entity. The entity might not have one."));
		}

		return removed ? 1 : 0;
	}
}
