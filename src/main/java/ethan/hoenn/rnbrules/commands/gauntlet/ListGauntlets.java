package ethan.hoenn.rnbrules.commands.gauntlet;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.RNBConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class ListGauntlets {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("listgauntlets")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();

					if (gauntlets.isEmpty()) {
						context.getSource().sendSuccess(new StringTextComponent("No gauntlets found."), false);
						return 1;
					}

					StringTextComponent message = new StringTextComponent("Gauntlets:\n");
					for (Map.Entry<String, List<String>> entry : gauntlets.entrySet()) {
						String gauntletID = entry.getKey();
						List<String> npcs = entry.getValue();
						message.append("\n- " + gauntletID + " (" + npcs.size() + " NPCs)");
					}

					context.getSource().sendSuccess(message, false);
					return 1;
				})
		);
	}
}
