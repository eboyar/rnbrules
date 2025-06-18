package ethan.hoenn.rnbrules.commands.HM;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.HiddenMachineManager;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class ListHM {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("hms").executes(context -> {
				ServerPlayerEntity player = context.getSource().getPlayerOrException();
				return listPlayerHMs(context.getSource(), player, player);
			})
		);

		dispatcher.register(
			Commands.literal("listhms")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
						return listPlayerHMs(context.getSource(), context.getSource().getPlayerOrException(), targetPlayer);
					})
				)
		);
	}

	private static int listPlayerHMs(CommandSource source, ServerPlayerEntity requestingPlayer, ServerPlayerEntity targetPlayer) {
		HiddenMachineManager hmManager = HiddenMachineManager.get((ServerWorld) targetPlayer.level);
		UUID playerUUID = targetPlayer.getUUID();
		Set<String> hms = hmManager.getPlayerHMs(playerUUID);

		if (hms.isEmpty()) {
			source.sendSuccess(
				new StringTextComponent(requestingPlayer == targetPlayer ? "You don't have any HMs." : targetPlayer.getDisplayName().getString() + " doesn't have any HMs.").withStyle(TextFormatting.GRAY),
				false
			);
		} else {
			source.sendSuccess(
				new StringTextComponent(requestingPlayer == targetPlayer ? "Your HMs (" + hms.size() + "): " : targetPlayer.getDisplayName().getString() + "'s HMs (" + hms.size() + "): ").withStyle(
					TextFormatting.GREEN
				),
				false
			);

			for (String hm : hms) {
				source.sendSuccess(new StringTextComponent(" - " + formatHMName(hm)).withStyle(TextFormatting.AQUA), false);
			}
		}
		return hms.size();
	}

	private static String formatHMName(String hmName) {
		String[] words = hmName.split("_");
		return java.util.Arrays.stream(words).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
	}
}
