package ethan.hoenn.rnbrules.utils.data.dialog;

import java.util.List;

public class DialogueChoiceData {

	private String text;
	private String next;
	private DialogueActionData action;
	private List<DialogueActionData> actions;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public DialogueActionData getAction() {
		return action;
	}

	public void setAction(DialogueActionData action) {
		this.action = action;
	}

	public List<DialogueActionData> getActions() {
		return actions;
	}

	public void setActions(List<DialogueActionData> actions) {
		this.actions = actions;
	}
}
