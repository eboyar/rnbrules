package ethan.hoenn.rnbrules.gui.league.formatselection;

import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.registries.ItemRegistry;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class FormatSelectionGui {

	private static final Map<UUID, FormatCallback> formatCallbacks = new HashMap<>();
	private static final Map<UUID, LeagueManager.LeagueMember> memberSelections = new HashMap<>();

	@FunctionalInterface
	public interface FormatCallback {
		void accept(boolean isSingles, LeagueManager.LeagueMember member);
	}

	public static void openGui(ServerPlayerEntity player, LeagueManager.LeagueMember member, int currentSinglesCount, int currentDoublesCount, FormatCallback callback) {
		formatCallbacks.put(player.getUUID(), callback);
		memberSelections.put(player.getUUID(), member);

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Select Battle Format").withStyle(TextFormatting.WHITE);
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new FormatSelectionContainer(windowId, inventory, member, currentSinglesCount, currentDoublesCount);
				}
			},
			buf -> {
				buf.writeInt(member.ordinal());
				buf.writeInt(currentSinglesCount);
				buf.writeInt(currentDoublesCount);
			}
		);
	}

	public static class FormatSelectionContainer extends Container {

		private static final EmptyInventory FORMAT_INVENTORY = new EmptyInventory(27);

		private static final Set<Integer> LEFT_GRID_SLOTS = IntStream.of(0, 1, 2, 9, 10, 11, 18, 19, 20).boxed().collect(Collectors.toSet());
		private static final Set<Integer> MIDDLE_GRID_SLOTS = IntStream.of(3, 4, 5, 12, 13, 14, 21, 22, 23).boxed().collect(Collectors.toSet());
		private static final Set<Integer> RIGHT_GRID_SLOTS = IntStream.of(6, 7, 8, 15, 16, 17, 24, 25, 26).boxed().collect(Collectors.toSet());

		private static final int SINGLES_SLOT_INDEX = 10;
		private static final int DOUBLES_SLOT_INDEX = 16;
		private static final int MEMBER_DISPLAY_SLOT_INDEX = 13;

		private boolean formatSelected = false;

		public FormatSelectionContainer(int windowId, PlayerInventory playerInventory, LeagueManager.LeagueMember member, int singlesCount, int doublesCount) {
			super(GuiRegistry.FORMAT_SELECTION_CONTAINER.get(), windowId);
			boolean canDoSingles = singlesCount < 2;

			boolean canDoDoubles = doublesCount < 2;

			ItemStack blackPane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack whitePane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack memberItem = getMemberDisplayItem(member);

			ItemStack singlesOption;
			if (canDoSingles) {
				singlesOption = new ItemStack(Items.LIME_CONCRETE);
				singlesOption.setHoverName(new StringTextComponent("SINGLES").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN).withBold(true)));

				ListNBT singleLore = new ListNBT();
				singleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("Challenge this Elite Four member").withStyle(TextFormatting.GRAY))));
				singleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("in a single battle format").withStyle(TextFormatting.GRAY))));

				CompoundNBT singleDisplay = singlesOption.getOrCreateTagElement("display");
				singleDisplay.put("Lore", singleLore);
			} else {
				singlesOption = new ItemStack(Items.GRAY_CONCRETE);
				singlesOption.setHoverName(new StringTextComponent("SINGLES").withStyle(Style.EMPTY.withColor(TextFormatting.GRAY).withBold(true)));

				ListNBT singleLore = new ListNBT();
				singleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("You have already completed").withStyle(TextFormatting.GRAY))));
				singleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("the maximum number of single battles").withStyle(TextFormatting.GRAY))));

				CompoundNBT singleDisplay = singlesOption.getOrCreateTagElement("display");
				singleDisplay.put("Lore", singleLore);
			}

			ItemStack doublesOption;
			if (canDoDoubles) {
				doublesOption = new ItemStack(Items.BLUE_CONCRETE);
				doublesOption.setHoverName(new StringTextComponent("DOUBLES").withStyle(Style.EMPTY.withColor(TextFormatting.BLUE).withBold(true)));

				ListNBT doubleLore = new ListNBT();
				doubleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("Challenge this Elite Four member").withStyle(TextFormatting.GRAY))));
				doubleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("in a double battle format").withStyle(TextFormatting.GRAY))));

				CompoundNBT doubleDisplay = doublesOption.getOrCreateTagElement("display");
				doubleDisplay.put("Lore", doubleLore);
			} else {
				doublesOption = new ItemStack(Items.GRAY_CONCRETE);
				doublesOption.setHoverName(new StringTextComponent("DOUBLES").withStyle(Style.EMPTY.withColor(TextFormatting.GRAY).withBold(true)));

				ListNBT doubleLore = new ListNBT();
				doubleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("You have already completed").withStyle(TextFormatting.GRAY))));
				doubleLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("the maximum number of double battles").withStyle(TextFormatting.GRAY))));

				CompoundNBT doubleDisplay = doublesOption.getOrCreateTagElement("display");
				doubleDisplay.put("Lore", doubleLore);
			}

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int slotIndex = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (slotIndex == SINGLES_SLOT_INDEX) {
						this.addSlot(new FormatSlot(FORMAT_INVENTORY, slotIndex, x, y, singlesOption, true, canDoSingles));
					} else if (slotIndex == DOUBLES_SLOT_INDEX) {
						this.addSlot(new FormatSlot(FORMAT_INVENTORY, slotIndex, x, y, doublesOption, false, canDoDoubles));
					} else if (slotIndex == MEMBER_DISPLAY_SLOT_INDEX) {
						this.addSlot(new StaticSlot(FORMAT_INVENTORY, slotIndex, x, y, memberItem));
					} else if (MIDDLE_GRID_SLOTS.contains(slotIndex)) {
						this.addSlot(new StaticSlot(FORMAT_INVENTORY, slotIndex, x, y, whitePane));
					} else if (LEFT_GRID_SLOTS.contains(slotIndex) || RIGHT_GRID_SLOTS.contains(slotIndex)) {
						this.addSlot(new StaticSlot(FORMAT_INVENTORY, slotIndex, x, y, blackPane));
					} else {
						this.addSlot(new StaticSlot(FORMAT_INVENTORY, slotIndex, x, y, blackPane));
					}
				}
			}
		}

		public FormatSelectionContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
			this(windowId, playerInventory, LeagueManager.LeagueMember.values()[data.readInt()], data.readInt(), data.readInt());
		}

		private ItemStack getMemberDisplayItem(LeagueManager.LeagueMember member) {
			ItemStack memberStack;
			switch (member) {
				case SIDNEY:
					memberStack = ItemRegistry.SIDNEY.get().getDefaultInstance();
					memberStack.setHoverName(new StringTextComponent("Sidney").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_GRAY).withBold(true)));
					break;
				case PHOEBE:
					memberStack = ItemRegistry.PHOEBE.get().getDefaultInstance();
					memberStack.setHoverName(new StringTextComponent("Phoebe").withStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withBold(true)));
					break;
				case GLACIA:
					memberStack = ItemRegistry.GLACIA.get().getDefaultInstance();
					memberStack.setHoverName(new StringTextComponent("Glacia").withStyle(Style.EMPTY.withColor(TextFormatting.AQUA).withBold(true)));
					break;
				case DRAKE:
					memberStack = ItemRegistry.DRAKE.get().getDefaultInstance();
					memberStack.setHoverName(new StringTextComponent("Drake").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD).withBold(true)));
					break;
				case WALLACE:
					memberStack = new ItemStack(Items.BARRIER).setHoverName(new StringTextComponent("Error: Wallace shouldn't be here").withStyle(TextFormatting.RED));
					break;
				default:
					memberStack = new ItemStack(Items.BARRIER).setHoverName(new StringTextComponent("Unknown Member").withStyle(TextFormatting.RED));
					break;
			}

			if (memberStack.hasTag() && memberStack.getTag().contains("display")) {
				memberStack.getTag().getCompound("display").remove("Lore");
			}
			return memberStack;
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (clickTypeIn == ClickType.PICKUP && slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);

				if (slot instanceof FormatSlot) {
					FormatSlot formatSlot = (FormatSlot) slot;
					if (formatSlot.isAvailable()) {
						this.formatSelected = true;

						if (!player.level.isClientSide && player instanceof ServerPlayerEntity) {
							UUID playerUUID = player.getUUID();

							FormatCallback callback = formatCallbacks.get(playerUUID);
							LeagueManager.LeagueMember member = memberSelections.get(playerUUID);

							memberSelections.remove(playerUUID);
							if (callback != null && member != null) {
								callback.accept(formatSlot.isSingles(), member);
								return ItemStack.EMPTY;
							}
						}

						return ItemStack.EMPTY;
					}
				}
			}

			if (player.inventory != null) {
				player.inventory.setCarried(ItemStack.EMPTY);
			}

			return ItemStack.EMPTY;
		}

		@Override
		public void removed(PlayerEntity playerIn) {
			super.removed(playerIn);

			if (!playerIn.level.isClientSide && !formatSelected) {
				UUID playerUUID = playerIn.getUUID();

				if (FormatSelectionGui.formatCallbacks.containsKey(playerUUID)) {
					FormatSelectionGui.formatCallbacks.remove(playerUUID);
					FormatSelectionGui.memberSelections.remove(playerUUID);
				}
			}
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
			return false;
		}

		@Override
		public boolean canDragTo(Slot slotIn) {
			return false;
		}

		@Override
		public boolean stillValid(PlayerEntity playerIn) {
			return true;
		}
	}

	private static class StaticSlot extends Slot {

		public StaticSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, ItemStack displayItem) {
			super(inventoryIn, index, xPosition, yPosition);
			inventoryIn.setItem(index, displayItem);
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

	private static class FormatSlot extends Slot {

		private final boolean isSingles;
		private final boolean isAvailable;

		public FormatSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, ItemStack displayItem, boolean isSingles, boolean isAvailable) {
			super(inventoryIn, index, xPosition, yPosition);
			this.isSingles = isSingles;
			this.isAvailable = isAvailable;
			inventoryIn.setItem(index, displayItem);
		}

		public boolean isSingles() {
			return isSingles;
		}

		public boolean isAvailable() {
			return isAvailable;
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

	private static class EmptyInventory implements IInventory {

		private final NonNullList<ItemStack> items;

		public EmptyInventory(int size) {
			this.items = NonNullList.withSize(size, ItemStack.EMPTY);
		}

		@Override
		public int getContainerSize() {
			return this.items.size();
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack itemstack : this.items) {
				if (!itemstack.isEmpty()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public ItemStack getItem(int index) {
			return this.items.get(index);
		}

		@Override
		public ItemStack removeItem(int index, int count) {
			ItemStack itemstack = ItemStackHelper.removeItem(this.items, index, count);
			if (!itemstack.isEmpty()) {
				this.setChanged();
			}
			return itemstack;
		}

		@Override
		public ItemStack removeItemNoUpdate(int index) {
			ItemStack itemstack = this.items.get(index);
			if (itemstack.isEmpty()) {
				return ItemStack.EMPTY;
			} else {
				this.items.set(index, ItemStack.EMPTY);
				return itemstack;
			}
		}

		@Override
		public void setItem(int index, ItemStack stack) {
			this.items.set(index, stack);
			if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
				stack.setCount(this.getMaxStackSize());
			}
			this.setChanged();
		}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public void clearContent() {
			this.items.clear();
			this.setChanged();
		}
	}

	public static void cleanupCallbacks(UUID playerUUID) {
		formatCallbacks.remove(playerUUID);
		memberSelections.remove(playerUUID);
	}

	public static void cleanupAllCallbacks() {
		formatCallbacks.clear();
		memberSelections.clear();
	}
}
