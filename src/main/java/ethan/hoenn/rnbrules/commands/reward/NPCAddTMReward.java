package ethan.hoenn.rnbrules.commands.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddTMReward {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npcaddtmreward")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("tm_gen", IntegerArgumentType.integer(1, 9)).then(
					Commands.argument("tm_num", IntegerArgumentType.integer(1, 100)).then(
						Commands.argument("entity", EntityArgument.entity()).executes(context ->
							addTMReward(context, IntegerArgumentType.getInteger(context, "tm_gen"), IntegerArgumentType.getInteger(context, "tm_num"), EntityArgument.getEntity(context, "entity"))
						)
					)
				)
			);

		dispatcher.register(command);
	}

	private static int addTMReward(CommandContext<CommandSource> context, int tmGen, int tmNum, Entity entity) throws CommandSyntaxException {
		CommandSource source = context.getSource();

		if (!(entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "The selected entity must be an NPC Trainer!"));
			return 0;
		}

		NPCTrainer trainer = (NPCTrainer) entity;

		
		CompoundNBT entityNBT = new CompoundNBT();
		entity.save(entityNBT);

		
		CompoundNBT winningsTag;
		if (entityNBT.contains("WinningsTag")) {
			winningsTag = entityNBT.getCompound("WinningsTag");
		} else {
			winningsTag = new CompoundNBT();
		}

		
		int nextSlot = 0;
		while (winningsTag.contains("item" + nextSlot)) {
			nextSlot++;

			if (nextSlot >= 10) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This trainer already has the maximum number of rewards!"));
				return 0;
			}
		}

		
		CompoundNBT tmItem = new CompoundNBT();
		tmItem.putString("id", "pixelmon:tm_gen" + tmGen);
		tmItem.putByte("Count", (byte) 1);

		CompoundNBT tmTag = new CompoundNBT();
		tmTag.putInt("Damage", 0);
		tmTag.putShort("tm", (short) tmNum);

		tmItem.put("tag", tmTag);

		
		winningsTag.put("item" + nextSlot, tmItem);
		entityNBT.put("WinningsTag", winningsTag);

		
		entity.load(entityNBT);

		String tmName = "TM" + tmNum;
		if (tmGen > 1) {
			tmName = "Gen " + tmGen + " " + tmName;
		}

		source.sendSuccess(
			new StringTextComponent(TextFormatting.GREEN + "Added " + TextFormatting.GOLD + tmName + TextFormatting.GREEN + " as reward for trainer " + TextFormatting.AQUA + trainer.getName().getString()),
			true
		);

		return 1;
	}
}
