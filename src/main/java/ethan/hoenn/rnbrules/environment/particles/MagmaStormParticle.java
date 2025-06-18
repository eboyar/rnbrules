package ethan.hoenn.rnbrules.environment.particles;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaStormParticle extends SpriteTexturedParticle {

	private final IAnimatedSprite spriteWithAge;

	private float initialAngle;
	private float swirlRadius;
	private float swirlSpeed;
	private float swirlExpansion;
	private float initialY;
	private float baseSize;

	private final int baseLifetime;
	private final float targetAlpha;

	protected MagmaStormParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite spriteWithAge, int particleType) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.spriteWithAge = spriteWithAge;
		this.initialY = (float) y;

		this.pickSprite(spriteWithAge);

		this.initialAngle = (float) (Math.random() * Math.PI * 2.0);
		this.swirlRadius = (float) (0.5 + Math.random() * 1.5);
		this.swirlSpeed = (float) (0.03 + Math.random() * 0.04);
		this.swirlExpansion = (float) (Math.random() * 0.02 - 0.01);

		this.yd = ySpeed * 0.5;

		this.xd = xSpeed * 0.02;
		this.zd = zSpeed * 0.02;

		switch (particleType) {
			case 0:
				this.baseSize = 0.10F + (float) Math.random() * 0.05F;
				this.setColor(0.2F, 0.2F, 0.2F);
				this.baseLifetime = 40 + (int) (Math.random() * 20);
				this.targetAlpha = 0.9F;
				this.hasPhysics = false;
				break;
			case 1:
				this.baseSize = 0.12F + (float) Math.random() * 0.06F;
				this.setColor(0.4F, 0.05F, 0.05F);
				this.baseLifetime = 50 + (int) (Math.random() * 25);
				this.targetAlpha = 0.8F;
				this.hasPhysics = false;
				break;
			case 2:
				this.baseSize = 0.15F + (float) Math.random() * 0.07F;
				this.setColor(0.7F, 0.1F, 0.1F);
				this.baseLifetime = 60 + (int) (Math.random() * 30);
				this.targetAlpha = 0.8F;
				this.hasPhysics = false;
				break;
			case 3:
				this.baseSize = 0.17F + (float) Math.random() * 0.08F;
				this.setColor(0.9F, 0.3F, 0.0F);
				this.baseLifetime = 70 + (int) (Math.random() * 35);
				this.targetAlpha = 0.85F;
				this.hasPhysics = false;
				break;
			case 4:
				this.baseSize = 0.2F + (float) Math.random() * 0.1F;
				this.setColor(1.0F, 0.6F, 0.0F);
				this.baseLifetime = 80 + (int) (Math.random() * 40);
				this.targetAlpha = 0.95F;
				this.hasPhysics = false;
				break;
			default:
				this.baseSize = 0.15F;
				this.setColor(0.7F, 0.3F, 0.1F);
				this.baseLifetime = 60;
				this.targetAlpha = 0.8F;
				break;
		}

		this.quadSize = this.baseSize;
		this.lifetime = this.baseLifetime;
		this.alpha = 0.0F;

		this.roll = (float) (Math.random() * Math.PI * 2.0);
		this.scale((float) (0.1D + Math.random() * 0.3D));

		this.setSpriteFromAge(spriteWithAge);
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 15728880;
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

		this.setSpriteFromAge(this.spriteWithAge);

		float lifecycle = (float) this.age / (float) this.lifetime;

		if (lifecycle < 0.1F) {
			this.alpha = MathHelper.lerp(lifecycle * 10.0F, 0.0F, this.targetAlpha);
		} else if (lifecycle > 0.8F) {
			this.alpha = MathHelper.lerp((lifecycle - 0.8F) * 5.0F, this.targetAlpha, 0.0F);
		} else {
			this.alpha = this.targetAlpha;
		}

		this.swirlRadius += this.swirlExpansion;
		if (this.swirlRadius < 0.2F) {
			this.swirlRadius = 0.2F;
			this.swirlExpansion = Math.abs(this.swirlExpansion);
		} else if (this.swirlRadius > 2.0F) {
			this.swirlRadius = 2.0F;
			this.swirlExpansion = -Math.abs(this.swirlExpansion);
		}

		float angle = this.initialAngle + (this.age * this.swirlSpeed);
		double swirlingX = this.xd + Math.sin(angle) * this.swirlRadius * 0.05;
		double swirlingZ = this.zd + Math.cos(angle) * this.swirlRadius * 0.05;

		this.yd -= 0.001 + Math.random() * 0.001;

		this.move(swirlingX, this.yd, swirlingZ);

		this.xd *= 0.98;
		this.zd *= 0.98;
		this.yd *= 0.97;

		this.oRoll = this.roll;
		this.roll += 0.02;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType> {

		private final IAnimatedSprite sprites;
		private final int particleType;

		public Factory(IAnimatedSprite sprite, int particleType) {
			this.sprites = sprite;
			this.particleType = particleType;
		}

		@Override
		public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new MagmaStormParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.particleType);
		}
	}
}
