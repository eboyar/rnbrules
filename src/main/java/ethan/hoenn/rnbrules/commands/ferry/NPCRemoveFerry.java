package ethan.hoenn.rnbrules.commands.ferry;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCRemoveFerry {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremoveferry")
				.requires(source -> source.hasPermission(2)) // Admin only
				.then(
					Commands.argument("npc", EntityArgument.entity()).executes(context -> {
						Entity entity = EntityArgument.getEntity(context, "npc");

						if (!(entity instanceof NPCChatting)) {
							context.getSource().sendFailure(new StringTextComponent("Target must be a Pixelmon NPC"));
							return 0;
						}

						NPCChatting npc = (NPCChatting) entity;
						CompoundNBT entityData = npc.getPersistentData();

						if (!entityData.contains("Sailor")) {
							context.getSource().sendFailure(new StringTextComponent("This NPC does not have a ferry destination tag"));
							return 0;
						}

						String previousDestination = entityData.getString("Sailor");
						entityData.remove("Sailor");

						context.getSource().sendSuccess(new StringTextComponent("Removed ferry destination tag (was: " + previousDestination + ")").withStyle(TextFormatting.GREEN), true);

						return 1;
					})
				)
		);
	}
}
