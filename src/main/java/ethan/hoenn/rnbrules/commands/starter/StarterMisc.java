package ethan.hoenn.rnbrules.commands.starter;

import static ethan.hoenn.rnbrules.utils.managers.StarterSelectionManager.StarterChoice;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.StarterSelectionManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class StarterMisc {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("resetstarter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("target", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
						StarterSelectionManager.get(target.getLevel()).removePlayer(target.getUUID());

						context.getSource().sendSuccess(new StringTextComponent("Reset starter for " + target.getName().getString()), true);
						return 1;
					})
				)
		);

		dispatcher.register(
			Commands.literal("checkstarter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("target", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
						StarterChoice choice = StarterSelectionManager.get(target.getLevel()).getPlayerSelection(target.getUUID());

						if (choice == null) {
							context.getSource().sendSuccess(new StringTextComponent(target.getName().getString() + " has no starter selected."), false);
						} else {
							context.getSource().sendSuccess(new StringTextComponent(target.getName().getString() + "'s starter is " + choice.name()), false);
						}
						return 1;
					})
				)
		);
	}
}
