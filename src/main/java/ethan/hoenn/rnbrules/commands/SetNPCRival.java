package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SetNPCRival {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setnpcrival")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("tier", IntegerArgumentType.integer(1, 4)).then(
						Commands.argument("entity", EntityArgument.entity()).executes(context -> {
							return setNPCRival(context.getSource(), IntegerArgumentType.getInteger(context, "tier"), EntityArgument.getEntity(context, "entity"));
						})
					)
				)
		);
	}

	private static int setNPCRival(CommandSource source, int tier, Entity entity) {
		if (tier < 1 || tier > 4) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Rival tier must be between 1 and 4!"));
			return 0;
		}

		if (!(entity instanceof NPCTrainer)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Target entity is not an NPC Trainer!"));
			return 0;
		}

		NPCTrainer trainer = (NPCTrainer) entity;
		CompoundNBT data = trainer.getPersistentData();

		data.putInt("Rival", tier);

		source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully set NPC Trainer as rival with tier " + tier), true);

		return 1;
	}
}
