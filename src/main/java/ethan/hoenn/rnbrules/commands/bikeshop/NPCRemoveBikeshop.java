package ethan.hoenn.rnbrules.commands.bikeshop;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCRemoveBikeshop {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremovebikeshop")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("npc", EntityArgument.entity()).executes(context -> {
						Entity entity = EntityArgument.getEntity(context, "npc");

						if (!(entity instanceof NPCChatting)) {
							context.getSource().sendFailure(new StringTextComponent("Target must be a Pixelmon NPC"));
							return 0;
						}

						NPCChatting npc = (NPCChatting) entity;
						CompoundNBT entityData = npc.getPersistentData();

						if (!entityData.contains("Bikeshop")) {
							context.getSource().sendFailure(new StringTextComponent("This NPC is not a Bikeshop NPC"));
							return 0;
						}

						entityData.remove("Bikeshop");
						context.getSource().sendSuccess(new StringTextComponent("Removed Bikeshop functionality from NPC").withStyle(TextFormatting.GREEN), true);

						return 1;
					})
				)
		);
	}
}
