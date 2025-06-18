package ethan.hoenn.rnbrules.commands.flight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.enums.FlightDestination;
import ethan.hoenn.rnbrules.utils.managers.FlyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class RemoveFlight {

	private static final SuggestionProvider<CommandSource> PLAYER_DESTINATIONS = (context, builder) -> {
		try {
			builder.suggest("ALL");
			ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
			FlyManager manager = FlyManager.get((ServerWorld) player.level);
			Set<String> destinations = manager.getPlayerDestinations(player.getUUID());

			return ISuggestionProvider.suggest(destinations, builder);
		} catch (Exception e) {
			builder.suggest("ALL");
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("removeflight")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("destination", StringArgumentType.word())
							.suggests(PLAYER_DESTINATIONS)
							.executes(context -> {
								try {
									ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");

									String destinationName = StringArgumentType.getString(context, "destination").toUpperCase();

									if (destinationName.equals("ALL")) {
										FlyManager manager = FlyManager.get((ServerWorld) player.level);

										manager.resetDestinations(player.getUUID());

										context.getSource().sendSuccess(new StringTextComponent("Removed ALL flight destinations from " + player.getDisplayName().getString()).withStyle(TextFormatting.GREEN), true);
										return 1;
									}

									try {
										FlightDestination destination = FlightDestination.valueOf(destinationName);

										FlyManager manager = FlyManager.get((ServerWorld) player.level);

										if (!manager.hasDestination(player.getUUID(), destination.name())) {
											context.getSource().sendFailure(new StringTextComponent(player.getDisplayName().getString() + " doesn't have access to " + destination.getDisplayName()));
											return 0;
										}

										manager.removeDestination(player.getUUID(), destination.name());

										context
											.getSource()
											.sendSuccess(
												new StringTextComponent("Removed flight destination " + destination.getDisplayName() + " from " + player.getDisplayName().getString()).withStyle(TextFormatting.GREEN),
												true
											);

										return 1;
									} catch (IllegalArgumentException e) {
										context.getSource().sendFailure(new StringTextComponent("Invalid destination: " + destinationName));
										return 0;
									}
								} catch (CommandSyntaxException e) {
									context.getSource().sendFailure(new StringTextComponent("Error executing command: " + e.getMessage()));
									return 0;
								}
							})
					)
				)
		);
	}
}
