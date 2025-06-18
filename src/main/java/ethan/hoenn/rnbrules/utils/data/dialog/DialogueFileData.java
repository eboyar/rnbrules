package ethan.hoenn.rnbrules.utils.data.dialog;

import java.util.List;

public class DialogueFileData {

	private String dialogue_id;
	private String npc_name;
	private List<DialoguePageData> pages;

	public String getDialogue_id() {
		return dialogue_id;
	}

	public void setDialogue_id(String dialogue_id) {
		this.dialogue_id = dialogue_id;
	}

	public String getNpc_name() {
		return npc_name;
	}

	public void setNpc_name(String npc_name) {
		this.npc_name = npc_name;
	}

	public List<DialoguePageData> getPages() {
		return pages;
	}

	public void setPages(List<DialoguePageData> pages) {
		this.pages = pages;
	}

	public String getDialogueId() {
		return dialogue_id;
	}

	public String getNpcName() {
		return npc_name;
	}
}
