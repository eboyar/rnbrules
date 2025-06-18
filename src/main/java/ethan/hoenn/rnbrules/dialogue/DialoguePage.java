package ethan.hoenn.rnbrules.dialogue;

import java.util.List;

public abstract class DialoguePage {

	private final String title;
	private final String message;

	protected DialoguePage(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public static BasicPage basic(String title, String message) {
		return new BasicPage(title, message);
	}

	public static ChoicePage choice(String title, String message, List<DialogueManager.DialogueChoice> choices) {
		return new ChoicePage(title, message, choices);
	}

	public static class BasicPage extends DialoguePage {

		public BasicPage(String title, String message) {
			super(title, message);
		}
	}

	public static class ChoicePage extends DialoguePage {

		private final List<DialogueManager.DialogueChoice> choices;

		public ChoicePage(String title, String message, List<DialogueManager.DialogueChoice> choices) {
			super(title, message);
			this.choices = choices;
		}

		public List<DialogueManager.DialogueChoice> getChoices() {
			return choices;
		}
	}
}
