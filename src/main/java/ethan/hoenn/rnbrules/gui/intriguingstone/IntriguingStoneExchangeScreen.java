package ethan.hoenn.rnbrules.gui.intriguingstone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("NullableProblems")
public class IntriguingStoneExchangeScreen extends ContainerScreen<IntriguingStoneExchangeGui.IntriguingStoneExchangeContainer> {

	public IntriguingStoneExchangeScreen(IntriguingStoneExchangeGui.IntriguingStoneExchangeContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.imageWidth = 176;
		this.imageHeight = 122;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GuiRegistry.GRAY_SELECT_27);

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title, this.titleLabelX, this.titleLabelY, 0x404040);
	}
}
