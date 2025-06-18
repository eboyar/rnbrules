package ethan.hoenn.rnbrules.commands.flight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.enums.FlightDestination;
import ethan.hoenn.rnbrules.utils.managers.FlyManager;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class AddFlight {

	private static final SuggestionProvider<CommandSource> DESTINATION_SUGGESTIONS = (context, builder) -> {
		builder.suggest("ALL");
		ISuggestionProvider.suggest(Arrays.stream(FlightDestination.values()).map(Enum::name).collect(Collectors.toList()), builder);
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("addflight")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("destination", StringArgumentType.word())
							.suggests(DESTINATION_SUGGESTIONS)
							.executes(context -> {
								try {
									ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
									String destinationName = StringArgumentType.getString(context, "destination").toUpperCase();
									return addFlightDestination(context.getSource(), player, destinationName);
								} catch (CommandSyntaxException e) {
									context.getSource().sendFailure(new StringTextComponent("Error executing command: " + e.getMessage()));
									return 0;
								}
							})
					)
				)
		);
	}

	private static int addFlightDestination(CommandSource source, ServerPlayerEntity player, String destinationName) {
		if (destinationName.equals("ALL")) {
			FlyManager manager = FlyManager.get((ServerWorld) player.level);
			int addedCount = 0;

			for (FlightDestination dest : FlightDestination.values()) {
				if (!manager.hasDestination(player.getUUID(), dest.name())) {
					manager.addDestination(player.getUUID(), dest.name());
					addedCount++;
				}
			}

			source.sendSuccess(
				new StringTextComponent("Added ALL flight destinations for " + player.getDisplayName().getString() + " (" + addedCount + " new destinations)").withStyle(TextFormatting.GREEN),
				true
			);
			return 1;
		}

		try {
			FlightDestination destination = FlightDestination.valueOf(destinationName);
			FlyManager manager = FlyManager.get((ServerWorld) player.level);

			if (manager.hasDestination(player.getUUID(), destination.name())) {
				source.sendFailure(new StringTextComponent(player.getDisplayName().getString() + " already has access to " + destination.getDisplayName()));
				return 0;
			}

			manager.addDestination(player.getUUID(), destination.name());

			source.sendSuccess(new StringTextComponent("Added flight destination " + destination.getDisplayName() + " for " + player.getDisplayName().getString()).withStyle(TextFormatting.GREEN), true);

			return 1;
		} catch (IllegalArgumentException e) {
			source.sendFailure(new StringTextComponent("Invalid destination: " + destinationName));
			return 0;
		}
	}
}
