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
public class HeatCaveEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();

	private static final float MAX_PARTICLES_PER_TICK = 10;
	private static final double PARTICLE_MAX_Y_OFFSET = 12.0;
	private static final double PARTICLE_MIN_Y_OFFSET = -5.0;

	private static final float MIN_SPAWN_RADIUS = 2.0F;
	private static final float MAX_SPAWN_RADIUS = 15.0F;

	private static final double SPEED_FACTOR_BASE = 0.02;
	private static final double Y_SPEED_BASE = -0.06;

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	private static final double HARDENED_CHANCE = 0.20;
	private static final double SUPERCOOL_CHANCE = 0.25;
	private static final double COOL_CHANCE = 0.30;
	private static final double HOT_CHANCE = 0.20;

	public HeatCaveEnvironment() {
		super(Environment.HEAT_CAVE);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		World world = getClientWorld();
		if (player == null || world == null) return;

		if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
			spawnHeatCaveParticles(player, world, delta);
		}

		if (soundTimer <= 0) {
			if (RANDOM.nextFloat() < 0.15f) {
				world.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_POP, SoundCategory.AMBIENT, 0.4F + RANDOM.nextFloat() * 0.2F, 0.5F + RANDOM.nextFloat() * 0.2F, false);
			}

			soundTimer = 80 + RANDOM.nextInt(100);
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
				world.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundCategory.AMBIENT, 1.0F, 0.7F, false);
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
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.HEAT_CAVE)) {
				return true;
			}
		}

		return false;
	}

	private void spawnHeatCaveParticles(PlayerEntity player, World world, float delta) {
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

		double centerX = player.getX();
		double centerY = player.getY() + 1.5;
		double centerZ = player.getZ();

		for (int i = 0; i < particlesToSpawn; i++) {
			float angle = (float) (RANDOM.nextDouble() * Math.PI * 2.0);
			float radius = MIN_SPAWN_RADIUS + RANDOM.nextFloat() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

			double spawnX = centerX + Math.sin(angle) * radius;
			double spawnZ = centerZ + Math.cos(angle) * radius;

			double yOffset = PARTICLE_MIN_Y_OFFSET + RANDOM.nextDouble() * (PARTICLE_MAX_Y_OFFSET - PARTICLE_MIN_Y_OFFSET);
			double spawnY = centerY + yOffset;

			BlockPos blockPos = new BlockPos(spawnX, spawnY, spawnZ);
			if (!world.getBlockState(blockPos).getMaterial().isReplaceable()) continue;

			double speedFactor = SPEED_FACTOR_BASE + RANDOM.nextDouble() * 0.01;

			double xSpeed = (RANDOM.nextDouble() * 2.0 - 1.0) * speedFactor;
			double zSpeed = (RANDOM.nextDouble() * 2.0 - 1.0) * speedFactor;

			double ySpeed = Y_SPEED_BASE - RANDOM.nextDouble() * 0.04;

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
