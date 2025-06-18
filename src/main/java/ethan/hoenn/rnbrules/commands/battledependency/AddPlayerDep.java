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

public class AddPlayerDep {

	private static final SuggestionProvider<CommandSource> DEPENDENCY_SUGGESTIONS = (context, builder) -> {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayerOrException();
			BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());
			Set<String> dependencies = manager.getAllDependencyIds();
			return ISuggestionProvider.suggest(dependencies, builder);
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("addplayerdep")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).then(
					Commands.argument("dependency", StringArgumentType.word())
						.suggests(DEPENDENCY_SUGGESTIONS)
						.executes(context -> addDependencyToPlayer(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "dependency")))
				)
			);

		dispatcher.register(command);
	}

	private static int addDependencyToPlayer(CommandContext<CommandSource> context, ServerPlayerEntity targetPlayer, String dependency) {
		CommandSource source = context.getSource();

		BattleDependencyManager manager = BattleDependencyManager.get(targetPlayer.getLevel());

		if (!manager.dependencyExists(dependency)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Dependency '" + dependency + "' doesn't exist!"));
			return 0;
		}

		if (manager.playerHasDependency(targetPlayer.getUUID(), dependency)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Player " + targetPlayer.getName().getString() + " already has the dependency '" + dependency + "'!"));
			return 0;
		}

		if (manager.addPlayerDependency(targetPlayer.getUUID(), dependency)) {
			String playerName = targetPlayer.getName().getString();
			String depDescription = manager.getDependency(dependency).getDescription();

			source.sendSuccess(
				new StringTextComponent(
					TextFormatting.GREEN +
					"Added dependency '" +
					TextFormatting.GOLD +
					dependency +
					TextFormatting.GREEN +
					"' to player " +
					TextFormatting.AQUA +
					playerName +
					TextFormatting.GREEN +
					".\nDescription: " +
					TextFormatting.WHITE +
					depDescription
				),
				true
			);

			return 1;
		} else {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Failed to add dependency to player!"));
			return 0;
		}
	}
}
