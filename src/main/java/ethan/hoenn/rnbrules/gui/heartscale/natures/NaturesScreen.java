package ethan.hoenn.rnbrules.gui.heartscale.natures;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class NaturesScreen extends ContainerScreen<NaturesGui.NaturesContainer> {

	public NaturesScreen(NaturesGui.NaturesContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.imageHeight = 132;
		this.imageWidth = 122;
		this.titleLabelY = 6;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GuiRegistry.GRAY_SELECT_36);

		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, this.title, 8.0F, 6.0F, 4210752);
	}
}
