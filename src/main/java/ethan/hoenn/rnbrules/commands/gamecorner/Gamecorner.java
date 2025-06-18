package ethan.hoenn.rnbrules.commands.gamecorner;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class Gamecorner {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("gamecorner")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					try {
						ServerPlayerEntity player = context.getSource().getPlayerOrException();
						GamecornerGui.openGui(player);
						return 1;
					} catch (Exception e) {
						context.getSource().sendFailure(new StringTextComponent("Failed to open Game Corner GUI: " + e.getMessage()));
						return 0;
					}
				})
		);
	}
}
