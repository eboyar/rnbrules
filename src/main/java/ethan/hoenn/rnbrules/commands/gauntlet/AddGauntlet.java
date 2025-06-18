package ethan.hoenn.rnbrules.commands.gauntlet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.RNBConfig;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class AddGauntlet {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("addgauntlet")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("gauntletID", StringArgumentType.string()).executes(context -> {
						String gauntletID = StringArgumentType.getString(context, "gauntletID");

						Map<String, ?> gauntlets = RNBConfig.getGauntlets();

						if (gauntlets.containsKey(gauntletID)) {
							context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " already exists!"));
							return 0;
						}

						RNBConfig.addNewGauntlet(gauntletID);
						context.getSource().sendSuccess(new StringTextComponent("Gauntlet " + gauntletID + " created successfully!"), true);

						return 1;
					})
				)
		);
	}
}
