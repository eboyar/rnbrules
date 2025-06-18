package ethan.hoenn.rnbrules.commands.heartscale;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.gui.heartscale.HeartscaleExchangeGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class HeartscaleExchange {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("heartscaleexchange")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrException();
					return executeCommand(context.getSource(), player);
				})
		);
	}

	private static int executeCommand(CommandSource source, ServerPlayerEntity player) {
		try {
			openHeartscaleExchangeGui(player);
			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("Error: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}

	private static void openHeartscaleExchangeGui(ServerPlayerEntity player) {
		HeartscaleExchangeGui.openGui(player);
	}
}
