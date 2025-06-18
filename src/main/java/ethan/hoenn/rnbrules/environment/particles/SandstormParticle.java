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
public class SandstormParticle extends SpriteTexturedParticle {

	private final IAnimatedSprite spriteWithAge;

	private float xDir;
	private float zDir;

	protected SandstormParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite spriteWithAge, boolean isMedium, boolean isDark) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.spriteWithAge = spriteWithAge;

		this.pickSprite(spriteWithAge);

		this.xDir = (float) (Math.random() * 2.0D - 1.0D) * 0.1F;
		this.zDir = (float) (Math.random() * 2.0D - 1.0D) * 0.1F;

		this.yd = ySpeed;

		this.xd = xSpeed + (Math.random() * 2.0D - 1.0D) * 0.05D;
		this.zd = zSpeed + (Math.random() * 2.0D - 1.0D) * 0.05D;

		if (isDark) {
			this.quadSize = 0.15F + (float) Math.random() * 0.1F;
			this.setColor(0.6F, 0.5F, 0.3F);
			this.alpha = 0.8F;
		} else if (isMedium) {
			this.quadSize = 0.125F + (float) Math.random() * 0.075F;
			this.setColor(0.75F, 0.65F, 0.45F);
			this.alpha = 0.7F;
		} else {
			this.quadSize = 0.1F + (float) Math.random() * 0.05F;
			this.setColor(0.9F, 0.8F, 0.6F);
			this.alpha = 0.6F;
		}

		this.lifetime = (int) (20.0D / (Math.random() * 0.8D + 0.2D));
		this.scale((float) (0.1D + Math.random() * 0.45D));

		this.setSpriteFromAge(spriteWithAge);
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

		this.setSpriteFromAge(this.spriteWithAge);

		this.xd += this.xDir * 0.01;
		this.zd += this.zDir * 0.01;

		this.yd -= 0.002;

		this.move(this.xd, this.yd, this.zd);

		this.xd *= 0.99;
		this.yd *= 0.99;
		this.zd *= 0.99;

		if (this.age > this.lifetime * 0.7) {
			this.alpha = Math.max(0, this.alpha - 0.05F);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType> {

		private final IAnimatedSprite sprites;
		private final boolean isMedium;
		private final boolean isDark;

		public Factory(IAnimatedSprite sprite, boolean isMedium, boolean isDark) {
			this.sprites = sprite;
			this.isMedium = isMedium;
			this.isDark = isDark;
		}

		@Override
		public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new SandstormParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.isMedium, this.isDark);
		}
	}
}
