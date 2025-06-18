package ethan.hoenn.rnbrules.commands.gamecorner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.RNBConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetGamecornerCompletionCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setgamecornercompletioncommand")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("command", StringArgumentType.greedyString()).executes(context -> setGamecornerCompletionCommand(context, StringArgumentType.getString(context, "command"))));

		dispatcher.register(command);
	}

	private static int setGamecornerCompletionCommand(CommandContext<CommandSource> context, String command) {
		CommandSource source = context.getSource();

		RNBConfig.setGamecornerCompletionCommand(command);

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Game Corner completion command set to: " + TextFormatting.YELLOW + command), true);

		return 1;
	}
}
