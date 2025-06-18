package ethan.hoenn.rnbrules.commands.fossil;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.RNBConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetFossilCompletionCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setfossilcompletioncommand")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("command", StringArgumentType.greedyString()).executes(context -> setFossilCompletionCommand(context, StringArgumentType.getString(context, "command"))));

		dispatcher.register(command);
	}

	private static int setFossilCompletionCommand(CommandContext<CommandSource> context, String command) {
		CommandSource source = context.getSource();

		RNBConfig.setFossilCompletionCommand(command);

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Fossil completion command set to: " + TextFormatting.YELLOW + command), true);

		return 1;
	}
}
