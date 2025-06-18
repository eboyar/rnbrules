package ethan.hoenn.rnbrules.commands.environment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.environment.server.ServerEnvironmentController;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class SetPlayerEnvironment {

	private static final SuggestionProvider<CommandSource> ENVIRONMENT_SUGGESTIONS = (context, builder) -> {
		for (Environment env : Environment.values()) {
			builder.suggest(env.getId());
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setplayerenvironment")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("environment", StringArgumentType.string())
							.suggests(ENVIRONMENT_SUGGESTIONS)
							.executes(context -> {
								ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
								String environmentId = StringArgumentType.getString(context, "environment");

								Environment environment = Environment.fromString(environmentId);
								if (environment == null) {
									context.getSource().sendFailure(new StringTextComponent("Invalid environment: " + environmentId));
									return 0;
								}

								applyEnvironmentToPlayer(player, environment);

								String message;
								if (environment == Environment.NONE) {
									message = "Removed environment from " + player.getName().getString();
								} else {
									message = "Set environment to " + environment.getId() + " for " + player.getName().getString();
								}

								context.getSource().sendSuccess(new StringTextComponent(message), true);
								return 1;
							})
					)
				)
		);
	}

	private static void applyEnvironmentToPlayer(ServerPlayerEntity player, Environment environment) {
		ServerEnvironmentController controller = ServerEnvironmentController.getInstance();
		controller.setPlayerEnvironment(player, environment);
	}
}
