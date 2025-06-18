package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class LocationInfoCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("locationinfo")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.player()).executes(context -> getLocationInfo(context, EntityArgument.getPlayer(context, "player"))))
			.executes(context -> getLocationInfo(context, context.getSource().getPlayerOrException()));

		dispatcher.register(command);
	}

	private static int getLocationInfo(CommandContext<CommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
		ServerWorld world = (ServerWorld) player.level;
		LocationManager locationManager = LocationManager.get(world);
		UUID playerUUID = player.getUUID();

		String currentLocation = locationManager.getPlayerCurrentLocation(playerUUID);
		String lastLocation = locationManager.getPlayerLastLocation(playerUUID);
		String[] movementDirection = locationManager.getPlayerMovementDirection(playerUUID);

		if (currentLocation == null) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Player " + player.getName().getString() + " has no recorded location."));
			return 0;
		}

		context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GOLD + "Location info for " + player.getName().getString() + ":"), false);

		context.getSource().sendSuccess(new StringTextComponent(TextFormatting.YELLOW + "Current location: " + TextFormatting.GREEN + currentLocation), false);

		if (lastLocation != null) {
			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.YELLOW + "Last location: " + TextFormatting.GREEN + lastLocation), false);
		}

		if (movementDirection != null) {
			context
				.getSource()
				.sendSuccess(
					new StringTextComponent(
						TextFormatting.YELLOW + "Last movement: " + TextFormatting.GREEN + movementDirection[0] + TextFormatting.YELLOW + " → " + TextFormatting.GREEN + movementDirection[1]
					),
					false
				);
		}

		return 1;
	}
}
