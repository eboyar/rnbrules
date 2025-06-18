package ethan.hoenn.rnbrules.environment;

import ethan.hoenn.rnbrules.utils.enums.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BaseEnvironment {

	protected final Environment type;

	public BaseEnvironment(Environment type) {
		this.type = type;
	}

	public Environment getType() {
		return type;
	}

	public abstract void update(float delta);

	public abstract void render(float partialTicks, float intensity);

	public abstract void onEnter(float progress);

	public abstract void onExit(float progress);

	public abstract boolean isInEnvironment(World world, BlockPos pos);

	protected PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	protected World getClientWorld() {
		return Minecraft.getInstance().level;
	}

	protected ActiveRenderInfo getCamera() {
		return Minecraft.getInstance().gameRenderer.getMainCamera();
	}
}
