package ethan.hoenn.rnbrules.environment.particles;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindParticle extends SpriteTexturedParticle {

	private final IAnimatedSprite spriteSet;
	private final boolean isAnimated;
	private final boolean isMystic;

	private float xDir;
	private float zDir;
	private float oscillationPhase;
	private float oscillationAmplitude;
	private float oscillationSpeed;

	WindParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite spriteSet, boolean isAnimated, boolean isMystic) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.spriteSet = spriteSet;
		this.isAnimated = isAnimated;
		this.isMystic = isMystic;

		this.pickSprite(spriteSet);

		this.xDir = (float) (Math.random() * 2.0D - 1.0D) * 0.03F;
		this.zDir = (float) (Math.random() * 2.0D - 1.0D) * 0.03F;

		this.oscillationPhase = (float) (Math.random() * Math.PI * 2.0);
		this.oscillationAmplitude = isMystic ? 0.01F + (float) Math.random() * 0.01F : 0.005F + (float) Math.random() * 0.005F;
		this.oscillationSpeed = 0.05F + (float) Math.random() * 0.05F;

		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
		float baseScale;
		if (isMystic) {
			if (isAnimated) {
				baseScale = 0.03F + (float) Math.random() * 0.04F;
				this.alpha = 0.75F;
				this.setColor(0.8F, 0.95F, 1.0F);
			} else {
				baseScale = 0.04F + (float) Math.random() * 0.06F;
				this.alpha = 0.85F;
				this.setColor(0.85F, 0.98F, 1.0F);
			}
		} else {
			if (isAnimated) {
				baseScale = 0.10F + (float) Math.random() * 0.1F;
				this.alpha = 0.9F;
				this.setColor(0.93F, 0.93F, 1.0F);
			} else {
				baseScale = 0.08F + (float) Math.random() * 0.2F;
				this.alpha = 0.75F;
				this.setColor(0.97F, 0.97F, 1.0F);
			}
		}
		this.quadSize = baseScale;

		double speed = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);

		if (speed < 0.001D) {
			speed = 0.001D;
		}

		int speedBasedLifetime = (int) (25.0D / (speed * 1.8D));

		speedBasedLifetime += (int) (Math.random() * 8) - 4;

		if (isAnimated) {
			speedBasedLifetime = Math.max(16, speedBasedLifetime);
		}

		if (isMystic) {
			speedBasedLifetime = (int) (speedBasedLifetime * 1.2);
		}

		this.lifetime = Math.max(10, Math.min(45, speedBasedLifetime));
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

		if (this.isAnimated) {
			int frameCount = 8;
			int frameSpeed = 2;
			int frameIndex = (this.age / frameSpeed) % frameCount;

			this.setSprite(this.spriteSet.get(frameIndex, frameCount));
		}

		this.xd += this.xDir * 0.008;
		this.zd += this.zDir * 0.008;

		if (this.isMystic) {
			float verticalOscillation = (float) Math.sin(this.oscillationPhase + this.age * this.oscillationSpeed) * this.oscillationAmplitude;
			this.yd += 0.0005 + verticalOscillation;

			float horizontalFactor = 0.008f;
			this.xd += Math.sin(this.age * 0.15) * horizontalFactor;
			this.zd += Math.cos(this.age * 0.15) * horizontalFactor;
		} else {
			float verticalOscillation = (float) Math.sin(this.oscillationPhase + this.age * this.oscillationSpeed) * (this.oscillationAmplitude * 0.5f);
			this.yd += 0.0002 + verticalOscillation;

			if (this.age % 5 == 0 && Math.random() < 0.3) {
				this.xd += (Math.random() - 0.5) * 0.01;
				this.zd += (Math.random() - 0.5) * 0.01;
			}
		}

		this.move(this.xd, this.yd, this.zd);

		float baseDrag;

		float ageProgress = (float) this.age / this.lifetime;

		if (this.isMystic) {
			baseDrag = 0.992F - (ageProgress * 0.04F);
		} else {
			baseDrag = 0.988F - (ageProgress * 0.06F);
		}

		this.xd *= baseDrag;
		this.yd *= baseDrag;
		this.zd *= baseDrag;

		if (this.age < this.lifetime * 0.1) {
			float fadeInProgress = (float) this.age / (this.lifetime * 0.1f);

			float targetAlpha = this.isAnimated ? this.alpha * 0.7f : this.alpha * 0.5f;
			this.alpha = targetAlpha * fadeInProgress + (this.alpha * 0.3f);
		} else if (this.age > this.lifetime * 0.8) {
			float fadeOutProgress = (this.age - this.lifetime * 0.8f) / (this.lifetime * 0.2f);
			this.alpha = this.alpha * (1.0f - fadeOutProgress);
		}

		if (this.age > this.lifetime * 0.7) {
			float shrinkProgress = (this.age - this.lifetime * 0.7f) / (this.lifetime * 0.3f);
			this.quadSize = this.quadSize * (1.0f - (shrinkProgress * 0.3f));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType> {

		private final IAnimatedSprite sprites;
		private final boolean isAnimated;
		private final boolean isMystic;

		public Factory(IAnimatedSprite sprite, boolean isAnimated, boolean isMystic) {
			this.sprites = sprite;
			this.isAnimated = isAnimated;
			this.isMystic = isMystic;
		}

		@Override
		public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new WindParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.isAnimated, this.isMystic);
		}
	}
}
