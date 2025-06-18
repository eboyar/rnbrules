package ethan.hoenn.rnbrules.commands.gauntlet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.RNBConfig;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class SetNextGauntlet {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setnextgauntlet")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("gauntletID", StringArgumentType.string())
						.then(
							Commands.argument("nextGauntletID", StringArgumentType.string()).executes(context -> {
								String gauntletID = StringArgumentType.getString(context, "gauntletID");
								String nextGauntletID = StringArgumentType.getString(context, "nextGauntletID");

								Map<String, ?> gauntlets = RNBConfig.getGauntlets();

								if (!gauntlets.containsKey(gauntletID)) {
									context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " does not exist!"));
									return 0;
								}

								if (!gauntlets.containsKey(nextGauntletID)) {
									context.getSource().sendFailure(new StringTextComponent("Next gauntlet " + nextGauntletID + " does not exist!"));
									return 0;
								}

								RNBConfig.setNextGauntlet(gauntletID, nextGauntletID);
								context.getSource().sendSuccess(new StringTextComponent("Gauntlet " + gauntletID + " will now automatically proceed to " + nextGauntletID + " when completed."), true);

								return 1;
							})
						)
						.executes(context -> {
							// Remove next gauntlet when no second argument is provided
							String gauntletID = StringArgumentType.getString(context, "gauntletID");

							Map<String, ?> gauntlets = RNBConfig.getGauntlets();

							if (!gauntlets.containsKey(gauntletID)) {
								context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " does not exist!"));
								return 0;
							}

							RNBConfig.setNextGauntlet(gauntletID, "");
							context.getSource().sendSuccess(new StringTextComponent("Removed automatic next gauntlet for " + gauntletID + "."), true);

							return 1;
						})
				)
		);
	}
}
