package ethan.hoenn.rnbrules.ai.goal;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.SpawnPointHelper;
import java.util.EnumSet;
import java.util.Random;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwimToSpawnPointGoal extends Goal {

	private final PixelmonEntity pixelmon;
	private final double speed;
	private final int maxDistanceSquared;
	private final int searchRadius = 2;
	private int pathRecalcDelay = 0;
	private BlockPos targetWaterPos;
	private final Random random = new Random();

	public SwimToSpawnPointGoal(PixelmonEntity pixelmon, double speed, int maxDistance) {
		this.pixelmon = pixelmon;
		this.speed = speed;
		this.maxDistanceSquared = maxDistance * maxDistance;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!this.pixelmon.isInWater() || !SpawnPointHelper.hasSpawnPoint(this.pixelmon)) {
			return false;
		}
		return SpawnPointHelper.distanceToSpawnSq(this.pixelmon) > this.maxDistanceSquared;
	}

	@Override
	public void start() {
		this.pathRecalcDelay = 0;
		this.targetWaterPos = null;
		tryFindValidPoint();
	}

	private void tryFindValidPoint() {
		BlockPos spawnPointBase = SpawnPointHelper.getSpawnPoint(this.pixelmon);
		World world = this.pixelmon.level;

		for (int attempts = 0; attempts < 15; attempts++) {
			int offsetX = this.random.nextInt(this.searchRadius * 2 + 1) - this.searchRadius;
			int offsetZ = this.random.nextInt(this.searchRadius * 2 + 1) - this.searchRadius;

			BlockPos potentialXZBase = new BlockPos(spawnPointBase.getX() + offsetX, this.pixelmon.getY(), spawnPointBase.getZ() + offsetZ);

			for (int yOffset = 0; yOffset >= -4; yOffset--) {
				BlockPos checkPos = potentialXZBase.offset(0, yOffset, 0);
				if (isSuitableWaterBlock(world, checkPos)) {
					this.targetWaterPos = checkPos;
					return;
				}
			}
			for (int yOffset = 1; yOffset <= 3; yOffset++) {
				BlockPos checkPos = potentialXZBase.offset(0, yOffset, 0);
				if (isSuitableWaterBlock(world, checkPos)) {
					this.targetWaterPos = checkPos;
					return;
				}
			}
		}
	}

	private boolean isSuitableWaterBlock(World world, BlockPos pos) {
		FluidState fluidState = world.getFluidState(pos);

		if (fluidState.is(FluidTags.WATER) && fluidState.isSource()) {
			BlockPos posAbove = pos.above();
			FluidState fluidAbove = world.getFluidState(posAbove);
			return fluidAbove.is(FluidTags.WATER) || !world.getBlockState(posAbove).getMaterial().isSolid();
		}
		return false;
	}

	@Override
	public void tick() {
		if (this.targetWaterPos == null) {
			return;
		}

		if (--this.pathRecalcDelay <= 0) {
			this.pathRecalcDelay = 30 + this.random.nextInt(30);
			this.pixelmon.getNavigation().moveTo(this.targetWaterPos.getX() + 0.5D, this.targetWaterPos.getY() + 0.5D, this.targetWaterPos.getZ() + 0.5D, this.speed);
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (!this.pixelmon.isInWater() || !SpawnPointHelper.hasSpawnPoint(this.pixelmon) || this.targetWaterPos == null || this.pixelmon.getNavigation().isDone()) {
			return false;
		}

		double distSqToSpawn = SpawnPointHelper.distanceToSpawnSq(this.pixelmon);
		return distSqToSpawn > this.maxDistanceSquared;
	}

	@Override
	public void stop() {
		this.pixelmon.getNavigation().stop();
		this.targetWaterPos = null;
	}
}
