package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetNPCTradeCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npctradecommand")
			.requires(source -> source.hasPermission(2)) // Requires permission level 2 (ops)
			.then(
				Commands.argument("entity", EntityArgument.entity()).then(
					Commands.argument("command", StringArgumentType.greedyString()).executes(context ->
						setTradeCommand(context, EntityArgument.getEntity(context, "entity"), StringArgumentType.getString(context, "command"))
					)
				)
			);

		dispatcher.register(command);
	}

	private static int setTradeCommand(CommandContext<CommandSource> context, Entity entity, String command) {
		CommandSource source = context.getSource();

		if (!(entity instanceof NPCTrader)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "The selected entity must be an NPC Trader!"));
			return 0;
		}

		NPCTrader trader = (NPCTrader) entity;
		trader.getPersistentData().putString("TradeCommand", command);

		source.sendSuccess(
			new StringTextComponent(TextFormatting.GREEN + "Set trade command for " + TextFormatting.AQUA + trader.getName().getString() + TextFormatting.GREEN + " to: " + TextFormatting.WHITE + command),
			true
		);

		return 1;
	}
}
