package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pixelmonmod.pixelmon.entities.npcs.*;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddBattleDep {

	private static final SuggestionProvider<CommandSource> DEPENDENCY_SUGGESTIONS = (context, builder) -> {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayerOrException();
			BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());
			Set<String> dependencies = manager.getAllDependencyIds();
			return ISuggestionProvider.suggest(dependencies, builder);
		} catch (CommandSyntaxException e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("npcaddbattledep")
			.requires(source -> source.hasPermission(2))
			.then(
				Commands.argument("dependency", StringArgumentType.word())
					.suggests(DEPENDENCY_SUGGESTIONS)
					.then(
						Commands.argument("entity", EntityArgument.entity()).executes(context ->
							addDependencyToNPC(context, StringArgumentType.getString(context, "dependency"), EntityArgument.getEntity(context, "entity"))
						)
					)
			);

		dispatcher.register(command);
	}

	private static int addDependencyToNPC(CommandContext<CommandSource> context, String dependency, Entity entity) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();
		BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());

		if (!manager.dependencyExists(dependency)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Dependency '" + dependency + "' doesn't exist!"));
			return 0;
		}

		if (entity instanceof NPCTrainer) {
			NPCTrainer trainer = (NPCTrainer) entity;
			if (manager.addDependencyToTrainer(trainer, dependency)) {
				String trainerName = trainer.getName().getString();
				String depDescription = manager.getDependency(dependency).getDescription();
				source.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Added dependency '" +
						TextFormatting.GOLD +
						dependency +
						TextFormatting.GREEN +
						"' to trainer " +
						TextFormatting.AQUA +
						trainerName +
						TextFormatting.GREEN +
						".\nDescription: " +
						TextFormatting.WHITE +
						depDescription
					),
					true
				);
				return 1;
			} else {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This trainer already has that dependency!"));
				return 0;
			}
		} else if (entity instanceof NPCChatting) {
			NPCChatting npc = (NPCChatting) entity;
			if (manager.addDependencyToNPC(npc, dependency)) {
				String npcName = npc.getName("en_us");
				String depDescription = manager.getDependency(dependency).getDescription();
				source.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Added dependency '" +
						TextFormatting.GOLD +
						dependency +
						TextFormatting.GREEN +
						"' to NPC " +
						TextFormatting.AQUA +
						npcName +
						TextFormatting.GREEN +
						".\nDescription: " +
						TextFormatting.WHITE +
						depDescription
					),
					true
				);
				return 1;
			} else {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This NPC already has that dependency!"));
				return 0;
			}
		} else if (entity instanceof NPCTutor) {
			NPCTutor tutor = (NPCTutor) entity;
			if (manager.addDependencyToTutor(tutor, dependency)) {
				String tutorName = tutor.getName().getString();
				String depDescription = manager.getDependency(dependency).getDescription();
				source.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Added dependency '" +
						TextFormatting.GOLD +
						dependency +
						TextFormatting.GREEN +
						"' to move tutor " +
						TextFormatting.AQUA +
						tutorName +
						TextFormatting.GREEN +
						".\nDescription: " +
						TextFormatting.WHITE +
						depDescription
					),
					true
				);
				return 1;
			} else {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This move tutor already has that dependency!"));
				return 0;
			}
		} else if (entity instanceof NPCTrader) {
			NPCTrader trader = (NPCTrader) entity;
			if (manager.addDependencyToNPCTrader(trader, dependency)) {
				String traderName = trader.getName().getString();
				String depDescription = manager.getDependency(dependency).getDescription();
				source.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Added dependency '" +
						TextFormatting.GOLD +
						dependency +
						TextFormatting.GREEN +
						"' to trader " +
						TextFormatting.AQUA +
						traderName +
						TextFormatting.GREEN +
						".\nDescription: " +
						TextFormatting.WHITE +
						depDescription
					),
					true
				);
				return 1;
			} else {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This trader already has that dependency!"));
				return 0;
			}
		} else if (entity instanceof NPCShopkeeper) {
			NPCShopkeeper shopkeeper = (NPCShopkeeper) entity;
			if (manager.addDependencyToShopkeeper(shopkeeper, dependency)) {
				String depDescription = manager.getDependency(dependency).getDescription();
				source.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Added dependency '" +
						TextFormatting.GOLD +
						dependency +
						TextFormatting.GREEN +
						"' to this shopkeeper " +
						TextFormatting.GREEN +
						".\nDescription: " +
						TextFormatting.WHITE +
						depDescription
					),
					true
				);
				return 1;
			} else {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "This shopkeeper already has that dependency!"));
				return 0;
			}
		} else {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "The selected entity must be an NPC Trainer, Chatting NPC, or Move Tutor!"));
			return 0;
		}
	}
}
