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

public class ResetRoamerData {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("resetroamerdata")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).executes(context -> {
					ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
					return resetRoamerData(context.getSource(), player);
				})
			);

		dispatcher.register(command);
	}

	private static int resetRoamerData(CommandSource source, ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();
		EncounterManager encounterManager = EncounterManager.get(player.getLevel());

		encounterManager.getEncounteredRoamers().remove(playerUUID);
		encounterManager.getRoamerEncounterRoutes().remove(playerUUID);

		encounterManager.setDirty();

		source.sendSuccess(new StringTextComponent("Roamer data reset for player: " + player.getName().getString()), true);
		return Command.SINGLE_SUCCESS;
	}
}
