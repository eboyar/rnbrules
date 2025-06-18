package ethan.hoenn.rnbrules.dialogue;

import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import javax.annotation.Nullable;

public abstract class DialogueType {

	protected DialogueType() {}

	public static final class BasicDialogue extends DialogueType {

		private final Dialogue dialogue;

		public BasicDialogue(Dialogue dialogue) {
			this.dialogue = dialogue;
		}

		public final Dialogue getDialogue() {
			return this.dialogue;
		}

		public final BasicDialogue copy(Dialogue dialogue) {
			return new BasicDialogue(dialogue);
		}

		@Override
		public String toString() {
			return "BasicDialogue(dialogue=" + this.dialogue + ')';
		}

		@Override
		public int hashCode() {
			return this.dialogue.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) return true;
			if (!(other instanceof BasicDialogue)) return false;
			BasicDialogue basicDialogue = (BasicDialogue) other;
			return dialogue.equals(basicDialogue.dialogue);
		}
	}

	public static final class ChoiceDialogue extends DialogueType {

		private final Dialogue dialogue;

		public ChoiceDialogue(Dialogue dialogue) {
			this.dialogue = dialogue;
		}

		public final Dialogue getDialogue() {
			return this.dialogue;
		}

		public final ChoiceDialogue copy(Dialogue dialogue) {
			return new ChoiceDialogue(dialogue);
		}

		@Override
		public String toString() {
			return "ChoiceDialogue(dialogue=" + this.dialogue + ')';
		}

		@Override
		public int hashCode() {
			return this.dialogue.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) return true;
			if (!(other instanceof ChoiceDialogue)) return false;
			ChoiceDialogue choiceDialogue = (ChoiceDialogue) other;
			return dialogue.equals(choiceDialogue.dialogue);
		}
	}
}
