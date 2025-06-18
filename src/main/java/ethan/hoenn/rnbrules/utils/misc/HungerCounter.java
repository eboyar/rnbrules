package ethan.hoenn.rnbrules.utils.misc;

public class HungerCounter {

	private static int tick = 0;

	public static int incrementAndGetTick() {
		return ++tick;
	}

	public static void resetTick() {
		tick = 0;
	}
}
