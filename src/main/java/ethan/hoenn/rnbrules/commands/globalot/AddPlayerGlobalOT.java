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

public class AddPlayerGlobalOT {

	private static final SuggestionProvider<CommandSource> GLOBAL_OT_SUGGESTIONS = (context, builder) -> {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayerOrException();
			GlobalOTManager manager = GlobalOTManager.get(player.getLevel());
			Set<String> globalOTs = manager.listGlobalOTs();

			ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
			Set<String> playerOTs = manager.listPlayerGlobalOTs(targetPlayer.getUUID());

			globalOTs.removeAll(playerOTs);

			return ISuggestionProvider.suggest(globalOTs, builder);
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("addplayerglobalot")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("player", EntityArgument.player()).then(
					Commands.argument("otName", StringArgumentType.string())
						.suggests(GLOBAL_OT_SUGGESTIONS)
						.executes(context -> addPlayerGlobalOT(context, EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "otName")))
				)
			);

		dispatcher.register(command);
	}

	private static int addPlayerGlobalOT(CommandContext<CommandSource> context, ServerPlayerEntity player, String otName) throws CommandSyntaxException {
		ServerPlayerEntity source = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(source.getLevel());
		UUID playerUUID = player.getUUID();

		boolean added = manager.addPlayerGlobalOT(playerUUID, otName);

		if (added) {
			context.getSource().sendSuccess(new StringTextComponent("Added global OT '" + otName + "' to player " + player.getName().getString()).withStyle(TextFormatting.GREEN), true);
		} else {
			context
				.getSource()
				.sendFailure(new StringTextComponent("Failed to add global OT '" + otName + "' to player " + player.getName().getString() + ". Either the OT doesn't exist or the player already has it."));
		}

		return added ? 1 : 0;
	}
}
