package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class LinkPairedTrainer {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("linkpairedtrainer")
				.requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("trainer1", EntityArgument.entity()).then(Commands.argument("trainer2", EntityArgument.entity()).executes(ctx -> linkPairedTrainers(ctx))))
		);
	}

	private static int linkPairedTrainers(CommandContext<CommandSource> context) {
		try {
			Entity entity1 = EntityArgument.getEntity(context, "trainer1");
			Entity entity2 = EntityArgument.getEntity(context, "trainer2");

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
			ServerWorld world = (ServerWorld) trainer1.level;

			CompoundNBT nbt1 = trainer1.getPersistentData();
			CompoundNBT nbt2 = trainer2.getPersistentData();

			if (nbt1.contains("Linked")) {
				UUID linkedUUID = nbt1.getUUID("Linked");
				Entity linkedEntity = world.getEntity(linkedUUID);

				if (linkedEntity instanceof NPCTrainer) {
					CompoundNBT linkedNBT = ((NPCTrainer) linkedEntity).getPersistentData();
					if (linkedNBT.contains("Linked")) {
						linkedNBT.remove("Linked");
						context
							.getSource()
							.sendSuccess(
								new StringTextComponent("Removed existing link from ")
									.append(new StringTextComponent(trainer1.getName("en_us")).withStyle(TextFormatting.YELLOW))
									.append(new StringTextComponent(" to "))
									.append(new StringTextComponent(((NPCTrainer) linkedEntity).getName("en_us")).withStyle(TextFormatting.YELLOW)),
								true
							);
					}
				}

				nbt1.remove("Linked");
			}

			if (nbt2.contains("Linked")) {
				UUID linkedUUID = nbt2.getUUID("Linked");
				Entity linkedEntity = world.getEntity(linkedUUID);

				if (linkedEntity instanceof NPCTrainer) {
					CompoundNBT linkedNBT = ((NPCTrainer) linkedEntity).getPersistentData();
					if (linkedNBT.contains("Linked")) {
						linkedNBT.remove("Linked");
						context
							.getSource()
							.sendSuccess(
								new StringTextComponent("Removed existing link from ")
									.append(new StringTextComponent(trainer2.getName("en_us")).withStyle(TextFormatting.YELLOW))
									.append(new StringTextComponent(" to "))
									.append(new StringTextComponent(((NPCTrainer) linkedEntity).getName("en_us")).withStyle(TextFormatting.YELLOW)),
								true
							);
					}
				}

				nbt2.remove("Linked");
			}

			nbt1.putUUID("Paired", trainer2.getUUID());
			nbt2.putUUID("Paired", trainer1.getUUID());

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent("Successfully paired trainers ")
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
