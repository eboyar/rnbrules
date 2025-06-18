package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.gui.league.LeagueGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class LeagueCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("league").requires(source -> source.hasPermission(2)).executes(LeagueCommand::executeLeagueCommand));
	}

	private static int executeLeagueCommand(CommandContext<CommandSource> context) {
		if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
			LeagueGui.openGui(player);
			return 1;
		} else {
			context.getSource().sendFailure(new StringTextComponent("This command can only be used by players").withStyle(TextFormatting.RED));
			return 0;
		}
	}
}
