package ethan.hoenn.rnbrules.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerInfoOverlay {

	private static String currentLocation = "";
	private static int levelCap = 0;
	private static int balance = 0;
	private static boolean enabled = true;

	public static void updateInfo(String location, int cap, int money) {
		currentLocation = location != null ? location : "";
		levelCap = cap;
		balance = money;
	}

	public static void setEnabled(boolean enable) {
		enabled = enable;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (!enabled || event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) {
			return;
		}

		MatrixStack matrixStack = event.getMatrixStack();
		FontRenderer font = mc.font;
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		int rightMargin = 0;
		int lineHeight = 10;
		int boxHeight = lineHeight * 6;
		int startY = (screenHeight / 2) - (boxHeight / 2) - 40;

		int backgroundColor = 0x66000000;
		int separatorColor = 0xFF333333;

		String title = TextFormatting.WHITE + "" + TextFormatting.BOLD + "Pixelmon " + TextFormatting.GREEN + "" + TextFormatting.BOLD + "Run & Bun";
		String locationText = TextFormatting.GREEN + currentLocation;
		String levelCapText = TextFormatting.WHITE + "Level Cap: " + TextFormatting.GOLD + levelCap;
		String balanceText = TextFormatting.WHITE + "Balance: " + TextFormatting.GOLD + "$" + balance;

		int maxWidth = Math.max(Math.max(font.width(title), font.width(locationText)), Math.max(font.width(levelCapText), font.width(balanceText))) + 16;
		int boxX = screenWidth - maxWidth - rightMargin;

		AbstractGui.fill(matrixStack, boxX, startY, boxX + maxWidth, startY + boxHeight + 5, backgroundColor);

		int currentY = startY + 4;

		drawLeftAlignedText(matrixStack, font, title, boxX + 8, currentY, 0xFFFFFF);
		currentY += lineHeight;

		AbstractGui.fill(matrixStack, boxX + 4, currentY + 4, boxX + maxWidth - 4, currentY + 5, separatorColor);
		currentY += lineHeight;
		drawLeftAlignedText(matrixStack, font, locationText, boxX + 8, currentY, 0xFFFFFF);
		currentY += lineHeight + 2;

		drawLeftAlignedText(matrixStack, font, levelCapText, boxX + 8, currentY, 0xFFFFFF);
		currentY += lineHeight + 2;

		drawLeftAlignedText(matrixStack, font, balanceText, boxX + 8, currentY, 0xFFFFFF);
		currentY += lineHeight;

		currentY -= 2;

		AbstractGui.fill(matrixStack, boxX + 4, currentY + 4, boxX + maxWidth - 4, currentY + 5, separatorColor);
	}

	private static void drawLeftAlignedText(MatrixStack matrixStack, FontRenderer font, String text, int x, int y, int color) {
		font.drawShadow(matrixStack, text, x, y, color);
	}
}
