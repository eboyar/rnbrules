/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.world.World;

public class WeatherCondition extends Condition<World> {

	public WeatherType weather = WeatherType.CLEAR;
	boolean invert = false;

	@Override
	public boolean conditionMet(World item) {
		return (WeatherType.get(item) == weather) != invert;
	}

	@Override
	public World itemFromPixelmon(PixelmonEntity entity) {
		return entity.level;
	}

	@Override
	public String toString() {
		return "WeatherCondition{" + "weather=" + weather.name() + ", invert=" + invert + '}';
	}
}
