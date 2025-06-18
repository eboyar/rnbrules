package ethan.hoenn.rnbrules.environment;

import ethan.hoenn.rnbrules.environment.client.ClientEnvironmentController;
import ethan.hoenn.rnbrules.registries.ParticleRegistry;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TailwindEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();

	private static final float MAX_PARTICLES_PER_TICK_BASE = 45;
	private static final double PARTICLE_MAX_Y_OFFSET = 20.0;
	private static final double PARTICLE_MIN_Y_OFFSET = -8.0;
	private static final double CLOSE_SPAWN_CHANCE = 0.55;
	private static final double CLOSE_SPAWN_RADIUS = 30.0;
	private static final double DIRECTION_DEVIATION = 0.20;
	private static final double Y_LEVEL_BIAS = 0.40;
	private static final double VERTICAL_LAYER_FACTOR = 0.75;

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	public TailwindEnvironment() {
		super(Environment.TAILWIND);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		if (player == null) return;

		if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
			spawnWindParticles(player, delta);
		}

		if (soundTimer <= 0) {
			soundTimer = 250 + RANDOM.nextInt(200);
		} else {
			soundTimer--;
		}
	}

	@Override
	public void render(float partialTicks, float intensity) {
		this.currentIntensity = intensity;
	}

	@Override
	public void onEnter(float progress) {
		currentIntensity = progress;

		if (progress > 0.1F && soundTimer <= 0) {
			soundTimer = 20;
		}
	}

	@Override
	public void onExit(float progress) {
		currentIntensity = 1.0F - progress;
	}

	public float getCurrentIntensity() {
		return this.currentIntensity;
	}

	@Override
	public boolean isInEnvironment(World world, BlockPos pos) {
		if (world == null) return false;

		PlayerEntity player = getClientPlayer();
		if (player != null) {
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.TAILWIND)) {
				return true;
			}
		}
		return false;
	}

	private void spawnWindParticles(PlayerEntity player, float delta) {
		World world = player.level;
		if (world == null) return;
		float effectiveMaxParticles = MAX_PARTICLES_PER_TICK_BASE;
		ParticleStatus particleStatus = Minecraft.getInstance().options.particles;

		switch (particleStatus) {
			case DECREASED:
				effectiveMaxParticles *= 0.6f;
				break;
			case MINIMAL:
				effectiveMaxParticles *= 0.2f;
				break;
			case ALL:
				effectiveMaxParticles *= 1.2f;
				break;
			default:
				break;
		}

		int particlesToSpawn = MathHelper.ceil(effectiveMaxParticles * currentIntensity);
		if (particlesToSpawn <= 0) return;

		double baseDirectionX1 = (Math.sin(player.tickCount * 0.008) * 0.7 + 0.1) * DIRECTION_DEVIATION;
		double baseDirectionZ1 = 1.0;

		double baseDirectionX2 = (Math.sin(player.tickCount * 0.012) * 0.5) * DIRECTION_DEVIATION;
		double baseDirectionZ2 = 1.0;

		double baseDirectionX3 = (Math.sin(player.tickCount * 0.01) * 0.3) * DIRECTION_DEVIATION;
		double baseDirectionZ3 = 1.0;

		double length1 = Math.sqrt(baseDirectionX1 * baseDirectionX1 + baseDirectionZ1 * baseDirectionZ1);
		baseDirectionX1 /= length1;
		baseDirectionZ1 /= length1;

		double length2 = Math.sqrt(baseDirectionX2 * baseDirectionX2 + baseDirectionZ2 * baseDirectionZ2);
		baseDirectionX2 /= length2;
		baseDirectionZ2 /= length2;

		double length3 = Math.sqrt(baseDirectionX3 * baseDirectionX3 + baseDirectionZ3 * baseDirectionZ3);
		baseDirectionX3 /= length3;
		baseDirectionZ3 /= length3;

		final double speedFactor1 = 1.8;
		final double speedFactor2 = 2.2;
		final double speedFactor3 = 2.65;

		baseDirectionX1 *= speedFactor1;
		baseDirectionZ1 *= speedFactor1;

		baseDirectionX2 *= speedFactor2;
		baseDirectionZ2 *= speedFactor2;

		baseDirectionX3 *= speedFactor3;
		baseDirectionZ3 *= speedFactor3;

		double spawnRadius = 60.0F;
		for (int i = 0; i < particlesToSpawn; i++) {
			boolean spawnClose = RANDOM.nextDouble() < CLOSE_SPAWN_CHANCE;
			double effectiveRadius = spawnClose ? CLOSE_SPAWN_RADIUS : spawnRadius;

			double angle = RANDOM.nextDouble() * Math.PI * 2.0;

			double distanceFactor = Math.sqrt(RANDOM.nextDouble());
			double distance = distanceFactor * effectiveRadius;

			double dX = Math.sin(angle) * distance;
			double dZ = Math.cos(angle) * distance;

			double northBias = 0.70;
			double spawnX = player.getX() + dX;
			double spawnZ;

			if (RANDOM.nextDouble() < northBias) {
				double northDistance = RANDOM.nextDouble() * RANDOM.nextDouble() * 25.0;
				spawnZ = player.getZ() + Math.min(dZ, 0) - northDistance;
			} else {
				spawnZ = player.getZ() + dZ;
			}

			double yRandom = RANDOM.nextDouble();

			int layer;

			if (RANDOM.nextDouble() < Y_LEVEL_BIAS) {
				yRandom = 0.3 + (RANDOM.nextDouble() * 0.4);
				layer = 2;
			} else if (yRandom < 0.4) {
				yRandom = RANDOM.nextDouble() * 0.3;
				layer = 1;
			} else {
				yRandom = 0.7 + (RANDOM.nextDouble() * 0.3);
				layer = 3;
			}

			double spawnY = player.getY() + PARTICLE_MIN_Y_OFFSET + yRandom * (PARTICLE_MAX_Y_OFFSET - PARTICLE_MIN_Y_OFFSET);

			BlockPos blockPos = new BlockPos(spawnX, spawnY, spawnZ);
			if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) continue;

			if (spawnY > player.getY() + 10.0 && !world.canSeeSky(blockPos)) {
				continue;
			}

			double xDirection, zDirection;

			switch (layer) {
				case 1:
					xDirection = baseDirectionX1;
					zDirection = baseDirectionZ1;
					break;
				case 3:
					xDirection = baseDirectionX3;
					zDirection = baseDirectionZ3;
					break;
				default:
					xDirection = baseDirectionX2;
					zDirection = baseDirectionZ2;
					break;
			}

			double distFactor = 1.0 - (distance / effectiveRadius) * 0.7;

			double heightFactor = 0.8 + ((yRandom - 0.5) * VERTICAL_LAYER_FACTOR);

			double speedFactor = distFactor * heightFactor;

			double speedVariance = 0.85 + (RANDOM.nextDouble() * 0.3);

			double xSpeed = xDirection * speedFactor * speedVariance + (RANDOM.nextDouble() * 0.1 - 0.05);

			double yFactor = 0.5 + (yRandom * 0.5);
			double ySpeed = (0.005 + RANDOM.nextDouble() * 0.02) * yFactor;

			double zSpeed = zDirection * speedFactor * speedVariance + (RANDOM.nextDouble() * 0.1 - 0.05);

			float particleRoll = RANDOM.nextFloat();

			if (layer == 1) {
				if (particleRoll < 0.6f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.85f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.95f) {
					world.addParticle(ParticleRegistry.WIND_MYSTIC.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else {
					world.addParticle(ParticleRegistry.WIND_MYSTIC_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				}
			} else if (layer == 3) {
				if (particleRoll < 0.25f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.5f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.75f) {
					world.addParticle(ParticleRegistry.WIND_MYSTIC.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else {
					world.addParticle(ParticleRegistry.WIND_MYSTIC_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				}
			} else {
				if (particleRoll < 0.4f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.7f) {
					world.addParticle(ParticleRegistry.WIND_NORMAL_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else if (particleRoll < 0.9f) {
					world.addParticle(ParticleRegistry.WIND_MYSTIC.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				} else {
					world.addParticle(ParticleRegistry.WIND_MYSTIC_ANIM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
				}
			}
		}
	}
}
