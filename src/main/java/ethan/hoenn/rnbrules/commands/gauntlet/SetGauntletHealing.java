package ethan.hoenn.rnbrules.commands.gauntlet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class SetGauntletHealing {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setgaunthealing")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("gauntletID", StringArgumentType.string()).then(
						Commands.argument("allowHealing", BoolArgumentType.bool()).executes(context -> {
							String gauntletID = StringArgumentType.getString(context, "gauntletID");
							boolean allowHealing = BoolArgumentType.getBool(context, "allowHealing");

							Map<String, ?> gauntlets = RNBConfig.getGauntlets();

							if (!gauntlets.containsKey(gauntletID)) {
								context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " does not exist!"));
								return 0;
							}

							GauntletManager gauntletManager = GauntletManager.get(context.getSource().getLevel());
							gauntletManager.setHealingAllowed(gauntletID, allowHealing);

							String healingStatus = allowHealing ? "enabled" : "disabled";
							context.getSource().sendSuccess(new StringTextComponent("Healing for gauntlet " + gauntletID + " " + healingStatus + "."), true);

							return 1;
						})
					)
				)
		);
	}
}
