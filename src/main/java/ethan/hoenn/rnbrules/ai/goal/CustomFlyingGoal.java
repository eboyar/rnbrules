package ethan.hoenn.rnbrules.ai.goal;

import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;

public class CustomFlyingGoal extends Goal {
	private static final long OWNER_FIND_INTERVAL;
	private static final double OWNER_DISTANCE_TO_TAKEOFF = (double)100.0F;
	private static final long TAKEOFF_COOLDOWN_MS = 1000;

	private final PixelmonEntity pixelmon;
	private long nextOwnerCheckTime;
	private long lastTakeoffAttempt = 0;
	private BlockPos currentFlightTarget;
	private int flightTicks = 0;
	private double takeOffSpeed = (double)0.0F;
	private int targetHeight = 0;
	private boolean takingOff = false;
	private int ticksToRefresh;
	private int nextWingBeat = 10;
	private int wingBeatTick = 0;
	boolean lastChangeDirection;

	public CustomFlyingGoal(PixelmonEntity entity) {
		this.pixelmon = entity;
		this.nextOwnerCheckTime = System.currentTimeMillis();
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	public boolean canUse() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastTakeoffAttempt < TAKEOFF_COOLDOWN_MS) {
			return false;
		}

		boolean canTakeoff = this.pixelmon.isOnGround() && this.pixelmon.getOwner() == null && this.pixelmon.getFlyingParameters() != null ? this.checkTakeOffConditions() : false;

		if (canTakeoff) {
			lastTakeoffAttempt = currentTime;
		}

		return canTakeoff;
	}

	private boolean checkTakeOffConditions() {
		if (this.pixelmon.grounded) {
			return false;
		} else if (this.pixelmon.getOwner() != null && this.pixelmon.getOwner().isAlive() && this.pixelmon.distanceToSqr(this.pixelmon.getOwner()) > (double)100.0F) {
			return true;
		} else {
			PlayerEntity nearest = this.pixelmon.level.getNearestPlayer(this.pixelmon, (double)6.0F);
			return nearest != null && nearest != this.pixelmon.getOwner() || Math.random() < 0.015;
		}
	}

	public boolean canContinueToUse() {
		return !this.pixelmon.isOnGround();
	}

	public void start() {
		this.takeOff();
	}

	public void tick() {
		++this.flightTicks;
		if (this.flightTicks > 100 && this.takingOff || this.takingOff && this.pixelmon.getY() >= (double)this.targetHeight) {
			this.takingOff = false;
			this.flightTicks = 0;
		}

		if (this.takingOff) {
			this.pixelmon.setDeltaMovement(new Vector3d((double)((float)this.pixelmon.getPokemon().getStat(BattleStatsType.SPEED) / 500.0F), (double)0.0F, (double)0.0F));
			this.pixelmon.setDeltaMovement(this.pixelmon.getDeltaMovement().x(), this.takeOffSpeed, this.pixelmon.getDeltaMovement().z());
		} else {
			if (this.pixelmon.getOwner() != null) {
				this.lookForOwnerEntity();
			}

			this.checkForLandingSpot();
			AxisAlignedBB box = this.pixelmon.getBoundingBox();
			RayTraceResult mop = this.pixelmon.level.clip(new RayTraceContext(new Vector3d(this.pixelmon.getX(), box.minY, this.pixelmon.getZ()), new Vector3d(this.pixelmon.getX() + this.pixelmon.getDeltaMovement().x() * (double)100.0F, box.minY, this.pixelmon.getZ() + this.pixelmon.getDeltaMovement().z() * (double)100.0F), BlockMode.COLLIDER, FluidMode.SOURCE_ONLY, this.pixelmon));
			if (mop.getType() == Type.MISS) {
				mop = this.pixelmon.level.clip(new RayTraceContext(new Vector3d(this.pixelmon.getX(), box.maxY, this.pixelmon.getZ()), new Vector3d(this.pixelmon.getX() + this.pixelmon.getDeltaMovement().x() * (double)100.0F, box.maxY, this.pixelmon.getZ() + this.pixelmon.getDeltaMovement().z() * (double)100.0F), BlockMode.COLLIDER, FluidMode.SOURCE_ONLY, this.pixelmon));
			}

			if (this.hasLandingSpot()) {
				if (mop.getType() == Type.MISS) {
					double d0 = (double)this.currentFlightTarget.getX() + (double)0.5F - this.pixelmon.getX();
					double d1 = (double)this.currentFlightTarget.getY() + 0.1 - this.pixelmon.getY();
					double d2 = (double)this.currentFlightTarget.getZ() + (double)0.5F - this.pixelmon.getZ();
					this.pixelmon.setDeltaMovement(this.pixelmon.getDeltaMovement().x() + (Math.signum(d0) - this.pixelmon.getDeltaMovement().x()) * (double)0.1F, this.pixelmon.getDeltaMovement().y() + (Math.signum(d1) * (double)0.7F - this.pixelmon.getDeltaMovement().y()) * (double)0.1F, this.pixelmon.getDeltaMovement().z() + (Math.signum(d2) - this.pixelmon.getDeltaMovement().z()) * (double)0.1F);
					float f = (float)(Math.atan2(this.pixelmon.getDeltaMovement().z(), this.pixelmon.getDeltaMovement().x()) * (double)180.0F / Math.PI) - 90.0F;
					float f1 = MathHelper.wrapDegrees(f - this.pixelmon.yRot);
					PixelmonEntity var10000 = this.pixelmon;
					var10000.yRot += f1;
				}
			} else {
				this.maintainFlight(mop.getType() != Type.MISS);
			}

			super.tick();
		}
	}

	private void checkForLandingSpot() {
		if (this.currentFlightTarget != null && (!this.pixelmon.level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
			this.currentFlightTarget = null;
		}

		if (this.currentFlightTarget == null || this.pixelmon.getRandom().nextInt(30) == 0) {
			this.currentFlightTarget = new BlockPos((int)(this.pixelmon.getX() + this.pixelmon.getDeltaMovement().x() * (double)200.0F + (double)this.pixelmon.getRandom().nextInt(10) - (double)5.0F), 0, (int)(this.pixelmon.getZ() + this.pixelmon.getDeltaMovement().z() * (double)200.0F + (double)this.pixelmon.getRandom().nextInt(10) - (double)5.0F));
			this.currentFlightTarget = this.pixelmon.level.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING, this.currentFlightTarget);
			BlockState state = this.pixelmon.level.getBlockState(this.currentFlightTarget);
			Material m = state.getMaterial();
			this.currentFlightTarget = this.currentFlightTarget.above();
			if (this.pixelmon.getFlyingParameters() != null && !this.pixelmon.getFlyingParameters().getLandingMaterials().willLand(m) || !this.pixelmon.level.isEmptyBlock(this.currentFlightTarget)) {
				this.currentFlightTarget = null;
			}
		}
	}

	private boolean hasLandingSpot() {
		return this.currentFlightTarget != null;
	}

	private void maintainFlight(boolean hasObstacle) {
		++this.wingBeatTick;
		if (hasObstacle || this.wingBeatTick >= this.nextWingBeat) {
			this.pickDirection(hasObstacle);
			this.nextWingBeat = this.pixelmon.getFlyingParameters().getFlapRate() + (int)(Math.random() * 0.4 * (double)this.pixelmon.getFlyingParameters().getFlapRate() - 0.2 * (double)this.pixelmon.getFlyingParameters().getFlapRate());
			this.pixelmon.travel(new Vector3d((double)0.0F, (double)0.0F, (double)(4.0F + (float)this.pixelmon.getPokemon().getStat(BattleStatsType.SPEED) / 100.0F * this.pixelmon.getFlyingParameters().getFlySpeedModifier())));
			this.pixelmon.setDeltaMovement(this.pixelmon.getDeltaMovement().x(), (double)(this.pixelmon.getFlyingParameters().getFlapRate() + 1) * 0.01, this.pixelmon.getDeltaMovement().z());
			this.wingBeatTick = 0;
		}
	}

	public void pickDirection(boolean useLastChangeDirection) {
		double rotAmt;
		if (useLastChangeDirection) {
			rotAmt = (double)(this.pixelmon.getRandom().nextInt(5) + 5);
			if (this.lastChangeDirection) {
				rotAmt *= (double)-1.0F;
			}
		} else {
			rotAmt = (double)(this.pixelmon.getRandom().nextInt(10) - 5);
			this.lastChangeDirection = rotAmt > (double)0.0F;
		}

		PixelmonEntity var10000 = this.pixelmon;
		var10000.yRot = (float)((double)var10000.yRot + rotAmt);
	}

	private void lookForOwnerEntity() {
		if (this.pixelmon.getOwner() != null && System.currentTimeMillis() > this.nextOwnerCheckTime) {
			this.nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
			this.currentFlightTarget = new BlockPos((int)this.pixelmon.getOwner().getX(), (int)this.pixelmon.getOwner().getY() + 1, (int)this.pixelmon.getOwner().getZ());
		}
	}

	private void takeOff() {
		this.pixelmon.setFlying(true);
		this.takingOff = true;
		this.flightTicks = 0;
		this.targetHeight = (int)this.pixelmon.getY() + RandomHelper.getRandomNumberBetween(this.pixelmon.getFlyingParameters().getFlyHeightMin(), this.pixelmon.getFlyingParameters().getFlyHeightMax());
		this.pixelmon.level.playSound((PlayerEntity)null, this.pixelmon.getX(), this.pixelmon.getY(), this.pixelmon.getZ(), (SoundEvent)SoundRegistration.TAKE_OFF.get(), SoundCategory.NEUTRAL, 0.8F, 1.0F);
		this.takeOffSpeed = 0.012 + (double)((float)this.pixelmon.getPokemon().getStat(BattleStatsType.SPEED) / 600.0F);
	}

	static {
		OWNER_FIND_INTERVAL = TimeUnit.SECONDS.toMillis(10L);
	}
}