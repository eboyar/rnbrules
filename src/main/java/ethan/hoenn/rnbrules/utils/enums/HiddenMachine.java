package ethan.hoenn.rnbrules.utils.enums;

public enum HiddenMachine {
	CUT("cut"),
	FLY("fly"),
	SURF("surf"),
	STRENGTH("strength"),
	FLASH("flash"),
	ROCK_SMASH("rock_smash"),
	WATERFALL("waterfall"),
	DIVE("dive");

	private final String hmId;

	HiddenMachine(String hmId) {
		this.hmId = hmId;
	}

	public String getHmId() {
		return hmId;
	}

	public static HiddenMachine fromString(String hmId) {
		for (HiddenMachine hm : HiddenMachine.values()) {
			if (hm.getHmId().equalsIgnoreCase(hmId)) {
				return hm;
			}
		}
		return null;
	}
}
