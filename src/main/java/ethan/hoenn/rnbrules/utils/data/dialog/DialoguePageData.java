package ethan.hoenn.rnbrules.utils.data.dialog;

import java.util.List;

public class DialoguePageData {

	private String id;
	private String type;
	private String text;
	private List<DialogueChoiceData> choices;
	private String next;
	private boolean end;
	private String tempname;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<DialogueChoiceData> getChoices() {
		return choices;
	}

	public void setChoices(List<DialogueChoiceData> choices) {
		this.choices = choices;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public String getTempname() {
		return tempname;
	}

	public void setTempname(String tempname) {
		this.tempname = tempname;
	}

	public boolean hasTempname() {
		return tempname != null && !tempname.isEmpty();
	}

	public boolean isChoicePage() {
		return "Choice".equalsIgnoreCase(type) && choices != null && !choices.isEmpty();
	}
}
