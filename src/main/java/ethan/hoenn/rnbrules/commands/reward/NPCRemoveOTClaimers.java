package ethan.hoenn.rnbrules.commands.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;

public class NPCRemoveOTClaimers {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcremoveotclaimers")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeClaimers(context, EntityArgument.getEntity(context, "entity"))))
		);

		dispatcher.register(
			Commands.literal("npcremoveonetimeclaimers")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeClaimers(context, EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int removeClaimers(CommandContext<CommandSource> context, Entity entity) throws CommandSyntaxException {
		CommandSource source = context.getSource();

		if (!(entity instanceof NPCChatting)) {
			source.sendFailure(new StringTextComponent("§cTarget must be a Chatting NPC!"));
			return 0;
		}

		NPCChatting npc = (NPCChatting) entity;
		CompoundNBT data = npc.getPersistentData();

		if (!data.contains("OneTimeReward")) {
			source.sendFailure(new StringTextComponent("§cThis NPC is not set up for one-time rewards!"));
			return 0;
		}

		int removedCount = 0;
		if (data.contains("Claimers")) {
			ListNBT oldList = data.getList("Claimers", 10);
			removedCount = oldList.size();
		}

		data.put("Claimers", new ListNBT());

		source.sendSuccess(new StringTextComponent("§aRemoved §e" + removedCount + "§a claimers from §b" + npc.getName("en_us")), true);

		return removedCount;
	}
}
