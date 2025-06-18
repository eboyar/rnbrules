/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.managers.ClientLocationManager;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;

public class LocationCondition extends Condition<String> {

	public String name;

	public LocationCondition() {
		this.type = "location";
	}

	@Override
	protected boolean conditionMet(String playerLocation) {
		if (playerLocation == null || name == null) {
			return false;
		}

		String normalizedPlayerLocation = LocationManager.normalizeLocationName(playerLocation);
		String normalizedConditionLocation = LocationManager.normalizeLocationName(name);

		return normalizedPlayerLocation.equals(normalizedConditionLocation);
	}

	@Override
	public String itemFromPixelmon(PixelmonEntity entity) {
		ClientLocationManager locationManager = ClientLocationManager.getInstance();
		return locationManager.getCurrentPlayerLocation();
	}

	@Override
	public String toString() {
		return "LocationCondition{" + "name='" + name + '\'' + '}';
	}
}
