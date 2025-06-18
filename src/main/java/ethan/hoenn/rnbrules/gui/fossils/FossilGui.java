package ethan.hoenn.rnbrules.gui.fossils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.enums.items.EnumFossils;
import com.pixelmonmod.pixelmon.items.CoveredFossilItem;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class FossilGui {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Random RANDOM = new Random();
	private static final int NUM_SLOTS = 27;
	private static final String FOSSIL_COMPLETION_OT = "FOSSIL_COMPLETION";

	private static final Set<Integer> FILLER_SLOTS = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26));

	public static final int FOSSIL_INPUT_SLOT = 13;
	public static final int PREVIEW_SLOT = 15;
	public static final int PROCESS_BUTTON_SLOT = 11;

	static {
		if (!FossilAssets.isInitialized()) {
			FossilAssets.initializeAssets();
		}
	}

	public static boolean isValidFossil(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Item item = stack.getItem();
		return item instanceof CoveredFossilItem;
	}

	public static EnumFossils getFossilFromStack(ItemStack stack) {
		if (!isValidFossil(stack)) {
			return null;
		}

		return ((CoveredFossilItem) stack.getItem()).getFossil();
	}

	public static EnumFossils processSpecialFossil(EnumFossils fossil) {
		if (fossil.getIndex() == 14) {
			int randomIndex = RANDOM.nextInt(11);
			return EnumFossils.fromIndex(randomIndex);
		}

		return fossil;
	}

	public static boolean restoreFossil(ServerPlayerEntity player, EnumFossils fossil) {
		if (fossil == null) {
			return false;
		}

		try {
			BankAccount userAccount = (BankAccount) BankAccountProxy.getBankAccount(
				player
			).orElseThrow(() -> new NullPointerException("bank account"));

			double balance = userAccount.getBalance().doubleValue();
			final int FOSSIL_COST = 3000;

			if (balance < FOSSIL_COST) {
				int needed = FOSSIL_COST - (int) balance;
				player.sendMessage(
					new StringTextComponent("You cannot afford to restore this fossil! You need ")
						.withStyle(TextFormatting.RED)
						.append(new StringTextComponent("₽" + needed).withStyle(TextFormatting.GOLD))
						.append(new StringTextComponent(" more.").withStyle(TextFormatting.RED)),
					player.getUUID()
				);
				return false;
			}

			EnumFossils processedFossil = processSpecialFossil(fossil);

			Species species = processedFossil.getPokemon();
			String speciesName = species.getName();

			PokemonSpecification spec = PokemonSpecificationProxy.create("species:" + speciesName + " lvl:5 cl:fossil");
			Pokemon pokemon = spec.create();

			if (pokemon != null) {
				userAccount.take(FOSSIL_COST);

				player.sendMessage(
					new StringTextComponent("Fossil restored! You got a ")
						.withStyle(TextFormatting.GOLD)
						.append(new StringTextComponent(speciesName).withStyle(TextFormatting.AQUA))
						.append(new StringTextComponent("!").withStyle(TextFormatting.GOLD)),
					player.getUUID()
				);

				player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8F, 1.0F);
				StorageProxy.getParty(player.getUUID()).add(pokemon);

				GlobalOTManager otManager = GlobalOTManager.get(player.getLevel());
				if (!otManager.playerHasGlobalOT(player.getUUID(), FOSSIL_COMPLETION_OT)) {
					otManager.addGlobalOT(FOSSIL_COMPLETION_OT);
					otManager.addPlayerGlobalOT(player.getUUID(), FOSSIL_COMPLETION_OT);

					String completionCommand = RNBConfig.getFossilCompletionCommand();
					if (completionCommand != null && !completionCommand.isEmpty()) {
						String command = completionCommand.replace("@pl", player.getScoreboardName());
						player.getServer().getCommands().performCommand(player.getServer().createCommandSourceStack().withPermission(4), command);
					}
				}

				return true;
			}
		} catch (Exception e) {
			LOGGER.error("Error restoring fossil", e);
		}

		return false;
	}

	public static void openGui(ServerPlayerEntity player) {
		if (!FossilAssets.isInitialized()) {
			FossilAssets.initializeAssets();
		}

		Inventory fossilInventory = new Inventory(NUM_SLOTS);

		ItemStack fillerPane = new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
		fillerPane.setHoverName(new StringTextComponent(" "));

		for (int i = 0; i < fossilInventory.getContainerSize(); i++) {
			if (FILLER_SLOTS.contains(i)) {
				fossilInventory.setItem(i, fillerPane.copy());
			}
		}

		ItemStack processButton = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
		processButton.setHoverName(new StringTextComponent("Restore Fossil").withStyle(style -> style.withColor(TextFormatting.GREEN).withItalic(false)));

		processButton.getOrCreateTag().putBoolean("HideTooltip", false);
		ListNBT loreList = new ListNBT();
		loreList.add(
			StringNBT.valueOf(
				ITextComponent.Serializer.toJson(
					new StringTextComponent("Cost: ").withStyle(TextFormatting.GRAY).append(new StringTextComponent("₽3,000").withStyle(TextFormatting.GOLD))
				)
			)
		);

		CompoundNBT display = processButton.getOrCreateTagElement("display");
		display.put("Lore", loreList);

		fossilInventory.setItem(PROCESS_BUTTON_SLOT, processButton);

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Fossil Restoration");
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new FossilContainer(windowId, inventory, fossilInventory);
				}
			}
		);
	}

	public static class FillerSlot extends Slot {

		public FillerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return false;
		}

		@Override
		public boolean isActive() {
			return true;
		}
	}

	public static class PreviewSlot extends Slot {

		public PreviewSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return false;
		}
	}

	public static class FossilSlot extends Slot {

		private final boolean isFossilSlot;

		public FossilSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean isFossilSlot) {
			super(inventoryIn, index, xPosition, yPosition);
			this.isFossilSlot = isFossilSlot;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			if (isFossilSlot) {
				return FossilGui.isValidFossil(stack);
			}
			return false;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	public static class FossilContainer extends Container {

		private final IInventory fossilSlotsInventory;
		private static final int PLAYER_INV_START_INDEX = NUM_SLOTS;
		private static final int PLAYER_HOTBAR_START_INDEX = PLAYER_INV_START_INDEX + 27;
		private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START_INDEX + 9;

		public FossilContainer(int windowId, PlayerInventory playerInventory, IInventory fossilSlotsInventory) {
			super(GuiRegistry.FOSSIL_CONTAINER.get(), windowId);
			checkContainerSize(fossilSlotsInventory, NUM_SLOTS);
			this.fossilSlotsInventory = fossilSlotsInventory;

			if (fossilSlotsInventory instanceof Inventory) {
				fossilSlotsInventory.startOpen(playerInventory.player);
			}

			addSlots(playerInventory, fossilSlotsInventory);
		}

		public FossilContainer(int windowId, PlayerInventory playerInventory) {
			this(windowId, playerInventory, new Inventory(NUM_SLOTS));
		}

		private void addSlots(PlayerInventory playerInventory, IInventory fossilSlotsInventory) {
			int slotX = 8;
			int slotY = 18;

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int index = col + row * 9;
					int x = slotX + col * 18;
					int y = slotY + row * 18;

					if (FILLER_SLOTS.contains(index)) {
						this.addSlot(new FillerSlot(fossilSlotsInventory, index, x, y));
					} else if (index == FOSSIL_INPUT_SLOT) {
						this.addSlot(new FossilSlot(fossilSlotsInventory, index, x, y, true));
					} else if (index == PREVIEW_SLOT) {
						this.addSlot(new PreviewSlot(fossilSlotsInventory, index, x, y));
					} else {
						this.addSlot(new FossilSlot(fossilSlotsInventory, index, x, y, false));
					}
				}
			}

			int playerInvY = slotY + (3 * 18) + 14;
			int playerInvX = 8;
			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					this.addSlot(new Slot(playerInventory, col + row * 9 + 9, playerInvX + col * 18, playerInvY + row * 18));
				}
			}

			int hotbarY = playerInvY + (3 * 18) + 4;
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col, playerInvX + col * 18, hotbarY));
			}
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			ItemStack itemstack = ItemStack.EMPTY;
			Slot slot = this.slots.get(index);

			if (slot == null || !slot.hasItem()) {
				return itemstack;
			}

			if (slot instanceof FillerSlot || slot instanceof PreviewSlot) {
				return ItemStack.EMPTY;
			}

			ItemStack sourceStack = slot.getItem();
			itemstack = sourceStack.copy();
			boolean slotChanged = false;

			if (index < PLAYER_INV_START_INDEX) {
				if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START_INDEX, TOTAL_SLOTS, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(sourceStack, itemstack);
				slotChanged = true;
			} else {
				if (isValidFossil(sourceStack) && this.fossilSlotsInventory.getItem(FOSSIL_INPUT_SLOT).isEmpty()) {
					ItemStack fossilCopy = sourceStack.copy();
					fossilCopy.setCount(1);
					this.fossilSlotsInventory.setItem(FOSSIL_INPUT_SLOT, fossilCopy);
					sourceStack.shrink(1);
					slotChanged = true;

					updatePreviewSafely(playerIn);

					return ItemStack.EMPTY;
				}

				if (index < PLAYER_HOTBAR_START_INDEX) {
					if (!this.moveItemStackTo(sourceStack, PLAYER_HOTBAR_START_INDEX, TOTAL_SLOTS, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index < TOTAL_SLOTS && !this.moveItemStackTo(sourceStack, PLAYER_INV_START_INDEX, PLAYER_HOTBAR_START_INDEX, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (sourceStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
				slotChanged = true;
			} else {
				slot.setChanged();
			}

			if (sourceStack.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, sourceStack);

			if (slotChanged && slot.index == FOSSIL_INPUT_SLOT) {
				updatePreviewSafely(playerIn);
			}

			return itemstack;
		}

		public void updatePreviewSlot() {
			ItemStack fossilStack = this.fossilSlotsInventory.getItem(FOSSIL_INPUT_SLOT);
			if (!fossilStack.isEmpty()) {
				EnumFossils fossil = getFossilFromStack(fossilStack);
				if (fossil != null) {
					if (fossil.getIndex() != 14) {
						this.fossilSlotsInventory.setItem(PREVIEW_SLOT, FossilAssets.getPreviewForFossil(fossil));
					}
				}
			} else {
				this.fossilSlotsInventory.setItem(PREVIEW_SLOT, ItemStack.EMPTY);
			}
		}

		private void updatePreviewSafely(PlayerEntity player) {
			updatePreviewSlot();

			if (!player.level.isClientSide && player.level.getServer() != null) {
				player.level.getServer().execute(this::updatePreviewSlot);
			}
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);

				if (slot instanceof FillerSlot || slot instanceof PreviewSlot) {
					return ItemStack.EMPTY;
				}

				if (slotId == PROCESS_BUTTON_SLOT && !player.level.isClientSide && player instanceof ServerPlayerEntity) {
					ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

					ItemStack fossilStack = this.fossilSlotsInventory.getItem(FOSSIL_INPUT_SLOT);
					if (isValidFossil(fossilStack)) {
						EnumFossils fossil = getFossilFromStack(fossilStack);

						if (fossil != null) {
							boolean success = restoreFossil(serverPlayer, fossil);

							if (success) {
								this.fossilSlotsInventory.setItem(FOSSIL_INPUT_SLOT, ItemStack.EMPTY);

								serverPlayer.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);

								serverPlayer.closeContainer();
							}

							return ItemStack.EMPTY;
						}
					} else {
						serverPlayer.sendMessage(new StringTextComponent("Please place a fossil in the input slot").withStyle(TextFormatting.RED), serverPlayer.getUUID());
					}

					return ItemStack.EMPTY;
				}

				if (slotId == FOSSIL_INPUT_SLOT || (clickTypeIn == ClickType.PICKUP && !player.inventory.getCarried().isEmpty() && isValidFossil(player.inventory.getCarried()))) {
					updatePreviewSlot();

					if (!player.level.isClientSide && player.level.getServer() != null) {
						player.level.getServer().execute(this::updatePreviewSlot);
					}
				}
			}

			ItemStack result = super.clicked(slotId, dragType, clickTypeIn, player);
			return result;
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public void removed(PlayerEntity player) {
			super.removed(player);

			if (this.fossilSlotsInventory instanceof Inventory && !player.level.isClientSide) {
				ItemStack fossilStack = this.fossilSlotsInventory.getItem(FOSSIL_INPUT_SLOT);
				if (!fossilStack.isEmpty()) {
					player.inventory.placeItemBackInInventory(player.level, fossilStack);
				}
			}

			if (this.fossilSlotsInventory instanceof Inventory) {
				this.fossilSlotsInventory.stopOpen(player);
			}
		}

		public EnumFossils getCurrentFossil() {
			ItemStack stack = this.fossilSlotsInventory.getItem(FOSSIL_INPUT_SLOT);
			return getFossilFromStack(stack);
		}
	}
}
