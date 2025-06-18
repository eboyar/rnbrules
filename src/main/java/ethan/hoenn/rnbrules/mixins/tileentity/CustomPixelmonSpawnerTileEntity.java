package ethan.hoenn.rnbrules.mixins.tileentity;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.requirement.impl.LevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.MaximumLevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.MinimumLevelRequirement;
import com.pixelmonmod.pixelmon.api.events.spawning.PixelmonSpawnerEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.blocks.tileentity.PixelmonSpawnerTileEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.SpawnPointHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PixelmonSpawnerTileEntity.class)
public class CustomPixelmonSpawnerTileEntity {

	@Shadow(remap = false)
	public int levelMin;

	@Shadow(remap = false)
	public int levelMax;

	@Unique
	private PixelmonEntity rnbrules$currentPixelmon;

	@Unique
	private PokemonSpecification rnbrules$spec;

	/**
	 * @author ethan
	 * @reason fix vertical search range
	 */
	@Overwrite(remap = false)
	private int getTopSolidBlock(int x, int y, int z) {
		PixelmonSpawnerTileEntity self = (PixelmonSpawnerTileEntity)(Object)this;
		World level = self.getLevel();
		boolean valid = false;

		for(int i = 1; i <= self.spawnRadius; ++i) {
			BlockPos pos = new BlockPos(x, y + i, z);
			Material blockMaterial = level.getBlockState(pos).getMaterial();
			if (PixelmonSpawnerTileEntity.VALID_LAND_AIR_MATERIALS.contains(blockMaterial) &&
					rnbrules$isSolidSurface(level, pos)) {
				y += i;
				valid = true;
				break;
			}
		}

		if (!valid) {
			for(int i = 1; i <= self.spawnRadius; ++i) {
				BlockPos pos = new BlockPos(x, y - i, z);
				Material blockMaterial = level.getBlockState(pos).getMaterial();
				if (PixelmonSpawnerTileEntity.VALID_LAND_AIR_MATERIALS.contains(blockMaterial) &&
						rnbrules$isSolidSurface(level, pos)) {
					y -= i;
					break;
				}
			}
		}

		return y;
	}

	@Unique
	private boolean rnbrules$isSolidSurface(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos, net.minecraft.util.Direction.UP) &&
				!worldIn.getBlockState(pos).getMaterial().isSolid() &&
				!worldIn.getBlockState(pos.above()).getMaterial().isSolid();
	}

	@Inject(
			method = "spawnPixelmon",
			at = @At(
					value = "INVOKE",
					target = "Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;setPos(DDD)V",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void captureCurrentPixelmon(PokemonSpecification spec, CallbackInfo ci,
										int x, int y, int z, boolean valid,
										PixelmonSpawnerEvent.Pre event,
										PixelmonEntity pixelmon) {
		this.rnbrules$currentPixelmon = pixelmon;
		this.rnbrules$spec = event.getSpec();
	}

	@Redirect(
			method = "spawnPixelmon",
			at = @At(
					value = "INVOKE",
					target = "Lcom/pixelmonmod/pixelmon/api/pokemon/Pokemon;setLevel(I)V"
			),
			remap = false
	)
	private void handleLevelSetting(Pokemon pokemon, int defaultLevel) {
		// Get the event specification from the current entity
		if (this.rnbrules$currentPixelmon == null) {
			// Fallback to default behavior if entity reference is missing
			pokemon.setLevel(defaultLevel);
			return;
		}

		// Get the spawner tile entity
		PixelmonSpawnerTileEntity self = (PixelmonSpawnerTileEntity)(Object)this;
		PokemonSpecification spec = this.rnbrules$spec;

		// Set level based on requirements
		if (spec != null && spec.getValue(LevelRequirement.class).isPresent()) {
			// Get level from requirement
			int level = spec.getValue(LevelRequirement.class).get();
			pokemon.setLevel(level);
		}
		else if (spec != null &&
				spec.getValue(MinimumLevelRequirement.class).isPresent() &&
				spec.getValue(MaximumLevelRequirement.class).isPresent()) {
			// Get min/max range from requirements
			int minLevel = spec.getValue(MinimumLevelRequirement.class).get();
			int maxLevel = spec.getValue(MaximumLevelRequirement.class).get();
			pokemon.setLevel(RandomHelper.getRandomNumberBetween(minLevel, maxLevel));
		}
		else {
			pokemon.setLevel(RandomHelper.getRandomNumberBetween(levelMin, levelMax));
		}

		// Clear the reference after use
		this.rnbrules$currentPixelmon = null;
		this.rnbrules$spec = null;
	}

	@Inject(
			method = "spawnPixelmon(Lcom/pixelmonmod/api/pokemon/PokemonSpecification;)V",
			at = @At(
					value = "INVOKE",
					target = "Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;resetAI()V",
					shift = At.Shift.AFTER,
					remap = false
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			remap = false
	)
	private void onSpawnPixelmon(PokemonSpecification spec, CallbackInfo ci,
								 int x, int y, int z, boolean valid,
								 com.pixelmonmod.pixelmon.api.events.spawning.PixelmonSpawnerEvent.Pre event,
								 PixelmonEntity pixelmon) {

		if (pixelmon != null && pixelmon.level instanceof ServerWorld) {
			SpawnPointHelper.setSpawnPoint(pixelmon);
		}
	}
}
