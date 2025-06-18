package ethan.hoenn.rnbrules.gui.gamecorner.gamecornerroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerAssets;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import java.util.List;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class GamecornerRollScreen extends ContainerScreen<GamecornerRollGui.GamecornerRollContainer> {

	private final List<ItemStack> pokemonPhotos;
	private final Pokemon winningPokemon;
	private final int winnerIndex;
	private final int poolSize;

	private int currentShift = 0;
	private int ticksUntilNextShift = 0;
	private int shiftInterval = 4;
	private int displayOffset = 0;
	private boolean isRolling = true;
	private boolean isLanded = false;
	private int landedTicks = 0;

	private int soundTickCounter = 0;
	private boolean hasPlayedWinSound = false;
	private boolean hasPlayedSlowSound = false;

	private static final int MIDDLE_ROW_Y = 36;
	private static final int MIDDLE_ROW_START_X = 8;
	private static final int SLOT_WIDTH = 18;
	private static final int MIDDLE_ROW_SLOT_COUNT = 9;
	private static final int CENTER_SLOT_OFFSET = 4;

	private static final int LAND_DURATION_TICKS = 40;
	private static final int TOTAL_SHIFTS_TARGET = 38;

	public GamecornerRollScreen(GamecornerRollGui.GamecornerRollContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.imageHeight = 122;
		this.titleLabelY = 6;

		this.pokemonPhotos = screenContainer.getPokemonPhotos();
		this.winningPokemon = screenContainer.getWinningPokemon();
		this.winnerIndex = screenContainer.getWinnerIndex();
		this.poolSize = this.pokemonPhotos.size();

		this.ticksUntilNextShift = this.shiftInterval;
	}

	@Override
	protected void init() {
		super.init();

		if (minecraft != null) {
			minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.BOOK_PAGE_TURN, 0.6f, 0.8f));
			minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.NOTE_BLOCK_CHIME, 0.3f, 0.7f));
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.poolSize == 0) {
			if (this.minecraft != null && this.minecraft.player != null) {
				this.minecraft.player.closeContainer();
			}
			return;
		}

		if (isRolling) {
			soundTickCounter++;
			if (soundTickCounter >= 4 && currentShift < 21) {
				soundTickCounter = 0;
				if (minecraft != null) {
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.1f, 1.5f));
				}
			}

			ticksUntilNextShift--;

			if (ticksUntilNextShift <= 0) {
				currentShift++;

				if (currentShift >= TOTAL_SHIFTS_TARGET) {
					isRolling = false;
					isLanded = true;

					if (minecraft != null && !hasPlayedWinSound) {
						hasPlayedWinSound = true;
						minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f));
						minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.PLAYER_LEVELUP, 0.75f, 1.0f));
					}

					displayOffset = (this.winnerIndex - CENTER_SLOT_OFFSET + this.poolSize) % this.poolSize;
				} else {
					displayOffset = (displayOffset + 1) % this.poolSize;

					if (currentShift >= 36) {
						shiftInterval = 12;

						if (minecraft != null) {
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.35f, 0.8f));
						}
					} else if (currentShift >= 33) {
						shiftInterval = 10;

						if (minecraft != null) {
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.35f, 0.9f));
						}
					} else if (currentShift >= 29) {
						shiftInterval = 8;

						if (minecraft != null && !hasPlayedSlowSound) {
							hasPlayedSlowSound = true;
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.WOODEN_TRAPDOOR_CLOSE, 0.2f, 0.8f));
						}

						if (minecraft != null) {
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.3f, 1.0f));
						}
					} else if (currentShift >= 25) {
						shiftInterval = 6;

						if (minecraft != null && currentShift % 2 == 0) {
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.2f));
						}
					} else if (currentShift >= 21) {
						shiftInterval = 5;

						if (minecraft != null && currentShift % 2 == 0) {
							minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.1f, 1.3f));
						}
					} else {
						shiftInterval = 4;
					}
					ticksUntilNextShift = shiftInterval;
				}
			}
		} else if (isLanded) {
			landedTicks++;
			if (landedTicks >= LAND_DURATION_TICKS) {
				if (this.minecraft != null && this.minecraft.player != null) {
					sendWinMessage();
					this.minecraft.player.closeContainer();
				}
			}
		}
	}

	private void sendWinMessage() {
		if (this.minecraft == null || this.minecraft.player == null || this.winningPokemon == null) return;

		if (this.menu != null) {
			this.menu.setWinMessageSent();
		}

		String formName = winningPokemon.getForm().getLocalizedName();
		String speciesName = winningPokemon.getSpecies().getLocalizedName();
		String displayName;
		if (formName != null && !formName.isEmpty() && !formName.equalsIgnoreCase("none")) {
			String capitalizedFormName = formName.substring(0, 1).toUpperCase() + formName.substring(1).toLowerCase();
			displayName = capitalizedFormName + " " + speciesName;
		} else {
			displayName = speciesName;
		}
		this.minecraft.player.displayClientMessage(
				new StringTextComponent("You won a ")
					.withStyle(TextFormatting.GOLD)
					.append(new StringTextComponent(displayName).withStyle(TextFormatting.AQUA))
					.append(new StringTextComponent("!").withStyle(TextFormatting.GOLD)),
				false
			);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderMiddleRow(matrixStack);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	private void renderMiddleRow(MatrixStack matrixStack) {
		if (this.itemRenderer == null || this.poolSize == 0) return;

		int startX = this.leftPos + MIDDLE_ROW_START_X;
		int startY = this.topPos + MIDDLE_ROW_Y;

		if (isLanded) {
			ItemStack winnerPhoto = GamecornerAssets.getPokemonPhoto(this.winningPokemon);
			for (int i = 0; i < MIDDLE_ROW_SLOT_COUNT; i++) {
				int x = startX + i * SLOT_WIDTH;
				this.itemRenderer.renderAndDecorateItem(winnerPhoto, x, startY);
				this.itemRenderer.renderGuiItemDecorations(this.font, winnerPhoto, x, startY, null);
			}
		} else if (isRolling) {
			for (int i = 0; i < MIDDLE_ROW_SLOT_COUNT; i++) {
				int photoIndex = (displayOffset + i) % this.poolSize;
				ItemStack photo = this.pokemonPhotos.get(photoIndex);
				int x = startX + i * SLOT_WIDTH;
				this.itemRenderer.renderAndDecorateItem(photo, x, startY);
				this.itemRenderer.renderGuiItemDecorations(this.font, photo, x, startY, null);
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.minecraft.getTextureManager().bind(GuiRegistry.GRAY_SELECT_27);

		int relX = (this.width - this.imageWidth) / 2;
		int relY = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, this.title, (float) (this.imageWidth / 2 - this.font.width(this.title) / 2), (float) this.titleLabelY, 4210752);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.minecraft != null && this.minecraft.options != null && keyCode == this.minecraft.options.keyInventory.getKey().getValue()) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed() {
		if (isLanded && landedTicks < LAND_DURATION_TICKS) {
			sendWinMessage();
		}
		super.removed();
	}
}
