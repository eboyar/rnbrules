package ethan.hoenn.rnbrules.environment.particles;

import java.util.Random;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AuroraVeilParticle extends SpriteTexturedParticle {

	private static final Random RANDOM = new Random();
	private final IAnimatedSprite spriteSet;
	private final boolean isLightSensitive;
	private final boolean isAnimated;
	private final int particleType;
	private final float oscillationFrequency;
	private final float oscillationAmplitude;
	private final float initialPhase;
	private double maxHeight;
	private final double startY;

	protected AuroraVeilParticle(
		ClientWorld world,
		double x,
		double y,
		double z,
		double xSpeed,
		double ySpeed,
		double zSpeed,
		IAnimatedSprite spriteSet,
		int particleType,
		boolean isLightSensitive,
		boolean isAnimated
	) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.spriteSet = spriteSet;
		this.particleType = particleType;
		this.isLightSensitive = isLightSensitive;
		this.isAnimated = isAnimated;

		this.startY = y;
		this.maxHeight = startY + 35.0D + (RANDOM.nextDouble() * 15.0D);

		this.maxHeight += (-3.0D + (RANDOM.nextDouble() * 6.0D));

		this.xd = xSpeed;

		this.yd = ySpeed + (RANDOM.nextDouble() * 0.02D);
		this.zd = zSpeed;
		this.oscillationFrequency = 0.02F + (RANDOM.nextFloat() * 0.01F);
		this.oscillationAmplitude = 0.005F + (RANDOM.nextFloat() * 0.005F);
		this.initialPhase = RANDOM.nextFloat() * 6.28F;
		switch (particleType) {
			case 0:
				this.quadSize = 0.15F + (RANDOM.nextFloat() * 0.2F);
				this.lifetime = 1140 + RANDOM.nextInt(380);
				this.gravity = 0.0F;
				this.alpha = 0.6F;
				this.setColor(0.8F, 0.9F, 1.0F);
				break;
			case 1:
				this.quadSize = 0.1F + (RANDOM.nextFloat() * 0.1F);
				this.lifetime = 1045 + RANDOM.nextInt(285);
				this.gravity = 0.0F;
				this.alpha = 0.7F;
				this.setColor(0.9F, 0.95F, 1.0F);
				break;
			case 2:
				this.quadSize = 0.06F + (RANDOM.nextFloat() * 0.08F);
				this.lifetime = 950 + RANDOM.nextInt(285);
				this.gravity = 0.0F;
				this.alpha = 0.8F;
				this.setColor(0.95F, 0.95F, 1.0F);
				break;
			case 3:
				this.quadSize = 0.05F + (RANDOM.nextFloat() * 0.07F);
				this.lifetime = 988 + RANDOM.nextInt(285);
				this.gravity = 0.0F;
				this.alpha = 0.9F;
				this.setColor(1.0F, 0.95F, 0.85F);
				break;
			case 4:
				this.quadSize = 0.04F + (RANDOM.nextFloat() * 0.06F);
				this.lifetime = 912 + RANDOM.nextInt(266);
				this.gravity = 0.0F;
				this.alpha = 0.8F;
				this.setColor(0.8F, 0.95F, 1.0F);
				break;
			case 5:
				this.quadSize = 0.03F + (RANDOM.nextFloat() * 0.05F);
				this.lifetime = 874 + RANDOM.nextInt(266);
				this.gravity = 0.0F;
				this.alpha = 0.7F;
				this.setColor(0.85F, 0.9F, 1.0F);
				break;
			default:
				this.quadSize = 0.1F;
				this.lifetime = 500 + RANDOM.nextInt(150);
				this.gravity = 0.0F;
				this.alpha = 0.8F;
				this.setColor(0.9F, 0.9F, 1.0F);
				break;
		}

		if (isAnimated) {
			this.pickSprite(spriteSet);
		} else {
			this.setSpriteFromAge(spriteSet);
		}
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		float lifeRatio = (float) this.age / (float) this.lifetime;

		if (this.isAnimated) {
			this.setSpriteFromAge(this.spriteSet);
		}

		if (this.y >= this.maxHeight) {
			this.remove();
			return;
		}

		if (RANDOM.nextInt(60) == 0) {
			this.alpha = Math.min(1.0F, this.alpha * 1.3F);
			this.quadSize *= 1.2F;

			this.yd += 0.03D;

			if (this.isLightSensitive) {
				this.rCol = Math.min(1.0F, this.rCol * 1.2F);
				this.gCol = Math.min(1.0F, this.gCol * 1.2F);
				this.bCol = Math.min(1.0F, this.bCol * 1.2F);
			}
		}
		float oscillationX = this.oscillationAmplitude * MathHelper.sin(this.initialPhase + this.age * this.oscillationFrequency);
		float oscillationZ = this.oscillationAmplitude * MathHelper.cos(this.initialPhase + this.age * this.oscillationFrequency);

		this.xd = (this.xd * 0.9D) + (oscillationX * 0.5);
		this.zd = (this.zd * 0.9D) + (oscillationZ * 0.5);

		this.yd = 0.12D + (RANDOM.nextFloat() * 0.01F);

		this.move(this.xd, this.yd, this.zd);

		this.xd *= 0.95D;
		this.yd *= 0.999D;
		this.zd *= 0.95D;

		if (this.isLightSensitive) {
			BlockPos pos = new BlockPos(this.x, this.y, this.z);
			int blockLight = this.level.getBrightness(LightType.BLOCK, pos);
			int skyLight = this.level.getBrightness(LightType.SKY, pos);
			int combinedLight = Math.max(blockLight, skyLight);

			float pulseFactor = 0.1F * MathHelper.sin(this.age * 0.1F);
			float lightFactor = Math.min((combinedLight / 15.0F + 0.4F) + pulseFactor, 1.0F);

			switch (this.particleType) {
				case 1:
					this.setColor(0.7F * lightFactor + 0.2F, 0.8F * lightFactor + 0.15F, 0.9F * lightFactor + 0.1F);
					break;
				case 3:
					this.setColor(1.0F * lightFactor, 0.9F * lightFactor + 0.05F, 0.7F * lightFactor + 0.15F);
					break;
				case 4:
					this.setColor(0.7F * lightFactor + 0.1F, 0.85F * lightFactor + 0.1F, 0.95F * lightFactor + 0.05F);
					break;
				default:
					break;
			}
		}

		float fadeStart = 0.75F;

		if (lifeRatio > fadeStart) {
			float lifeFade = 1.0F - ((lifeRatio - fadeStart) / (1.0F - fadeStart));

			lifeFade = (float) Math.pow(lifeFade, 0.5);
			this.alpha = Math.max(0.3F, this.alpha * lifeFade);
		}

		if (lifeRatio < 0.1F) {
			this.quadSize = this.quadSize * (0.8F + (lifeRatio * 2.0F));
		}

		if (this.isLightSensitive && lifeRatio > 0.5F) {
			float colorShift = (lifeRatio - 0.5F) * 2.0F;

			switch (this.particleType) {
				case 1:
					this.rCol = Math.max(this.rCol - (0.05F * colorShift), 0.5F);
					this.bCol = Math.min(this.bCol + (0.05F * colorShift), 1.0F);
					break;
				case 3:
					this.rCol = Math.min(this.rCol + (0.03F * colorShift), 1.0F);
					this.gCol = Math.min(this.gCol + (0.03F * colorShift), 1.0F);
					break;
				case 4:
					this.rCol = Math.max(this.rCol - (0.03F * colorShift), 0.6F);
					this.gCol = Math.min(this.gCol + (0.02F * colorShift), 1.0F);
					this.bCol = Math.min(this.bCol + (0.05F * colorShift), 1.0F);
					break;
				default:
					break;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType> {

		private final IAnimatedSprite sprites;
		private final int particleType;
		private final boolean isLightSensitive;
		private final boolean isAnimated;

		public Factory(IAnimatedSprite sprites, int particleType, boolean isLightSensitive, boolean isAnimated) {
			this.sprites = sprites;
			this.particleType = particleType;
			this.isLightSensitive = isLightSensitive;
			this.isAnimated = isAnimated;
		}

		@Override
		public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new AuroraVeilParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.particleType, this.isLightSensitive, this.isAnimated);
		}
	}
}
