package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.utils.managers.DialogueNPCManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddDialogue {

	private static final SuggestionProvider<CommandSource> DIALOGUE_SUGGESTIONS = (context, builder) -> {
		List<String> dialogueIds = new ArrayList<>(DialogueRegistry.INSTANCE.getAllDialogues().keySet());
		return ISuggestionProvider.suggest(dialogueIds, builder);
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcadddialogue")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("dialogueId", StringArgumentType.string())
						.suggests(DIALOGUE_SUGGESTIONS)
						.then(
							Commands.argument("entity", EntityArgument.entity()).executes(context -> {
								String dialogueId = StringArgumentType.getString(context, "dialogueId");
								return addDialogueToNPC(context.getSource(), EntityArgument.getEntity(context, "entity"), dialogueId);
							})
						)
				)
		);
	}

	private static int addDialogueToNPC(CommandSource source, net.minecraft.entity.Entity entity, String dialogueId) {
		if (!DialogueRegistry.INSTANCE.hasDialogue(dialogueId)) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Dialogue ID does not exist: " + TextFormatting.GOLD + dialogueId));
			return 0;
		}

		boolean success = DialogueNPCManager.get().addDialogueToNPC(source, entity, dialogueId);
		return success ? Command.SINGLE_SUCCESS : 0;
	}
}
