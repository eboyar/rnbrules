package ethan.hoenn.rnbrules.gui.intriguingstone;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class IntriguingStoneExchangeGui {

	private static final int INVENTORY_SIZE = 27;
	private static final int ROWS = 3;
	private static final int COLUMNS = 9;
	private static final int SLOT_START_X = 8;
	private static final int SLOT_START_Y = 18;
	private static final int SLOT_SIZE = 18;

	private static final Set<Integer> HELD_ITEM_SLOTS = IntStream.of(11, 12, 13, 14, 15).boxed().collect(Collectors.toSet());

	private static final Map<Integer, Item> SLOT_TO_ITEM_MAP = new HashMap<Integer, Item>() {
		{
			put(11, PixelmonItems.leftovers);
			put(12, PixelmonItems.choice_band);
			put(13, PixelmonItems.choice_scarf);
			put(14, PixelmonItems.choice_specs);
			put(15, PixelmonItems.assault_vest);
		}
	};

	private static final Set<Item> EXCHANGEABLE_ITEMS = new HashSet<>(
		Arrays.asList(PixelmonItems.intriguing_stone, PixelmonItems.leftovers, PixelmonItems.choice_band, PixelmonItems.choice_scarf, PixelmonItems.choice_specs, PixelmonItems.assault_vest)
	);

	private static final IInventory EMPTY_INVENTORY = new Inventory(INVENTORY_SIZE);

	public static void openGui(ServerPlayerEntity player) {
		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Intriguing Stone Exchange").withStyle(TextFormatting.WHITE);
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new IntriguingStoneExchangeContainer(windowId, inventory);
				}
			}
		);
	}

	public static class IntriguingStoneExchangeContainer extends Container {

		private final PlayerInventory playerInventory;

		public IntriguingStoneExchangeContainer(int windowId, PlayerInventory playerInventory) {
			super(GuiRegistry.INTRIGUING_STONE_EXCHANGE_CONTAINER.get(), windowId);
			this.playerInventory = playerInventory;

			ItemStack fillerPane = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));

			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLUMNS; col++) {
					int slotIndex = col + row * COLUMNS;
					int x = SLOT_START_X + col * SLOT_SIZE;
					int y = SLOT_START_Y + row * SLOT_SIZE;

					if (HELD_ITEM_SLOTS.contains(slotIndex)) {
						addSlot(new HeldItemSlot(EMPTY_INVENTORY, slotIndex, x, y, slotIndex, playerInventory.player));
					} else {
						addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, fillerPane.copy()));
					}
				}
			}
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
			return false;
		}

		@Override
		public boolean canDragTo(Slot slot) {
			return false;
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (clickTypeIn == ClickType.PICKUP && slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);

				if (slot instanceof HeldItemSlot) {
					HeldItemSlot heldItemSlot = (HeldItemSlot) slot;
					heldItemSlot.onClick(player);
					return ItemStack.EMPTY;
				} else if (slot instanceof FillerSlot) {
					return ItemStack.EMPTY;
				}
			}

			if (player.inventory != null) {
				player.inventory.setCarried(ItemStack.EMPTY);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		private static class FillerSlot extends Slot {

			private final ItemStack displayStack;

			public FillerSlot(IInventory inventory, int slotIndex, int x, int y, ItemStack displayStack) {
				super(inventory, slotIndex, x, y);
				this.displayStack = displayStack;
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

			@Override
			public ItemStack getItem() {
				return displayStack;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		}

		private static class HeldItemSlot extends Slot {

			private final int slotIndex;
			private final PlayerEntity player;

			public HeldItemSlot(IInventory inventory, int slotIndex, int x, int y, int index, PlayerEntity player) {
				super(inventory, slotIndex, x, y);
				this.slotIndex = index;
				this.player = player;
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(PlayerEntity player) {
				return true;
			}

			@Override
			public boolean isActive() {
				return true;
			}

			private boolean isPlayerHoldingItem(Item item) {
				if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == item) {
					return true;
				}

				if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() == item) {
					return true;
				}

				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					if (i >= 36) continue;

					ItemStack stack = player.inventory.getItem(i);
					if (!stack.isEmpty() && stack.getItem() == item) {
						return true;
					}
				}

				return false;
			}

			@Override
			public ItemStack getItem() {
				Item item;
				String name;
				TextFormatting color;

				switch (slotIndex) {
					case 11:
						item = PixelmonItems.leftovers;
						name = "Leftovers";
						color = TextFormatting.RED;
						break;
					case 12:
						item = PixelmonItems.choice_band;
						name = "Choice Band";
						color = TextFormatting.BLUE;
						break;
					case 13:
						item = PixelmonItems.choice_scarf;
						name = "Choice Scarf";
						color = TextFormatting.AQUA;
						break;
					case 14:
						item = PixelmonItems.choice_specs;
						name = "Choice Specs";
						color = TextFormatting.YELLOW;
						break;
					case 15:
						item = PixelmonItems.assault_vest;
						name = "Assault Vest";
						color = TextFormatting.LIGHT_PURPLE;
						break;
					default:
						return new ItemStack(Items.LIME_STAINED_GLASS_PANE);
				}

				boolean playerHasItem = isPlayerHoldingItem(item);

				ItemStack stack = new ItemStack(item);
				stack.setHoverName(new StringTextComponent(name).withStyle(style -> style.withColor(color).withItalic(false).withBold(true)));

				if (playerHasItem) {
					addLore(stack, "§a§lCurrent Item", "§fYou are already holding this item");
				} else {
					addLore(stack, "§fCost: §f1× §eIntriguing Stone §for any equally powerful item");
				}

				return stack;
			}

			private void addLore(ItemStack stack, String... loreLines) {
				CompoundNBT displayTag = stack.getOrCreateTagElement("display");
				ListNBT loreList = new ListNBT();

				for (String line : loreLines) {
					loreList.add(StringNBT.valueOf("{\"text\":\"" + line + "\"}"));
				}

				displayTag.put("Lore", loreList);
			}

			public void onClick(PlayerEntity player) {
				if (!(player instanceof ServerPlayerEntity)) {
					return;
				}

				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				Item targetItem = SLOT_TO_ITEM_MAP.get(slotIndex);

				if (targetItem == null) {
					return;
				}

				if (isPlayerHoldingItem(targetItem)) {
					player.sendMessage(new StringTextComponent("You already have this item!").withStyle(TextFormatting.RED), player.getUUID());
					return;
				}

				ItemStack rewardItem = new ItemStack(targetItem);

				if (!playerHasInventorySpace(player, rewardItem)) {
					player.closeContainer();
					player.sendMessage(new StringTextComponent("Your inventory is full!").withStyle(TextFormatting.GRAY), player.getUUID());
					return;
				}

				if (consumeExchangeableItem(player)) {
					player.inventory.add(rewardItem);
					player.closeContainer();
					player.inventory.setChanged();
					player.containerMenu.broadcastChanges();
				} else {
					player.sendMessage(new StringTextComponent("You need an Intriguing Stone or equally powerful item to make this exchange!").withStyle(TextFormatting.RED), player.getUUID());
				}
			}

			private boolean consumeExchangeableItem(PlayerEntity player) {
				ItemStack mainHand = player.getMainHandItem();
				ItemStack offHand = player.getOffhandItem();

				Item targetItem = SLOT_TO_ITEM_MAP.get(slotIndex);

				if (!mainHand.isEmpty() && EXCHANGEABLE_ITEMS.contains(mainHand.getItem())) {
					if (mainHand.getItem() == targetItem) {
						return false;
					}

					mainHand.shrink(1);

					if (mainHand.isEmpty()) {
						player.setItemInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
					}

					return true;
				}

				if (!offHand.isEmpty() && EXCHANGEABLE_ITEMS.contains(offHand.getItem())) {
					if (offHand.getItem() == targetItem) {
						return false;
					}

					offHand.shrink(1);

					if (offHand.isEmpty()) {
						player.setItemInHand(net.minecraft.util.Hand.OFF_HAND, ItemStack.EMPTY);
					}

					return true;
				}

				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					if (i >= 36) continue;

					ItemStack stack = player.inventory.getItem(i);
					if (!stack.isEmpty() && EXCHANGEABLE_ITEMS.contains(stack.getItem())) {
						if (stack.getItem() == targetItem) {
							continue;
						}

						stack.shrink(1);

						if (stack.isEmpty()) {
							player.inventory.setItem(i, ItemStack.EMPTY);
						}

						return true;
					}
				}

				return false;
			}

			private boolean playerHasInventorySpace(PlayerEntity player, ItemStack stack) {
				ItemStack itemToCheck = stack.copy();
				PlayerInventory inventory = player.inventory;

				for (int i = 0; i < inventory.getContainerSize(); i++) {
					if (i >= 36) continue;

					ItemStack existingStack = inventory.getItem(i);

					if (existingStack.isEmpty()) {
						return true;
					}

					if (ItemStack.isSame(existingStack, itemToCheck) && ItemStack.tagMatches(existingStack, itemToCheck)) {
						int remaining = existingStack.getMaxStackSize() - existingStack.getCount();
						if (remaining >= itemToCheck.getCount()) {
							return true;
						}

						itemToCheck.setCount(itemToCheck.getCount() - remaining);
					}
				}

				return itemToCheck.getCount() <= 0;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		}
	}
}
