package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTutor;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ChangeTutorName {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("changemovetutorname")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("entity", EntityArgument.entity()).then(
						Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
							Entity entity = EntityArgument.getEntity(context, "entity");
							String newName = StringArgumentType.getString(context, "name");

							if (!(entity instanceof NPCTutor)) {
								context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Target must be a Move Tutor NPC"));
								return 0;
							}

							NPCTutor tutor = (NPCTutor) entity;
							String oldName = tutor.getName().getString();

							tutor.setCustomName(new StringTextComponent(newName));
							tutor.setName(newName);

							context
								.getSource()
								.sendSuccess(
									new StringTextComponent(
										TextFormatting.GREEN +
										"Changed move tutor name from '" +
										TextFormatting.YELLOW +
										oldName +
										TextFormatting.GREEN +
										"' to '" +
										TextFormatting.YELLOW +
										newName +
										TextFormatting.GREEN +
										"'"
									),
									true
								);

							return 1;
						})
					)
				)
		);
	}
}
