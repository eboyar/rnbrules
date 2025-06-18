package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCRemoveBattleDeps {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npcremovebattledeps")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("entity", EntityArgument.entity()).executes(context -> removeDependenciesFromNPC(context, EntityArgument.getEntity(context, "entity"))));

		dispatcher.register(command);
	}

	private static int removeDependenciesFromNPC(CommandContext<CommandSource> context, Entity entity) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();
		BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());

		if (entity instanceof NPCTrainer) {
			NPCTrainer trainer = (NPCTrainer) entity;
			if (!manager.trainerHasDependencies(trainer)) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This trainer doesn't have any dependencies to remove!"));
				return 0;
			}

			Set<String> currentDeps = manager.getTrainerDependencies(trainer);
			int depCount = currentDeps.size();

			manager.clearTrainerDependencies(trainer);

			String trainerName = trainer.getName().getString();
			source.sendSuccess(
				new StringTextComponent(
					TextFormatting.GREEN + "Removed " + TextFormatting.GOLD + depCount + TextFormatting.GREEN + " dependencies from trainer " + TextFormatting.AQUA + trainerName + TextFormatting.GREEN + "."
				),
				true
			);
			return depCount;
		} else if (entity instanceof NPCChatting) {
			NPCChatting npc = (NPCChatting) entity;
			if (!manager.npcHasDependencies(npc)) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This NPC doesn't have any dependencies to remove!"));
				return 0;
			}

			Set<String> currentDeps = manager.getNPCDependencies(npc);
			int depCount = currentDeps.size();

			manager.clearNPCDependencies(npc);

			String npcName = npc.getName("en_us");
			source.sendSuccess(
				new StringTextComponent(
					TextFormatting.GREEN + "Removed " + TextFormatting.GOLD + depCount + TextFormatting.GREEN + " dependencies from NPC " + TextFormatting.AQUA + npcName + TextFormatting.GREEN + "."
				),
				true
			);
			return depCount;
		} else {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "The selected entity must be an NPC Trainer or Chatting NPC!"));
			return 0;
		}
	}
}
