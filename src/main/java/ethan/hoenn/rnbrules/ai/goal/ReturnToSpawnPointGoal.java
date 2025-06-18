package ethan.hoenn.rnbrules.ai.goal;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.SpawnPointHelper;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class ReturnToSpawnPointGoal extends Goal {

	private final PixelmonEntity pixelmon;
	private final double speed;
	private final int maxDistanceSquared;
	private int pathRecalcDelay = 0;

	public ReturnToSpawnPointGoal(PixelmonEntity pixelmon, double speed, int maxDistance) {
		this.pixelmon = pixelmon;
		this.speed = speed;
		this.maxDistanceSquared = maxDistance * maxDistance;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!SpawnPointHelper.hasSpawnPoint(pixelmon)) {
			return false;
		}
		return SpawnPointHelper.distanceToSpawnSq(pixelmon) > maxDistanceSquared;
	}

	@Override
	public void start() {
		pathRecalcDelay = 0;
	}

	@Override
	public void tick() {
		if (--pathRecalcDelay <= 0) {
			pathRecalcDelay = 10;
			BlockPos spawnPoint = SpawnPointHelper.getSpawnPoint(pixelmon);
			this.pixelmon.getNavigation().moveTo(spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, this.speed);
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (!SpawnPointHelper.hasSpawnPoint(pixelmon)) {
			return false;
		}

		double distSq = SpawnPointHelper.distanceToSpawnSq(pixelmon);
		return distSq > maxDistanceSquared && !pixelmon.getNavigation().isDone();
	}
}
