package ethan.hoenn.rnbrules.multiplayer;

import java.util.Objects;

public class Rank {

	private final String id;
	private final String displayName;
	private final String prefix;
	private final int priority;
	private final String colorCode;
	private final int badgeCount;
	private final boolean showBadgeCount;

	public Rank(String id, String displayName, String prefix, int priority, String colorCode, int badgeCount, boolean showBadgeCount) {
		this.id = id;
		this.displayName = displayName;
		this.prefix = prefix;
		this.priority = priority;
		this.colorCode = colorCode;
		this.badgeCount = badgeCount;
		this.showBadgeCount = showBadgeCount;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPrefix() {
		return prefix;
	}

	public int getPriority() {
		return priority;
	}

	public String getColorCode() {
		return colorCode;
	}

	public int getBadgeCount() {
		return badgeCount;
	}

	public boolean showBadgeCount() {
		return showBadgeCount;
	}

	public String getFormattedPrefix() {
		StringBuilder formatted = new StringBuilder();

		if (showBadgeCount) {
			formatted.append("&7(").append(badgeCount).append(") [");
		} else {
			formatted.append("&7[");
		}

		formatted.append(colorCode).append(displayName).append("&7]");

		return formatted.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Rank rank = (Rank) obj;
		return Objects.equals(id, rank.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return (
			"Rank{" +
			"id='" +
			id +
			'\'' +
			", displayName='" +
			displayName +
			'\'' +
			", prefix='" +
			prefix +
			'\'' +
			", priority=" +
			priority +
			", colorCode='" +
			colorCode +
			'\'' +
			", badgeCount=" +
			badgeCount +
			", showBadgeCount=" +
			showBadgeCount +
			'}'
		);
	}

	public static final Rank NEWCOMER = new Rank("newcomer", "Newcomer", "newcomer", 0, "&2", 0, false);
	public static final Rank NOVICE = new Rank("novice", "Novice", "novice", 10, "&a", 1, true);
	public static final Rank ROOKIE = new Rank("rookie", "Rookie", "rookie", 20, "&3", 2, true);
	public static final Rank JUNIOR = new Rank("junior", "Junior", "junior", 30, "&e", 3, true);
	public static final Rank ADEPT = new Rank("adept", "Adept", "adept", 40, "&b", 4, true);
	public static final Rank ACE = new Rank("ace", "Ace", "ace", 50, "&c", 5, true);
	public static final Rank VETERAN = new Rank("veteran", "Veteran", "veteran", 60, "&f", 6, true);
	public static final Rank EXPERT = new Rank("expert", "Expert", "expert", 70, "&d", 7, true);
	public static final Rank MASTER = new Rank("master", "Master", "master", 80, "&9", 8, true);
	public static final Rank CHAMPION = new Rank("champion", "Champion", "champion", 90, "&6&l", 9, false);
}
