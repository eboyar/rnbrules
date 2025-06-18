package ethan.hoenn.rnbrules.multiplayer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import ethan.hoenn.rnbrules.utils.managers.SettingsManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ChatHandler {

	private static final Set<String> PROFANE_WORDS = new HashSet<>();
	private static final List<String> PROFANE_WORDS_SORTED = new ArrayList<>();
	private static boolean profanityFilterInitialized = false;

	static {
		initializeProfanityFilter();
	}

	private static void initializeProfanityFilter() {
		try {
			InputStream inputStream = ChatHandler.class.getResourceAsStream("/profane.json");
			if (inputStream != null) {
				Gson gson = new Gson();
				Type listType = new TypeToken<List<String>>() {}.getType();
				List<String> profaneList = gson.fromJson(new InputStreamReader(inputStream), listType);

				for (String word : profaneList) {
					PROFANE_WORDS.add(word.toLowerCase());
				}

				PROFANE_WORDS_SORTED.addAll(PROFANE_WORDS);
				PROFANE_WORDS_SORTED.sort((a, b) -> Integer.compare(b.length(), a.length()));

				inputStream.close();
				profanityFilterInitialized = true;
				System.out.println("[RNBrules] Loaded " + PROFANE_WORDS.size() + " profane words for chat filter");
			} else {
				System.err.println("[RNBrules] Could not find profane.json file");
			}
		} catch (IOException e) {
			System.err.println("[RNBrules] Error loading profanity filter: " + e.getMessage());
		}
	}

	private static String filterProfanity(String message) {
		if (!profanityFilterInitialized || PROFANE_WORDS_SORTED.isEmpty()) {
			return message;
		}

		String lowerMessage = message.toLowerCase();
		String filteredMessage = message;

		for (String profaneWord : PROFANE_WORDS_SORTED) {
			if (lowerMessage.contains(profaneWord)) {
				StringBuilder replacement = new StringBuilder();
				for (int i = 0; i < profaneWord.length(); i++) {
					replacement.append("*");
				}

				String regex = "(?i)" + Pattern.quote(profaneWord);
				filteredMessage = filteredMessage.replaceAll(regex, replacement.toString());
				lowerMessage = filteredMessage.toLowerCase();
			}
		}

		return filteredMessage;
	}

	private static String formatTime(long milliseconds) {
		if (milliseconds <= 0) {
			return "0 seconds";
		}

		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		StringBuilder result = new StringBuilder();

		if (days > 0) {
			result.append(days).append(" day").append(days > 1 ? "s" : "");
		}
		if (hours % 24 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(hours % 24).append(" hour").append(hours % 24 > 1 ? "s" : "");
		}
		if (minutes % 60 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(minutes % 60).append(" minute").append(minutes % 60 > 1 ? "s" : "");
		}
		if (seconds % 60 > 0 && result.length() == 0) {
			result.append(seconds % 60).append(" second").append(seconds % 60 > 1 ? "s" : "");
		}

		return result.toString();
	}

	@SubscribeEvent
	public static void onPlayerChat(ServerChatEvent event) {
		ServerPlayerEntity sender = event.getPlayer();
		ProgressionManager progressionManager = ProgressionManager.get();
		SettingsManager settingsManager = SettingsManager.get();

		if (settingsManager != null && settingsManager.isPlayerMuted(sender.getUUID())) {
			event.setCanceled(true);
			String reason = settingsManager.getMuteReason(sender.getUUID());
			long expiry = settingsManager.getMuteExpiry(sender.getUUID());

			if (expiry == -1L) {
				sender.sendMessage(new StringTextComponent(TextFormatting.RED + "You are permanently muted. Reason: " + reason), sender.getUUID());
			} else {
				long timeLeft = expiry - System.currentTimeMillis();
				String timeLeftStr = formatTime(timeLeft);
				sender.sendMessage(new StringTextComponent(TextFormatting.RED + "You are muted for " + timeLeftStr + ". Reason: " + reason), sender.getUUID());
			}
			return;
		}

		event.setCanceled(true);

		if (progressionManager != null) {
			Rank senderRank = progressionManager.getPlayerRankObject(sender.getUUID());
			StaffRank senderStaffRank = progressionManager.getPlayerStaffRankObject(sender.getUUID());

			String formattedPrefix = senderRank.getFormattedPrefix();
			String convertedPrefix = convertColorCodes(formattedPrefix);

			String senderNameColor = "&f";
			if (senderStaffRank != null) {
				senderNameColor = senderStaffRank.getNameColor();
			}
			String convertedNameColor = convertColorCodes(senderNameColor);

			String originalMessage = event.getMessage();

			for (ServerPlayerEntity recipient : sender.getServer().getPlayerList().getPlayers()) {
				if (settingsManager != null && settingsManager.isPersonallyMuted(recipient.getUUID(), sender.getUUID())) {
					continue;
				}

				if (settingsManager != null && settingsManager.isMuteAllEnabled(recipient.getUUID())) {
					if (senderStaffRank == null || !senderStaffRank.equals(StaffRank.ADMIN)) {
						continue;
					}
				}

				String messageToSend = originalMessage;

				if (settingsManager != null && settingsManager.isProfanityFilterEnabled(recipient.getUUID())) {
					messageToSend = filterProfanity(originalMessage);
				}

				ITextComponent rankComponent = new StringTextComponent(convertedPrefix + " ");
				ITextComponent playerNameComponent = new StringTextComponent(convertedNameColor + sender.getDisplayName().getString());
				ITextComponent messageComponent = new StringTextComponent(convertColorCodes("&f: ") + messageToSend);

				ITextComponent finalMessage = rankComponent.copy().append(playerNameComponent).append(messageComponent);

				recipient.sendMessage(finalMessage, sender.getUUID());
			}
		}
	}

	private static String convertColorCodes(String text) {
		return text
			.replace("&0", TextFormatting.BLACK.toString())
			.replace("&1", TextFormatting.DARK_BLUE.toString())
			.replace("&2", TextFormatting.DARK_GREEN.toString())
			.replace("&3", TextFormatting.DARK_AQUA.toString())
			.replace("&4", TextFormatting.DARK_RED.toString())
			.replace("&5", TextFormatting.DARK_PURPLE.toString())
			.replace("&6", TextFormatting.GOLD.toString())
			.replace("&7", TextFormatting.GRAY.toString())
			.replace("&8", TextFormatting.DARK_GRAY.toString())
			.replace("&9", TextFormatting.BLUE.toString())
			.replace("&a", TextFormatting.GREEN.toString())
			.replace("&b", TextFormatting.AQUA.toString())
			.replace("&c", TextFormatting.RED.toString())
			.replace("&d", TextFormatting.LIGHT_PURPLE.toString())
			.replace("&e", TextFormatting.YELLOW.toString())
			.replace("&f", TextFormatting.WHITE.toString())
			.replace("&l", TextFormatting.BOLD.toString())
			.replace("&m", TextFormatting.STRIKETHROUGH.toString())
			.replace("&n", TextFormatting.UNDERLINE.toString())
			.replace("&o", TextFormatting.ITALIC.toString())
			.replace("&r", TextFormatting.RESET.toString());
	}
}
