package ethan.hoenn.rnbrules.utils.enums;

public enum Environment {
	RAIN("Rain"),
	SUN("Sun"),
	SANDSTORM("Sandstorm"),
	HAIL("Hail"),
	THUNDERSTORM("Thunderstorm"),
	TAILWIND("Tailwind"),
	MAGMA_STORM("MagmaStorm"),
	HEAT_CAVE("HeatCave"),
	PSYCHIC_TERRAIN("PsychicTerrain"),
	AURORA_VEIL("AuroraVeil"),
	AURORA_CAVE("AuroraCave"),
	NONE("NONE");

	private final String id;

	Environment(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static Environment fromString(String id) {
		for (Environment type : values()) {
			if (type.getId().equalsIgnoreCase(id)) {
				return type;
			}
		}
		return null;
	}
}
