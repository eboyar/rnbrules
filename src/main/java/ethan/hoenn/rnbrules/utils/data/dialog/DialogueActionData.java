package ethan.hoenn.rnbrules.utils.data.dialog;

import java.util.List;

public class DialogueActionData {

	private String type;
	private List<String> messages;
	private List<String> commands;
	private List<String> battledeps;
	private List<String> cost;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public List<String> getBattledeps() {
		return battledeps;
	}

	public void setBattledeps(List<String> battledeps) {
		this.battledeps = battledeps;
	}

	public List<String> getCost() {
		return cost;
	}

	public void setCost(List<String> cost) {
		this.cost = cost;
	}
}
