package ethan.hoenn.rnbrules.environment;

import ethan.hoenn.rnbrules.environment.client.ClientEnvironmentController;
import ethan.hoenn.rnbrules.registries.ParticleRegistry;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaStormEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();

	private static final float MAX_PARTICLES_PER_TICK = 25;
	private static final double PARTICLE_MAX_Y_OFFSET = 15.0;
	private static final double PARTICLE_MIN_Y_OFFSET = -8.0;

	private static final float MIN_SPAWN_RADIUS = 4.0F;
	private static final float MAX_SPAWN_RADIUS = 13.0F;

	private double centerOffsetX = 0.0;
	private double centerOffsetZ = 0.0;
	private double targetOffsetX = 0.0;
	private double targetOffsetZ = 0.0;
	private int offsetUpdateTimer = 0;
	private static final int OFFSET_UPDATE_FREQUENCY = 100;

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	private static final double HARDENED_CHANCE = 0.05;
	private static final double SUPERCOOL_CHANCE = 0.15;
	private static final double COOL_CHANCE = 0.25;
	private static final double HOT_CHANCE = 0.30;

	public MagmaStormEnvironment() {
		super(Environment.MAGMA_STORM);
		updateTornadoOffset(1.0f);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		World world = getClientWorld();
		if (player == null || world == null) return;

		offsetUpdateTimer++;
		if (offsetUpdateTimer >= OFFSET_UPDATE_FREQUENCY) {
			offsetUpdateTimer = 0;
			updateTornadoOffset(0.1f);
		} else {
			centerOffsetX += (targetOffsetX - centerOffsetX) * 0.02;
			centerOffsetZ += (targetOffsetZ - centerOffsetZ) * 0.02;
		}

		if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
			spawnMagmaParticles(player, world, delta);
		}

		if (soundTimer <= 0) {
			if (RANDOM.nextFloat() < 0.2f) {
				world.playLocalSound(
					player.getX() + centerOffsetX,
					player.getY(),
					player.getZ() + centerOffsetZ,
					SoundEvents.LAVA_AMBIENT,
					SoundCategory.AMBIENT,
					0.6F + RANDOM.nextFloat() * 0.2F,
					0.6F + RANDOM.nextFloat() * 0.2F,
					false
				);
			}

			soundTimer = 40 + RANDOM.nextInt(60);
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
			World world = getClientWorld();
			PlayerEntity player = getClientPlayer();

			if (world != null && player != null) {
				world.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundCategory.AMBIENT, 2.0F, 0.5F, false);
			}

			soundTimer = 20;
		}
	}

	@Override
	public void onExit(float progress) {
		currentIntensity = 1.0F - progress;
	}

	@Override
	public boolean isInEnvironment(World world, BlockPos pos) {
		if (world == null) return false;

		PlayerEntity player = getClientPlayer();
		if (player != null) {
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.MAGMA_STORM)) {
				return true;
			}
		}

		return false;
	}

	private void updateTornadoOffset(float changeAmount) {
		float maxOffset = 6.0f;
		targetOffsetX = (RANDOM.nextFloat() * 2.0 - 1.0) * maxOffset * changeAmount + targetOffsetX * (1.0f - changeAmount);
		targetOffsetZ = (RANDOM.nextFloat() * 2.0 - 1.0) * maxOffset * changeAmount + targetOffsetZ * (1.0f - changeAmount);

		targetOffsetX = MathHelper.clamp(targetOffsetX, -maxOffset, maxOffset);
		targetOffsetZ = MathHelper.clamp(targetOffsetZ, -maxOffset, maxOffset);
	}

	private void spawnMagmaParticles(PlayerEntity player, World world, float delta) {
		float effectiveMaxParticles = MAX_PARTICLES_PER_TICK;
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

		double centerX = player.getX() + centerOffsetX;
		double centerY = player.getY() + 1.5;
		double centerZ = player.getZ() + centerOffsetZ;

		for (int i = 0; i < particlesToSpawn; i++) {
			float angle = (float) (RANDOM.nextDouble() * Math.PI * 2.0);
			float radius = MIN_SPAWN_RADIUS + RANDOM.nextFloat() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

			double spawnX = centerX + Math.sin(angle) * radius;
			double spawnZ = centerZ + Math.cos(angle) * radius;

			double yBias = RANDOM.nextDouble();
			yBias = yBias * yBias;
			double spawnY = centerY + PARTICLE_MIN_Y_OFFSET + yBias * (PARTICLE_MAX_Y_OFFSET - PARTICLE_MIN_Y_OFFSET);

			BlockPos blockPos = new BlockPos(spawnX, spawnY, spawnZ);
			if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) continue;

			double dx = spawnX - centerX;
			double dz = spawnZ - centerZ;
			double dist = Math.sqrt(dx * dx + dz * dz);

			if (dist > 0.0001) {
				dx /= dist;
				dz /= dist;
			}

			double speedFactor = 0.05 + RANDOM.nextDouble() * 0.05;
			double xSpeed = dz * speedFactor;
			double zSpeed = -dx * speedFactor;

			double radialFactor = 0.02 * (RANDOM.nextDouble() * 2.0 - 1.0);
			xSpeed += dx * radialFactor;
			zSpeed += dz * radialFactor;

			double ySpeed = -0.04 - RANDOM.nextDouble() * 0.06;

			double particleRoll = RANDOM.nextDouble();

			if (particleRoll < HARDENED_CHANCE) {
				world.addParticle(ParticleRegistry.MAGMA_HARDENED.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < HARDENED_CHANCE + SUPERCOOL_CHANCE) {
				world.addParticle(ParticleRegistry.MAGMA_SUPERCOOL.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < HARDENED_CHANCE + SUPERCOOL_CHANCE + COOL_CHANCE) {
				world.addParticle(ParticleRegistry.MAGMA_COOL.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < HARDENED_CHANCE + SUPERCOOL_CHANCE + COOL_CHANCE + HOT_CHANCE) {
				world.addParticle(ParticleRegistry.MAGMA_HOT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else {
				world.addParticle(ParticleRegistry.MAGMA_SUPERHOT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			}
		}
	}
}
