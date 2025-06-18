package ethan.hoenn.rnbrules.commands.environment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

public class SetEnvironment {

	private static final SuggestionProvider<CommandSource> ENVIRONMENT_SUGGESTIONS = (context, builder) -> {
		for (Environment env : Environment.values()) {
			builder.suggest(env.getId());
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setenvironment")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("environment", StringArgumentType.string())
						.suggests(ENVIRONMENT_SUGGESTIONS)
						.then(
							Commands.argument("entity", EntityArgument.entity()).executes(context -> {
								String environmentId = StringArgumentType.getString(context, "environment");
								Entity entity = EntityArgument.getEntity(context, "entity");

								Environment environment = Environment.fromString(environmentId);
								if (environment == null) {
									context.getSource().sendFailure(new StringTextComponent("Invalid environment: " + environmentId));
									return 0;
								}

								CompoundNBT entityData = entity.getPersistentData();

								if (environment == Environment.NONE) {
									entityData.remove("Environment");
									context.getSource().sendSuccess(new StringTextComponent("Removed Environment tag from " + entity.getName().getString()), true);
								} else {
									entityData.putString("Environment", environment.getId());
									context.getSource().sendSuccess(new StringTextComponent("Set Environment to " + environment.getId() + " for " + entity.getName().getString()), true);
								}

								return 1;
							})
						)
				)
		);
	}
}
