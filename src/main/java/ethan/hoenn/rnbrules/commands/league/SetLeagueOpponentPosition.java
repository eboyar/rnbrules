package ethan.hoenn.rnbrules.commands.league;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.RNBConfig.TeleportLocation;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetLeagueOpponentPosition {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setarenaopponentposition")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("arena", IntegerArgumentType.integer(1, 5)).executes(context -> setArenaOpponentPosition(context)));

		dispatcher.register(command);
	}

	private static int setArenaOpponentPosition(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();
		int arenaNumber = IntegerArgumentType.getInteger(context, "arena");

		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();

		TeleportLocation location = new TeleportLocation(x, y, z);
		RNBConfig.setLeagueOpponentPosition(arenaNumber, location);

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Arena " + arenaNumber + " opponent position set to your current location."), true);
		source.sendSuccess(new StringTextComponent(TextFormatting.GRAY + "Coordinates: " + String.format("%.2f, %.2f, %.2f", x, y, z)), false);

		return 1;
	}
}
