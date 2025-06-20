package ethan.hoenn.rnbrules.utils.managers;

import java.util.UUID;

/**
 * Interface for managers that can reset player-specific data.
 * All managers that store player data should implement this interface
 * to allow for centralized player data reset functionality.
 */
public interface ResetableManager {
	boolean resetPlayerData(UUID playerUUID);
}
