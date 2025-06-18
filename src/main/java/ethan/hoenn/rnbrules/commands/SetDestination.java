package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.enums.FerryDestination;
import ethan.hoenn.rnbrules.utils.enums.FlightDestination;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetDestination {

	private static final List<String> DESTINATION_TYPES = Arrays.asList("flight", "ferry");

	private static final SuggestionProvider<CommandSource> TYPE_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(DESTINATION_TYPES, builder);

	private static final SuggestionProvider<CommandSource> LOCATION_SUGGESTIONS = (context, builder) -> {
		String type = StringArgumentType.getString(context, "type").toLowerCase();

		if (type.equals("flight")) {
			return ISuggestionProvider.suggest(Arrays.stream(FlightDestination.values()).map(Enum::name).collect(Collectors.toList()), builder);
		} else if (type.equals("ferry")) {
			return ISuggestionProvider.suggest(Arrays.stream(FerryDestination.values()).map(Enum::name).collect(Collectors.toList()), builder);
		}

		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setdestination")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("type", StringArgumentType.word())
						.suggests(TYPE_SUGGESTIONS)
						.then(Commands.argument("location", StringArgumentType.word()).suggests(LOCATION_SUGGESTIONS).executes(SetDestination::executeSetDestination))
				)
		);
	}

	private static int executeSetDestination(CommandContext<CommandSource> context) {
		try {
			CommandSource source = context.getSource();
			ServerPlayerEntity player = source.getPlayerOrException();

			String type = StringArgumentType.getString(context, "type").toLowerCase();
			String locationName = StringArgumentType.getString(context, "location").toUpperCase();

			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();

			RNBConfig.TeleportLocation teleportLocation = new RNBConfig.TeleportLocation(x, y, z);

			if (type.equals("flight")) {
				try {
					FlightDestination destination = FlightDestination.valueOf(locationName);

					RNBConfig.setTownLocation(locationName, teleportLocation);

					source.sendSuccess(
						new StringTextComponent("Flight destination '" + destination.getDisplayName() + "' set to your current location: " + String.format("(%.2f, %.2f, %.2f)", x, y, z)).withStyle(
							TextFormatting.GREEN
						),
						true
					);
				} catch (IllegalArgumentException e) {
					source.sendFailure(new StringTextComponent("Invalid flight destination: " + locationName));
					return 0;
				}
			} else if (type.equals("ferry")) {
				try {
					FerryDestination destination = FerryDestination.valueOf(locationName);

					RNBConfig.setFerryLocation(locationName, teleportLocation);

					source.sendSuccess(
						new StringTextComponent("Ferry destination '" + destination.getDisplayName() + "' set to your current location: " + String.format("(%.2f, %.2f, %.2f)", x, y, z)).withStyle(
							TextFormatting.GREEN
						),
						true
					);
				} catch (IllegalArgumentException e) {
					source.sendFailure(new StringTextComponent("Invalid ferry destination: " + locationName));
					return 0;
				}
			} else {
				source.sendFailure(new StringTextComponent("Invalid destination type. Use 'flight' or 'ferry'."));
				return 0;
			}

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent("Error executing command: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}
}
