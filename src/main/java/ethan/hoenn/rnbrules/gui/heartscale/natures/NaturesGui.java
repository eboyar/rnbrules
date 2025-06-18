package ethan.hoenn.rnbrules.gui.heartscale.natures;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import ethan.hoenn.rnbrules.gui.heartscale.HeartscaleExchangeGui;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
public class NaturesGui {

	private static final int INVENTORY_SIZE = 36;
	private static final int ROWS = 6;
	private static final int COLUMNS = 6;
	private static final int SLOT_START_X = 8;
	private static final int SLOT_START_Y = 18;
	private static final int SLOT_SIZE = 18;
	private static final int HEART_SCALE_COST = 3;

	private static final String[][] NATURE_MATRIX = {
		{ "BACK", "ATTACK_DOWN", "DEFENSE_DOWN", "SP_ATK_DOWN", "SP_DEF_DOWN", "SPEED_DOWN" },
		{ "ATTACK_UP", "Hardy", "Lonely", "Adamant", "Naughty", "Brave" },
		{ "DEFENSE_UP", "Bold", "Docile", "Impish", "Lax", "Relaxed" },
		{ "SP_ATK_UP", "Modest", "Mild", "Bashful", "Rash", "Quiet" },
		{ "SP_DEF_UP", "Calm", "Gentle", "Careful", "Quirky", "Sassy" },
		{ "SPEED_UP", "Timid", "Hasty", "Jolly", "Naive", "Serious" },
	};

	private static final String[] STAT_NAMES = { "???", "Attack", "Defense", "Sp. Atk", "Sp. Def", "Speed" };

	private static final Map<String, Item> MINT_ITEM_MAP = createMintItemMap();

	private static Map<String, Item> createMintItemMap() {
		Map<String, Item> map = new HashMap<>();
		map.put("hardy", PixelmonItems.mint_hardy);
		map.put("lonely", PixelmonItems.mint_lonely);
		map.put("adamant", PixelmonItems.mint_adamant);
		map.put("naughty", PixelmonItems.mint_naughty);
		map.put("brave", PixelmonItems.mint_brave);
		map.put("bold", PixelmonItems.mint_bold);
		map.put("docile", PixelmonItems.mint_docile);
		map.put("impish", PixelmonItems.mint_impish);
		map.put("lax", PixelmonItems.mint_lax);
		map.put("relaxed", PixelmonItems.mint_relaxed);
		map.put("modest", PixelmonItems.mint_modest);
		map.put("mild", PixelmonItems.mint_mild);
		map.put("bashful", PixelmonItems.mint_bashful);
		map.put("rash", PixelmonItems.mint_rash);
		map.put("quiet", PixelmonItems.mint_quiet);
		map.put("calm", PixelmonItems.mint_calm);
		map.put("gentle", PixelmonItems.mint_gentle);
		map.put("careful", PixelmonItems.mint_careful);
		map.put("quirky", PixelmonItems.mint_quirky);
		map.put("sassy", PixelmonItems.mint_sassy);
		map.put("timid", PixelmonItems.mint_timid);
		map.put("hasty", PixelmonItems.mint_hasty);
		map.put("jolly", PixelmonItems.mint_jolly);
		map.put("naive", PixelmonItems.mint_naive);
		map.put("serious", PixelmonItems.mint_serious);
		return map;
	}

	private static final IInventory EMPTY_INVENTORY = new Inventory(INVENTORY_SIZE);

	public static void openGui(ServerPlayerEntity player) {
		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Nature Mints").withStyle(TextFormatting.WHITE);
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new NaturesContainer(windowId, inventory);
				}
			}
		);
	}

	public static class NaturesContainer extends Container {

		private final PlayerInventory playerInventory;

		public NaturesContainer(int windowId, PlayerInventory playerInventory) {
			super(GuiRegistry.NATURES_CONTAINER.get(), windowId);
			this.playerInventory = playerInventory;

			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLUMNS; col++) {
					int slotIndex = col + row * COLUMNS;
					int x = SLOT_START_X + col * SLOT_SIZE;
					int y = SLOT_START_Y + row * SLOT_SIZE;

					this.addSlot(new NatureSlot(EMPTY_INVENTORY, slotIndex, x, y, row, col));
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

				if (slot instanceof NatureSlot) {
					NatureSlot natureSlot = (NatureSlot) slot;
					if (natureSlot.canPurchase(player)) {
						natureSlot.onPurchase(player);
					}
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
	}

	public static class NatureSlot extends Slot {

		private final int row;
		private final int col;
		private final SlotType slotType;

		public NatureSlot(IInventory inventory, int slotIndex, int x, int y, int row, int col) {
			super(inventory, slotIndex, x, y);
			this.row = row;
			this.col = col;
			this.slotType = determineSlotType(row, col);
		}

		private SlotType determineSlotType(int row, int col) {
			if (row == 0 && col == 0) {
				return SlotType.BACK_BUTTON;
			} else if (row == 0 && col > 0) {
				return SlotType.DECREASED_STAT;
			} else if (col == 0 && row > 0) {
				return SlotType.INCREASED_STAT;
			} else if (row > 0 && col > 0) {
				return SlotType.NATURE_MINT;
			} else {
				return SlotType.FILLER;
			}
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity player) {
			return canPurchase(player);
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public ItemStack getItem() {
			switch (slotType) {
				case BACK_BUTTON:
					return createBackButtonItem();
				case DECREASED_STAT:
					return createDecreasedStatItem(col);
				case INCREASED_STAT:
					return createIncreasedStatItem(row);
				case NATURE_MINT:
					return createNatureMintItem(row, col);
				default:
					return createFillerItem();
			}
		}

		private ItemStack createBackButtonItem() {
			ItemStack stack = new ItemStack(Items.BARRIER);
			stack.setHoverName(new StringTextComponent("Back").withStyle(TextFormatting.RED));
			return stack;
		}

		private ItemStack createDecreasedStatItem(int col) {
			ItemStack stack = new ItemStack(Items.RED_STAINED_GLASS_PANE);
			String statName = getStatName(col);
			stack.setHoverName(new StringTextComponent("- " + statName).withStyle(TextFormatting.RED));
			return stack;
		}

		private ItemStack createIncreasedStatItem(int row) {
			ItemStack stack = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
			String statName = getStatName(row);
			stack.setHoverName(new StringTextComponent("+ " + statName).withStyle(TextFormatting.GREEN));
			return stack;
		}

		private ItemStack createNatureMintItem(int row, int col) {
			String natureName = getNatureName(row, col);
			ItemStack stack = createMintStack(natureName);
			addLore(stack, "§7Cost: §f" + HEART_SCALE_COST + "× §dHeart Scale");
			return stack;
		}

		private ItemStack createFillerItem() {
			ItemStack stack = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
			stack.setHoverName(new StringTextComponent(" "));
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

		public boolean canPurchase(PlayerEntity player) {
			if (slotType == SlotType.BACK_BUTTON) {
				return true;
			}

			if (slotType != SlotType.NATURE_MINT) {
				return false;
			}

			int heartScalesCount = countHeartScales(player);
			return heartScalesCount >= HEART_SCALE_COST;
		}

		private int countHeartScales(PlayerEntity player) {
			int count = 0;

			// Check main hand
			ItemStack mainHand = player.getMainHandItem();
			if (!mainHand.isEmpty() && mainHand.getItem() == PixelmonItems.heart_scale) {
				count += mainHand.getCount();
			}

			// Check off hand
			ItemStack offHand = player.getOffhandItem();
			if (!offHand.isEmpty() && offHand.getItem() == PixelmonItems.heart_scale) {
				count += offHand.getCount();
			}

			// Check inventory
			for (int i = 0; i < player.inventory.getContainerSize(); i++) {
				if (i >= 36) continue; // Skip armor slots

				ItemStack stack = player.inventory.getItem(i);
				if (!stack.isEmpty() && stack.getItem() == PixelmonItems.heart_scale) {
					count += stack.getCount();
				}
			}

			return count;
		}

		public void onPurchase(PlayerEntity player) {
			if (!(player instanceof ServerPlayerEntity)) {
				return;
			}

			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

			if (slotType == SlotType.BACK_BUTTON) {
				player.closeContainer();
				HeartscaleExchangeGui.openGui(serverPlayer);
				return;
			}

			if (slotType != SlotType.NATURE_MINT) {
				return;
			}

			String natureName = getNatureName(row, col);
			ItemStack mintStack = createMintStack(natureName);

			if (!playerHasInventorySpace(player, mintStack)) {
				player.closeContainer();
				player.sendMessage(new StringTextComponent("Your inventory is full!").withStyle(TextFormatting.GRAY), player.getUUID());
				return;
			}

			if (consumeHeartScales(player, HEART_SCALE_COST)) {
				if (!player.inventory.add(mintStack)) {
					player.drop(mintStack, false);
				}

				player.closeContainer();
				player.inventory.setChanged();
				player.containerMenu.broadcastChanges();
			}
		}

		private boolean consumeHeartScales(PlayerEntity player, int count) {
			int heartScalesRemaining = count;

			// First, check both hands
			ItemStack mainHand = player.getMainHandItem();
			if (!mainHand.isEmpty() && mainHand.getItem() == PixelmonItems.heart_scale) {
				int toRemove = Math.min(mainHand.getCount(), heartScalesRemaining);
				mainHand.shrink(toRemove);
				heartScalesRemaining -= toRemove;

				if (mainHand.isEmpty()) {
					player.setItemInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
				}

				if (heartScalesRemaining <= 0) {
					return true;
				}
			}

			if (heartScalesRemaining > 0) {
				ItemStack offHand = player.getOffhandItem();
				if (!offHand.isEmpty() && offHand.getItem() == PixelmonItems.heart_scale) {
					int toRemove = Math.min(offHand.getCount(), heartScalesRemaining);
					offHand.shrink(toRemove);
					heartScalesRemaining -= toRemove;

					if (offHand.isEmpty()) {
						player.setItemInHand(net.minecraft.util.Hand.OFF_HAND, ItemStack.EMPTY);
					}

					if (heartScalesRemaining <= 0) {
						return true;
					}
				}
			}

			// If we still need more heart scales, check the inventory
			if (heartScalesRemaining > 0) {
				for (int i = 0; i < player.inventory.getContainerSize(); i++) {
					if (i >= 36) continue; // Skip armor slots

					ItemStack stack = player.inventory.getItem(i);
					if (!stack.isEmpty() && stack.getItem() == PixelmonItems.heart_scale) {
						int toRemove = Math.min(stack.getCount(), heartScalesRemaining);
						stack.shrink(toRemove);
						heartScalesRemaining -= toRemove;

						if (stack.isEmpty()) {
							player.inventory.setItem(i, ItemStack.EMPTY);
						}

						if (heartScalesRemaining <= 0) {
							return true;
						}
					}
				}
			}

			return heartScalesRemaining <= 0;
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

		private String getStatName(int index) {
			if (index >= 0 && index < STAT_NAMES.length) {
				return STAT_NAMES[index];
			}
			return "???";
		}

		private String getNatureName(int row, int col) {
			if (row >= 0 && row < NATURE_MATRIX.length && col >= 0 && col < NATURE_MATRIX[row].length) {
				return NATURE_MATRIX[row][col];
			}
			return "";
		}

		private static ItemStack createMintStack(String natureName) {
			Item mintItem = MINT_ITEM_MAP.getOrDefault(natureName.toLowerCase(), Items.PAPER);
			return new ItemStack(mintItem);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		private enum SlotType {
			BACK_BUTTON,
			DECREASED_STAT,
			INCREASED_STAT,
			NATURE_MINT,
			FILLER,
		}
	}
}
