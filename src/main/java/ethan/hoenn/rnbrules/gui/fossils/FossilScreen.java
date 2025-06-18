package ethan.hoenn.rnbrules.gui.fossils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pixelmonmod.pixelmon.enums.items.EnumFossils;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import java.util.List;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class FossilScreen extends ContainerScreen<FossilGui.FossilContainer> {

	private static final int TOP_SECTION_HEIGHT = 17 + (3 * 18);
	private static final int PLAYER_INV_SECTION_HEIGHT = 94;

	private int animationTicks = 0;
	private int currentPreviewIndex = 0;
	private boolean isRandomFossil = false;
	private List<ItemStack> previewItems;

	public FossilScreen(FossilGui.FossilContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.imageWidth = 176;
		this.imageHeight = TOP_SECTION_HEIGHT + PLAYER_INV_SECTION_HEIGHT;
		this.inventoryLabelY = TOP_SECTION_HEIGHT + 1;

		if (!FossilAssets.isInitialized()) {
			FossilAssets.initializeAssets();
		}

		this.previewItems = FossilAssets.getAllPreviews();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		if (isMouseOverSlot(FossilGui.PREVIEW_SLOT, mouseX, mouseY) && isRandomFossil && !this.menu.slots.get(FossilGui.PREVIEW_SLOT).getItem().isEmpty()) {
			this.renderTooltip(matrixStack, new StringTextComponent("Unknown Fossil").withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), mouseX, mouseY);
		} else {
			this.renderTooltip(matrixStack, mouseX, mouseY);
		}
	}

	private boolean isMouseOverSlot(int slotIndex, int mouseX, int mouseY) {
		if (slotIndex < 0 || slotIndex >= this.menu.slots.size()) return false;

		net.minecraft.inventory.container.Slot slot = this.menu.slots.get(slotIndex);
		return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.minecraft == null) return;

		this.minecraft.getTextureManager().bind(GuiRegistry.SELECT_27_INVENTORY);
		this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title, (float) this.titleLabelX, 6.0F, 4210752);
		this.font.draw(matrixStack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY - 10000, 4210752);
	}

	@Override
	public void tick() {
		super.tick();

		EnumFossils currentFossil = this.menu.getCurrentFossil();

		if (currentFossil != null && currentFossil.getIndex() == 14) {
			isRandomFossil = true;

			animationTicks++;

			if (animationTicks >= 40) {
				animationTicks = 0;
				currentPreviewIndex = (currentPreviewIndex + 1) % previewItems.size();

				if (!previewItems.isEmpty()) {
					ItemStack previewStack = previewItems.get(currentPreviewIndex).copy();
					this.menu.getSlot(FossilGui.PREVIEW_SLOT).set(previewStack);
				}
			} else if (animationTicks == 1) {
				if (!previewItems.isEmpty() && this.menu.getSlot(FossilGui.PREVIEW_SLOT).getItem().isEmpty()) {
					this.menu.getSlot(FossilGui.PREVIEW_SLOT).set(previewItems.get(0).copy());
				}
			}
		} else if (currentFossil == null) {
			isRandomFossil = false;
			animationTicks = 0;
			currentPreviewIndex = 0;

			if (!this.menu.getSlot(FossilGui.PREVIEW_SLOT).getItem().isEmpty()) {
				this.menu.getSlot(FossilGui.PREVIEW_SLOT).set(ItemStack.EMPTY);
			}
		} else {
			isRandomFossil = false;
		}
	}
}
