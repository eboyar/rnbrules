package ethan.hoenn.rnbrules.dialogue.yaml;

import ethan.hoenn.rnbrules.dialogue.DialogueManager;
import ethan.hoenn.rnbrules.dialogue.DialoguePage;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueChoiceData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialoguePageData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;

public class DialogueParser {

	private static final DialogueParser INSTANCE = new DialogueParser();

	public static DialogueParser getInstance() {
		return INSTANCE;
	}

	public List<DialoguePage> buildDialogueChain(String dialogueId, ServerPlayerEntity player) {
		try {
			DialogueFileData dialogueData = DialogueRegistry.INSTANCE.getDialogue(dialogueId);
			if (dialogueData == null) {
				System.err.println("Dialogue not found: " + dialogueId);
				return null;
			}

			if (dialogueData.getPages() == null || dialogueData.getPages().isEmpty()) {
				System.err.println("Dialogue has no pages: " + dialogueId);
				return null;
			}

			DialoguePageData firstPage = dialogueData.getPages().get(0);
			return buildDialogueChainFromPage(dialogueData, firstPage.getId(), player);
		} catch (Exception e) {
			System.err.println("Error building dialogue chain for ID '" + dialogueId + "': " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public List<DialoguePage> buildDialogueChainFromPage(DialogueFileData dialogueData, String startPageId, ServerPlayerEntity player) {
		List<DialoguePage> pages = new ArrayList<>();

		try {
			Map<String, DialoguePageData> pageMap = new HashMap<>();
			for (DialoguePageData page : dialogueData.getPages()) {
				pageMap.put(page.getId(), page);
			}

			DialoguePageData currentPage = pageMap.get(startPageId);
			if (currentPage == null) {
				throw new IllegalArgumentException("Page with ID '" + startPageId + "' not found in dialogue '" + dialogueData.getDialogue_id() + "'");
			}

			buildChainRecursive(dialogueData, currentPage, pages, pageMap, player, new HashSet<>());
		} catch (Exception e) {
			System.err.println("Error building dialogue chain from page '" + startPageId + "': " + e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}

		return pages;
	}

	private void buildChainRecursive(
		DialogueFileData dialogueData,
		DialoguePageData currentPage,
		List<DialoguePage> resultPages,
		Map<String, DialoguePageData> pageMap,
		ServerPlayerEntity player,
		Set<String> visitedPages
	) {
		if (visitedPages.contains(currentPage.getId())) {
			System.err.println("Detected loop in dialogue chain: page '" + currentPage.getId() + "' has already been visited.");
			return;
		}

		visitedPages.add(currentPage.getId());

		if (resultPages.size() > 100) {
			throw new RuntimeException("Dialogue chain too long, possible infinite loop in dialogue '" + dialogueData.getDialogue_id() + "'");
		}

		String npcName = currentPage.hasTempname() ? currentPage.getTempname() : dialogueData.getNpc_name();

		if (currentPage.isChoicePage()) {
			List<DialogueManager.DialogueChoice> choices = new ArrayList<>();
			for (DialogueChoiceData choiceData : currentPage.getChoices()) {
				choices.add(createChoice(choiceData, dialogueData, pageMap));
			}

			resultPages.add(DialoguePage.choice(npcName, currentPage.getText(), choices));
		} else {
			resultPages.add(DialoguePage.basic(npcName, currentPage.getText()));

			if (currentPage.isEnd()) {
				return;
			}

			if (currentPage.getNext() != null && !currentPage.getNext().isEmpty() && pageMap.containsKey(currentPage.getNext())) {
				DialoguePageData nextPage = pageMap.get(currentPage.getNext());
				buildChainRecursive(dialogueData, nextPage, resultPages, pageMap, player, visitedPages);
			} else {
				int currentIndex = dialogueData.getPages().indexOf(currentPage);
				if (currentIndex >= 0 && currentIndex < dialogueData.getPages().size() - 1) {
					DialoguePageData nextPage = dialogueData.getPages().get(currentIndex + 1);
					buildChainRecursive(dialogueData, nextPage, resultPages, pageMap, player, visitedPages);
				}
			}
		}
	}

	private DialogueManager.DialogueChoice createChoice(DialogueChoiceData choiceData, DialogueFileData dialogueData, Map<String, DialoguePageData> pageMap) {
		return new DialogueManager.DialogueChoice(choiceData.getText(), choiceData.getActions(), choiceData.getAction(), choiceData.getNext(), dialogueData, pageMap);
	}
}
