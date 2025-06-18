package ethan.hoenn.rnbrules.gui.heartscale;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import ethan.hoenn.rnbrules.gui.heartscale.natures.NaturesGui;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
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
public class HeartscaleExchangeGui {

	private static final int INVENTORY_SIZE = 27;
	private static final int ROWS = 3;
	private static final int COLUMNS = 9;
	private static final int SLOT_START_X = 8;
	private static final int SLOT_START_Y = 18;
	private static final int SLOT_SIZE = 18;

	private static final Set<Integer> BOTTLE_CAP_SLOTS = IntStream.of(12).boxed().collect(Collectors.toSet());
	private static final Set<Integer> MINTS_SLOTS = IntStream.of(14).boxed().collect(Collectors.toSet());

	private static final int BOTTLE_CAP_COST = 1;
	private static final int MINT_COST = 3;

	private static final IInventory EMPTY_INVENTORY = new Inventory(INVENTORY_SIZE);

	public static void openGui(ServerPlayerEntity player) {
		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Heart Scale Exchange").withStyle(TextFormatting.WHITE);
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new HeartscaleExchangeContainer(windowId, inventory);
				}
			}
		);
	}

	public static class HeartscaleExchangeContainer extends Container {

		private final PlayerInventory playerInventory;

		public HeartscaleExchangeContainer(int windowId, PlayerInventory playerInventory) {
			super(GuiRegistry.HEARTSCALE_EXCHANGE_CONTAINER.get(), windowId);
			this.playerInventory = playerInventory;

			ItemStack fillerPane = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));

			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLUMNS; col++) {
					int slotIndex = col + row * COLUMNS;
					int x = SLOT_START_X + col * SLOT_SIZE;
					int y = SLOT_START_Y + row * SLOT_SIZE;

					if (BOTTLE_CAP_SLOTS.contains(slotIndex)) {
						addSlot(new ExchangeSlot(EMPTY_INVENTORY, slotIndex, x, y, ExchangeType.BOTTLE_CAP));
					} else if (MINTS_SLOTS.contains(slotIndex)) {
						addSlot(new ExchangeSlot(EMPTY_INVENTORY, slotIndex, x, y, ExchangeType.NATURES));
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

				if (slot instanceof ExchangeSlot) {
					ExchangeSlot exchangeSlot = (ExchangeSlot) slot;
					exchangeSlot.onClick(player);
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

		private enum ExchangeType {
			BOTTLE_CAP,
			NATURES,
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

		private static class ExchangeSlot extends Slot {

			private final ExchangeType exchangeType;

			public ExchangeSlot(IInventory inventory, int slotIndex, int x, int y, ExchangeType exchangeType) {
				super(inventory, slotIndex, x, y);
				this.exchangeType = exchangeType;
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

			@Override
			public ItemStack getItem() {
				if (exchangeType == ExchangeType.BOTTLE_CAP) {
					ItemStack stack = new ItemStack(PixelmonItems.silver_bottle_cap);
					stack.setHoverName(new StringTextComponent("Silver Bottle Cap").withStyle(TextFormatting.AQUA));
					addLore(stack, "§7Cost: §f" + BOTTLE_CAP_COST + "× §dHeart Scale");
					return stack;
				} else if (exchangeType == ExchangeType.NATURES) {
					ItemStack stack = new ItemStack(PixelmonItems.mint_seeds);
					stack.setHoverName(new StringTextComponent("Nature Mints").withStyle(TextFormatting.GREEN));
					addLore(stack, "§7Exchange §dHeart Scales §7for §6Nature Mints", "§7Each mint costs §f" + MINT_COST + "× §dHeart Scale");
					return stack;
				}

				return new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE);
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

				if (exchangeType == ExchangeType.BOTTLE_CAP) {
					handleBottleCapExchange(serverPlayer);
				} else if (exchangeType == ExchangeType.NATURES) {
					handleNaturesExchange(serverPlayer);
				}
			}

			private void handleBottleCapExchange(ServerPlayerEntity player) {
				ItemStack bottleCap = new ItemStack(PixelmonItems.silver_bottle_cap);

				if (!playerHasInventorySpace(player, bottleCap)) {
					player.closeContainer();
					player.sendMessage(new StringTextComponent("Your inventory is full!").withStyle(TextFormatting.GRAY), player.getUUID());
					return;
				}

				if (consumeHeartScales(player, BOTTLE_CAP_COST)) {
					player.inventory.add(bottleCap);
					player.closeContainer();
					player.inventory.setChanged();
					player.containerMenu.broadcastChanges();
				}
			}

			private void handleNaturesExchange(ServerPlayerEntity player) {
				player.closeContainer();
				NaturesGui.openGui(player);
			}

			private boolean consumeHeartScales(PlayerEntity player, int count) {
				int heartScalesFound = 0;

				// First, check both hands
				ItemStack mainHand = player.getMainHandItem();
				ItemStack offHand = player.getOffhandItem();

				if (!mainHand.isEmpty() && mainHand.getItem() == PixelmonItems.heart_scale) {
					int toTake = Math.min(mainHand.getCount(), count - heartScalesFound);
					mainHand.shrink(toTake);
					heartScalesFound += toTake;

					if (mainHand.isEmpty()) {
						player.setItemInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
					}

					if (heartScalesFound >= count) {
						return true;
					}
				}

				if (!offHand.isEmpty() && offHand.getItem() == PixelmonItems.heart_scale) {
					int toTake = Math.min(offHand.getCount(), count - heartScalesFound);
					offHand.shrink(toTake);
					heartScalesFound += toTake;

					if (offHand.isEmpty()) {
						player.setItemInHand(net.minecraft.util.Hand.OFF_HAND, ItemStack.EMPTY);
					}

					if (heartScalesFound >= count) {
						return true;
					}
				}

				// If we still need more heart scales, check the inventory
				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					if (i >= 36) continue; // Skip armor slots

					ItemStack stack = player.inventory.getItem(i);
					if (!stack.isEmpty() && stack.getItem() == PixelmonItems.heart_scale) {
						int toTake = Math.min(stack.getCount(), count - heartScalesFound);
						stack.shrink(toTake);
						heartScalesFound += toTake;

						if (stack.isEmpty()) {
							player.inventory.setItem(i, ItemStack.EMPTY);
						}

						if (heartScalesFound >= count) {
							return true;
						}
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
