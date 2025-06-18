package ethan.hoenn.rnbrules.commands.globalot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import java.util.Set;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ListPlayerGlobalOTs {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("listplayerglobalots")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("player", EntityArgument.player()).executes(context -> listPlayerGlobalOTs(context, EntityArgument.getPlayer(context, "player"))));

		dispatcher.register(command);
	}

	private static int listPlayerGlobalOTs(CommandContext<CommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
		ServerPlayerEntity source = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(source.getLevel());
		UUID playerUUID = player.getUUID();

		Set<String> playerOTs = manager.listPlayerGlobalOTs(playerUUID);

		if (playerOTs.isEmpty()) {
			context.getSource().sendSuccess(new StringTextComponent("Player " + player.getName().getString() + " has no global OTs."), false);
		} else {
			context.getSource().sendSuccess(new StringTextComponent("Global OTs for player " + player.getName().getString() + ":").withStyle(TextFormatting.GOLD), false);

			for (String ot : playerOTs) {
				context.getSource().sendSuccess(new StringTextComponent("- " + ot).withStyle(TextFormatting.YELLOW), false);
			}
		}

		return playerOTs.size();
	}
}
