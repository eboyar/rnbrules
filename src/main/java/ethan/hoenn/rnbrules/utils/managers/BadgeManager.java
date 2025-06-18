package ethan.hoenn.rnbrules.utils.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class BadgeManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "badges";
	private static final String PLAYER_BADGES_TAG = "player_badges";
	private static final String UUID_TAG = "uuid";
	private static final String BADGES_TAG = "badges";

	private static BadgeManager instance;
	private final Map<UUID, Set<String>> playerBadges = new HashMap<>();

	public BadgeManager() {
		super(DATA_TAG);
	}

	public static BadgeManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(BadgeManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = playerBadges.containsKey(playerUUID);
		if (hadData) {
			playerBadges.remove(playerUUID);
			this.setDirty();
		}
		return hadData;
	}

	public Set<String> getPlayerBadges(UUID uuid) {
		return Collections.unmodifiableSet(new HashSet<>(playerBadges.getOrDefault(uuid, new HashSet<>())));
	}

	public boolean addBadge(UUID uuid, String badge) {
		boolean added = playerBadges.computeIfAbsent(uuid, k -> new HashSet<>()).add(badge);
		if (added) {
			this.setDirty();
		}
		return added;
	}

	public boolean hasBadge(UUID uuid, String badge) {
		Set<String> badges = playerBadges.get(uuid);
		return badges != null && badges.contains(badge);
	}

	public boolean removeBadge(UUID uuid, String badge) {
		Set<String> badges = playerBadges.get(uuid);
		if (badges != null && badges.remove(badge)) {
			this.setDirty();
			return true;
		}
		return false;
	}

	public boolean resetBadges(UUID uuid) {
		if (playerBadges.containsKey(uuid)) {
			playerBadges.remove(uuid);
			this.setDirty();
			return true;
		}
		return false;
	}

	public int getBadgeCount(UUID uuid) {
		Set<String> badges = playerBadges.get(uuid);
		return badges != null ? badges.size() : 0;
	}

	@Override
	public void load(CompoundNBT nbt) {
		playerBadges.clear();
		ListNBT badgesList = nbt.getList(PLAYER_BADGES_TAG, 10);

		for (int i = 0; i < badgesList.size(); i++) {
			CompoundNBT playerData = badgesList.getCompound(i);
			UUID uuid = UUID.fromString(playerData.getString(UUID_TAG));
			Set<String> badges = new HashSet<>();
			ListNBT badgesNBT = playerData.getList(BADGES_TAG, 8);

			for (int j = 0; j < badgesNBT.size(); j++) {
				badges.add(badgesNBT.getString(j));
			}

			playerBadges.put(uuid, badges);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		ListNBT badgesList = new ListNBT();

		for (Map.Entry<UUID, Set<String>> entry : playerBadges.entrySet()) {
			CompoundNBT playerData = new CompoundNBT();
			playerData.putString(UUID_TAG, entry.getKey().toString());

			ListNBT badgesNBT = new ListNBT();
			for (String badge : entry.getValue()) {
				badgesNBT.add(StringNBT.valueOf(badge));
			}

			playerData.put(BADGES_TAG, badgesNBT);
			badgesList.add(playerData);
		}

		nbt.put(PLAYER_BADGES_TAG, badgesList);
		return nbt;
	}
}
