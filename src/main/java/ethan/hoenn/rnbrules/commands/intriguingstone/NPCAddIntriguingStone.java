package ethan.hoenn.rnbrules.commands.intriguingstone;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddIntriguingStone {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcaddintriguingstone")
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

						entityData.putBoolean("IntriguingStone", true);

						context.getSource().sendSuccess(new StringTextComponent("NPC set as Intriguing Stone NPC").withStyle(TextFormatting.GREEN), true);

						return 1;
					})
				)
		);
	}
}
