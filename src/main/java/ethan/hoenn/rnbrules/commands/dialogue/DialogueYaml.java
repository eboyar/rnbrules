package ethan.hoenn.rnbrules.commands.dialogue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ethan.hoenn.rnbrules.dialogue.DialoguePage;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.dialogue.DialogueUtils;
import ethan.hoenn.rnbrules.dialogue.yaml.DialogueParser;
import java.io.File;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class DialogueYaml {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> reloadCommand = Commands.literal("reloaddialogues")
			.requires(source -> source.hasPermission(2))
			.executes(context -> {
				File worldDir = context.getSource().getServer().getWorldPath(new net.minecraft.world.storage.FolderName("data")).toFile().getParentFile();
				DialogueRegistry.INSTANCE.loadAllDialogues(worldDir);

				context.getSource().sendSuccess(new StringTextComponent("Reloaded dialogue files."), true);
				return 1;
			});

		LiteralArgumentBuilder<CommandSource> showDialogueCommand = Commands.literal("showdialogue").then(
			Commands.argument("dialogueId", StringArgumentType.string()).executes(context -> {
				if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
					String dialogueId = StringArgumentType.getString(context, "dialogueId");

					if (!DialogueRegistry.INSTANCE.hasDialogue(dialogueId)) {
						context.getSource().sendFailure(new StringTextComponent("Dialogue with ID '" + dialogueId + "' not found."));
						return 0;
					}

					List<DialoguePage> pages = DialogueParser.getInstance().buildDialogueChain(dialogueId, player);
					if (pages != null && !pages.isEmpty()) {
						DialogueUtils.showChainedDialogue(player, pages);
						return 1;
					} else {
						context.getSource().sendFailure(new StringTextComponent("Failed to build dialogue chain for '" + dialogueId + "'."));
						return 0;
					}
				}
				context.getSource().sendFailure(new StringTextComponent("This command can only be used by players."));
				return 0;
			})
		);

		LiteralArgumentBuilder<CommandSource> listDialoguesCommand = Commands.literal("listdialogues")
			.requires(source -> source.hasPermission(2))
			.executes(context -> {
				context.getSource().sendSuccess(new StringTextComponent("Available dialogues:"), false);

				DialogueRegistry.INSTANCE.getAllDialogues()
					.forEach((id, dialogue) -> {
						context.getSource().sendSuccess(new StringTextComponent("- " + id + " (" + dialogue.getNpcName() + ")"), false);
					});

				return 1;
			});

		dispatcher.register(reloadCommand);
		dispatcher.register(showDialogueCommand);
		dispatcher.register(listDialoguesCommand);
	}
}
