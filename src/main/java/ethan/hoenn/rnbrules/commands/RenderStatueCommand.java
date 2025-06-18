package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import ethan.hoenn.rnbrules.utils.managers.StatueVisibilityManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class RenderStatueCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("renderstatue")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("statueUUID", UUIDArgument.uuid()).then(
						Commands.argument("player", EntityArgument.player()).then(
							Commands.argument("visible", BoolArgumentType.bool()).executes(context ->
								executeToggleVisibility(context.getSource(), UUIDArgument.getUuid(context, "statueUUID"), EntityArgument.getPlayer(context, "player"), BoolArgumentType.getBool(context, "visible"))
							)
						)
					)
				)
		);
	}

	private static int executeToggleVisibility(CommandSource source, UUID statueUUID, ServerPlayerEntity player, boolean visible) throws CommandSyntaxException {
		boolean statueExists = false;
		for (ServerWorld world : source.getServer().getAllLevels()) {
			for (Entity entity : world.getAllEntities()) {
				if (entity instanceof StatueEntity && entity.getUUID().equals(statueUUID)) {
					statueExists = true;
					break;
				}
			}
			if (statueExists) break;
		}

		if (!statueExists) {
			throw new SimpleCommandExceptionType(new StringTextComponent("Statue with that UUID was not found")).create();
		}

		if (visible) {
			StatueVisibilityManager.get(player.getLevel()).showStatueForPlayer(statueUUID, player);
			source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Statue is now visible for " + player.getName().getString()), true);
		} else {
			StatueVisibilityManager.get(player.getLevel()).hideStatueForPlayer(statueUUID, player);
			source.sendSuccess(new StringTextComponent(TextFormatting.RED + "Statue is now hidden for " + player.getName().getString()), true);
		}

		return 1;
	}
}
