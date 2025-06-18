package ethan.hoenn.rnbrules.utils.managers;

import java.util.UUID;

/**
 * Interface for managers that can reset player-specific data.
 * All managers that store player data should implement this interface
 * to allow for centralized player data reset functionality.
 */
public interface ResetableManager {
	/**
	 * Resets all data associated with the specified player UUID.
	 * This should remove all player-specific data from the manager
	 * and mark the manager as dirty for saving.
	 *
	 * @param playerUUID The UUID of the player whose data should be reset
	 * @return true if data was found and reset, false if no data existed for this player
	 */
	boolean resetPlayerData(UUID playerUUID);
}
