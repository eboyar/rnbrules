package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.data.BattleDependency;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetBattleDepDescription {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setbattledepdesc")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("id", StringArgumentType.word()).then(
					Commands.argument("description", StringArgumentType.greedyString()).executes(context ->
						setDescription(context, StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "description"))
					)
				)
			);

		dispatcher.register(command);
	}

	private static int setDescription(CommandContext<CommandSource> context, String id, String description) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();

		BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());

		BattleDependency dependency = manager.getDependency(id);
		if (dependency == null) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "No dependency with ID '" + id + "' exists!"));
			return 0;
		}

		dependency.setDescription(description);
		manager.setDirty();

		source.sendSuccess(
			new StringTextComponent(TextFormatting.GREEN + "Updated description of dependency '" + TextFormatting.GOLD + id + TextFormatting.GREEN + "' to: " + TextFormatting.WHITE + description),
			true
		);

		return 1;
	}
}
