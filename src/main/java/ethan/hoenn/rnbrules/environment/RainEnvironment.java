package ethan.hoenn.rnbrules.environment;

import ethan.hoenn.rnbrules.environment.client.ClientEnvironmentController;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainEnvironment extends BaseEnvironment {

	private static final Random RANDOM = new Random();

	private float currentIntensity = 0.0F;
	private int soundTimer = 0;

	public RainEnvironment() {
		super(Environment.RAIN);
	}

	@Override
	public void update(float delta) {
		PlayerEntity player = getClientPlayer();
		if (player == null) return;

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
			if (ClientEnvironmentController.getCurrentClientEnvironment().equals(Environment.RAIN)) {
				return true;
			}
		}
		return false;
	}
}
