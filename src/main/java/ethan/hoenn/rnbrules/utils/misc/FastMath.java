package ethan.hoenn.rnbrules.utils.misc;

public final class FastMath {

	private FastMath() {}

	public static float fastAtan2(float y, float x) {
		if (x == 0.0f) {
			return (y > 0.0f) ? (float) Math.PI / 2 : (y == 0.0f ? 0.0f : -(float) Math.PI / 2);
		}
		float atan;
		float z = y / x;
		if (Math.abs(z) < 1.0f) {
			atan = z / (1.0f + 0.28f * z * z);
			if (x < 0.0f) {
				return (y < 0.0f) ? atan - (float) Math.PI : atan + (float) Math.PI;
			}
		} else {
			atan = (float) Math.PI / 2 - z / (z * z + 0.28f);
			if (y < 0.0f) {
				return atan - (float) Math.PI;
			}
		}
		return atan;
	}

	public static double fastAtan2(double y, double x) {
		if (x == 0.0) {
			return (y > 0.0) ? Math.PI / 2 : (y == 0.0 ? 0.0 : -Math.PI / 2);
		}
		double atan;
		double z = y / x;
		if (Math.abs(z) < 1.0) {
			atan = z / (1.0 + 0.28 * z * z);
			if (x < 0.0) {
				return (y < 0.0) ? atan - Math.PI : atan + Math.PI;
			}
		} else {
			atan = Math.PI / 2 - z / (z * z + 0.28);
			if (y < 0.0) {
				return atan - Math.PI;
			}
		}
		return atan;
	}
}
