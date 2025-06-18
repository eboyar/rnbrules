package ethan.hoenn.rnbrules.utils.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum FerryRoute {
	WESTERN_HOENN("Western Hoenn Route"),
	EASTERN_HOENN("Eastern Hoenn Route");

	private final String displayName;

	FerryRoute(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getRouteId() {
		return name().toLowerCase();
	}

	public List<FerryDestination> getDestinations() {
		return Arrays.stream(FerryDestination.values()).filter(destination -> destination.getRoute() == this).collect(Collectors.toList());
	}

	public static FerryRoute fromString(String routeId) {
		try {
			return valueOf(routeId.toUpperCase());
		} catch (IllegalArgumentException e) {
			for (FerryRoute route : values()) {
				if (route.getRouteId().equalsIgnoreCase(routeId)) {
					return route;
				}
			}
			return null;
		}
	}
}
