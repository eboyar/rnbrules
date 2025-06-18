package ethan.hoenn.rnbrules.commands.gauntlet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.RNBConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;

public class RemoveGauntletNPC {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("removegauntletnpc")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("gauntletID", StringArgumentType.string()).then(
						Commands.argument("entity", EntityArgument.entity()).executes(context -> {
							String gauntletID = StringArgumentType.getString(context, "gauntletID");
							Entity entity = EntityArgument.getEntity(context, "entity");

							String entityName = entity.getName().getString();
							String entityUUID = entity.getUUID().toString();

							Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();

							if (gauntlets.containsKey(gauntletID)) {
								if (gauntlets.get(gauntletID).contains(entityUUID)) {
									RNBConfig.removeNPCFromGauntlet(entityUUID, gauntletID);
									context.getSource().sendSuccess(new StringTextComponent("NPC " + entityName + " removed from " + gauntletID), true);
								} else {
									context.getSource().sendFailure(new StringTextComponent("NPC " + entityName + " is not in " + gauntletID));
								}
							} else {
								context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " does not exist!"));
							}

							return 1;
						})
					)
				)
		);
	}
}
