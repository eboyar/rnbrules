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

public class AddGauntletNPC {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("addgauntletnpc")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("gauntletID", StringArgumentType.string()).then(
						Commands.argument("entity", EntityArgument.entity()).executes(context -> {
							String gauntletID = StringArgumentType.getString(context, "gauntletID");
							Entity entity = EntityArgument.getEntity(context, "entity");

							String entityName = entity.getName().getString();
							String entityUUID = entity.getUUID().toString();

							Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();

							if (!gauntlets.containsKey(gauntletID)) {
								context.getSource().sendFailure(new StringTextComponent("Gauntlet " + gauntletID + " does not exist!"));
								return 0;
							}

							List<String> npcList = gauntlets.get(gauntletID);
							if (npcList.contains(entityUUID)) {
								context.getSource().sendFailure(new StringTextComponent("NPC " + entityName + " is already in " + gauntletID + "!"));
								return 0;
							}

							RNBConfig.addNPCToGauntlet(entityUUID, gauntletID);
							context.getSource().sendSuccess(new StringTextComponent("NPC " + entityName + " added to " + gauntletID), true);

							return 1;
						})
					)
				)
		);
	}
}
