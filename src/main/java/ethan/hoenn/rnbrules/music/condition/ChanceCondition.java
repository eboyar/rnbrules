/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.util.concurrent.ThreadLocalRandom;

public class ChanceCondition extends Condition<Void> {

	public double chance = 1.0;

	@Override
	protected boolean conditionMet(Void item) {
		return ThreadLocalRandom.current().nextDouble() >= chance;
	}

	@Override
	public Void itemFromPixelmon(PixelmonEntity entity) {
		return null;
	}
}
