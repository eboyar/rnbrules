package ethan.hoenn.rnbrules.gui.itemupgrade;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemUpgradeScreen extends ContainerScreen<ItemUpgradeGui.ItemUpgradeContainer> {

	private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("rnbrules", "textures/gui/item_upgrades.png");
	private static final int TOP_SECTION_HEIGHT = 17 + (3 * 18);
	private static final int PLAYER_INV_SECTION_HEIGHT = 94;

	private static final int INFO_BOX_WIDTH = 140;
	private static final int INFO_BOX_PADDING = 6;
	private static final int INFO_BOX_BG_COLOR = 0x90000000;
	private static final int INFO_BOX_TEXT_COLOR = 0xFFFFFF;

	private List<ITextComponent> infoTextLines;

	public ItemUpgradeScreen(ItemUpgradeGui.ItemUpgradeContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.imageWidth = 176;
		this.imageHeight = TOP_SECTION_HEIGHT + PLAYER_INV_SECTION_HEIGHT;
		this.inventoryLabelY = TOP_SECTION_HEIGHT + 1;

		this.infoTextLines = new ArrayList<>();
		this.infoTextLines.add(new StringTextComponent("Crafting Guide").withStyle(TextFormatting.UNDERLINE, TextFormatting.YELLOW));
		this.infoTextLines.add(StringTextComponent.EMPTY);
		this.infoTextLines.add(new StringTextComponent("1. Place the required"));
		this.infoTextLines.add(new StringTextComponent("   upgrade components"));
		this.infoTextLines.add(new StringTextComponent("   for either item"));
		this.infoTextLines.add(new StringTextComponent("   into the correct slots."));
		this.infoTextLines.add(StringTextComponent.EMPTY);
		this.infoTextLines.add(new StringTextComponent("2. Once all items are"));
		this.infoTextLines.add(new StringTextComponent("   placed correctly, click"));
		this.infoTextLines.add(new StringTextComponent("   on §oany§r of the placed"));
		this.infoTextLines.add(new StringTextComponent("   components to craft"));
		this.infoTextLines.add(new StringTextComponent("   the final item."));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		this.renderInfoBox(matrixStack);

		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	private void renderInfoBox(MatrixStack matrixStack) {
		if (this.minecraft == null) return;

		int guiLeft = this.leftPos;
		int guiTop = this.topPos;

		int boxX = guiLeft + this.imageWidth + INFO_BOX_PADDING;
		int boxY = guiTop;

		int textHeight = 0;
		for (ITextComponent line : this.infoTextLines) {
			textHeight += this.font.lineHeight + 1;
		}

		if (!this.infoTextLines.isEmpty()) {
			textHeight -= 1;
		}

		int boxHeight = textHeight + (INFO_BOX_PADDING * 2);

		AbstractGui.fill(matrixStack, boxX, boxY, boxX + INFO_BOX_WIDTH, boxY + boxHeight, INFO_BOX_BG_COLOR);

		int textX = boxX + INFO_BOX_PADDING;
		int textY = boxY + INFO_BOX_PADDING;
		for (ITextComponent line : this.infoTextLines) {
			this.font.draw(matrixStack, line, (float) textX, (float) textY, INFO_BOX_TEXT_COLOR);
			textY += this.font.lineHeight + 1;
		}
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.minecraft == null) return;
		this.minecraft.getTextureManager().bind(CONTAINER_BACKGROUND);

		this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title, (float) this.titleLabelX, 6.0F, 4210752);
		this.font.draw(matrixStack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY - 10000, 4210752);
	}
}
