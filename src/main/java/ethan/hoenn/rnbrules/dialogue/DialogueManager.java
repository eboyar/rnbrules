package ethan.hoenn.rnbrules.dialogue;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueChoiceEvent;
import ethan.hoenn.rnbrules.dialogue.actions.DialogueActionManager;
import ethan.hoenn.rnbrules.dialogue.yaml.DialogueParser;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueFileData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("CallToPrintStackTrace")
public class DialogueManager {

	public static final DialogueManager INSTANCE = new DialogueManager();
	private volatile ExecutorService dialogueThreadPool = Executors.newCachedThreadPool();
	private final Object poolLock = new Object();

	private final ConcurrentHashMap<UUID, DialogueType> activeDialogues = new ConcurrentHashMap<>();

	private DialogueManager() {
		Pixelmon.EVENT_BUS.register(this);
		DialogueActionManager.getInstance();
	}

	private String processText(String text, ServerPlayerEntity player) {
		return text.replace("&", "§").replace("@p", player.getName().getString());
	}

	private ExecutorService getThreadPool() {
		ExecutorService currentPool = dialogueThreadPool;
		if (currentPool == null || currentPool.isShutdown() || currentPool.isTerminated()) {
			synchronized (poolLock) {
				currentPool = dialogueThreadPool;
				if (currentPool == null || currentPool.isShutdown() || currentPool.isTerminated()) {
					dialogueThreadPool = Executors.newCachedThreadPool();
					currentPool = dialogueThreadPool;
				}
			}
		}
		return currentPool;
	}

	public void createBasicDialogue(ServerPlayerEntity player, String title, String message) {
		try {
			String formattedTitle = processText(title, player);
			String formattedMessage = processText(message, player);

			Dialogue dialogue = Dialogue.builder().setName(formattedTitle).setText(formattedMessage).escapeCloses().build();

			activeDialogues.put(player.getUUID(), new DialogueType.BasicDialogue(dialogue));

			if (player.getServer() != null) {
				player
					.getServer()
					.execute(() -> {
						List<Dialogue> dialogueList = new ArrayList<>();
						dialogueList.add(dialogue);
						Dialogue.setPlayerDialogueData(player, dialogueList, true);
					});
			}
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(new StringTextComponent("§cError creating dialogue. See server logs."), player.getUUID());
		}
	}

	public void createChainedDialogue(ServerPlayerEntity player, List<DialoguePage> pages) {
		if (pages == null || pages.isEmpty()) {
			player.sendMessage(new StringTextComponent("§cCannot create dialogue with no pages."), player.getUUID());
			return;
		}

		getThreadPool()
			.submit(() -> {
				try {
					List<Dialogue> dialogueChain = new ArrayList<>();
					DialogueType initialType = null;

					for (int i = 0; i < pages.size(); i++) {
						DialoguePage page = pages.get(i);
						String formattedTitle = processText(page.getTitle(), player);
						String formattedMessage = processText(page.getMessage(), player);

						Dialogue.DialogueBuilder builder = Dialogue.builder().setName("§9§l" + formattedTitle).setText(formattedMessage);

						if (page instanceof DialoguePage.ChoicePage) {
							DialoguePage.ChoicePage choicePage = (DialoguePage.ChoicePage) page;
							for (DialogueChoice choice : choicePage.getChoices()) {
								String processedChoiceText = processText(choice.getText(), player);
								Choice dChoice = Choice.builder().setText(processedChoiceText).setHandle(event -> handleChoiceSelection(event, choice)).build();
								builder.addChoice(dChoice);
							}

							if (i == 0) {
								initialType = new DialogueType.ChoiceDialogue(null);
							}
						} else if (i == 0) {
							initialType = new DialogueType.BasicDialogue(null);
						}

						//if any of the page titles contains the exact strings "Elite Four" or "Champion" use requireManualClose. If it does NOT contain that, use escapeCloses.
						if (formattedTitle.contains("Elite Four") || formattedTitle.contains("Champion")) {
							builder.requireManualClose();
						} else {
							builder.escapeCloses();
						}
						dialogueChain.add(builder.build());
					}

					if (player.getServer() != null) {
						DialogueType finalInitialType = initialType;
						player
							.getServer()
							.execute(() -> {
								try {
									if (dialogueChain.isEmpty()) {
										player.sendMessage(new StringTextComponent("§cError: Dialogue chain became empty."), player.getUUID());
										return;
									}

									Dialogue firstDialogue = dialogueChain.get(0);
									if (finalInitialType instanceof DialogueType.ChoiceDialogue) {
										activeDialogues.put(player.getUUID(), new DialogueType.ChoiceDialogue(firstDialogue));
									} else {
										activeDialogues.put(player.getUUID(), new DialogueType.BasicDialogue(firstDialogue));
									}

									Dialogue.setPlayerDialogueData(player, dialogueChain, true);
								} catch (Exception e) {
									e.printStackTrace();
									player.sendMessage(new StringTextComponent("§cError finalizing dialogue chain. See server logs."), player.getUUID());
								}
							});
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (player.getServer() != null) {
						player
							.getServer()
							.execute(() -> {
								player.sendMessage(new StringTextComponent("§cError building dialogue chain off-thread. See server logs."), player.getUUID());
							});
					}
				}
			});
	}

	public void createChoiceDialogue(ServerPlayerEntity player, String title, String message, List<DialogueChoice> choices) {
		try {
			String formattedTitle = processText(title, player);
			String formattedMessage = processText(message, player);

			List<DialogueChoice> processedChoices = new ArrayList<>();
			for (DialogueChoice choice : choices) {
				String processedText = processText(choice.getText(), player);
				if (choice.getCustomAction() != null) {
					processedChoices.add(new DialogueChoice(processedText, choice.getCustomAction()));
				} else {
					processedChoices.add(new DialogueChoice(processedText, choice.getActions(), choice.getLegacyAction(), choice.getNextPageId(), choice.getDialogueData(), choice.getPageMap()));
				}
			}

			Dialogue dialogue = buildChoiceDialogue(formattedTitle, formattedMessage, processedChoices, false);

			if (player.getServer() != null) {
				player
					.getServer()
					.execute(() -> {
						activeDialogues.put(player.getUUID(), new DialogueType.ChoiceDialogue(dialogue));

						List<Dialogue> dialogueList = new ArrayList<>();
						dialogueList.add(dialogue);
						Dialogue.setPlayerDialogueData(player, dialogueList, true);
					});
			}
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(new StringTextComponent("§cError creating dialogue. See server logs."), player.getUUID());
		}
	}

	private void handleChoiceSelection(DialogueChoiceEvent event, DialogueChoice choice) {
		try {
			if (choice.getActions() != null && !choice.getActions().isEmpty()) {
				for (DialogueActionData action : choice.getActions()) {
					DialogueActionManager.getInstance().processAction(action, event.player);
				}
			} else if (choice.getLegacyAction() != null) {
				DialogueActionManager.getInstance().processAction(choice.getLegacyAction(), event.player);
			}

			if (choice.getCustomAction() != null) {
				if (event.player.getServer() != null) {
					event.player.getServer().execute(() -> choice.getCustomAction().accept(event));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (event.player.getServer() != null) {
				event.player
					.getServer()
					.execute(() -> {
						event.player.sendMessage(new StringTextComponent("Error processing dialogue choice actions. See server logs."), event.player.getUUID());
					});
			}
		}

		try {
			if (choice.getNextPageId() != null && !choice.getNextPageId().isEmpty() && choice.getDialogueData() != null && choice.getPageMap() != null) {
				if (!choice.getPageMap().containsKey(choice.getNextPageId())) {
					event.player.sendMessage(new StringTextComponent("Error: Next page '" + choice.getNextPageId() + "' not found."), event.player.getUUID());
					return;
				}

				event.player.closeContainer();

				getThreadPool()
					.submit(() -> {
						try {
							List<DialoguePage> nextPages = DialogueParser.getInstance().buildDialogueChainFromPage(choice.getDialogueData(), choice.getNextPageId(), event.player);

							if (nextPages != null && !nextPages.isEmpty()) {
								if (event.player.getServer() != null) {
									event.player
										.getServer()
										.execute(() -> {
											createChainedDialogue(event.player, nextPages);
										});
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							if (event.player.getServer() != null) {
								event.player
									.getServer()
									.execute(() -> {
										event.player.sendMessage(new StringTextComponent("Error building next dialogue chain off-thread. See server logs."), event.player.getUUID());
									});
							}
						}
					});
			}
		} catch (Exception e) {
			e.printStackTrace();
			event.player.sendMessage(new StringTextComponent("Error initiating navigation to next dialogue. See server logs."), event.player.getUUID());
		}
	}

	private Dialogue buildChoiceDialogue(String title, String message, List<DialogueChoice> choices, boolean escapeCloses) {
		Dialogue.DialogueBuilder builder = Dialogue.builder().setName(title).setText(message);

		if (escapeCloses) {
			builder.escapeCloses();
		} else {
			builder.requireManualClose();
		}

		for (DialogueChoice choice : choices) {
			Choice pixelmonChoice = Choice.builder().setText(choice.getText()).setHandle(event -> handleChoiceSelection(event, choice)).build();
			builder.addChoice(pixelmonChoice);
		}

		return builder.build();
	}

	public void shutdown() {
		synchronized (poolLock) {
			ExecutorService currentPool = dialogueThreadPool;
			if (currentPool != null && !currentPool.isShutdown()) {
				currentPool.shutdown();
				try {
					if (!currentPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
						currentPool.shutdownNow();
						if (!currentPool.awaitTermination(800, TimeUnit.MILLISECONDS)) System.err.println("Dialogue thread pool did not terminate");
					}
				} catch (InterruptedException ie) {
					currentPool.shutdownNow();
					Thread.currentThread().interrupt();
				}
				System.out.println("DialogueManager thread pool shut down.");
			} else {
				System.out.println("DialogueManager thread pool already shut down or null.");
			}
		}
	}

	public static class DialogueChoice {

		private final String text;
		private final List<DialogueActionData> actions;
		private final DialogueActionData legacyAction;
		private final String nextPageId;
		private final DialogueFileData dialogueData;
		private final Map<String, ?> pageMap;
		private final Consumer<DialogueChoiceEvent> customAction;

		public DialogueChoice(String text, List<DialogueActionData> actions, DialogueActionData legacyAction, String nextPageId, DialogueFileData dialogueData, Map<String, ?> pageMap) {
			this.text = text;
			this.actions = actions;
			this.legacyAction = legacyAction;
			this.nextPageId = nextPageId;
			this.dialogueData = dialogueData;
			this.pageMap = pageMap;
			this.customAction = null;
		}

		public DialogueChoice(String text, Consumer<DialogueChoiceEvent> action) {
			this.text = text;
			this.customAction = action;
			this.actions = null;
			this.legacyAction = null;
			this.nextPageId = null;
			this.dialogueData = null;
			this.pageMap = null;
		}

		public String getText() {
			return text;
		}

		public List<DialogueActionData> getActions() {
			return actions;
		}

		public DialogueActionData getLegacyAction() {
			return legacyAction;
		}

		public String getNextPageId() {
			return nextPageId;
		}

		public DialogueFileData getDialogueData() {
			return dialogueData;
		}

		public Map<String, ?> getPageMap() {
			return pageMap;
		}

		public Consumer<DialogueChoiceEvent> getCustomAction() {
			return customAction;
		}
	}
}
