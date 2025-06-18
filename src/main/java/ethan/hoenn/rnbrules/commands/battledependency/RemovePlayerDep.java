package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class RemovePlayerDep {

	private static final SuggestionProvider<CommandSource> PLAYER_DEPENDENCY_SUGGESTIONS = (context, builder) -> {
		try {
			ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
			BattleDependencyManager manager = BattleDependencyManager.get(targetPlayer.getLevel());
			Set<String> dependencies = manager.getPlayerDependencies(targetPlayer.getUUID());
			return ISuggestionProvider.suggest(dependencies, builder);
		} catch (Exception e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("removeplayerdep")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).then(
					Commands.argument("dependency", StringArgumentType.word())
						.suggests(PLAYER_DEPENDENCY_SUGGESTIONS)
						.executes(context -> removeDependencyFromPlayer(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "dependency")))
				)
			);

		dispatcher.register(command);
	}

	private static int removeDependencyFromPlayer(CommandContext<CommandSource> context, ServerPlayerEntity targetPlayer, String dependency) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity executor = source.getPlayerOrException();

		BattleDependencyManager manager = BattleDependencyManager.get(executor.getLevel());

		if (!manager.playerHasDependency(targetPlayer.getUUID(), dependency)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Player " + targetPlayer.getName().getString() + " doesn't have the dependency '" + dependency + "'!"));
			return 0;
		}

		if (manager.removePlayerDependency(targetPlayer.getUUID(), dependency)) {
			String playerName = targetPlayer.getName().getString();
			String depDescription = manager.getDependency(dependency) != null ? manager.getDependency(dependency).getDescription() : "Unknown";

			source.sendSuccess(
				new StringTextComponent(TextFormatting.GREEN + "Removed dependency '" + TextFormatting.GOLD + dependency + TextFormatting.GREEN + "' from player " + TextFormatting.AQUA + playerName),
				true
			);

			return 1;
		} else {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Failed to remove dependency from player!"));
			return 0;
		}
	}
}
