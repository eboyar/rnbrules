package ethan.hoenn.rnbrules.commands.globalot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AddGlobalOT {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("addglobalot")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("name", StringArgumentType.string()).executes(context -> addGlobalOT(context, StringArgumentType.getString(context, "name"))));

		dispatcher.register(command);
	}

	private static int addGlobalOT(CommandContext<CommandSource> context, String otName) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(player.getLevel());

		boolean added = manager.addGlobalOT(otName);

		if (added) {
			context.getSource().sendSuccess(new StringTextComponent("Added new global OT: " + otName).withStyle(TextFormatting.GREEN), true);
		} else {
			context.getSource().sendFailure(new StringTextComponent("Global OT already exists: " + otName));
		}

		return added ? 1 : 0;
	}
}
