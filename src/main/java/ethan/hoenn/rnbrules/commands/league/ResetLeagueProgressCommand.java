package ethan.hoenn.rnbrules.commands.league;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class ResetLeagueProgressCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("resetleagueprogress")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.player()).executes(context -> resetLeagueProgress(context)));

		dispatcher.register(command);
	}

	private static int resetLeagueProgress(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
		ServerWorld world = source.getLevel();

		LeagueManager leagueManager = LeagueManager.get(world);
		leagueManager.resetPlayerLeagueProgress(targetPlayer.getUUID());

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Reset league progress for player " + TextFormatting.AQUA + targetPlayer.getName().getString()), true);

		return 1;
	}
}
