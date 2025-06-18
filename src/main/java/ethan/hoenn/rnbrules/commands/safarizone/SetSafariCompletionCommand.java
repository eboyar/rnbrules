package ethan.hoenn.rnbrules.commands.safarizone;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.RNBConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetSafariCompletionCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setsafaricompletioncommand")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("command", StringArgumentType.greedyString()).executes(context -> setSafariCompletionCommand(context, StringArgumentType.getString(context, "command"))));

		dispatcher.register(command);
	}

	private static int setSafariCompletionCommand(CommandContext<CommandSource> context, String command) {
		CommandSource source = context.getSource();

		RNBConfig.setSafariCompletionCommand(command);

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Safari Zone completion command set to: " + TextFormatting.YELLOW + command), true);
		source.sendSuccess(new StringTextComponent(TextFormatting.GRAY + "Use @pl to refer to the player name in your command."), false);

		return 1;
	}
}
