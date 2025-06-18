package ethan.hoenn.rnbrules.gui.backpack;

import ethan.hoenn.rnbrules.registries.GuiRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

@SuppressWarnings("NullableProblems")
public class BigBackpackContainer extends Container {

	private static final int ROWS = 8;
	private static final int COLUMNS = 23;
	private static final int SLOT_START_X = 6;
	private static final int SLOT_START_Y = 8;
	private static final int SLOT_SIZE = 18;
	private static final int PLAYER_INVENTORY_START_X = 132;
	private static final int PLAYER_INVENTORY_START_Y = 159;
	private static final int PLAYER_HOTBAR_Y = 217;
	private static final int BACKPACK_SLOTS_COUNT = ROWS * COLUMNS;

	private final ItemStack backpack;
	private final Hand hand;
	private final IItemHandler inventory;

	public BigBackpackContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
		super(GuiRegistry.BACKPACK_CONTAINER.get(), windowId);
		boolean isMainHand = data.readBoolean();
		this.hand = isMainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
		this.backpack = playerInventory.player.getItemInHand(hand);

		this.inventory = backpack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new IllegalStateException("Backpack item has no inventory capability"));

		initContainer(playerInventory);
	}

	public BigBackpackContainer(int windowId, PlayerInventory playerInventory, ItemStack backpack, Hand hand) {
		super(GuiRegistry.BACKPACK_CONTAINER.get(), windowId);
		this.backpack = backpack;
		this.hand = hand;

		this.inventory = backpack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new IllegalStateException("Backpack item has no inventory capability"));

		initContainer(playerInventory);
	}

	private void initContainer(PlayerInventory playerInventory) {
		// Add backpack inventory slots
		addBackpackSlots();

		// Add player inventory slots
		addPlayerInventorySlots(playerInventory);
	}

	private void addBackpackSlots() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				int index = col + row * COLUMNS;
				int x = SLOT_START_X + col * SLOT_SIZE;
				int y = SLOT_START_Y + row * SLOT_SIZE;
				addSlot(new SlotItemHandler(inventory, index, x, y));
			}
		}
	}

	private void addPlayerInventorySlots(PlayerInventory playerInventory) {
		// Add player main inventory slots (3 rows x 9 columns)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int index = col + row * 9 + 9; // +9 because hotbar is 0-8
				int x = PLAYER_INVENTORY_START_X + col * SLOT_SIZE;
				int y = PLAYER_INVENTORY_START_Y + row * SLOT_SIZE;
				addSlot(new Slot(playerInventory, index, x, y));
			}
		}

		// Add player hotbar slots (9 columns)
		for (int col = 0; col < 9; col++) {
			int x = PLAYER_INVENTORY_START_X + col * SLOT_SIZE;
			addSlot(new Slot(playerInventory, col, x, PLAYER_HOTBAR_Y));
		}
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		ItemStack heldItem = hand == Hand.MAIN_HAND ? player.getMainHandItem() : player.getOffhandItem();
		return !heldItem.isEmpty() && ItemStack.isSame(heldItem, backpack);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			itemstack = slotStack.copy();

			if (index < BACKPACK_SLOTS_COUNT) {
				// If clicking a backpack slot, try to move to player inventory
				if (!this.moveItemStackTo(slotStack, BACKPACK_SLOTS_COUNT, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				// If clicking a player inventory slot, try to move to backpack
				if (!this.moveItemStackTo(slotStack, 0, BACKPACK_SLOTS_COUNT, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public boolean canDragTo(Slot slotIn) {
		ItemStack stack = slotIn.getItem();
		return !ItemStack.isSame(stack, backpack) && super.canDragTo(slotIn);
	}
}
