package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.data.BattleDependency;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ListBattleDeps {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("listbattledeps").requires(source -> source.hasPermission(2)).executes(ListBattleDeps::listDependencies);

		dispatcher.register(command);
	}

	private static int listDependencies(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();
		ServerPlayerEntity player;

		try {
			player = source.getPlayerOrException();
		} catch (CommandSyntaxException e) {
			source.sendFailure(new StringTextComponent("This command must be executed by a player."));
			return 0;
		}

		BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());
		Set<String> dependencyIds = manager.getAllDependencyIds();

		if (dependencyIds.isEmpty()) {
			source.sendSuccess(new StringTextComponent(TextFormatting.GRAY + "There are no battle dependencies registered."), false);
			return 0;
		}

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "==== Battle Dependencies ===="), false);

		for (String id : dependencyIds) {
			BattleDependency dep = manager.getDependency(id);
			source.sendSuccess(new StringTextComponent(TextFormatting.GOLD + id + TextFormatting.WHITE + ": " + TextFormatting.AQUA + dep.getDescription()), false);
		}

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "=========================="), false);

		return dependencyIds.size();
	}
}
