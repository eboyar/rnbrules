package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClauseRegistry;

import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.EnumMegaItemsUnlocked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.server.ServerWorld;

public class MultiBattleManager {

	private static final Map<UUID, UUID> copiedTrainerToOriginal = new HashMap<>();
	private static final Map<UUID, Set<UUID>> playerToBattleTrainers = new HashMap<>();
	private static final Map<UUID, NPCTrainer> copyTrainerEntities = new HashMap<>();
	private static final Map<UUID, Integer> trainerRemovalTimers = new HashMap<>();
	private static final Map<UUID, UUID> pairedTrainerRelationships = new HashMap<>();

	public static boolean startTrainerBattle(PlayerParticipant pp, TrainerParticipant otp) {
		if (isBattleOngoing(pp.player.getUUID())) {
			return false;
		}

		ServerPlayerEntity player = pp.player;
		NPCTrainer originalTrainer = otp.trainer;
		ServerWorld world = player.getLevel();

		int numPokemon = 1;

		NPCTrainer linkedTrainer = null;
		NPCTrainer playerPartnerTrainer = null;
		NPCTrainer pairedTrainer = null;
		CompoundNBT persistentData = originalTrainer.getPersistentData();
		UUID linkedTrainerUUID;
		UUID playerPartnerUUID;
		UUID pairedTrainerUUID;

		if (persistentData.contains("Linked")) {
			linkedTrainerUUID = persistentData.getUUID("Linked");
			Entity linkedEntity = world.getEntity(linkedTrainerUUID);
			if (linkedEntity instanceof NPCTrainer) {
				linkedTrainer = (NPCTrainer) linkedEntity;
			}
		} else {
			linkedTrainerUUID = null;
		}

		if (persistentData.contains("Paired")) {
			pairedTrainerUUID = persistentData.getUUID("Paired");
			Entity pairedEntity = world.getEntity(pairedTrainerUUID);
			if (pairedEntity instanceof NPCTrainer) {
				pairedTrainer = (NPCTrainer) pairedEntity;
			}
		} else {
			pairedTrainerUUID = null;
		}

		if (persistentData.contains("PlayerPartner")) {
			playerPartnerUUID = persistentData.getUUID("PlayerPartner");
			Entity partnerEntity = world.getEntity(playerPartnerUUID);
			if (partnerEntity instanceof NPCTrainer) {
				playerPartnerTrainer = (NPCTrainer) partnerEntity;
			}
		}

		boolean isTagBattle = playerPartnerTrainer != null && linkedTrainer != null;
		boolean isLinkedBattle = !isTagBattle && linkedTrainer != null;
		boolean isSingleTrainerTagBattle = !isTagBattle && !isLinkedBattle && playerPartnerTrainer != null;
		boolean isPairedBattle = pairedTrainer != null;

		NPCTrainer copyTrainer = createTrainerCopy(originalTrainer, player);

		TrainerParticipant tp;
		if (isTagBattle || isLinkedBattle) {
			tp = new TrainerParticipant(copyTrainer, 1);
		} else if (isSingleTrainerTagBattle) {
			tp = new TrainerParticipant(copyTrainer, 2);
		} else {
			numPokemon = originalTrainer.getBattleType() == BattleType.SINGLE ? 1 : 2;
			tp = new TrainerParticipant(copyTrainer, numPokemon);
		}

		registerTrainerCopy(player, originalTrainer, copyTrainer);
		if (isPairedBattle) {
			registerPairedTrainerRelationship(copyTrainer.getUUID(), pairedTrainerUUID);
		}

		BattleBuilder battleBuilder = BattleBuilder.builder().startSync().ignoreTempParties().disableExp().allowSpectators(true);

		if (isTagBattle) {
			NPCTrainer copyPlayerPartner = createTrainerCopy(playerPartnerTrainer, player);
			TrainerParticipant playerPartnerTP = new TrainerParticipant(copyPlayerPartner, 1);
			registerTrainerCopy(player, playerPartnerTrainer, copyPlayerPartner);

			NPCTrainer copyLinkedTrainer = createTrainerCopy(linkedTrainer, player);
			TrainerParticipant linkedTP = new TrainerParticipant(copyLinkedTrainer, 1);
			registerTrainerCopy(player, linkedTrainer, copyLinkedTrainer);

			pp.setNumControlledPokemon(1);

			battleBuilder.teamOne(pp, playerPartnerTP);
			battleBuilder.teamTwo(tp, linkedTP);
		} else if (isLinkedBattle) {
			NPCTrainer copyLinkedTrainer = createTrainerCopy(linkedTrainer, player);
			TrainerParticipant linkedTP = new TrainerParticipant(copyLinkedTrainer, 1);
			registerTrainerCopy(player, linkedTrainer, copyLinkedTrainer);

			battleBuilder.teamOne(pp);
			battleBuilder.teamTwo(tp, linkedTP);
		} else if (isSingleTrainerTagBattle) {
			NPCTrainer copyPlayerPartner = createTrainerCopy(playerPartnerTrainer, player);
			TrainerParticipant playerPartnerTP = new TrainerParticipant(copyPlayerPartner, 1);
			registerTrainerCopy(player, playerPartnerTrainer, copyPlayerPartner);

			pp.setNumControlledPokemon(1);

			battleBuilder.teamOne(pp, playerPartnerTP);
			battleBuilder.teamTwo(tp);
		} else {
			battleBuilder.teamOne(pp);
			battleBuilder.teamTwo(tp);
		}

		battleBuilder
			.rules(copyTrainer.battleRules)
			.endHandler((battleEndEvent, battleController) -> {
				removeOngoingBattle(player.getUUID());

				if (battleEndEvent.getResult(player).orElse(null) == BattleResults.VICTORY) {
					UUID originalTrainerUUID = getOriginalTrainerUUID(copyTrainer.getUUID());

					if (originalTrainerUUID != null) {
						updateTrainerEncounters(world, originalTrainerUUID, player.getUUID());

						if (linkedTrainerUUID != null) {
							updateTrainerEncounters(world, linkedTrainerUUID, player.getUUID());
						}

						if (pairedTrainerUUID != null) {
							updateTrainerEncounters(world, pairedTrainerUUID, player.getUUID());
						}
					}
				}
			})
			.start();
		return true;
	}

	private static void updateTrainerEncounters(ServerWorld world, UUID trainerUUID, UUID playerUUID) {
		Entity entity = world.getEntity(trainerUUID);

		if (entity instanceof NPCTrainer) {
			ProgressionManager pm = ProgressionManager.get();
			
			// Check if this trainer is part of a gauntlet
			GauntletManager gm = GauntletManager.get(world);
			String gauntletId = gm.findGauntletForTrainerWithPartners(trainerUUID.toString(), world);
			
			if (gauntletId != null && pm.isInGauntlet(playerUUID)) {
				// If this is a gauntlet trainer and the player is in a gauntlet,
				// mark as temporary defeat - the GauntletListeners will handle promotion to permanent
				pm.markGauntletTrainerDefeated(playerUUID, trainerUUID);
			} else {
				// For non-gauntlet trainers, mark as permanent defeat
				pm.markTrainerDefeated(playerUUID, trainerUUID);
			}
		}
	}

	private static NPCTrainer createTrainerCopy(NPCTrainer originalTrainer, ServerPlayerEntity player) {
		NPCTrainer copy = new NPCTrainer(player.level);

		CompoundNBT persistentData = originalTrainer.getPersistentData();
		int rivalTier = 0;

		if (persistentData.contains("Rival")) {
			rivalTier = persistentData.getInt("Rival");
		}

		if (rivalTier > 0) {
			StarterSelectionManager starterManager = StarterSelectionManager.get(player.getLevel());
			StarterSelectionManager.StarterChoice playerStarter = starterManager.getPlayerSelection(player.getUUID());

			if (playerStarter != null) {
				boolean success = RivalTeamsManager.getInstance().applyRivalTeam(copy, player.getUUID(), rivalTier, playerStarter);

				if (success) {
					//System.out.println("Applied rival team for tier " + rivalTier + " with player starter " + playerStarter);
				} else {
					copyOriginalTeam(originalTrainer, copy);
				}
			} else {
				//System.out.println("No starter choice found for player, using default team");
				copyOriginalTeam(originalTrainer, copy);
			}
		} else {
			copyOriginalTeam(originalTrainer, copy);
		}

		copy.updateTrainerLevel();
		copy.setBattleAIMode(originalTrainer.getBattleAIMode());
		copy.setNoAi(true);

		copy.battleRules = originalTrainer.battleRules.clone();
		List<BattleClause> battleClauses = new ArrayList<>();
		battleClauses.add(BattleClauseRegistry.BAG_CLAUSE);

		GauntletManager gauntletManager = GauntletManager.get(player.getLevel());
		if (gauntletManager.isPartOfAnyGauntlet(player.getUUID())) {
			battleClauses.add(BattleClauseRegistry.FORFEIT_CLAUSE);
		}
		copy.battleRules.setNewClauses(battleClauses);

		copy.setPos(originalTrainer.getX(), originalTrainer.getY(), originalTrainer.getZ());
		player.getLevel().addFreshEntity(copy);
		copy.addEffect(new EffectInstance(Effects.INVISIBILITY, Integer.MAX_VALUE, 0, true, true));

		copy.greeting = originalTrainer.greeting;
		copy.winMessage = originalTrainer.winMessage;
		copy.loseMessage = originalTrainer.loseMessage;

		copy.winMoney = originalTrainer.winMoney;
		copy.updateDrops(originalTrainer.getWinnings());

		copy.playerEncounters = originalTrainer.playerEncounters;

		copy.setMegaItem(EnumMegaItemsUnlocked.Mega);
		copy.isGymLeader = originalTrainer.isGymLeader;

		copy.getPersistentData().putBoolean("CopyTrainer", true);

		if (persistentData.contains("trainercommands")) {
			CompoundNBT originalCommands = persistentData.getCompound("trainercommands");
			CompoundNBT copiedCommands = originalCommands.copy();
			copy.getPersistentData().put("trainercommands", copiedCommands);
		}

		if (persistentData.contains("Environment")) {
			String environment = persistentData.getString("Environment");
			copy.getPersistentData().putString("Environment", environment);
		}

		if (persistentData.contains("Linked")) {
			UUID linkedUUID = persistentData.getUUID("Linked");
			copy.getPersistentData().putUUID("Linked", linkedUUID);
		}

		copy.winCommands = originalTrainer.winCommands;
		copy.loseCommands = originalTrainer.loseCommands;
		copy.forfeitCommands = originalTrainer.forfeitCommands;
		copy.preBattleCommands = originalTrainer.preBattleCommands;
		copy.interactCommands = originalTrainer.interactCommands;

		copy.setName(originalTrainer.getName("en_us"));
		copy.setNickName(originalTrainer.getNickName());
		copy.usingDefaultGreeting = false;
		copy.usingDefaultLose = false;
		copy.usingDefaultWin = false;
		copy.usingDefaultName = true;

		copy.setUUID(UUID.randomUUID());

		return copy;
	}

	public static void registerTrainerCopy(ServerPlayerEntity player, NPCTrainer originalTrainer, NPCTrainer copyTrainer) {
		UUID copyUUID = copyTrainer.getUUID();
		UUID originalUUID = originalTrainer.getUUID();
		UUID playerUUID = player.getUUID();

		copiedTrainerToOriginal.put(copyUUID, originalUUID);
		copyTrainerEntities.put(copyUUID, copyTrainer);

		playerToBattleTrainers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(copyUUID);
	}

	public static void registerPairedTrainerRelationship(UUID copyUUID, UUID pairedTrainerUUID) {
		pairedTrainerRelationships.put(copyUUID, pairedTrainerUUID);
	}

	public static UUID getOriginalTrainerUUID(UUID copyUUID) {
		return copiedTrainerToOriginal.get(copyUUID);
	}

	public static UUID getCopiedTrainerUUID(UUID playerUUID) {
		Set<UUID> trainers = playerToBattleTrainers.get(playerUUID);
		if (trainers != null && !trainers.isEmpty()) {
			return trainers.iterator().next();
		}
		return null;
	}

	public static void removeOngoingBattle(UUID playerUUID) {
		Set<UUID> battleTrainers = playerToBattleTrainers.get(playerUUID);
		if (battleTrainers != null) {
			for (UUID trainerUUID : battleTrainers) {
				trainerRemovalTimers.put(trainerUUID, 5);
			}
			playerToBattleTrainers.remove(playerUUID);
		}
	}

	public static boolean isBattleOngoing(UUID playerUUID) {
		return playerToBattleTrainers.containsKey(playerUUID);
	}

	private static void copyOriginalTeam(NPCTrainer originalTrainer, NPCTrainer copy) {
		for (int i = 0; i < originalTrainer.getPokemonStorage().countAll(); i++) {
			if (originalTrainer.getPokemonStorage().get(i) != null) {
				Pokemon pokemon = PokemonBuilder.copy(Objects.requireNonNull(originalTrainer.getPokemonStorage().get(i))).build();
				copy.getPokemonStorage().set(i, pokemon);
			}
		}
	}

	public static void tickTrainerRemovals() {
		Iterator<Map.Entry<UUID, Integer>> iterator = trainerRemovalTimers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			UUID trainerUUID = entry.getKey();
			int remainingTicks = entry.getValue() - 1;

			if (remainingTicks <= 0) {
				NPCTrainer trainer = copyTrainerEntities.get(trainerUUID);
				if (trainer != null && trainer.isAlive()) {
					trainer.remove();
				}

				copyTrainerEntities.remove(trainerUUID);
				copiedTrainerToOriginal.remove(trainerUUID);
				pairedTrainerRelationships.remove(trainerUUID);
				iterator.remove();
			} else {
				entry.setValue(remainingTicks);
			}
		}
	}
}
