package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class LinkNPCTrainer {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("linknpctrainer")
				.requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("enemyTrainer1", EntityArgument.entity()).then(Commands.argument("enemyTrainer2", EntityArgument.entity()).executes(ctx -> linkTrainers(ctx))))
		);
	}

	private static int linkTrainers(CommandContext<CommandSource> context) {
		try {
			Entity entity1 = EntityArgument.getEntity(context, "enemyTrainer1");
			Entity entity2 = EntityArgument.getEntity(context, "enemyTrainer2");

			if (!(entity1 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("First entity must be an NPCTrainer!"));
				return 0;
			}

			if (!(entity2 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("Second entity must be an NPCTrainer!"));
				return 0;
			}

			NPCTrainer trainer1 = (NPCTrainer) entity1;
			NPCTrainer trainer2 = (NPCTrainer) entity2;

			CompoundNBT nbt1 = trainer1.getPersistentData();
			CompoundNBT nbt2 = trainer2.getPersistentData();

			nbt1.putUUID("Linked", trainer2.getUUID());
			nbt2.putUUID("Linked", trainer1.getUUID());

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent("Successfully linked trainers ")
						.append(new StringTextComponent(trainer1.getName("en_us")).withStyle(TextFormatting.YELLOW))
						.append(new StringTextComponent(" and "))
						.append(new StringTextComponent(trainer2.getName("en_us")).withStyle(TextFormatting.YELLOW)),
					true
				);

			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent("Error: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}
}
