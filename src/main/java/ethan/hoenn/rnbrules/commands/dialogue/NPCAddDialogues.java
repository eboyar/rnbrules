package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddDialogues {

	private static final SuggestionProvider<CommandSource> DIALOGUE_SUGGESTIONS = (context, builder) -> {
		List<String> dialogueIds = new ArrayList<>(DialogueRegistry.INSTANCE.getAllDialogues().keySet());
		return ISuggestionProvider.suggest(dialogueIds, builder);
	};

	private static final SuggestionProvider<CommandSource> DEPENDENCY_SUGGESTIONS = (context, builder) -> {
		try {
			ServerPlayerEntity player = context.getSource().getPlayerOrException();
			BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());
			Set<String> dependencies = manager.getAllDependencyIds();
			return ISuggestionProvider.suggest(dependencies, builder);
		} catch (Exception e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcadddialogues")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("entity", EntityArgument.entity()).then(
						Commands.argument("dialogueId", StringArgumentType.string())
							.suggests(DIALOGUE_SUGGESTIONS)
							.then(
								Commands.argument("dialogOrder", IntegerArgumentType.integer(1))
									.executes(context ->
										addDialogueToNPC(
											context,
											EntityArgument.getEntity(context, "entity"),
											StringArgumentType.getString(context, "dialogueId"),
											IntegerArgumentType.getInteger(context, "dialogOrder"),
											new String[0]
										)
									)
									.then(addBattleDep(1))
							)
					)
				)
		);
	}

	private static ArgumentBuilder<CommandSource, ?> addBattleDep(int index) {
		return Commands.argument("battleDep" + index, StringArgumentType.string())
			.suggests(DEPENDENCY_SUGGESTIONS)
			.executes(context -> {
				String[] battleDeps = new String[index];
				for (int i = 1; i <= index; i++) {
					battleDeps[i - 1] = StringArgumentType.getString(context, "battleDep" + i);
				}
				return addDialogueToNPC(
					context,
					EntityArgument.getEntity(context, "entity"),
					StringArgumentType.getString(context, "dialogueId"),
					IntegerArgumentType.getInteger(context, "dialogOrder"),
					battleDeps
				);
			})
			.then(
				index < 5
					? addBattleDep(index + 1)
					: Commands.literal("end").executes(context -> {
						String[] battleDeps = new String[index];
						for (int i = 1; i <= index; i++) {
							battleDeps[i - 1] = StringArgumentType.getString(context, "battleDep" + i);
						}
						return addDialogueToNPC(
							context,
							EntityArgument.getEntity(context, "entity"),
							StringArgumentType.getString(context, "dialogueId"),
							IntegerArgumentType.getInteger(context, "dialogOrder"),
							battleDeps
						);
					})
			);
	}

	private static int addDialogueToNPC(CommandContext<CommandSource> context, Entity entity, String dialogueId, int dialogOrder, String[] battleDeps) {
		CommandSource source = context.getSource();
		ServerPlayerEntity player;

		try {
			player = source.getPlayerOrException();
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Error: Player not found"));
			return 0;
		}

		if (!DialogueRegistry.INSTANCE.hasDialogue(dialogueId)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Dialogue ID does not exist: " + TextFormatting.GOLD + dialogueId));
			return 0;
		}

		BattleDependencyManager bdm = BattleDependencyManager.get(player.getLevel());
		List<String> validBattleDeps = new ArrayList<>();
		for (String dep : battleDeps) {
			if (!dep.isEmpty()) {
				if (!bdm.dependencyExists(dep)) {
					source.sendFailure(new StringTextComponent(TextFormatting.RED + "Battle dependency does not exist: " + TextFormatting.GOLD + dep));
					return 0;
				}
				validBattleDeps.add(dep);
			}
		}

		boolean success = DialogueNPCManager.get().addMultipleDialoguesToNPC(source, entity, dialogueId, dialogOrder, validBattleDeps.toArray(new String[0]));

		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
