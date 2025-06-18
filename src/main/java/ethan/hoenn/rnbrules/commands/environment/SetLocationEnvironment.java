package ethan.hoenn.rnbrules.commands.environment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.environment.server.ServerEnvironmentController;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class SetLocationEnvironment {

	private static final SuggestionProvider<CommandSource> ENVIRONMENT_SUGGESTIONS = (context, builder) -> {
		for (Environment env : Environment.values()) {
			builder.suggest(env.getId());
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setlocationenvironment")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("location", StringArgumentType.string()).then(
						Commands.argument("environment", StringArgumentType.string())
							.suggests(ENVIRONMENT_SUGGESTIONS)
							.executes(context -> {
								String location = StringArgumentType.getString(context, "location");
								String environmentId = StringArgumentType.getString(context, "environment");

								Environment environment = Environment.fromString(environmentId);
								if (environment == null) {
									context.getSource().sendFailure(new StringTextComponent("Invalid environment: " + environmentId));
									return 0;
								}

								String normalizedLocation = LocationManager.normalizeLocationName(location);

								boolean success = LocationManager.get(context.getSource().getLevel()).setLocationEnvironment(normalizedLocation, environment);

								String message = "";
								if (environment == Environment.NONE && success) {
									message = "Removed environment from location: " + location;
								} else if (success) {
									message = "Set environment to " + environment.getId() + " for location: " + location;
								}

								context.getSource().sendSuccess(new StringTextComponent(message), true);
								return 1;
							})
					)
				)
		);
	}
}
