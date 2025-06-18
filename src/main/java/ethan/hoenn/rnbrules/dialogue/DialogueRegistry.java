package ethan.hoenn.rnbrules.dialogue;

import ethan.hoenn.rnbrules.dialogue.yaml.DialogueFileLoader;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueRegistry {

	public static final DialogueRegistry INSTANCE = new DialogueRegistry();

	private final Map<String, DialogueFileData> loadedDialogues = new HashMap<>();

	public void loadAllDialogues(File worldDir) {
		loadedDialogues.clear();

		List<DialogueFileData> dialogues = DialogueFileLoader.loadAllDialogues(worldDir);

		for (DialogueFileData dialogue : dialogues) {
			loadedDialogues.put(dialogue.getDialogue_id(), dialogue);
		}

		System.out.println("Loaded " + loadedDialogues.size() + " dialogue files");
	}

	public DialogueFileData getDialogue(String dialogueId) {
		return loadedDialogues.get(dialogueId);
	}

	public boolean hasDialogue(String dialogueId) {
		return loadedDialogues.containsKey(dialogueId);
	}

	public Map<String, DialogueFileData> getAllDialogues() {
		return new HashMap<>(loadedDialogues);
	}
}
