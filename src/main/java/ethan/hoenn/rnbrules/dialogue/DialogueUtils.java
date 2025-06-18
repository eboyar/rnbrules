package ethan.hoenn.rnbrules.dialogue;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueChoiceEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class DialogueUtils {

	public static void showBasicDialogue(ServerPlayerEntity player, String title, String message) {
		DialogueManager.INSTANCE.createBasicDialogue(player, title, message);
	}

	public static void showChoiceDialogue(ServerPlayerEntity player, String title, String message, List<DialogueManager.DialogueChoice> choices) {
		DialogueManager.INSTANCE.createChoiceDialogue(player, title, message, choices);
	}

	public static void showChainedDialogue(ServerPlayerEntity player, List<DialoguePage> pages) {
		DialogueManager.INSTANCE.createChainedDialogue(player, pages);
	}

	public static class ChainedDialogueBuilder {

		private ServerPlayerEntity player;
		private final List<DialoguePage> pages = new ArrayList<>();

		public static ChainedDialogueBuilder create() {
			return new ChainedDialogueBuilder();
		}

		public ChainedDialogueBuilder player(ServerPlayerEntity player) {
			this.player = player;
			return this;
		}

		public ChainedDialogueBuilder addBasicPage(String title, String message) {
			pages.add(DialoguePage.basic(title, message));
			return this;
		}

		public ChainedDialogueBuilder addChoicePage(String title, String message, List<DialogueManager.DialogueChoice> choices) {
			pages.add(DialoguePage.choice(title, message, choices));
			return this;
		}

		public void send() {
			if (player == null || pages.isEmpty()) {
				throw new IllegalStateException("Player and at least one page must be set before sending dialogue");
			}
			DialogueUtils.showChainedDialogue(player, pages);
		}
	}

	public static class ChoiceDialogueBuilder {

		private String title;
		private String message;
		private ServerPlayerEntity player;
		private final List<DialogueManager.DialogueChoice> choices = new ArrayList<>();

		public static ChoiceDialogueBuilder create() {
			return new ChoiceDialogueBuilder();
		}

		public ChoiceDialogueBuilder player(ServerPlayerEntity player) {
			this.player = player;
			return this;
		}

		public ChoiceDialogueBuilder title(String title) {
			this.title = title;
			return this;
		}

		public ChoiceDialogueBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ChoiceDialogueBuilder addChoice(String text, Runnable action) {
			choices.add(new DialogueManager.DialogueChoice(text, event -> action.run()));
			return this;
		}

		public ChoiceDialogueBuilder addChoice(String text, Consumer<DialogueChoiceEvent> action) {
			choices.add(new DialogueManager.DialogueChoice(text, action));
			return this;
		}

		public void send() {
			if (player == null || title == null || message == null) {
				throw new IllegalStateException("Player, title, and message must be set before sending dialogue");
			}
			DialogueManager.INSTANCE.createChoiceDialogue(player, title, message, choices);
		}
	}
}
