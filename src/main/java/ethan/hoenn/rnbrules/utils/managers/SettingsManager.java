package ethan.hoenn.rnbrules.utils.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class SettingsManager extends WorldSavedData {

	private static final String DATA_TAG = "settings";
	private static SettingsManager instance;

	private static final Map<UUID, Boolean> profanityFilterEnabled = new HashMap<>();
	private static final Map<UUID, Boolean> muteAllEnabled = new HashMap<>();
	private static final Map<UUID, Long> mutedPlayers = new HashMap<>();
	private static final Map<UUID, Long> tempBannedPlayers = new HashMap<>();
	private static final Map<UUID, String> muteReasons = new HashMap<>();
	private static final Map<UUID, String> tempBanReasons = new HashMap<>();
	private static final Map<UUID, Set<UUID>> personalMutes = new HashMap<>();

	public SettingsManager() {
		super(DATA_TAG);
	}

	public static void init(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(SettingsManager::new, DATA_TAG);
		}
	}

	public static SettingsManager get() {
		return instance;
	}

	public void setProfanityFilterEnabled(UUID playerUUID, boolean enabled) {
		profanityFilterEnabled.put(playerUUID, enabled);
		this.setDirty();
	}

	public boolean isProfanityFilterEnabled(UUID playerUUID) {
		return profanityFilterEnabled.getOrDefault(playerUUID, true);
	}

	public boolean toggleProfanityFilter(UUID playerUUID) {
		boolean currentSetting = isProfanityFilterEnabled(playerUUID);
		boolean newSetting = !currentSetting;
		setProfanityFilterEnabled(playerUUID, newSetting);
		return newSetting;
	}

	public void setMuteAllEnabled(UUID playerUUID, boolean enabled) {
		muteAllEnabled.put(playerUUID, enabled);
		this.setDirty();
	}

	public boolean isMuteAllEnabled(UUID playerUUID) {
		return muteAllEnabled.getOrDefault(playerUUID, false);
	}

	public boolean toggleMuteAll(UUID playerUUID) {
		boolean currentSetting = isMuteAllEnabled(playerUUID);
		boolean newSetting = !currentSetting;
		setMuteAllEnabled(playerUUID, newSetting);
		return newSetting;
	}

	public void mutePlayer(UUID playerUUID, String reason, long expiryTime) {
		mutedPlayers.put(playerUUID, expiryTime);
		muteReasons.put(playerUUID, reason);
		this.setDirty();
	}

	public void mutePlayerPermanent(UUID playerUUID, String reason) {
		mutedPlayers.put(playerUUID, -1L);
		muteReasons.put(playerUUID, reason);
		this.setDirty();
	}

	public void unmutePlayer(UUID playerUUID) {
		mutedPlayers.remove(playerUUID);
		muteReasons.remove(playerUUID);
		this.setDirty();
	}

	public boolean isPlayerMuted(UUID playerUUID) {
		Long expiryTime = mutedPlayers.get(playerUUID);
		if (expiryTime == null) {
			return false;
		}

		if (expiryTime == -1L) {
			return true;
		}

		if (System.currentTimeMillis() > expiryTime) {
			unmutePlayer(playerUUID);
			return false;
		}

		return true;
	}

	public String getMuteReason(UUID playerUUID) {
		return muteReasons.get(playerUUID);
	}

	public long getMuteExpiry(UUID playerUUID) {
		return mutedPlayers.getOrDefault(playerUUID, 0L);
	}

	public void tempBanPlayer(UUID playerUUID, String reason, long expiryTime) {
		tempBannedPlayers.put(playerUUID, expiryTime);
		tempBanReasons.put(playerUUID, reason);
		this.setDirty();
	}

	public void removeTempBan(UUID playerUUID) {
		tempBannedPlayers.remove(playerUUID);
		tempBanReasons.remove(playerUUID);
		this.setDirty();
	}

	public boolean isPlayerTempBanned(UUID playerUUID) {
		Long expiryTime = tempBannedPlayers.get(playerUUID);
		if (expiryTime == null) {
			return false;
		}

		if (System.currentTimeMillis() > expiryTime) {
			removeTempBan(playerUUID);
			return false;
		}

		return true;
	}

	public String getTempBanReason(UUID playerUUID) {
		return tempBanReasons.get(playerUUID);
	}

	public long getTempBanExpiry(UUID playerUUID) {
		return tempBannedPlayers.getOrDefault(playerUUID, 0L);
	}

	public static long parseTimeString(String timeString) {
		if (timeString == null || timeString.isEmpty()) {
			return 0;
		}

		long totalMilliseconds = 0;
		StringBuilder numberBuilder = new StringBuilder();

		for (char c : timeString.toLowerCase().toCharArray()) {
			if (Character.isDigit(c)) {
				numberBuilder.append(c);
			} else if (c == 'd' || c == 'h' || c == 'm') {
				if (numberBuilder.length() > 0) {
					int number = Integer.parseInt(numberBuilder.toString());
					switch (c) {
						case 'd':
							totalMilliseconds += number * 24 * 60 * 60 * 1000L;
							break;
						case 'h':
							totalMilliseconds += number * 60 * 60 * 1000L;
							break;
						case 'm':
							totalMilliseconds += number * 60 * 1000L;
							break;
					}
					numberBuilder.setLength(0);
				}
			}
		}
		return totalMilliseconds;
	}

	public void addPersonalMute(UUID mutingPlayer, UUID mutedPlayer) {
		personalMutes.computeIfAbsent(mutingPlayer, k -> new HashSet<>()).add(mutedPlayer);
		this.setDirty();
	}

	public void removePersonalMute(UUID mutingPlayer, UUID mutedPlayer) {
		Set<UUID> mutedSet = personalMutes.get(mutingPlayer);
		if (mutedSet != null) {
			mutedSet.remove(mutedPlayer);
			if (mutedSet.isEmpty()) {
				personalMutes.remove(mutingPlayer);
			}
			this.setDirty();
		}
	}

	public boolean isPersonallyMuted(UUID mutingPlayer, UUID mutedPlayer) {
		Set<UUID> mutedSet = personalMutes.get(mutingPlayer);
		return mutedSet != null && mutedSet.contains(mutedPlayer);
	}

	public boolean togglePersonalMute(UUID mutingPlayer, UUID mutedPlayer) {
		if (isPersonallyMuted(mutingPlayer, mutedPlayer)) {
			removePersonalMute(mutingPlayer, mutedPlayer);
			return false;
		} else {
			addPersonalMute(mutingPlayer, mutedPlayer);
			return true;
		}
	}

	public Set<UUID> getPersonalMutes(UUID mutingPlayer) {
		return personalMutes.getOrDefault(mutingPlayer, new HashSet<>());
	}

	@Override
	public void load(CompoundNBT nbt) {
		profanityFilterEnabled.clear();
		muteAllEnabled.clear();
		mutedPlayers.clear();
		muteReasons.clear();
		tempBannedPlayers.clear();
		tempBanReasons.clear();
		personalMutes.clear();

		if (nbt.contains("ProfanityFilterSettings")) {
			CompoundNBT filterNBT = nbt.getCompound("ProfanityFilterSettings");
			for (String playerUUIDStr : filterNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				boolean enabled = filterNBT.getBoolean(playerUUIDStr);
				profanityFilterEnabled.put(playerUUID, enabled);
			}
		}

		if (nbt.contains("MuteAllSettings")) {
			CompoundNBT muteAllNBT = nbt.getCompound("MuteAllSettings");
			for (String playerUUIDStr : muteAllNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				boolean enabled = muteAllNBT.getBoolean(playerUUIDStr);
				muteAllEnabled.put(playerUUID, enabled);
			}
		}

		if (nbt.contains("MutedPlayers")) {
			CompoundNBT muteNBT = nbt.getCompound("MutedPlayers");
			for (String playerUUIDStr : muteNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				long expiryTime = muteNBT.getLong(playerUUIDStr);
				mutedPlayers.put(playerUUID, expiryTime);
			}
		}

		if (nbt.contains("MuteReasons")) {
			CompoundNBT reasonNBT = nbt.getCompound("MuteReasons");
			for (String playerUUIDStr : reasonNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				String reason = reasonNBT.getString(playerUUIDStr);
				muteReasons.put(playerUUID, reason);
			}
		}

		if (nbt.contains("TempBannedPlayers")) {
			CompoundNBT tempBanNBT = nbt.getCompound("TempBannedPlayers");
			for (String playerUUIDStr : tempBanNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				long expiryTime = tempBanNBT.getLong(playerUUIDStr);
				tempBannedPlayers.put(playerUUID, expiryTime);
			}
		}

		if (nbt.contains("TempBanReasons")) {
			CompoundNBT reasonNBT = nbt.getCompound("TempBanReasons");
			for (String playerUUIDStr : reasonNBT.getAllKeys()) {
				UUID playerUUID = UUID.fromString(playerUUIDStr);
				String reason = reasonNBT.getString(playerUUIDStr);
				tempBanReasons.put(playerUUID, reason);
			}
		}

		if (nbt.contains("PersonalMutes")) {
			CompoundNBT personalMuteNBT = nbt.getCompound("PersonalMutes");
			for (String mutingPlayerStr : personalMuteNBT.getAllKeys()) {
				UUID mutingPlayer = UUID.fromString(mutingPlayerStr);
				CompoundNBT mutedPlayersNBT = personalMuteNBT.getCompound(mutingPlayerStr);

				Set<UUID> mutedSet = new HashSet<>();
				for (String mutedPlayerStr : mutedPlayersNBT.getAllKeys()) {
					if (mutedPlayersNBT.getBoolean(mutedPlayerStr)) {
						mutedSet.add(UUID.fromString(mutedPlayerStr));
					}
				}
				if (!mutedSet.isEmpty()) {
					personalMutes.put(mutingPlayer, mutedSet);
				}
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT filterNBT = new CompoundNBT();
		for (Map.Entry<UUID, Boolean> entry : profanityFilterEnabled.entrySet()) {
			filterNBT.putBoolean(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("ProfanityFilterSettings", filterNBT);

		CompoundNBT muteAllNBT = new CompoundNBT();
		for (Map.Entry<UUID, Boolean> entry : muteAllEnabled.entrySet()) {
			muteAllNBT.putBoolean(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("MuteAllSettings", muteAllNBT);

		CompoundNBT muteNBT = new CompoundNBT();
		for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
			muteNBT.putLong(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("MutedPlayers", muteNBT);

		CompoundNBT muteReasonNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : muteReasons.entrySet()) {
			muteReasonNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("MuteReasons", muteReasonNBT);

		CompoundNBT tempBanNBT = new CompoundNBT();
		for (Map.Entry<UUID, Long> entry : tempBannedPlayers.entrySet()) {
			tempBanNBT.putLong(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("TempBannedPlayers", tempBanNBT);

		CompoundNBT tempBanReasonNBT = new CompoundNBT();
		for (Map.Entry<UUID, String> entry : tempBanReasons.entrySet()) {
			tempBanReasonNBT.putString(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("TempBanReasons", tempBanReasonNBT);

		CompoundNBT personalMuteNBT = new CompoundNBT();
		for (Map.Entry<UUID, Set<UUID>> entry : personalMutes.entrySet()) {
			CompoundNBT mutedPlayersNBT = new CompoundNBT();
			for (UUID mutedPlayer : entry.getValue()) {
				mutedPlayersNBT.putBoolean(mutedPlayer.toString(), true);
			}
			personalMuteNBT.put(entry.getKey().toString(), mutedPlayersNBT);
		}
		nbt.put("PersonalMutes", personalMuteNBT);

		return nbt;
	}
}
