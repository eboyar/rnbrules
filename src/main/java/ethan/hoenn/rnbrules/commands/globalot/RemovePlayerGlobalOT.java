package ethan.hoenn.rnbrules.commands.globalot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import java.util.Set;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class RemovePlayerGlobalOT {

	private static final SuggestionProvider<CommandSource> PLAYER_OT_SUGGESTIONS = (context, builder) -> {
		try {
			ServerPlayerEntity player = context.getSource().getPlayerOrException();
			ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
			GlobalOTManager manager = GlobalOTManager.get(player.getLevel());

			Set<String> playerOTs = manager.listPlayerGlobalOTs(targetPlayer.getUUID());

			return ISuggestionProvider.suggest(playerOTs, builder);
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("removeplayerglobalot")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).then(
					Commands.argument("otName", StringArgumentType.string())
						.suggests(PLAYER_OT_SUGGESTIONS)
						.executes(context -> removePlayerGlobalOT(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "otName")))
				)
			);

		dispatcher.register(command);
	}

	private static int removePlayerGlobalOT(CommandContext<CommandSource> context, ServerPlayerEntity player, String otName) throws CommandSyntaxException {
		ServerPlayerEntity source = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(source.getLevel());
		UUID playerUUID = player.getUUID();

		boolean removed = manager.removePlayerGlobalOT(playerUUID, otName);

		if (removed) {
			context.getSource().sendSuccess(new StringTextComponent("Removed global OT '" + otName + "' from player " + player.getName().getString()).withStyle(TextFormatting.GREEN), true);
		} else {
			context.getSource().sendFailure(new StringTextComponent("Failed to remove global OT '" + otName + "' from player " + player.getName().getString() + ". Player doesn't have this OT."));
		}

		return removed ? 1 : 0;
	}
}
