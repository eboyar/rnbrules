package ethan.hoenn.rnbrules.commands.globalot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddGlobalOT {

	private static final SuggestionProvider<CommandSource> GLOBAL_OT_SUGGESTIONS = (context, builder) -> {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayerOrException();
			GlobalOTManager manager = GlobalOTManager.get(player.getLevel());
			Set<String> globalOTs = manager.listGlobalOTs();

			return ISuggestionProvider.suggest(globalOTs, builder);
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npcaddglobalot")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("otName", StringArgumentType.string())
					.suggests(GLOBAL_OT_SUGGESTIONS)
					.then(
						Commands.argument("entity", EntityArgument.entity()).executes(context ->
							addGlobalOTToNPC(context, StringArgumentType.getString(context, "otName"), EntityArgument.getEntity(context, "entity"))
						)
					)
			);

		dispatcher.register(command);
	}

	private static int addGlobalOTToNPC(CommandContext<CommandSource> context, String otName, Entity entity) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		GlobalOTManager manager = GlobalOTManager.get(player.getLevel());

		boolean added = manager.npcAddGlobalOT(entity, otName);

		if (added) {
			context.getSource().sendSuccess(new StringTextComponent("Added global OT '" + otName + "' to entity at " + entity.blockPosition()).withStyle(TextFormatting.GREEN), true);
		} else {
			context.getSource().sendFailure(new StringTextComponent("Failed to add global OT '" + otName + "' to entity. The OT might not exist."));
		}

		return added ? 1 : 0;
	}
}
