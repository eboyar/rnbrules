package ethan.hoenn.rnbrules.gui.battleoverlays;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.utils.enums.BoardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class LocationPopupOverlay {

	private static final int ANIMATION_DURATION = 10;
	private static final int DISPLAY_DURATION = 80;
	private static final int TOTAL_DURATION = ANIMATION_DURATION * 2 + DISPLAY_DURATION;

	private static final int SLAB_WIDTH = 100;
	private static final int SLAB_HEIGHT = 40;
	private static final int MARGIN_X = 10;
	private static final int MARGIN_Y = 10;

	private static boolean enabled = false;
	private static ITextComponent locationText;
	private static int animationTick = 0;
	private static float currentX = 0;
	private static BoardType currentBoardType = BoardType.WOOD;

	public static void showLocation(String locationName, BoardType boardType) {
		showLocation(new StringTextComponent(locationName), boardType);
	}

	public static void showLocation(ITextComponent location, BoardType boardType) {
		locationText = location;
		currentBoardType = boardType;
		animationTick = 0;
		enabled = true;
	}

	public static void tick() {
		if (!enabled) return;

		animationTick++;

		if (animationTick >= TOTAL_DURATION) {
			enabled = false;
			animationTick = 0;
			return;
		}

		float scale = getScale();
		if (animationTick <= ANIMATION_DURATION) {
			float progress = (float) animationTick / ANIMATION_DURATION;
			progress = easeOut(progress);
			currentX = MathHelper.lerp(progress, -(SLAB_WIDTH * scale), MARGIN_X);
		} else if (animationTick >= ANIMATION_DURATION + DISPLAY_DURATION) {
			float progress = (float) (animationTick - ANIMATION_DURATION - DISPLAY_DURATION) / ANIMATION_DURATION;
			progress = easeIn(progress);
			currentX = MathHelper.lerp(progress, MARGIN_X, -(SLAB_WIDTH * scale));
		} else {
			currentX = MARGIN_X;
		}
	}

	public static void render(MatrixStack matrixStack) {
		if (!enabled || locationText == null) return;

		Minecraft mc = Minecraft.getInstance();
		FontRenderer fontRenderer = mc.font;

		float scale = getScale();
		float x = currentX;
		float y = MARGIN_Y;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		matrixStack.pushPose();
		matrixStack.scale(scale, scale, 1.0f);

		float scaledX = x / scale;
		float scaledY = y / scale;

		int boardX = Math.round(scaledX);
		int boardY = Math.round(scaledY);

		mc.getTextureManager().bind(currentBoardType.getTexture());
		drawTexturedRect(matrixStack, boardX, boardY, SLAB_WIDTH, SLAB_HEIGHT);
		String text = locationText.getString();

		float textScale = getTextScale(text.length());

		matrixStack.pushPose();
		matrixStack.scale(textScale, textScale, 1.0f);

		int scaledTextWidth = (int) (fontRenderer.width(text) * textScale);
		float textX = (boardX + (SLAB_WIDTH - scaledTextWidth) / 2.0f) / textScale;
		float textY = (boardY + (SLAB_HEIGHT - fontRenderer.lineHeight * textScale) / 2.0f) / textScale;

		fontRenderer.drawShadow(matrixStack, locationText, textX, textY, 0xFFFFFF);

		matrixStack.popPose();

		matrixStack.popPose();
		RenderSystem.disableBlend();
	}

	private static void drawTexturedRect(MatrixStack matrixStack, int x, int y, int width, int height) {
		AbstractGui.blit(matrixStack, x, y, 0, 0, width, height, width, height);
	}

	private static float easeOut(float t) {
		return 1 - (float) Math.pow(1 - t, 3);
	}

	private static float easeIn(float t) {
		return (float) Math.pow(t, 3);
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void reset() {
		enabled = false;
		locationText = null;
		animationTick = 0;
		currentX = 0;
		currentBoardType = BoardType.WOOD;
	}

	public static float getScale() {
		int width = Minecraft.getInstance().getWindow().getWidth();
		int height = Minecraft.getInstance().getWindow().getHeight();

		if (width >= 3840 && height >= 2160) {
			return 2.5f;
		} else if (width >= 2560 && height >= 1440) {
			return 2.0f;
		} else {
			return 1.5f;
		}
	}

	private static float getTextScale(int textLength) {
		if (textLength >= 20) {
			return 0.5f;
		} else if (textLength >= 14) {
			return 0.8f;
		} else {
			return 1.0f;
		}
	}
}
