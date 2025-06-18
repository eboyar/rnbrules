package ethan.hoenn.rnbrules.commands.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;

public class NPCAddOTReward {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcaddotreward")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> addOneTimeReward(context, EntityArgument.getEntity(context, "entity"))))
		);

		dispatcher.register(
			Commands.literal("npcaddonetimereward")
				.requires(source -> source.hasPermission(2))
				.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> addOneTimeReward(context, EntityArgument.getEntity(context, "entity"))))
		);
	}

	private static int addOneTimeReward(CommandContext<CommandSource> context, Entity entity) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();

		if (!(entity instanceof NPCChatting)) {
			source.sendFailure(new StringTextComponent("§cTarget must be a Chatting NPC!"));
			return 0;
		}

		ItemStack heldItem = player.getMainHandItem();
		if (heldItem.isEmpty()) {
			source.sendFailure(new StringTextComponent("§cYou must be holding an item to set as a reward!"));
			return 0;
		}

		NPCChatting npc = (NPCChatting) entity;
		CompoundNBT data = npc.getPersistentData();

		data.putBoolean("OneTimeReward", true);

		if (!data.contains("Claimers")) {
			data.put("Claimers", new ListNBT());
		}

		CompoundNBT itemNBT = new CompoundNBT();
		heldItem.save(itemNBT);
		data.put("RewardItem", itemNBT);

		source.sendSuccess(new StringTextComponent("§aAdded §6" + heldItem.getHoverName().getString() + "§a as a one-time reward for §b" + npc.getName("en_us")), true);

		return 1;
	}
}
