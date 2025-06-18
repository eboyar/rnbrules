package ethan.hoenn.rnbrules.environment;

import ethan.hoenn.rnbrules.environment.client.ClientEnvironmentController;
import ethan.hoenn.rnbrules.registries.ParticleRegistry;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AuroraVeilEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();
	private static final float MAX_PARTICLES_PER_TICK_BASE = 28.0F;
	private static final double MIN_Y_OFFSET = -5.0D;
	private static final double MAX_Y_OFFSET = 5.0D;

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	public AuroraVeilEnvironment() {
		super(Environment.AURORA_VEIL);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		if (player == null) return;

		if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
			spawnAuroraParticles(player, delta);
		}

		if (soundTimer <= 0) {
			soundTimer = 300 + RANDOM.nextInt(200);
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
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.AURORA_VEIL)) {
				return true;
			}
		}
		return false;
	}

	private void spawnAuroraParticles(PlayerEntity player, float delta) {
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

		double spawnRadius = 22.0D;

		for (int i = 0; i < particlesToSpawn; i++) {
			double angle = RANDOM.nextDouble() * Math.PI * 2.0;
			double distance = RANDOM.nextDouble() * spawnRadius;

			double offsetX = Math.sin(angle) * distance;
			double offsetZ = Math.cos(angle) * distance;

			double spawnX = player.getX() + offsetX;
			double spawnZ = player.getZ() + offsetZ;

			double spawnY = player.getY() + MIN_Y_OFFSET + (RANDOM.nextDouble() * (MAX_Y_OFFSET - MIN_Y_OFFSET));

			BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
			BlockState spawnState = world.getBlockState(spawnPos);

			if (spawnState.getMaterial().isReplaceable() && spawnState.getMaterial() != Material.WATER) {
				continue;
			}

			BlockPos abovePos = spawnPos.above();
			BlockState aboveState = world.getBlockState(abovePos);

			if (!aboveState.getMaterial().isReplaceable() || aboveState.getMaterial().isLiquid()) {
				continue;
			}

			boolean inWater = spawnState.getMaterial() == Material.WATER || world.getFluidState(spawnPos).isSource();

			double oscillationFactor = 0.005 + (RANDOM.nextDouble() * 0.003);
			double riseSpeed = 0.07 + (RANDOM.nextDouble() * 0.03);

			double initialPhase = RANDOM.nextDouble() * Math.PI * 2.0;

			if (inWater) {
				riseSpeed *= 0.9D;
				oscillationFactor *= 0.95D;
			}

			double xSpeed = Math.sin(initialPhase) * oscillationFactor;
			double ySpeed = riseSpeed;
			double zSpeed = Math.cos(initialPhase) * oscillationFactor;

			float particleRoll = RANDOM.nextFloat();

			if (particleRoll < 0.1f) {
				world.addParticle(ParticleRegistry.FROST_ROCK.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < 0.2f) {
				world.addParticle(ParticleRegistry.FROST_ROCK_LIGHT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < 0.5f) {
				world.addParticle(ParticleRegistry.SNOW_STAR.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < 0.6f) {
				world.addParticle(ParticleRegistry.GLOWSTONE_LIGHT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else if (particleRoll < 0.8f) {
				world.addParticle(ParticleRegistry.FROST_LIGHT.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			} else {
				world.addParticle(ParticleRegistry.FROST_STAR.get(), spawnX, spawnY, spawnZ, xSpeed, ySpeed, zSpeed);
			}
		}
	}
}
