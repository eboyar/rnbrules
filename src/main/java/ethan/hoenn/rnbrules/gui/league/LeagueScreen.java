package ethan.hoenn.rnbrules.gui.league;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class LeagueScreen extends ContainerScreen<LeagueGui.LeagueContainer> {

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 76;

	public LeagueScreen(LeagueGui.LeagueContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.inventoryLabelY = -10000;
		this.imageHeight = GUI_HEIGHT;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GuiRegistry.GRAY_SELECT_27);
		int left = (this.width - GUI_WIDTH) / 2;
		int top = (this.height - GUI_HEIGHT) / 2;
		this.blit(matrixStack, left, top, 0, 0, GUI_WIDTH, GUI_HEIGHT);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title.copy().withStyle(TextFormatting.WHITE), this.titleLabelX, this.titleLabelY, 0x404040);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}
}
