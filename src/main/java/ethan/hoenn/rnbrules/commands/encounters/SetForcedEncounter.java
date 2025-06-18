package ethan.hoenn.rnbrules.commands.encounters;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

public class SetForcedEncounter {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setforcedencounter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("entity", EntityArgument.entity()).then(
						Commands.argument("encounterID", StringArgumentType.string()).executes(context -> {
							Entity target = EntityArgument.getEntity(context, "entity");
							String encounterID = StringArgumentType.getString(context, "encounterID");

							CompoundNBT persistentData = target.getPersistentData();
							persistentData.putString("ForcedEncounter", encounterID);

							context.getSource().sendSuccess(new StringTextComponent("Set ForcedEncounter to '" + encounterID + "' on entity: " + target.getName().getString()), true);

							return 1;
						})
					)
				)
		);
	}
}
