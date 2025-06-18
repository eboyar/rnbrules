package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.BeatTrainerEvent;
import com.pixelmonmod.pixelmon.api.events.HealerEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.items.MaxPartyRestore;
import ethan.hoenn.rnbrules.items.PartyRestore;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import ethan.hoenn.rnbrules.utils.managers.MultiBattleManager;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GauntletListeners {

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBattleStartedPost(BattleStartedEvent.Post event) {
		List<BattleParticipant> participants = event.getBattleController().participants;

		for (BattleParticipant participant : participants) {
			if (participant.getEntity() instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) participant.getEntity();
				UUID playerUUID = player.getUUID();

				GauntletManager gm = GauntletManager.get(player.getLevel());

				if (gm.isPartOfAnyGauntlet(playerUUID)) {
					String gauntletId = gm.getCurrentGauntletName(playerUUID);

					if (gm.isDeathlessGauntlet(playerUUID, gauntletId)) {
						PartyStorage party = StorageProxy.getParty(player);
						if (party != null) {
							Pokemon[] pokemon = party.getAll();
							for (Pokemon pkm : pokemon) {
								if (pkm != null && pkm.isFainted()) {
									gm.setDeathlessStatus(playerUUID, gauntletId, false);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBeatTrainer(BeatTrainerEvent event) {
		UUID copiedTrainerUUID = event.trainer.getUUID();
		UUID originalTrainerUUID = MultiBattleManager.getOriginalTrainerUUID(copiedTrainerUUID);
		UUID playerUUID = event.player.getUUID();

		if (originalTrainerUUID == null) return;

		Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();
		GauntletManager gm = GauntletManager.get(event.player.getLevel());
		ProgressionManager pm = ProgressionManager.get();
		String originalTrainerUUIDString = originalTrainerUUID.toString();

		String gauntletName = null;
		int position = -1;

		for (Map.Entry<String, List<String>> entry : gauntlets.entrySet()) {
			String currentGauntlet = entry.getKey();
			List<String> trainerUUIDs = entry.getValue();

			if (trainerUUIDs.contains(originalTrainerUUIDString)) {
				gauntletName = currentGauntlet;
				position = trainerUUIDs.indexOf(originalTrainerUUIDString);
				break;
			}
		}

		if (gauntletName == null) {
			gauntletName = gm.findGauntletForTrainerWithPartners(originalTrainerUUIDString, event.player.getLevel());
			if (gauntletName != null) {
				position = gm.getEffectiveTrainerPosition(originalTrainerUUIDString, gauntletName, event.player.getLevel());
			}
		}

		if (gauntletName == null) return;

		if (!gm.isPartOfAnyGauntlet(playerUUID)) {
			if (position == 0) {
				int initialFaintedCount = 0;
				PartyStorage party = StorageProxy.getParty(event.player);
				if (party != null) {
					Pokemon[] pokemon = party.getAll();
					for (Pokemon pkm : pokemon) {
						if (pkm != null && pkm.isFainted()) {
							initialFaintedCount++;
						}
					}
				}

				gm.addGauntlet(playerUUID, gauntletName, initialFaintedCount);
				pm.startGauntlet(playerUUID, gauntletName);
			} else {
				return;
			}
		} else if (!gauntletName.equals(gm.getCurrentGauntletName(playerUUID))) {
			return;
		}

		
		pm.markGauntletTrainerDefeated(playerUUID, originalTrainerUUID);

		List<Boolean> currentProgress = gm.getGauntletProgress(playerUUID, gauntletName);
		currentProgress.set(position, true);

		boolean willCompleteGauntlet = true;
		for (boolean progress : currentProgress) {
			if (!progress) {
				willCompleteGauntlet = false;
				break;
			}
		}

		if (willCompleteGauntlet) {
			boolean wasDeathless = gm.isDeathlessGauntlet(playerUUID, gauntletName);

			String nextGauntletName = RNBConfig.getNextGauntlet(gauntletName);
			boolean hasNextGauntlet = nextGauntletName != null && !nextGauntletName.isEmpty() && RNBConfig.getGauntlets().containsKey(nextGauntletName);

			boolean shouldGiveDeathlessReward = wasDeathless;
			gm.removeGauntlet(playerUUID, gauntletName);
			
			
			pm.completeGauntletSuccessfully(playerUUID);

			if (hasNextGauntlet) {
				int initialFaintedCount = 0;
				PartyStorage party = StorageProxy.getParty(event.player);
				if (party != null) {
					Pokemon[] pokemon = party.getAll();
					for (Pokemon pkm : pokemon) {
						if (pkm != null && pkm.isFainted()) {
							initialFaintedCount++;
						}
					}
				}
				gm.addGauntlet(playerUUID, nextGauntletName, initialFaintedCount);
				pm.startGauntlet(playerUUID, nextGauntletName);
				gm.setPreviousGauntletDeathlessStatus(playerUUID, nextGauntletName, wasDeathless);
				shouldGiveDeathlessReward = false;
			} else {
				String parentGauntletId = RNBConfig.findParentGauntlet(gauntletName);

				if (parentGauntletId != null) {
					boolean previousWasDeathless = gm.wasPreviousGauntletDeathless(playerUUID, gauntletName);
					shouldGiveDeathlessReward = wasDeathless && previousWasDeathless;

					gm.clearPreviousGauntletDeathlessStatus(playerUUID, gauntletName);
				}
			}
			if (shouldGiveDeathlessReward) {
				String command = RNBConfig.getDeathlessCommand(gauntletName);
				if (command != null && !command.isEmpty()) {
					command = command.replace("@pl", event.player.getScoreboardName());

					MinecraftServer server = event.player.getServer();
					if (server != null) {
						server.getCommands().performCommand(server.createCommandSourceStack(), command);
					}
				}
			}
		} else {
			gm.completePortion(playerUUID, gauntletName, position);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBattleEnd(BattleEndEvent event) {
		List<BattleParticipant> participants = event.getBattleController().participants;

		for (BattleParticipant participant : participants) {
			if (participant.getEntity() instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) participant.getEntity();
				UUID playerUUID = player.getUUID();

				GauntletManager gm = GauntletManager.get(player.getLevel());
				ProgressionManager pm = ProgressionManager.get();

				if (gm.isPartOfAnyGauntlet(playerUUID)) {
					String gauntletId = gm.getCurrentGauntletName(playerUUID);

					if (gm.isDeathlessGauntlet(playerUUID, gauntletId)) {
						int initialFaintedCount = gm.getInitialFaintedCount(playerUUID, gauntletId);
						int currentFaintedCount = 0;

						PartyStorage party = StorageProxy.getParty(player);
						if (party != null) {
							Pokemon[] pokemon = party.getAll();
							for (Pokemon pkm : pokemon) {
								if (pkm != null && pkm.isFainted()) {
									currentFaintedCount++;
								}
							}
						}

						if (currentFaintedCount > initialFaintedCount) {
							gm.setDeathlessStatus(playerUUID, gauntletId, false);
						}
					}

					if (StorageProxy.getParty(player) != null) {
						Pokemon[] pkms = StorageProxy.getParty(player).getAll();
						boolean allFainted = true;

						for (Pokemon pkm : pkms) {
							if (pkm != null && !pkm.isFainted()) {
								allFainted = false;
								break;
							}
						}

						if (allFainted) {
							if (player.getLevel() instanceof ServerWorld) {
								ServerWorld serverWorld = (ServerWorld) player.getLevel();
								String currentGauntletId = gm.getCurrentGauntletName(playerUUID);

								String parentGauntletId = RNBConfig.findParentGauntlet(currentGauntletId);

								if (parentGauntletId != null) {
									resetEncounterData(playerUUID, currentGauntletId, serverWorld);
									resetEncounterData(playerUUID, parentGauntletId, serverWorld);

									gm.removeGauntlet(playerUUID, currentGauntletId);
									
									pm.failGauntlet(playerUUID);
								} else {
									resetEncounterData(playerUUID, currentGauntletId, serverWorld);
									gm.removeGauntlet(playerUUID, currentGauntletId);
									
									pm.failGauntlet(playerUUID);
								}
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onUseHealer(HealerEvent.Pre event) {
		PlayerEntity player = event.player;
		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			GauntletManager gm = GauntletManager.get(serverPlayer.getLevel());
			if (gm.isPartOfAnyGauntlet(player.getUUID()) && !gm.isHealingAllowedForPlayer(player.getUUID())) {
				player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to heal your Pokemon."), player.getUUID());
				event.setCanceled(true);
			}

			PlayerInventory pl = player.inventory;
			for (ItemStack item : pl.items) {
				if (item.getItem() instanceof MaxPartyRestore || item.getItem() instanceof PartyRestore) {
					item.setDamageValue(0);
				}
			}
		}
	}

	public static void resetEncounterData(UUID playerUUID, String gauntletName, ServerWorld world) {
		Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();
		ProgressionManager pm = ProgressionManager.get();

		if (!gauntlets.containsKey(gauntletName)) {
			return;
		}

		List<String> trainerUUIDs = gauntlets.get(gauntletName);

		
		pm.clearTempDefeatedTrainers(playerUUID);
		
		
		for (String trainerUUIDString : trainerUUIDs) {
			try {
				UUID trainerUUID = UUID.fromString(trainerUUIDString);
				
				pm.removeDefeatedTrainer(playerUUID, trainerUUID);
				
				Entity entity = world.getEntity(trainerUUID);
				if (entity instanceof NPCTrainer) {
					NPCTrainer trainer = (NPCTrainer) entity;
					handleLinkedPairedTrainers(trainer, playerUUID, world, pm);
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid UUID format: " + trainerUUIDString);
			}
		}
	}

	private static void handleLinkedPairedTrainers(NPCTrainer trainer, UUID playerUUID, ServerWorld world, ProgressionManager pm) {
		CompoundNBT persistentData = trainer.getPersistentData();

		
		
		
		if (persistentData.contains("Linked")) {
			UUID linkedTrainerUUID = persistentData.getUUID("Linked");
			
			pm.removeGauntletTrainerDefeated(playerUUID, linkedTrainerUUID);
			
			pm.removeDefeatedTrainer(playerUUID, linkedTrainerUUID);
		}

		if (persistentData.contains("Paired")) {
			UUID pairedTrainerUUID = persistentData.getUUID("Paired");
			
			pm.removeGauntletTrainerDefeated(playerUUID, pairedTrainerUUID);
			
			pm.removeDefeatedTrainer(playerUUID, pairedTrainerUUID);
		}
	}
}
