package ethan.hoenn.rnbrules.commands.itemupgrade;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.gui.itemupgrade.ItemUpgradeGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class ItemUpgrade {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("itemupgrade")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					try {
						ServerPlayerEntity player = context.getSource().getPlayerOrException();
						ItemUpgradeGui.openGui(player);
						return 1;
					} catch (Exception e) {
						context.getSource().sendFailure(new StringTextComponent("Failed to open Item Upgrade GUI: " + e.getMessage()));
						return 0;
					}
				})
		);
	}
}
