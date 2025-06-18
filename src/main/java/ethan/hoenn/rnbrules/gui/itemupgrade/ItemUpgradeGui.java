package ethan.hoenn.rnbrules.gui.itemupgrade;

import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.registries.ItemRegistry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class ItemUpgradeGui {

	private static final Set<Integer> BLACK_FILLER_SLOTS = new HashSet<>(Arrays.asList(4, 13, 22));
	private static final Set<Integer> PURPLE_FILLER_SLOTS = new HashSet<>(Arrays.asList(0, 1, 2, 3, 9, 12, 18, 19, 20, 21));
	private static final Set<Integer> YELLOW_FILLER_SLOTS = new HashSet<>(Arrays.asList(5, 7, 14, 16, 23, 25, 26));

	private static final Set<Integer> ALL_FILLER_SLOTS = Stream.of(BLACK_FILLER_SLOTS, PURPLE_FILLER_SLOTS, YELLOW_FILLER_SLOTS).flatMap(Set::stream).collect(Collectors.toSet());

	public static final int PARTY_RESTORE_SLOT = 1;
	public static final int MAX_SERUM_SLOT = 9;
	public static final int MAX_CASING_SLOT = 10;
	public static final int MAX_AEROSOLIZER_SLOT = 11;
	public static final int MEGA_TRANSCEIVER_SLOT = 15;
	public static final int BATTERY_SLOT = 6;

	public static final Set<Integer> ALL_FUNCTIONAL_SLOTS = new HashSet<>(Arrays.asList(PARTY_RESTORE_SLOT, MAX_SERUM_SLOT, MAX_CASING_SLOT, MAX_AEROSOLIZER_SLOT, MEGA_TRANSCEIVER_SLOT, BATTERY_SLOT));

	private static final Set<Integer> MAX_RESTORE_TRIGGER_SLOTS = new HashSet<>(Arrays.asList(PARTY_RESTORE_SLOT, MAX_SERUM_SLOT, MAX_CASING_SLOT, MAX_AEROSOLIZER_SLOT));
	private static final Set<Integer> POKELINK_TRIGGER_SLOTS = new HashSet<>(Arrays.asList(BATTERY_SLOT, MEGA_TRANSCEIVER_SLOT));

	public static void openGui(ServerPlayerEntity player) {
		Inventory upgradeInventory = new Inventory(27);

		ItemStack blackPane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
		blackPane.setHoverName(new StringTextComponent(" "));
		ItemStack purplePane = new ItemStack(Items.PURPLE_STAINED_GLASS_PANE);
		purplePane.setHoverName(new StringTextComponent(" "));
		ItemStack yellowPane = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
		yellowPane.setHoverName(new StringTextComponent(" "));

		for (int i = 0; i < upgradeInventory.getContainerSize(); i++) {
			if (BLACK_FILLER_SLOTS.contains(i)) {
				upgradeInventory.setItem(i, blackPane.copy());
			} else if (PURPLE_FILLER_SLOTS.contains(i)) {
				upgradeInventory.setItem(i, purplePane.copy());
			} else if (YELLOW_FILLER_SLOTS.contains(i)) {
				upgradeInventory.setItem(i, yellowPane.copy());
			}
		}

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Widget's Upgrades");
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new ItemUpgradeContainer(windowId, inventory, upgradeInventory);
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

	public static class UpgradeSlot extends Slot {

		private final Predicate<ItemStack> validator;

		public UpgradeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, Predicate<ItemStack> validator) {
			super(inventoryIn, index, xPosition, yPosition);
			this.validator = validator;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return validator.test(stack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	public static class ItemUpgradeContainer extends Container {

		private final IInventory upgradeSlotsInventory;
		private static final int NUM_UPGRADE_SLOTS = 27;
		private static final int PLAYER_INV_START_INDEX = NUM_UPGRADE_SLOTS;
		private static final int PLAYER_HOTBAR_START_INDEX = PLAYER_INV_START_INDEX + 27;
		private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START_INDEX + 9;

		private boolean craftingOccurred = false;

		public ItemUpgradeContainer(int windowId, PlayerInventory playerInventory, IInventory upgradeSlotsInventory) {
			super(GuiRegistry.ITEM_UPGRADE_CONTAINER.get(), windowId);
			checkContainerSize(upgradeSlotsInventory, NUM_UPGRADE_SLOTS);
			this.upgradeSlotsInventory = upgradeSlotsInventory;
			if (upgradeSlotsInventory instanceof Inventory) {
				upgradeSlotsInventory.startOpen(playerInventory.player);
			}
			addSlots(playerInventory, upgradeSlotsInventory);
		}

		public ItemUpgradeContainer(int windowId, PlayerInventory playerInventory) {
			this(windowId, playerInventory, new Inventory(NUM_UPGRADE_SLOTS));
		}

		private void addSlots(PlayerInventory playerInventory, IInventory upgradeSlotsInventory) {
			int upgradeSlotX = 8;
			int upgradeSlotY = 18;

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int index = col + row * 9;
					int x = upgradeSlotX + col * 18;
					int y = upgradeSlotY + row * 18;

					if (ALL_FILLER_SLOTS.contains(index)) {
						this.addSlot(new FillerSlot(upgradeSlotsInventory, index, x, y));
					} else {
						Predicate<ItemStack> validator = stack -> false;
						if (index == PARTY_RESTORE_SLOT) validator = stack -> stack.getItem() == ItemRegistry.PARTY_RESTORE.get();
						else if (index == MAX_SERUM_SLOT) validator = stack -> stack.getItem() == ItemRegistry.MAX_HEALING_SERUM.get();
						else if (index == MAX_CASING_SLOT) validator = stack -> stack.getItem() == ItemRegistry.MAX_CASING.get();
						else if (index == MAX_AEROSOLIZER_SLOT) validator = stack -> stack.getItem() == ItemRegistry.MAX_AEROSOLIZER.get();
						else if (index == MEGA_TRANSCEIVER_SLOT) validator = stack -> stack.getItem() == ItemRegistry.MEGA_TRANSCEIVER.get();
						else if (index == BATTERY_SLOT) validator = stack -> stack.getItem() == ItemRegistry.BATTERY.get();

						this.addSlot(new UpgradeSlot(upgradeSlotsInventory, index, x, y, validator));
					}
				}
			}

			int playerInvY = upgradeSlotY + (3 * 18) + 14;
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

			if (slot instanceof FillerSlot) {
				return ItemStack.EMPTY;
			}

			ItemStack sourceStack = slot.getItem();
			itemstack = sourceStack.copy();
			Item sourceItem = sourceStack.getItem();

			if (index < PLAYER_INV_START_INDEX) {
				if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START_INDEX, TOTAL_SLOTS, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(sourceStack, itemstack);
			} else {
				boolean movedToUpgradeSlot = false;

				if (sourceItem == ItemRegistry.PARTY_RESTORE.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, PARTY_RESTORE_SLOT, PARTY_RESTORE_SLOT + 1, false);
				} else if (sourceItem == ItemRegistry.MAX_HEALING_SERUM.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, MAX_SERUM_SLOT, MAX_SERUM_SLOT + 1, false);
				} else if (sourceItem == ItemRegistry.MAX_CASING.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, MAX_CASING_SLOT, MAX_CASING_SLOT + 1, false);
				} else if (sourceItem == ItemRegistry.MAX_AEROSOLIZER.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, MAX_AEROSOLIZER_SLOT, MAX_AEROSOLIZER_SLOT + 1, false);
				} else if (sourceItem == ItemRegistry.MEGA_TRANSCEIVER.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, MEGA_TRANSCEIVER_SLOT, MEGA_TRANSCEIVER_SLOT + 1, false);
				} else if (sourceItem == ItemRegistry.BATTERY.get()) {
					movedToUpgradeSlot = this.moveItemStackTo(sourceStack, BATTERY_SLOT, BATTERY_SLOT + 1, false);
				}

				if (!movedToUpgradeSlot) {
					if (index < PLAYER_HOTBAR_START_INDEX) {
						if (!this.moveItemStackTo(sourceStack, PLAYER_HOTBAR_START_INDEX, TOTAL_SLOTS, false)) {
							return ItemStack.EMPTY;
						}
					} else {
						if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START_INDEX, PLAYER_HOTBAR_START_INDEX, false)) {
							return ItemStack.EMPTY;
						}
					}
					return itemstack;
				}
			}

			if (sourceStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (sourceStack.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, sourceStack);
			return itemstack;
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (!player.level.isClientSide && clickTypeIn == ClickType.PICKUP && slotId >= 0 && slotId < PLAYER_INV_START_INDEX) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				boolean crafted = false;

				if (MAX_RESTORE_TRIGGER_SLOTS.contains(slotId)) {
					ItemStack partyRestoreStack = upgradeSlotsInventory.getItem(PARTY_RESTORE_SLOT);
					ItemStack serumStack = upgradeSlotsInventory.getItem(MAX_SERUM_SLOT);
					ItemStack casingStack = upgradeSlotsInventory.getItem(MAX_CASING_SLOT);
					ItemStack aerosolizerStack = upgradeSlotsInventory.getItem(MAX_AEROSOLIZER_SLOT);

					if (
						partyRestoreStack.getItem() == ItemRegistry.PARTY_RESTORE.get() &&
						serumStack.getItem() == ItemRegistry.MAX_HEALING_SERUM.get() &&
						casingStack.getItem() == ItemRegistry.MAX_CASING.get() &&
						aerosolizerStack.getItem() == ItemRegistry.MAX_AEROSOLIZER.get()
					) {
						upgradeSlotsInventory.setItem(PARTY_RESTORE_SLOT, ItemStack.EMPTY);
						upgradeSlotsInventory.setItem(MAX_SERUM_SLOT, ItemStack.EMPTY);
						upgradeSlotsInventory.setItem(MAX_CASING_SLOT, ItemStack.EMPTY);
						upgradeSlotsInventory.setItem(MAX_AEROSOLIZER_SLOT, ItemStack.EMPTY);

						ItemStack result = new ItemStack(ItemRegistry.MAX_PARTY_RESTORE.get());
						if (!serverPlayer.inventory.add(result)) {
							serverPlayer.drop(result, false);
						}
						serverPlayer.sendMessage(
							new StringTextComponent("You crafted a ")
								.withStyle(TextFormatting.GOLD)
								.append(new StringTextComponent("Max Party Restore").withStyle(TextFormatting.DARK_PURPLE))
								.append(new StringTextComponent("!").withStyle(TextFormatting.GOLD)),
							serverPlayer.getUUID()
						);
						serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8F, 1.0F);
						serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 0.7F, 1.5F);
						crafted = true;
					}
				} else if (POKELINK_TRIGGER_SLOTS.contains(slotId)) {
					ItemStack batteryStack = upgradeSlotsInventory.getItem(BATTERY_SLOT);
					ItemStack transceiverStack = upgradeSlotsInventory.getItem(MEGA_TRANSCEIVER_SLOT);

					if (batteryStack.getItem() == ItemRegistry.BATTERY.get() && transceiverStack.getItem() == ItemRegistry.MEGA_TRANSCEIVER.get()) {
						upgradeSlotsInventory.setItem(BATTERY_SLOT, ItemStack.EMPTY);
						upgradeSlotsInventory.setItem(MEGA_TRANSCEIVER_SLOT, ItemStack.EMPTY);

						ItemStack result = new ItemStack(ItemRegistry.POKELINK.get());
						if (!serverPlayer.inventory.add(result)) {
							serverPlayer.drop(result, false);
						}
						serverPlayer.sendMessage(
							new StringTextComponent("You crafted a ")
								.withStyle(TextFormatting.GOLD)
								.append(new StringTextComponent("PokéLink").withStyle(TextFormatting.RED))
								.append(new StringTextComponent("!").withStyle(TextFormatting.GOLD)),
							serverPlayer.getUUID()
						);

						serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8F, 1.0F);
						serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 0.7F, 1.5F);
						crafted = true;
					}
				}

				if (crafted) {
					this.craftingOccurred = true;
					serverPlayer.closeContainer();

					return ItemStack.EMPTY;
				}
			}

			return super.clicked(slotId, dragType, clickTypeIn, player);
		}

		@Override
		public void removed(PlayerEntity pPlayer) {
			super.removed(pPlayer);

			if (!this.craftingOccurred && this.upgradeSlotsInventory instanceof Inventory && !pPlayer.level.isClientSide) {
				for (int index : ALL_FUNCTIONAL_SLOTS) {
					ItemStack stack = this.upgradeSlotsInventory.removeItemNoUpdate(index);
					if (!stack.isEmpty()) {
						pPlayer.inventory.placeItemBackInInventory(pPlayer.level, stack);
					}
				}
			}

			if (this.upgradeSlotsInventory instanceof Inventory) {
				this.upgradeSlotsInventory.stopOpen(pPlayer);
			}
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
	}
}
