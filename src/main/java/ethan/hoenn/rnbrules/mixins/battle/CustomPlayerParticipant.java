package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.accessors.ICustomPlayerParticipant;
import ethan.hoenn.rnbrules.utils.misc.FastMath;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerParticipant.class, remap = false)
public class CustomPlayerParticipant implements ICustomPlayerParticipant {

	@Shadow
	public boolean hasAmuletCoin;

	@Shadow
	private int amuletCoinMultiplier;

	@Shadow
	public boolean hasHappyHour;

	@Shadow
	private int happyHourMultiplier;

	@Shadow
	public ServerPlayerEntity player;

	@Unique
	private boolean rnbrules$deathless = false;

	@Unique
	private int rnbrules$deathlessMultiplier = 3;

	@Unique
	private transient Vector3d rnbrules$tempSafePos;

	@Unique
	private transient float rnbrules$tempYRot;

	@Unique
	private transient PixelmonEntity rnbrules$entityBeingSwitched;

	@Unique
	public void rnbrules$setDeathless(boolean deathless) {
		this.rnbrules$deathless = deathless;
	}

	@Unique
	public boolean rnbrules$isDeathless() {
		return this.rnbrules$deathless;
	}

	@Unique
	public void rnbrules$setDeathlessMultiplier(int multiplier) {
		this.rnbrules$deathlessMultiplier = multiplier;
	}

	@Unique
	public int rnbrules$getDeathlessMultiplier() {
		return this.rnbrules$deathlessMultiplier;
	}

	@Inject(method = "getPrizeMoneyMultiplier", at = @At("HEAD"), cancellable = true)
	private void onGetPrizeMoneyMultiplier(CallbackInfoReturnable<Integer> cir) {
		int multiplier = 1;
		if (this.hasAmuletCoin) {
			multiplier *= this.amuletCoinMultiplier;
		}
		if (this.hasHappyHour) {
			multiplier *= this.happyHourMultiplier;
		}
		if (this.rnbrules$deathless) {
			multiplier *= this.rnbrules$deathlessMultiplier;
		}
		cir.setReturnValue(multiplier);
	}

	@Redirect(
		method = "<init>(Lnet/minecraft/entity/player/ServerPlayerEntity;Ljava/util/List;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/api/pokemon/Pokemon;getOrSpawnPixelmon(Lnet/minecraft/entity/Entity;)Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;",
			remap = false
		)
	)
	private PixelmonEntity redirectGetOrSpawnPixelmon(Pokemon pokemon, Entity parent, ServerPlayerEntity p, List<Pokemon> teamSelection, int numControlledPokemon) {
		// Find which Pokémon in the selection we're currently spawning
		int pokemonIndex = teamSelection.indexOf(pokemon);

		// Get player's position and look direction
		Vector3d playerPos = new Vector3d(p.getX(), p.getY(), p.getZ());

		// Calculate spawn direction based on player's rotation
		float playerYaw = p.yRot;
		double lookX = -Math.sin(Math.toRadians(playerYaw));
		double lookZ = Math.cos(Math.toRadians(playerYaw));
		Vector3d direction = new Vector3d(lookX, 0, lookZ).normalize();

		// Calculate spawn position based on battle type (singles vs doubles)
		Vector3d spawnPos;
		float yRot;

		boolean isDoubleBattle = numControlledPokemon > 1;

		if (!isDoubleBattle) {
			// Single battle: directly in front of player
			spawnPos = playerPos.add(direction.scale(1.2));
			yRot = playerYaw;
		} else {
			// Double battle: position depends on index
			Vector3d perpendicular = new Vector3d(-direction.z, 0, direction.x).normalize();

			if (pokemonIndex == 0) {
				// First Pokémon to the left
				Vector3d offset = direction.scale(1.1).add(perpendicular.scale(0.8));
				spawnPos = playerPos.add(offset);
				yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
			} else {
				// Second Pokémon to the right
				Vector3d offset = direction.scale(1.1).add(perpendicular.scale(-0.8));
				spawnPos = playerPos.add(offset);
				yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
			}
		}

		// Check for safe spawn position
		Vector3d safePos = rnbrules$getSafeSpawnPos(p.level, spawnPos);

		return pokemon.getOrSpawnPixelmon(p.level, safePos.x, safePos.y, safePos.z, yRot, 0.0F);
	}

	@Redirect(
		method = "switchPokemon",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/api/pokemon/Pokemon;getOrSpawnPixelmon(Lnet/minecraft/entity/Entity;)Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;",
			remap = false
		)
	)
	private PixelmonEntity redirectSwitchPokemonSpawn(Pokemon instance, Entity parentEntity, PixelmonWrapper oldWrapper_host, UUID newPixelmonUUID_host) {
		PlayerParticipant self = (PlayerParticipant) (Object) this;
		boolean isDoubleBattle = self.controlledPokemon.size() > 1;

		Vector3d playerPos = new Vector3d(parentEntity.getX(), parentEntity.getY(), parentEntity.getZ());
		Vector3d targetPos = rnbrules$getOpponentPos();
		Vector3d dirToOpponent = targetPos.subtract(playerPos).normalize();

		Vector3d spawnPos;
		float yRot;

		if (!isDoubleBattle) {
			spawnPos = playerPos.add(dirToOpponent.scale(1.2));
			yRot = (float) Math.toDegrees(FastMath.fastAtan2(-dirToOpponent.x, dirToOpponent.z));
		} else {
			Vector3d perpendicular = new Vector3d(-dirToOpponent.z, 0, dirToOpponent.x).normalize();
			int position = oldWrapper_host.battlePosition;

			if (position == 0) {
				Vector3d offset = dirToOpponent.scale(1.1).add(perpendicular.scale(0.8));
				spawnPos = playerPos.add(offset);
				yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
			} else {
				Vector3d offset = dirToOpponent.scale(1.1).add(perpendicular.scale(-0.8));
				spawnPos = playerPos.add(offset);
				yRot = (float) Math.toDegrees(FastMath.fastAtan2(-offset.x, offset.z));
			}
		}

		Vector3d safePos = rnbrules$getSafeSpawnPos(parentEntity.level, spawnPos);

		this.rnbrules$tempSafePos = safePos;
		this.rnbrules$tempYRot = yRot;

		PixelmonEntity spawnedEntity = instance.getOrSpawnPixelmon(parentEntity.level, safePos.x, safePos.y, safePos.z, yRot, 0.0F);
		this.rnbrules$entityBeingSwitched = spawnedEntity;
		return spawnedEntity;
	}

	@Redirect(method = "switchPokemon", at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;moveTo(DDDFF)V"), remap = true)
	private void redirectMoveToInSwitchPokemon(PixelmonEntity entity, double x, double y, double z, float yaw, float pitch) {
		if (entity == this.rnbrules$entityBeingSwitched && this.rnbrules$entityBeingSwitched != null && this.rnbrules$tempSafePos != null) {
			entity.moveTo(this.rnbrules$tempSafePos.x, this.rnbrules$tempSafePos.y, this.rnbrules$tempSafePos.z, this.rnbrules$tempYRot, 0.0F);
			this.rnbrules$tempSafePos = null;
			this.rnbrules$tempYRot = 0.0f;
			this.rnbrules$entityBeingSwitched = null;
		} else {
			entity.moveTo(x, y, z, yaw, pitch);
		}
	}

	@Unique
	private Vector3d rnbrules$getOpponentPos() {
		PlayerParticipant self = (PlayerParticipant) (Object) this;

		Vector3d defaultPos = new Vector3d(this.player.getX() + Math.sin(Math.toRadians(-this.player.yRot)) * 5, this.player.getY(), this.player.getZ() + Math.cos(Math.toRadians(-this.player.yRot)) * 5);

		try {
			if (self.bc != null) {
				List<BattleParticipant> opponents = self.getOpponents();
				if (opponents != null && !opponents.isEmpty()) {
					for (BattleParticipant opponent : opponents) {
						if (opponent.getEntity() != null) {
							return opponent.getEntity().position();
						}
					}
				}
			}
		} catch (Exception e) {}

		return defaultPos;
	}

	@Unique
	private Vector3d rnbrules$getSafeSpawnPos(World world, Vector3d position) {
		int x = (int) Math.floor(position.x);
		int y = (int) Math.floor(position.y);
		int z = (int) Math.floor(position.z);

		BlockState blockState = world.getBlockState(new BlockPos(x, y, z));

		if (!(blockState.getMaterial().isLiquid() || blockState.getMaterial().isReplaceable())) {
			return new Vector3d(position.x, y + 1, position.z);
		}

		return position;
	}
}
