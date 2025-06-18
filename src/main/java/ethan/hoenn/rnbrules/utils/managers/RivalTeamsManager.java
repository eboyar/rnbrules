package ethan.hoenn.rnbrules.utils.managers;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.export.exception.PokemonImportException;
import com.pixelmonmod.pixelmon.api.util.PokePasteReader;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RivalTeamsManager {

	private static RivalTeamsManager instance;
	private ExecutorService executor;
	private static final Logger LOGGER = LogManager.getLogger("RNBRivalTeams");

	private final Map<Integer, Map<StarterSelectionManager.StarterChoice, List<Pokemon>>> cachedRivalTeams = new ConcurrentHashMap<>();

	private static final Map<Integer, Map<StarterSelectionManager.StarterChoice, String>> RIVAL_TEAM_PASTES = new HashMap<>();

	static {
		//oldale
		Map<StarterSelectionManager.StarterChoice, String> tier1Teams = new HashMap<>();
		tier1Teams.put(StarterSelectionManager.StarterChoice.TURTWIG, "https://pokepast.es/b9999f70363ab59c");
		tier1Teams.put(StarterSelectionManager.StarterChoice.CHIMCHAR, "https://pokepast.es/7932925bf51d04b6");
		tier1Teams.put(StarterSelectionManager.StarterChoice.PIPLUP, "https://pokepast.es/5c3460478bb60f8e");
		RIVAL_TEAM_PASTES.put(1, tier1Teams);

		//cycling road
		Map<StarterSelectionManager.StarterChoice, String> tier2Teams = new HashMap<>();
		tier2Teams.put(StarterSelectionManager.StarterChoice.TURTWIG, "https://pokepast.es/b2c934f4a22dd24f");
		tier2Teams.put(StarterSelectionManager.StarterChoice.CHIMCHAR, "https://pokepast.es/5958f2304129e67c");
		tier2Teams.put(StarterSelectionManager.StarterChoice.PIPLUP, "https://pokepast.es/178dbce8a9e937a8");
		RIVAL_TEAM_PASTES.put(2, tier2Teams);

		//route119
		Map<StarterSelectionManager.StarterChoice, String> tier3Teams = new HashMap<>();
		tier3Teams.put(StarterSelectionManager.StarterChoice.TURTWIG, "https://pokepast.es/ee9a7a74b8eee66b");
		tier3Teams.put(StarterSelectionManager.StarterChoice.CHIMCHAR, "https://pokepast.es/d2e78fb5a98f9117");
		tier3Teams.put(StarterSelectionManager.StarterChoice.PIPLUP, "https://pokepast.es/78a6d95f9af3b382");
		RIVAL_TEAM_PASTES.put(3, tier3Teams);

		//lilycove
		Map<StarterSelectionManager.StarterChoice, String> tier4Teams = new HashMap<>();
		tier4Teams.put(StarterSelectionManager.StarterChoice.TURTWIG, "https://pokepast.es/65dce3ac2b3bbef1");
		tier4Teams.put(StarterSelectionManager.StarterChoice.CHIMCHAR, "https://pokepast.es/747c65e6426031bc");
		tier4Teams.put(StarterSelectionManager.StarterChoice.PIPLUP, "https://pokepast.es/8649fe30cc1ee496");
		RIVAL_TEAM_PASTES.put(4, tier4Teams);
	}

	private RivalTeamsManager() {
		initExecutor();
	}

	private void initExecutor() {
		if (executor == null || executor.isShutdown()) {
			executor = Executors.newSingleThreadExecutor();
		}
	}

	public static RivalTeamsManager getInstance() {
		if (instance == null) {
			instance = new RivalTeamsManager();
		}
		return instance;
	}

	public void preloadAllTeams() {
		LOGGER.info("Preloading rival teams...");

		initExecutor();

		for (Map.Entry<Integer, Map<StarterSelectionManager.StarterChoice, String>> tierEntry : RIVAL_TEAM_PASTES.entrySet()) {
			int tier = tierEntry.getKey();
			Map<StarterSelectionManager.StarterChoice, String> teamUrls = tierEntry.getValue();

			for (Map.Entry<StarterSelectionManager.StarterChoice, String> teamEntry : teamUrls.entrySet()) {
				StarterSelectionManager.StarterChoice starter = teamEntry.getKey();
				String pasteUrl = teamEntry.getValue();

				loadTeamAsync(tier, starter, pasteUrl);
			}
		}
	}

	private void loadTeamAsync(int tier, StarterSelectionManager.StarterChoice starter, String pasteUrl) {
		initExecutor();

		CompletableFuture.supplyAsync(
			() -> {
				try {
					return PokePasteReader.from(pasteUrl).build();
				} catch (PokemonImportException e) {
					LOGGER.error("Failed to load rival team for tier {} and starter {}: {}", tier, starter, e.getMessage());
					return null;
				}
			},
			executor
		).thenAccept(team -> {
			if (team != null) {
				cachedRivalTeams.computeIfAbsent(tier, k -> new ConcurrentHashMap<>()).put(starter, team);
			}
		});
	}

	public List<Pokemon> getRivalTeam(int tier, StarterSelectionManager.StarterChoice playerStarter) {
		if (cachedRivalTeams.containsKey(tier) && cachedRivalTeams.get(tier).containsKey(playerStarter)) {
			return cachedRivalTeams.get(tier).get(playerStarter);
		}

		String pasteUrl = RIVAL_TEAM_PASTES.get(tier).get(playerStarter);
		try {
			List<Pokemon> team = PokePasteReader.from(pasteUrl).build();
			cachedRivalTeams.computeIfAbsent(tier, k -> new ConcurrentHashMap<>()).put(playerStarter, team);
			return team;
		} catch (PokemonImportException e) {
			LOGGER.error("Failed to load rival team on demand: {}", e.getMessage());
			return null;
		}
	}

	public boolean applyRivalTeam(NPCTrainer trainer, UUID playerUUID, int tier, StarterSelectionManager.StarterChoice playerStarter) {
		List<Pokemon> team = getRivalTeam(tier, playerStarter);

		if (team == null || team.isEmpty()) {
			LOGGER.error("Failed to get rival team for tier {} and starter {}", tier, playerStarter);
			return false;
		}

		for (int i = 0; i < team.size(); i++) {
			trainer.getPokemonStorage().set(i, team.get(i));
		}

		trainer.updateTrainerLevel();
		return true;
	}

	public void shutdown() {
		if (executor != null && !executor.isShutdown()) {
			try {
				executor.shutdown();
				if (!executor.awaitTermination(2, SECONDS)) {
					LOGGER.warn("Forcing RivalTeamsManager executor shutdown after timeout");
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				LOGGER.error("RivalTeamsManager shutdown interrupted", e);
				Thread.currentThread().interrupt();
			}
		}

		cachedRivalTeams.clear();
	}
}
