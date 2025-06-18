package ethan.hoenn.rnbrules.gui.backpack;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("NullableProblems")
@OnlyIn(Dist.CLIENT)
public class BigBackpackScreen extends ContainerScreen<BigBackpackContainer> {

	public BigBackpackScreen(BigBackpackContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.imageWidth = 425;
		this.imageHeight = 239;

		this.inventoryLabelX = -10000;
		this.inventoryLabelY = -10000;

		this.titleLabelX = 57;
		this.titleLabelY = 154;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GuiRegistry.BIG_BACKPACK);

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;

		blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
	}
}
