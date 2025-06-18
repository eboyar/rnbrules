package ethan.hoenn.rnbrules.gui.battleoverlays;

import static net.minecraftforge.api.distmarker.Dist.*;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = CLIENT)
public class RoamerOverlay {

	private static String titleText = null;
	private static String subtitleText = null;
	private static long displayUntil = 0;
	private static long fadeInTime = 0;
	private static long stayTime = 0;
	private static long fadeOutTime = 0;
	private static long startTime = 0;
	private static final float TITLE_SCALE = 5.0F;
	private static final float SUBTITLE_SCALE = 2.2F;
	private static final int TITLE_COLOR = 0xFFAA88FF;
	private static final int SUBTITLE_COLOR = 0xFFFFD700;
	private static final float TITLE_BOUNCE_AMPLITUDE = 2.0F;

	//TODO: switch back to system time for cleaner transition?
	public static void showRoamerOverlay(String title, String subtitle, int durationTicks) {
		titleText = title.toUpperCase();

		if (subtitle != null && !subtitle.isEmpty()) {
			subtitleText = "» " + subtitle + " «";
		} else {
			subtitleText = null;
		}

		fadeInTime = 750;
		stayTime = durationTicks * 50L - fadeInTime - 750;
		fadeOutTime = 750;
		startTime = System.currentTimeMillis();
		displayUntil = startTime + fadeInTime + stayTime + fadeOutTime;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || titleText == null) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime > displayUntil) {
			titleText = null;
			subtitleText = null;
			return;
		}

		MatrixStack stack = event.getMatrixStack();
		FontRenderer font = mc.font;
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		float alpha = 1.0F;
		long elapsedTime = currentTime - startTime;

		if (elapsedTime < fadeInTime) {
			alpha = (float) elapsedTime / fadeInTime;
		} else if (elapsedTime > fadeInTime + stayTime) {
			alpha = 1.0F - (float) (elapsedTime - fadeInTime - stayTime) / fadeOutTime;
		}

		alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
		int alphaInt = Math.round(alpha * 255.0F);

		if (alphaInt > 8) {
			int titleColorWithAlpha = (alphaInt << 24) | (TITLE_COLOR & 0xFFFFFF);
			int subtitleColorWithAlpha = (alphaInt << 24) | (SUBTITLE_COLOR & 0xFFFFFF);
			float titleY = screenHeight / 3.0F;

			float bounceOffset = 0;
			if (elapsedTime < fadeInTime + stayTime) {
				float bounceProgress = (elapsedTime % 1000) / 1000.0F;
				bounceOffset = (float) Math.sin(bounceProgress * Math.PI * 2) * TITLE_BOUNCE_AMPLITUDE;
			}

			stack.pushPose();
			stack.translate(screenWidth / 2.0F, titleY + bounceOffset, 0.0F);
			stack.scale(TITLE_SCALE, TITLE_SCALE, 1.0F);
			int titleWidth = font.width(titleText);
			font.drawShadow(stack, titleText, -titleWidth / 2.0F, -10, titleColorWithAlpha);
			stack.popPose();

			if (subtitleText != null) {
				stack.pushPose();
				stack.translate(screenWidth / 2.0F, titleY + 40.0F, 0.0F);
				stack.scale(SUBTITLE_SCALE, SUBTITLE_SCALE, 1.0F);
				int subtitleWidth = font.width(subtitleText);
				font.drawShadow(stack, subtitleText, -subtitleWidth / 2.0F, 0, subtitleColorWithAlpha);
				stack.popPose();
			}
		}
	}
}
