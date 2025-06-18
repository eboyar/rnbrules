package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.TrainerPartyStorage;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.FastMath;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NPCTrainer.class)
public abstract class CustomNPCTrainer {

	@Shadow(remap = false)
	public BattleController battleController;

	@Shadow(remap = false)
	private TrainerPartyStorage party;

	@ModifyVariable(method = "loseBattle", at = @At(value = "STORE", ordinal = 0), name = "calculatedWinMoney", remap = false)
	private int modifyCalculatedWinMoney(int original) {
		return ((NPCTrainer) (Object) this).winMoney;
	}

	@Inject(method = "loseBattle", at = @At("HEAD"), cancellable = true, remap = false)
	private void onLoseBattle(ArrayList<BattleParticipant> opponents, CallbackInfo ci) {
		NPCTrainer self = (NPCTrainer) (Object) this;

		if (opponents.size() == 1 && opponents.get(0) instanceof PlayerParticipant) {
			if (self.battleController != null && self.battleController.participants.size() > 2) {
				boolean allTrainersDefeated = true;

				for (BattleParticipant participant : self.battleController.participants) {
					if (participant instanceof PlayerParticipant || rnbrules$isAlliedWithPlayer(participant)) {
						continue;
					}

					if (!participant.isDefeated) {
						allTrainersDefeated = false;
						break;
					}
				}

				if (!allTrainersDefeated) {
					ci.cancel();
					return;
				}
			}
			return;
		}

		ArrayList<BattleParticipant> playerParticipants = opponents.stream().filter(participant -> participant instanceof PlayerParticipant).collect(Collectors.toCollection(ArrayList::new));

		if (!playerParticipants.isEmpty()) {
			ci.cancel();
			self.loseBattle(playerParticipants);
		}
	}

	@Inject(method = "winBattle", at = @At("HEAD"), cancellable = true, remap = false)
	private void onWinBattle(ArrayList<BattleParticipant> opponents, CallbackInfo ci) {
		NPCTrainer self = (NPCTrainer) (Object) this;

		if (self.battleController != null && self.battleController.participants.size() > 2) {
			boolean anyPlayerStillFighting = false;

			for (BattleParticipant participant : self.battleController.participants) {
				if ((participant instanceof PlayerParticipant || rnbrules$isAlliedWithPlayer(participant)) && !participant.isDefeated) {
					anyPlayerStillFighting = true;
					break;
				}
			}

			boolean anyTrainerStillFighting = false;

			for (BattleParticipant participant : self.battleController.participants) {
				if (participant instanceof PlayerParticipant || rnbrules$isAlliedWithPlayer(participant)) {
					continue;
				}

				if (participant instanceof TrainerParticipant && ((TrainerParticipant) participant).trainer == self) {
					continue;
				}

				if (!participant.isDefeated) {
					anyTrainerStillFighting = true;
					break;
				}
			}

			if (anyPlayerStillFighting || anyTrainerStillFighting) {
				ci.cancel();
			}
		}
	}

	@Unique
	private boolean rnbrules$isAlliedWithPlayer(BattleParticipant participant) {
		if (participant.getAllies() != null) {
			for (BattleParticipant ally : participant.getAllies()) {
				if (ally instanceof PlayerParticipant) {
					return true;
				}
			}
		}
		return false;
	}

	@Inject(method = "releasePokemon(Ljava/util/UUID;)Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;", at = @At("HEAD"), cancellable = true, remap = false)
	private void positionPokemon(UUID pokemonUUID, CallbackInfoReturnable<PixelmonEntity> cir) {
		NPCTrainer trainer = (NPCTrainer) (Object) this;
		CompoundNBT persistentData = trainer.getPersistentData();

		if (battleController != null) {
			try {
				TrainerParticipant trainerPart = null;
				for (BattleParticipant participant : battleController.participants) {
					if (participant instanceof TrainerParticipant && ((TrainerParticipant) participant).trainer == trainer) {
						trainerPart = (TrainerParticipant) participant;
						break;
					}
				}

				if (trainerPart == null) return;

				int pokemonPosition = -1;
				for (int i = 0; i < trainerPart.allPokemon.length; i++) {
					if (trainerPart.allPokemon[i].getPokemonUUID().equals(pokemonUUID)) {
						pokemonPosition = i;
						break;
					}
				}

				if (pokemonPosition < 0) return;

				if (persistentData.contains("Linked")) {
					UUID linkedTrainerUUID = persistentData.getUUID("Linked");
					NPCTrainer linkedTrainer = null;

					List<NPCTrainer> nearbyTrainers = trainer.level.getEntitiesOfClass(NPCTrainer.class, trainer.getBoundingBox().inflate(2.0), entity -> entity.getUUID().equals(linkedTrainerUUID));

					if (!nearbyTrainers.isEmpty()) {
						linkedTrainer = nearbyTrainers.get(0);

						PlayerEntity playerOpponent = rnbrules$findPlayerInBattle(trainer);
						if (playerOpponent != null) {
							Vector3d trainerPos = trainer.position();
							Vector3d linkedPos = linkedTrainer.position();
							Vector3d playerPos = playerOpponent.position();

							Vector3d centerPoint = trainerPos.add(linkedPos).scale(0.5);

							Vector3d directionToPlayer = playerPos.subtract(centerPoint).normalize();

							Vector3d spawnPos = trainerPos.add(directionToPlayer.scale(1.2));
							float yRot = (float) Math.toDegrees(FastMath.fastAtan2(-directionToPlayer.x, directionToPlayer.z));

							rnbrules$spawnPokemonAndReturn(trainer, pokemonUUID, spawnPos, yRot, cir);
							return;
						}
					}
				} else if (persistentData.contains("Paired")) {
					UUID pairedTrainerUUID = persistentData.getUUID("Paired");
					NPCTrainer pairedTrainer = null;

					List<NPCTrainer> nearbyTrainers = trainer.level.getEntitiesOfClass(NPCTrainer.class, trainer.getBoundingBox().inflate(2.0), entity -> entity.getUUID().equals(pairedTrainerUUID));

					if (!nearbyTrainers.isEmpty()) {
						pairedTrainer = nearbyTrainers.get(0);

						PlayerEntity playerOpponent = rnbrules$findPlayerInBattle(trainer);
						if (playerOpponent != null) {
							Vector3d playerPos = playerOpponent.position();

							Vector3d sourcePos;
							if (pokemonPosition == 0) {
								sourcePos = trainer.position();
							} else {
								sourcePos = pairedTrainer.position();
							}

							Vector3d directionToPlayer = playerPos.subtract(sourcePos).normalize();

							Vector3d spawnPos = sourcePos.add(directionToPlayer.scale(1.2));
							float yRot = (float) Math.toDegrees(FastMath.fastAtan2(-directionToPlayer.x, directionToPlayer.z));

							rnbrules$spawnPokemonAndReturn(trainer, pokemonUUID, spawnPos, yRot, cir);
							return;
						}
					}
				} else if (persistentData.contains("IsPlayerPartner") && persistentData.getBoolean("IsPlayerPartner")) {
					List<TrainerParticipant> enemyTrainers = new ArrayList<>();

					for (BattleParticipant participant : battleController.participants) {
						if (participant == trainerPart || participant instanceof PlayerParticipant || (participant.getAllies() != null && participant.getAllies().contains(trainerPart))) {
							continue;
						}

						if (participant instanceof TrainerParticipant) {
							enemyTrainers.add((TrainerParticipant) participant);
						}
					}

					if (!enemyTrainers.isEmpty()) {
						Vector3d trainerPos = trainer.position();
						Vector3d directionToEnemy;

						if (enemyTrainers.size() == 1) {
							Vector3d enemyPos = enemyTrainers.get(0).getEntity().position();
							directionToEnemy = enemyPos.subtract(trainerPos).normalize();
						} else {
							Vector3d centerPoint = new Vector3d(0, 0, 0);
							for (TrainerParticipant enemy : enemyTrainers) {
								centerPoint = centerPoint.add(enemy.getEntity().position());
							}
							centerPoint = centerPoint.scale(1.0 / enemyTrainers.size());
							directionToEnemy = centerPoint.subtract(trainerPos).normalize();
						}

						Vector3d spawnPos = trainerPos.add(directionToEnemy.scale(1.2));
						float yRot = (float) Math.toDegrees(FastMath.fastAtan2(-directionToEnemy.x, directionToEnemy.z));

						rnbrules$spawnPokemonAndReturn(trainer, pokemonUUID, spawnPos, yRot, cir);
						return;
					}
				}

				PlayerEntity playerOpponent = rnbrules$findPlayerInBattle(trainer);
				if (playerOpponent != null) {
					boolean isDoubleBattle = trainerPart.numControlledPokemon > 1;
					Vector3d trainerPos = trainer.position();
					Vector3d playerPos = playerOpponent.position();
					Vector3d directionToPlayer = playerPos.subtract(trainerPos).normalize();

					Vector3d spawnPos;
					float yRot;

					if (!isDoubleBattle) {
						spawnPos = trainerPos.add(directionToPlayer.scale(1.2));
						yRot = (float) Math.toDegrees(FastMath.fastAtan2(-directionToPlayer.x, directionToPlayer.z));
					} else {
						Vector3d perpendicular = new Vector3d(-directionToPlayer.z, 0, directionToPlayer.x).normalize();
						if (pokemonPosition == 0) {
							Vector3d offset = directionToPlayer.scale(1.1).add(perpendicular.scale(1.1));
							spawnPos = trainerPos.add(offset);
							yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
						} else {
							Vector3d offset = directionToPlayer.scale(1.1).add(perpendicular.scale(-1.1));
							spawnPos = trainerPos.add(offset);
							yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
						}
					}

					rnbrules$spawnPokemonAndReturn(trainer, pokemonUUID, spawnPos, yRot, cir);
				}
			} catch (Exception e) {
				System.out.println("Error in custom Pokemon positioning: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Unique
	private PlayerEntity rnbrules$findPlayerInBattle(NPCTrainer trainer) {
		if (battleController != null) {
			for (BattleParticipant participant : battleController.participants) {
				if (participant instanceof TrainerParticipant && ((TrainerParticipant) participant).trainer == trainer) {
					List<BattleParticipant> opponents = battleController.getOpponents(participant);
					for (BattleParticipant opponent : opponents) {
						if (opponent instanceof PlayerParticipant) {
							return (PlayerEntity) opponent.getEntity();
						}
					}
					break;
				}
			}
		}
		return null;
	}

	@Unique
	private void rnbrules$spawnPokemonAndReturn(NPCTrainer trainer, UUID pokemonUUID, Vector3d spawnPos, float yRot, CallbackInfoReturnable<PixelmonEntity> cir) {
		Pokemon pokemon = this.party.find(pokemonUUID);
		if (pokemon != null) {
			Vector3d safePos = rnbrules$getSafeSpawnPosition(trainer.level, spawnPos);

			PixelmonEntity entity = pokemon.getOrSpawnPixelmon(trainer.level, safePos.x, safePos.y, safePos.z, yRot, 0.0F);
			cir.setReturnValue(entity);
		}
	}

	@Unique
	private Vector3d rnbrules$getSafeSpawnPosition(World world, Vector3d position) {
		int x = (int) Math.floor(position.x);
		int y = (int) Math.floor(position.y);
		int z = (int) Math.floor(position.z);

		BlockState blockState = world.getBlockState(new BlockPos(x, y, z));

		if (!(blockState.getMaterial().isLiquid() || blockState.getMaterial().isReplaceable())) {
			return new Vector3d(position.x, y + 1, position.z);
		}

		return new Vector3d(position.x, position.y, position.z);
	}
}
