package ethan.hoenn.rnbrules.commands.encounters;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ethan.hoenn.rnbrules.utils.managers.EncounterManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class ToggleRoamers {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("toggleroamers")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).executes(context -> {
					ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
					return toggleRoamers(context.getSource(), player);
				})
			);

		dispatcher.register(command);
	}

	private static int toggleRoamers(CommandSource source, ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		EncounterManager encounterManager = EncounterManager.get(player.getLevel());

		boolean newState = encounterManager.toggleRoamerEncounters(playerUUID);

		String statusText = newState ? "enabled" : "disabled";
		source.sendSuccess(new StringTextComponent("Roamer encounters " + statusText + " for player: " + player.getName().getString()), true);

		return Command.SINGLE_SUCCESS;
	}
}
