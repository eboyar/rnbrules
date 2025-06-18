package ethan.hoenn.rnbrules.dialogue.yaml;

import ethan.hoenn.rnbrules.utils.data.dialog.DialogueChoiceData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialoguePageData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DialogueFileLoader {

	private static final String DIALOGUE_DIR = "data/rnbdialogues";

	public static List<DialogueFileData> loadAllDialogues(File worldDir) {
		List<DialogueFileData> dialogues = new ArrayList<>();

		File dialogueDir = new File(worldDir, DIALOGUE_DIR);
		if (!dialogueDir.exists()) {
			dialogueDir.mkdirs();
			return dialogues;
		}

		System.out.println("Looking for dialogues in: " + dialogueDir.getAbsolutePath());

		File[] files = dialogueDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
		if (files == null) {
			return dialogues;
		}

		for (File file : files) {
			try {
				DialogueFileData dialogue = loadDialogueFile(file);
				if (dialogue != null) {
					dialogues.add(dialogue);
					System.out.println("Successfully loaded dialogue: " + dialogue.getDialogueId());
				}
			} catch (Exception e) {
				System.err.println("Error loading dialogue file " + file.getName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}

		return dialogues;
	}

	public static DialogueFileData loadDialogueFile(File file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			LoaderOptions loaderOptions = new LoaderOptions();
			loaderOptions.setAllowDuplicateKeys(false);
			loaderOptions.setMaxAliasesForCollections(10);
			loaderOptions.setCodePointLimit(5 * 1024 * 1024);

			Constructor constructor = new Constructor(DialogueFileData.class, loaderOptions);

			Yaml yaml = new Yaml(constructor);

			DialogueFileData dialogue = yaml.load(fis);
			validateDialogue(dialogue, file.getName());
			return dialogue;
		} catch (IOException e) {
			System.err.println("Failed to read dialogue file " + file.getName());
			e.printStackTrace();
			return null;
		}
	}

	private static void validateDialogue(DialogueFileData dialogue, String fileName) {
		if (dialogue == null) {
			throw new IllegalArgumentException("Failed to parse dialogue file " + fileName);
		}

		if (dialogue.getDialogue_id() == null || dialogue.getDialogue_id().isEmpty()) {
			throw new IllegalArgumentException("Dialogue file " + fileName + " is missing dialogue_id");
		}

		if (dialogue.getNpc_name() == null || dialogue.getNpc_name().isEmpty()) {
			throw new IllegalArgumentException("Dialogue file " + fileName + " is missing npc_name");
		}

		if (dialogue.getPages() == null || dialogue.getPages().isEmpty()) {
			throw new IllegalArgumentException("Dialogue file " + fileName + " has no pages");
		}

		for (DialoguePageData page : dialogue.getPages()) {
			if (page.getId() == null || page.getId().isEmpty()) {
				throw new IllegalArgumentException("Page in " + fileName + " is missing id");
			}

			if (page.getType() == null || page.getType().isEmpty()) {
				throw new IllegalArgumentException("Page '" + page.getId() + "' in " + fileName + " is missing type");
			}

			if (page.getText() == null || page.getText().isEmpty()) {
				throw new IllegalArgumentException("Page '" + page.getId() + "' in " + fileName + " is missing text");
			}

			if (page.isChoicePage()) {
				if (page.getChoices() == null || page.getChoices().isEmpty()) {
					throw new IllegalArgumentException("Choice page '" + page.getId() + "' in " + fileName + " has no choices");
				}

				for (DialogueChoiceData choice : page.getChoices()) {
					if (choice.getText() == null || choice.getText().isEmpty()) {
						throw new IllegalArgumentException("Choice in page '" + page.getId() + "' in " + fileName + " is missing text");
					}
				}
			}
		}
	}
}
