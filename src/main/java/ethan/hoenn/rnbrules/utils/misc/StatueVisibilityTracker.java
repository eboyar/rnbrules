package ethan.hoenn.rnbrules.utils.misc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StatueVisibilityTracker {

	private static final Set<UUID> HIDDEN_STATUES = new HashSet<>();

	public static void hideStatue(UUID statueUUID) {
		if (statueUUID != null) {
			HIDDEN_STATUES.add(statueUUID);
		}
	}

	public static void showStatue(UUID statueUUID) {
		if (statueUUID != null) {
			HIDDEN_STATUES.remove(statueUUID);
		}
	}

	public static boolean shouldRenderStatue(UUID statueUUID) {
		return statueUUID == null || !HIDDEN_STATUES.contains(statueUUID);
	}

	public static void clear() {
		HIDDEN_STATUES.clear();
	}
}
