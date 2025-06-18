package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AddBattleDep {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("addbattledep")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("id", StringArgumentType.word()).then(
					Commands.argument("description", StringArgumentType.greedyString()).executes(context ->
						addDependency(context, StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "description"))
					)
				)
			);

		dispatcher.register(command);
	}

	private static int addDependency(CommandContext<CommandSource> context, String id, String description) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();

		BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());

		if (manager.dependencyExists(id)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "A dependency with ID '" + id + "' already exists!"));
			return 0;
		}

		if (manager.addDependency(id, description)) {
			source.sendSuccess(
				new StringTextComponent(
					TextFormatting.GREEN + "Successfully added dependency: " + TextFormatting.GOLD + id + TextFormatting.GREEN + " with description: " + TextFormatting.WHITE + description
				),
				true
			);
			return 1;
		} else {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Failed to add dependency!"));
			return 0;
		}
	}
}
