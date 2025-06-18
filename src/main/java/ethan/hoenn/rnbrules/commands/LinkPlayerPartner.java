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

public class LinkPlayerPartner {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("linkplayerpartner")
				.requires(cs -> cs.hasPermission(2))
				.then(Commands.argument("enemyTrainer", EntityArgument.entity()).then(Commands.argument("playerPartnerTrainer", EntityArgument.entity()).executes(ctx -> linkSingleEnemyPlayerPartner(ctx))))
				.then(
					Commands.argument("enemyTrainer1", EntityArgument.entity()).then(
						Commands.argument("enemyTrainer2", EntityArgument.entity()).then(Commands.argument("playerPartnerTrainer", EntityArgument.entity()).executes(ctx -> linkTwoEnemiesPlayerPartner(ctx)))
					)
				)
		);
	}

	private static int linkSingleEnemyPlayerPartner(CommandContext<CommandSource> context) {
		try {
			Entity entity1 = EntityArgument.getEntity(context, "enemyTrainer");
			Entity entity2 = EntityArgument.getEntity(context, "playerPartnerTrainer");

			if (!(entity1 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("Enemy NPC must be an NPCTrainer!"));
				return 0;
			}

			if (!(entity2 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("Player partner NPC must be an NPCTrainer!"));
				return 0;
			}

			NPCTrainer enemyTrainer = (NPCTrainer) entity1;
			NPCTrainer partnerTrainer = (NPCTrainer) entity2;

			CompoundNBT nbt1 = enemyTrainer.getPersistentData();
			CompoundNBT nbtPartner = partnerTrainer.getPersistentData();

			nbt1.putUUID("PlayerPartner", partnerTrainer.getUUID());
			nbtPartner.putBoolean("IsPlayerPartner", true);

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent("Successfully set up tag battle with enemy trainer ")
						.append(new StringTextComponent(enemyTrainer.getName("en_us")).withStyle(TextFormatting.RED))
						.append(new StringTextComponent(" against player and ally "))
						.append(new StringTextComponent(partnerTrainer.getName("en_us")).withStyle(TextFormatting.GREEN)),
					true
				);

			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent("Error: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}

	private static int linkTwoEnemiesPlayerPartner(CommandContext<CommandSource> context) {
		try {
			Entity entity1 = EntityArgument.getEntity(context, "enemyTrainer1");
			Entity entity2 = EntityArgument.getEntity(context, "enemyTrainer2");
			Entity entity3 = EntityArgument.getEntity(context, "playerPartnerTrainer");

			if (!(entity1 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("First enemy NPC must be an NPCTrainer!"));
				return 0;
			}

			if (!(entity2 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("Second enemy NPC must be an NPCTrainer!"));
				return 0;
			}

			if (!(entity3 instanceof NPCTrainer)) {
				context.getSource().sendFailure(new StringTextComponent("Player partner NPC must be an NPCTrainer!"));
				return 0;
			}

			NPCTrainer enemyTrainer1 = (NPCTrainer) entity1;
			NPCTrainer enemyTrainer2 = (NPCTrainer) entity2;
			NPCTrainer partnerTrainer = (NPCTrainer) entity3;

			CompoundNBT nbt1 = enemyTrainer1.getPersistentData();
			CompoundNBT nbt2 = enemyTrainer2.getPersistentData();
			CompoundNBT nbtPartner = partnerTrainer.getPersistentData();

			nbt1.putUUID("PlayerPartner", partnerTrainer.getUUID());
			nbt2.putUUID("PlayerPartner", partnerTrainer.getUUID());

			nbt1.putUUID("Linked", enemyTrainer2.getUUID());
			nbt2.putUUID("Linked", enemyTrainer1.getUUID());

			nbtPartner.putBoolean("IsPlayerPartner", true);

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent("Successfully set up tag battle with enemy trainers ")
						.append(new StringTextComponent(enemyTrainer1.getName("en_us")).withStyle(TextFormatting.RED))
						.append(new StringTextComponent(" and "))
						.append(new StringTextComponent(enemyTrainer2.getName("en_us")).withStyle(TextFormatting.RED))
						.append(new StringTextComponent(" against player and ally "))
						.append(new StringTextComponent(partnerTrainer.getName("en_us")).withStyle(TextFormatting.GREEN)),
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
