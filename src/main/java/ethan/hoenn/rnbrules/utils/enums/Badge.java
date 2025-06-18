package ethan.hoenn.rnbrules.utils.enums;

public enum Badge {
	STONE_BADGE("stone_badge"),
	KNUCKLE_BADGE("knuckle_badge"),
	DYNAMO_BADGE("dynamo_badge"),
	BALANCE_BADGE("balance_badge"),
	HEAT_BADGE("heat_badge"),
	FEATHER_BADGE("feather_badge"),
	MIND_BADGE("mind_badge"),
	RAIN_BADGE("rain_badge");

	private final String badgeId;

	Badge(String badgeId) {
		this.badgeId = badgeId;
	}

	public String getBadgeId() {
		return badgeId;
	}

	public static Badge fromString(String badgeId) {
		for (Badge badge : Badge.values()) {
			if (badge.getBadgeId().equalsIgnoreCase(badgeId)) {
				return badge;
			}
		}
		return null;
	}
}
