package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleTickEvent;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStats;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.status.AuroraVeil;
import com.pixelmonmod.pixelmon.battles.status.LightScreen;
import com.pixelmonmod.pixelmon.battles.status.Reflect;
import com.pixelmonmod.pixelmon.battles.status.Spikes;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.battles.status.StealthRock;
import com.pixelmonmod.pixelmon.battles.status.StickyWeb;
import com.pixelmonmod.pixelmon.battles.status.Tailwind;
import com.pixelmonmod.pixelmon.battles.status.ToxicSpikes;
import ethan.hoenn.rnbrules.network.BattleInfoPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.status.CustomAuroraVeil;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;

public class BattleInfoListener {

	private final Map<Integer, BattleCache> battleCaches = new ConcurrentHashMap<>();
	private final StringBuilder sharedStringBuilder = new StringBuilder(100);
	private final ArrayList<TrainerParticipant> enemyTrainersPool = new ArrayList<>(2);
	private final ArrayList<TrainerParticipant> allyTrainersPool = new ArrayList<>(1);
	private final ArrayList<PixelmonWrapper> enemyPokemonPool = new ArrayList<>(2);
	private final ArrayList<PixelmonWrapper> allyPokemonPool = new ArrayList<>(2);

	private final String[] sharedEnemyStatsArray = { "", "" };
	private final Boolean[] sharedEnemyIsMegaArray = { false, false };
	private final String[] sharedAllyStatsArray = { "", "" };
	private final Boolean[] sharedAllyIsMegaArray = { false, false };

	private static final BattleInfoPacket EMPTY_PACKET = new BattleInfoPacket("", "", false, false, false, "", "", "", "", "");
	private static final int BASE_TICK_RATE = 5;
	private static final int MAX_TICK_RATE = 20;
	private static final int BATTLES_PER_TICK_INCREASE = 8;

	private static final int LIGHT_SCREEN_FLAG = 1;
	private static final int REFLECT_FLAG = 2;
	private static final int AURORA_VEIL_FLAG = 4;
	private static final int TAILWIND_FLAG = 8;
	private static final int STEALTH_ROCK_FLAG = 16;
	private static final int STICKY_WEB_FLAG = 32;

	private static final String STEALTH_ROCK_STR = "Stealth Rock;";
	private static final String STICKY_WEB_STR = "Sticky Web;";
	private static final String LIGHT_SCREEN_STR = "Light Screen;";
	private static final String REFLECT_STR = "Reflect;";
	private static final String AURORA_VEIL_STR = "Aurora Veil;";
	private static final String TAILWIND_STR = "Tailwind;";

	private static final Map<String, String> ENV_NAME_CACHE = new HashMap<>();

	static {
		ENV_NAME_CACHE.put("MagmaStorm", "Magma Storm");
		ENV_NAME_CACHE.put("PsychicTerrain", "Psychic Terrain");
		ENV_NAME_CACHE.put("AuroraVeil", "");
		ENV_NAME_CACHE.put("Tailwind", "");
	}

	@SubscribeEvent
	public void onBattleTick(BattleTickEvent event) {
		BattleController bc = event.getBattleController();
		BattleCache cache = battleCaches.computeIfAbsent(bc.battleIndex, id -> new BattleCache());

		int currentTickRate = calculateDynamicTickRate();
		cache.tickCounter++;
		if (cache.tickCounter < currentTickRate) return;
		cache.tickCounter = 0;

		List<BattleParticipant> bps = bc.participants;
		if (bps == null || bps.size() < 2) return;

		PlayerParticipant pp = null;
		enemyTrainersPool.clear();
		allyTrainersPool.clear();

		for (BattleParticipant bp : bps) {
			if (bp instanceof PlayerParticipant) {
				pp = (PlayerParticipant) bp;
				break;
			}
		}

		if (pp == null) return;

		int playerTeam = pp.team;

		for (BattleParticipant bp : bc.participants) {
			if (bp instanceof TrainerParticipant) {
				TrainerParticipant tp = (TrainerParticipant) bp;
				if (tp.team == playerTeam) {
					allyTrainersPool.add(tp);
				} else {
					enemyTrainersPool.add(tp);
				}
			}
		}

		if (enemyTrainersPool.isEmpty()) return;

		ServerPlayerEntity player = pp.player;
		ArrayList<PixelmonWrapper> actives = bc.getActivePokemon();
		if (actives.size() < 2) return;

		BattleState currentState = cache.currentState;
		resetBattleState(currentState);

		boolean isTagBattle = !allyTrainersPool.isEmpty();
		boolean isLinkedBattle = enemyTrainersPool.size() > 1;

		boolean forceDoubleBattle = isTagBattle || isLinkedBattle;
		currentState.isDoubleBattle = forceDoubleBattle || actives.size() > 2;

		currentState.playerName = pp.player.getName().getString();
		currentState.isTagBattle = isTagBattle;
		currentState.isLinkedBattle = isLinkedBattle;

		TrainerParticipant firstEnemyTrainer = enemyTrainersPool.get(0);
		currentState.trainerName = firstEnemyTrainer.trainer.getName().getString();

		if (isLinkedBattle && enemyTrainersPool.size() > 1) {
			currentState.secondEnemyTrainerName = enemyTrainersPool.get(1).trainer.getName().getString();
		}

		if (isTagBattle) {
			currentState.allyTrainerName = allyTrainersPool.get(0).trainer.getName().getString();
		}

		String rawEnvironment = firstEnemyTrainer.trainer.getPersistentData().getString("Environment");
		currentState.environment = normalizeEnvironmentName(rawEnvironment);

		if (currentState.isDoubleBattle) {
			processDoubleBattle(actives, currentState, playerTeam, isTagBattle, isLinkedBattle);
		} else {
			processSingleBattle(actives, currentState);
		}

		if (!currentState.equals(cache.lastState)) {
			sendBattleInfoPacket(player, currentState);
			copyBattleState(currentState, cache.lastState);
		}
	}

	private int calculateDynamicTickRate() {
		int battleCount = battleCaches.size();
		int tickRate = BASE_TICK_RATE + (battleCount / BATTLES_PER_TICK_INCREASE);
		return Math.min(tickRate, MAX_TICK_RATE);
	}

	private void resetBattleState(BattleState state) {
		state.isDoubleBattle = false;
		state.isTagBattle = false;
		state.isLinkedBattle = false;
		state.environment = "";
		state.playerName = "";
		state.trainerName = "";
		state.secondEnemyTrainerName = "";
		state.allyTrainerName = "";
		state.playerStatuses = "";
		state.trainerStatuses = "";
		state.secondEnemyTrainerStatuses = "";
		state.allyTrainerStatuses = "";
		state.enemyStats = "";
		state.allyStats = "";
		state.enemyIsMega = false;
		state.allyIsMega = false;

		Arrays.fill(sharedEnemyStatsArray, "");
		Arrays.fill(sharedEnemyIsMegaArray, false);
		Arrays.fill(sharedAllyStatsArray, "");
		Arrays.fill(sharedAllyIsMegaArray, false);
	}

	private void copyBattleState(BattleState source, BattleState target) {
		target.isDoubleBattle = source.isDoubleBattle;
		target.isTagBattle = source.isTagBattle;
		target.isLinkedBattle = source.isLinkedBattle;
		target.environment = source.environment;
		target.playerName = source.playerName;
		target.trainerName = source.trainerName;
		target.secondEnemyTrainerName = source.secondEnemyTrainerName;
		target.allyTrainerName = source.allyTrainerName;
		target.playerStatuses = source.playerStatuses;
		target.trainerStatuses = source.trainerStatuses;
		target.secondEnemyTrainerStatuses = source.secondEnemyTrainerStatuses;
		target.allyTrainerStatuses = source.allyTrainerStatuses;

		if (source.isDoubleBattle) {
			target.enemyStatsList = new ArrayList<>(source.enemyStatsList);
			target.enemyIsMegaList = new ArrayList<>(source.enemyIsMegaList);
			target.allyStatsList = new ArrayList<>(source.allyStatsList);
			target.allyIsMegaList = new ArrayList<>(source.allyIsMegaList);
		} else {
			target.enemyStats = source.enemyStats;
			target.allyStats = source.allyStats;
			target.enemyIsMega = source.enemyIsMega;
			target.allyIsMega = source.allyIsMega;
		}
	}

	private void processDoubleBattle(ArrayList<PixelmonWrapper> actives, BattleState state, int playerTeam, boolean isTagBattle, boolean isLinkedBattle) {
		Arrays.fill(sharedEnemyStatsArray, "");
		Arrays.fill(sharedEnemyIsMegaArray, false);
		Arrays.fill(sharedAllyStatsArray, "");
		Arrays.fill(sharedAllyIsMegaArray, false);

		state.enemyStatsList = Arrays.asList(sharedEnemyStatsArray);
		state.enemyIsMegaList = Arrays.asList(sharedEnemyIsMegaArray);
		state.allyStatsList = Arrays.asList(sharedAllyStatsArray);
		state.allyIsMegaList = Arrays.asList(sharedAllyIsMegaArray);

		boolean processedPlayerStatus = false;
		boolean processedTrainerStatus = false;
		boolean processedSecondEnemyTrainerStatus = false;
		boolean processedAllyTrainerStatus = false;

		enemyPokemonPool.clear();
		allyPokemonPool.clear();

		for (PixelmonWrapper wrapper : actives) {
			int participantTeam = wrapper.getParticipant().team;
			if (participantTeam == playerTeam) {
				allyPokemonPool.add(wrapper);
			} else {
				enemyPokemonPool.add(wrapper);
			}
		}

		if (isTagBattle) {
			for (PixelmonWrapper wrapper : allyPokemonPool) {
				boolean isPlayerOwned = wrapper.getPlayerOwner() != null;
				int position = isPlayerOwned ? 1 : 0;

				BattleStats wrapperStats = wrapper.getBattleStats();
				sharedAllyStatsArray[position] = formatStats(wrapperStats);
				sharedAllyIsMegaArray[position] = wrapper.isMega;

				if (isPlayerOwned && !processedPlayerStatus) {
					state.playerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedPlayerStatus = true;
				} else if (!isPlayerOwned && !processedAllyTrainerStatus) {
					state.allyTrainerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedAllyTrainerStatus = true;
				}
			}
		} else {
			for (PixelmonWrapper wrapper : allyPokemonPool) {
				int position = wrapper.battlePosition;

				if (position < 0 || position >= 2) continue;

				int displayPosition = position == 0 ? 1 : 0;

				BattleStats stats = wrapper.getBattleStats();
				sharedAllyStatsArray[displayPosition] = formatStats(stats);
				sharedAllyIsMegaArray[displayPosition] = wrapper.isMega;

				if (!processedPlayerStatus) {
					state.playerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedPlayerStatus = true;
				}
			}
		}

		if (isLinkedBattle) {
			TrainerParticipant firstEnemyTrainer = null;
			TrainerParticipant secondEnemyTrainer = null;

			if (enemyTrainersPool.size() >= 2) {
				firstEnemyTrainer = enemyTrainersPool.get(0);
				secondEnemyTrainer = enemyTrainersPool.get(1);
			} else if (!enemyTrainersPool.isEmpty()) {
				firstEnemyTrainer = enemyTrainersPool.get(0);
			}

			for (PixelmonWrapper wrapper : enemyPokemonPool) {
				int position = 0;

				if (firstEnemyTrainer != null && secondEnemyTrainer != null) {
					for (PixelmonWrapper trainerPokemon : secondEnemyTrainer.controlledPokemon) {
						if (trainerPokemon == wrapper) {
							position = 1;
							break;
						}
					}
				}

				BattleStats wrapperStats = wrapper.getBattleStats();
				sharedEnemyStatsArray[position] = formatStats(wrapperStats);
				sharedEnemyIsMegaArray[position] = wrapper.isMega;

				if (position == 0 && !processedTrainerStatus) {
					state.trainerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedTrainerStatus = true;
				} else if (position == 1 && !processedSecondEnemyTrainerStatus) {
					state.secondEnemyTrainerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedSecondEnemyTrainerStatus = true;
				}
			}
		} else {
			for (PixelmonWrapper wrapper : enemyPokemonPool) {
				int position = wrapper.battlePosition;

				if (position < 0 || position >= 2) continue;

				sharedEnemyStatsArray[position] = formatStats(wrapper.getBattleStats());
				sharedEnemyIsMegaArray[position] = wrapper.isMega;

				if (!processedTrainerStatus) {
					state.trainerStatuses = formatTeamWideStatusEffects(wrapper.getStatuses());
					processedTrainerStatus = true;
				}
			}
		}
	}

	private void processSingleBattle(ArrayList<PixelmonWrapper> actives, BattleState state) {
		PixelmonWrapper pwAlly, pwEnemy;
		if (actives.get(0).getPlayerOwner() != null) {
			pwAlly = actives.get(0);
			pwEnemy = actives.get(1);
		} else {
			pwAlly = actives.get(1);
			pwEnemy = actives.get(0);
		}

		state.playerStatuses = formatStatusEffects(pwAlly.getStatuses());
		state.trainerStatuses = formatStatusEffects(pwEnemy.getStatuses());
		state.allyStats = formatStats(pwAlly.getBattleStats());
		state.allyIsMega = pwAlly.isMega;
		state.enemyStats = formatStats(pwEnemy.getBattleStats());
		state.enemyIsMega = pwEnemy.isMega;
	}

	private void sendBattleInfoPacket(ServerPlayerEntity player, BattleState state) {
		BattleInfoPacket packet;
		if (state.isDoubleBattle) {
			packet = new BattleInfoPacket(
				state.enemyStatsList,
				state.enemyIsMegaList,
				state.allyStatsList,
				state.allyIsMegaList,
				true,
				state.environment,
				state.playerName,
				state.trainerName,
				state.playerStatuses,
				state.trainerStatuses,
				state.isTagBattle,
				state.isLinkedBattle,
				state.secondEnemyTrainerName,
				state.allyTrainerName,
				state.secondEnemyTrainerStatuses,
				state.allyTrainerStatuses
			);
		} else {
			packet = new BattleInfoPacket(
				state.enemyStats,
				state.allyStats,
				state.enemyIsMega,
				state.allyIsMega,
				true,
				state.environment,
				state.playerName,
				state.trainerName,
				state.playerStatuses,
				state.trainerStatuses
			);
		}

		PacketHandler.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	private String normalizeEnvironmentName(String rawEnvironment) {
		if (rawEnvironment.isEmpty()) return "";
		return ENV_NAME_CACHE.getOrDefault(rawEnvironment, rawEnvironment);
	}

	private String formatTeamWideStatusEffects(List<StatusBase> statuses) {
		if (statuses == null || statuses.isEmpty()) return "";

		int statusFlags = 0;
		for (StatusBase status : statuses) {
			if (status instanceof LightScreen) {
				statusFlags |= LIGHT_SCREEN_FLAG;
			} else if (status instanceof Reflect) {
				statusFlags |= REFLECT_FLAG;
			} else if (status instanceof AuroraVeil || status instanceof CustomAuroraVeil) {
				statusFlags |= AURORA_VEIL_FLAG;
			} else if (status instanceof Tailwind) {
				statusFlags |= TAILWIND_FLAG;
			}
		}

		if (statusFlags == 0) return "";

		sharedStringBuilder.setLength(0);
		if ((statusFlags & LIGHT_SCREEN_FLAG) != 0) sharedStringBuilder.append(LIGHT_SCREEN_STR);
		if ((statusFlags & REFLECT_FLAG) != 0) sharedStringBuilder.append(REFLECT_STR);
		if ((statusFlags & AURORA_VEIL_FLAG) != 0) sharedStringBuilder.append(AURORA_VEIL_STR);
		if ((statusFlags & TAILWIND_FLAG) != 0) sharedStringBuilder.append(TAILWIND_STR);

		int length = sharedStringBuilder.length();
		if (length > 0) {
			sharedStringBuilder.setLength(length - 1);
		}
		return sharedStringBuilder.toString();
	}

	private String formatStatusEffects(List<StatusBase> statuses) {
		if (statuses == null || statuses.isEmpty()) return "";

		int statusFlags = 0;
		int spikesCount = 0;
		int toxicSpikesCount = 0;

		for (StatusBase status : statuses) {
			if (status instanceof Spikes) {
				spikesCount = ((Spikes) status).getNumLayers();
			} else if (status instanceof ToxicSpikes) {
				toxicSpikesCount = ((ToxicSpikes) status).getNumLayers();
			} else if (status instanceof StealthRock) {
				statusFlags |= STEALTH_ROCK_FLAG;
			} else if (status instanceof StickyWeb) {
				statusFlags |= STICKY_WEB_FLAG;
			} else if (status instanceof LightScreen) {
				statusFlags |= LIGHT_SCREEN_FLAG;
			} else if (status instanceof Reflect) {
				statusFlags |= REFLECT_FLAG;
			} else if (status instanceof AuroraVeil || status instanceof CustomAuroraVeil) {
				statusFlags |= AURORA_VEIL_FLAG;
			} else if (status instanceof Tailwind) {
				statusFlags |= TAILWIND_FLAG;
			}
		}

		if (statusFlags == 0 && spikesCount == 0 && toxicSpikesCount == 0) return "";

		sharedStringBuilder.setLength(0);
		if ((statusFlags & STEALTH_ROCK_FLAG) != 0) sharedStringBuilder.append(STEALTH_ROCK_STR);
		if (spikesCount > 0) sharedStringBuilder.append("Spikes: ").append(spikesCount).append("x;");
		if (toxicSpikesCount > 0) sharedStringBuilder.append("Toxic Spikes: ").append(toxicSpikesCount).append("x;");
		if ((statusFlags & STICKY_WEB_FLAG) != 0) sharedStringBuilder.append(STICKY_WEB_STR);
		if ((statusFlags & LIGHT_SCREEN_FLAG) != 0) sharedStringBuilder.append(LIGHT_SCREEN_STR);
		if ((statusFlags & REFLECT_FLAG) != 0) sharedStringBuilder.append(REFLECT_STR);
		if ((statusFlags & AURORA_VEIL_FLAG) != 0) sharedStringBuilder.append(AURORA_VEIL_STR);
		if ((statusFlags & TAILWIND_FLAG) != 0) sharedStringBuilder.append(TAILWIND_STR);

		int length = sharedStringBuilder.length();
		if (length > 0) {
			sharedStringBuilder.setLength(length - 1);
		}
		return sharedStringBuilder.toString();
	}

	private String formatStats(BattleStats stats) {
		if (stats == null) return "";

		boolean hasModifiedStats =
			stats.getAttackModifier() != 100 || stats.getDefenseModifier() != 100 || stats.getSpecialAttackModifier() != 100 || stats.getSpecialDefenseModifier() != 100 || stats.getSpeedModifier() != 100;

		if (!hasModifiedStats) return "";

		sharedStringBuilder.setLength(0);

		int atkMod = (int) stats.getAttackModifier();
		if (atkMod != 100) {
			sharedStringBuilder.append("Atk:");
			appendModifierValue(sharedStringBuilder, atkMod);
		}

		int defMod = stats.getDefenseModifier();
		if (defMod != 100) {
			sharedStringBuilder.append("Def:");
			appendModifierValue(sharedStringBuilder, defMod);
		}

		int spaMod = (int) stats.getSpecialAttackModifier();
		if (spaMod != 100) {
			sharedStringBuilder.append("SpA:");
			appendModifierValue(sharedStringBuilder, spaMod);
		}

		int spdMod = stats.getSpecialDefenseModifier();
		if (spdMod != 100) {
			sharedStringBuilder.append("SpD:");
			appendModifierValue(sharedStringBuilder, spdMod);
		}

		int speMod = (int) stats.getSpeedModifier();
		if (speMod != 100) {
			sharedStringBuilder.append("Spe:");
			appendModifierValue(sharedStringBuilder, speMod);
		}

		return sharedStringBuilder.toString().trim();
	}

	private void appendModifierValue(StringBuilder sb, int modifier) {
		if (modifier % 100 == 0) {
			sb.append(modifier / 100).append("x ");
		} else {
			double modValue = (double) modifier / 100;
			sb.append(modValue).append("x ");
		}
	}

	@SubscribeEvent
	public void onBattleStart(BattleStartedEvent.Post event) {
		BattleController bc = event.getBattleController();
		for (BattleParticipant bp : bc.participants) {
			if (bp instanceof PlayerParticipant) {
				ServerPlayerEntity player = ((PlayerParticipant) bp).player;
				PacketHandler.INSTANCE.sendTo(EMPTY_PACKET, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		}
	}

	@SubscribeEvent
	public void onBattleEnd(BattleEndEvent event) {
		BattleController bc = event.getBattleController();
		battleCaches.remove(bc.battleIndex);

		for (BattleParticipant bp : bc.participants) {
			if (bp instanceof PlayerParticipant) {
				ServerPlayerEntity player = ((PlayerParticipant) bp).player;
				PacketHandler.INSTANCE.sendTo(EMPTY_PACKET, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		}
	}

	private static class BattleCache {

		int tickCounter = 0;
		BattleState currentState = new BattleState();
		BattleState lastState = new BattleState();
	}

	private static class BattleState {

		boolean isDoubleBattle;
		boolean isTagBattle;
		boolean isLinkedBattle;
		String environment = "";
		String playerName = "";
		String trainerName = "";
		String secondEnemyTrainerName = "";
		String allyTrainerName = "";
		String playerStatuses = "";
		String trainerStatuses = "";
		String secondEnemyTrainerStatuses = "";
		String allyTrainerStatuses = "";

		String enemyStats = "";
		String allyStats = "";
		boolean enemyIsMega = false;
		boolean allyIsMega = false;

		List<String> enemyStatsList = Collections.emptyList();
		List<Boolean> enemyIsMegaList = Collections.emptyList();
		List<String> allyStatsList = Collections.emptyList();
		List<Boolean> allyIsMegaList = Collections.emptyList();

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof BattleState)) return false;

			BattleState other = (BattleState) obj;

			if (isDoubleBattle != other.isDoubleBattle || isTagBattle != other.isTagBattle || isLinkedBattle != other.isLinkedBattle || enemyIsMega != other.enemyIsMega || allyIsMega != other.allyIsMega) {
				return false;
			}

			if (
				!stringEquals(environment, other.environment) ||
				!stringEquals(playerName, other.playerName) ||
				!stringEquals(trainerName, other.trainerName) ||
				!stringEquals(secondEnemyTrainerName, other.secondEnemyTrainerName) ||
				!stringEquals(allyTrainerName, other.allyTrainerName) ||
				!stringEquals(playerStatuses, other.playerStatuses) ||
				!stringEquals(trainerStatuses, other.trainerStatuses) ||
				!stringEquals(secondEnemyTrainerStatuses, other.secondEnemyTrainerStatuses) ||
				!stringEquals(allyTrainerStatuses, other.allyTrainerStatuses)
			) {
				return false;
			}

			if (isDoubleBattle) {
				return (
					listsEqual(enemyStatsList, other.enemyStatsList) &&
					listsEqual(enemyIsMegaList, other.enemyIsMegaList) &&
					listsEqual(allyStatsList, other.allyStatsList) &&
					listsEqual(allyIsMegaList, other.allyIsMegaList)
				);
			} else {
				return stringEquals(enemyStats, other.enemyStats) && stringEquals(allyStats, other.allyStats);
			}
		}

		private static boolean stringEquals(String a, String b) {
			return Objects.equals(a, b);
		}

		private static <T> boolean listsEqual(List<T> a, List<T> b) {
			if (a == b) return true;
			if (a == null || b == null || a.size() != b.size()) return false;

			for (int i = 0; i < a.size(); i++) {
				T itemA = a.get(i);
				T itemB = b.get(i);
				if (!Objects.equals(itemA, itemB)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			int result = 17;

			result = 31 * result + (isDoubleBattle ? 1231 : 1237);
			result = 31 * result + (isTagBattle ? 1231 : 1237);
			result = 31 * result + (isLinkedBattle ? 1231 : 1237);

			result = 31 * result + (environment != null ? environment.hashCode() : 0);
			result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
			result = 31 * result + (trainerName != null ? trainerName.hashCode() : 0);
			result = 31 * result + (secondEnemyTrainerName != null ? secondEnemyTrainerName.hashCode() : 0);
			result = 31 * result + (allyTrainerName != null ? allyTrainerName.hashCode() : 0);
			result = 31 * result + (playerStatuses != null ? playerStatuses.hashCode() : 0);
			result = 31 * result + (trainerStatuses != null ? trainerStatuses.hashCode() : 0);
			result = 31 * result + (secondEnemyTrainerStatuses != null ? secondEnemyTrainerStatuses.hashCode() : 0);
			result = 31 * result + (allyTrainerStatuses != null ? allyTrainerStatuses.hashCode() : 0);

			if (isDoubleBattle) {
				result = 31 * result + (enemyStatsList != null ? enemyStatsList.hashCode() : 0);
				result = 31 * result + (enemyIsMegaList != null ? enemyIsMegaList.hashCode() : 0);
				result = 31 * result + (allyStatsList != null ? allyStatsList.hashCode() : 0);
				result = 31 * result + (allyIsMegaList != null ? allyIsMegaList.hashCode() : 0);
			} else {
				result = 31 * result + (enemyStats != null ? enemyStats.hashCode() : 0);
				result = 31 * result + (allyStats != null ? allyStats.hashCode() : 0);
				result = 31 * result + (enemyIsMega ? 1231 : 1237);
				result = 31 * result + (allyIsMega ? 1231 : 1237);
			}
			return result;
		}
	}
}
