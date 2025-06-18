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
public class SandstormEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();

	private static final float MAX_PARTICLES_PER_TICK_BASE = 100;
	private static final double PARTICLE_MAX_Y_OFFSET = 12.0;
	private static final double PARTICLE_MIN_Y_OFFSET = -3.0;

	private static final double CLOSE_SPAWN_CHANCE = 0.65;
	private static final double CLOSE_SPAWN_RADIUS = 16.0;
	private static final double DIRECTION_DEVIATION = 0.3;
	private static final double Y_LEVEL_BIAS = 0.70;

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	public SandstormEnvironment() {
		super(Environment.SANDSTORM);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		if (player == null) return;

		if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
			spawnSandParticles(player, delta);
		}

		if (soundTimer <= 0) {
			soundTimer = 300 + RANDOM.nextInt(300);
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
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.SANDSTORM)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private void spawnSandParticles(PlayerEntity player, float delta) {
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

		double directionX = (Math.sin(player.tickCount * 0.01) * 0.5 + 0.5) * DIRECTION_DEVIATION;
		double directionZ = 1.0;

		double length = Math.sqrt(directionX * directionX + directionZ * directionZ);
		directionX /= length;
		directionZ /= length;

		directionX *= 2.0;
		directionZ *= 2.0;

		double spawnRadius = 32F;

		for (int i = 0; i < particlesToSpawn; i++) {
			boolean spawnClose = RANDOM.nextDouble() < CLOSE_SPAWN_CHANCE;
			double effectiveRadius = spawnClose ? CLOSE_SPAWN_RADIUS : spawnRadius;

			double angle = RANDOM.nextDouble() * Math.PI * 2.0;
			double distance = RANDOM.nextDouble() * effectiveRadius;

			double dX = Math.sin(angle) * distance;
			double dZ = Math.cos(angle) * distance;

			double northBias = 0.4;
			double spawnX = player.getX() + dX;
			double spawnZ;

			if (RANDOM.nextDouble() < northBias) {
				spawnZ = player.getZ() + dZ - RANDOM.nextDouble() * 10.0;
			} else {
				spawnZ = player.getZ() + dZ;
			}

			double yRandom = RANDOM.nextDouble();

			if (RANDOM.nextDouble() < Y_LEVEL_BIAS) {
				yRandom = yRandom * 0.6 + 0.2;
			}
			double spawnY = player.getY() + PARTICLE_MIN_Y_OFFSET + yRandom * (PARTICLE_MAX_Y_OFFSET - PARTICLE_MIN_Y_OFFSET);
			BlockPos blockPos = new BlockPos(spawnX, spawnY, spawnZ);
			if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) continue;

			if (spawnY > player.getY() + 10.0 && !world.canSeeSky(blockPos)) {
				continue;
			}

			double distanceFactor = 1.0 - (distance / effectiveRadius);

			double xSpeed = directionX * (0.15 + 0.25 * distanceFactor) + (RANDOM.nextFloat() * 0.15 - 0.075);
			double ySpeed = -0.01 - RANDOM.nextFloat() * 0.02;
			double zSpeed = directionZ * (0.15 + 0.25 * distanceFactor) + (RANDOM.nextFloat() * 0.15 - 0.075);

			double particleTypeRoll = RANDOM.nextDouble();
			if (particleTypeRoll < 0.3) {
				world.addParticle(ParticleRegistry.SANDSTORM_DARK.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleTypeRoll < 0.6) {
				world.addParticle(ParticleRegistry.SANDSTORM_MEDIUM.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else {
				world.addParticle(ParticleRegistry.SANDSTORM_LIGHT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			}
		}
	}
}
