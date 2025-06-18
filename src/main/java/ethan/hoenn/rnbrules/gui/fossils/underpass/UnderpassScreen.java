package ethan.hoenn.rnbrules.gui.fossils.underpass;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class UnderpassScreen extends ContainerScreen<UnderpassGui.UnderpassContainer> {

	public UnderpassScreen(UnderpassGui.UnderpassContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.imageWidth = 212;
		this.imageHeight = 55;
		this.inventoryLabelY = -10000;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		@SuppressWarnings("deprecation")
		Runnable setupColor = () -> RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		setupColor.run();

		if (this.minecraft == null) return;

		this.minecraft.getTextureManager().bind(GuiRegistry.GRAY_SELECT_11);
		this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title, (float) (this.imageWidth / 2 - this.font.width(this.title) / 2), 6.0F, 0xFFFFFF);
	}
}
