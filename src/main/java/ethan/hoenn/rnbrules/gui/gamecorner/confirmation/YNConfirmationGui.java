package ethan.hoenn.rnbrules.gui.gamecorner.confirmation;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class YNConfirmationGui {

	private static ConfirmationCallback confirmationCallback;
	private static ItemStack itemToDisplay = ItemStack.EMPTY;
	private static String currentLevelSpec = "";
	private static int currentCost = 0;

	@FunctionalInterface
	public interface ConfirmationCallback {
		void accept(boolean confirmed, List<Pokemon> pool, Pokemon winner, String levelSpec);
	}

	public static void openGui(ServerPlayerEntity player, String title, ItemStack displayItem, List<Pokemon> pool, Pokemon winner, String levelSpec, int cost, ConfirmationCallback callback) {
		confirmationCallback = callback;
		itemToDisplay = displayItem.copy();
		currentLevelSpec = levelSpec;
		currentCost = cost;

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent(title);
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new YNConfirmationContainer(windowId, inventory, itemToDisplay, pool, winner, currentLevelSpec, currentCost);
				}
			},
			buf -> {
				buf.writeItem(itemToDisplay);
				buf.writeInt(pool.size());
				for (Pokemon p : pool) {
					CompoundNBT nbt = new CompoundNBT();
					p.writeToNBT(nbt);
					buf.writeNbt(nbt);
				}
				if (winner != null) {
					buf.writeBoolean(true);
					CompoundNBT winnerNbt = new CompoundNBT();
					winner.writeToNBT(winnerNbt);
					buf.writeNbt(winnerNbt);
				} else {
					buf.writeBoolean(false);
				}
				buf.writeUtf(currentLevelSpec);
				buf.writeInt(currentCost);
			}
		);
	}

	public static class YNConfirmationContainer extends Container {

		private static final EmptyInventory CONFIRMATION_INVENTORY = new EmptyInventory(27);

		private static final Set<Integer> LEFT_GRID_SLOTS = IntStream.of(0, 1, 2, 9, 10, 11, 18, 19, 20).boxed().collect(Collectors.toSet());
		private static final Set<Integer> MIDDLE_GRID_SLOTS = IntStream.of(3, 4, 5, 12, 13, 14, 21, 22, 23).boxed().collect(Collectors.toSet());
		private static final Set<Integer> RIGHT_GRID_SLOTS = IntStream.of(6, 7, 8, 15, 16, 17, 24, 25, 26).boxed().collect(Collectors.toSet());

		private static final int YES_SLOT_INDEX = 10;
		private static final int NO_SLOT_INDEX = 16;
		private static final int DISPLAY_SLOT_INDEX = 13;

		private final ItemStack displayItem;
		private final List<Pokemon> containerPool;
		private final Pokemon containerWinner;
		private final String containerLevelSpec;
		private final int containerCost;

		private boolean confirmationHandled = false;

		public YNConfirmationContainer(int windowId, PlayerInventory playerInventory, ItemStack displayItem, List<Pokemon> pool, Pokemon winner, String levelSpec, int cost) {
			super(GuiRegistry.YN_CONFIRMATION_CONTAINER.get(), windowId);
			this.displayItem = displayItem;
			this.containerPool = new ArrayList<>(pool);
			this.containerWinner = winner;
			this.containerLevelSpec = levelSpec;
			this.containerCost = cost;

			ItemStack limePane = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack blackPane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack redPane = new ItemStack(Items.RED_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));

			ItemStack yesConcrete = new ItemStack(Items.LIME_CONCRETE);
			yesConcrete.setHoverName(new StringTextComponent("YES").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN).withBold(true)));

			ItemStack noConcrete = new ItemStack(Items.RED_CONCRETE);
			noConcrete.setHoverName(new StringTextComponent("NO").withStyle(Style.EMPTY.withColor(TextFormatting.RED).withBold(true)));

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int slotIndex = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (LEFT_GRID_SLOTS.contains(slotIndex)) {
						if (slotIndex == YES_SLOT_INDEX) {
							this.addSlot(new ConfirmationSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, true, yesConcrete));
						} else {
							this.addSlot(new FillerSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, limePane.copy()));
						}
					} else if (MIDDLE_GRID_SLOTS.contains(slotIndex)) {
						if (slotIndex == DISPLAY_SLOT_INDEX) {
							this.addSlot(new FillerSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, this.displayItem));
						} else {
							this.addSlot(new FillerSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, blackPane.copy()));
						}
					} else if (RIGHT_GRID_SLOTS.contains(slotIndex)) {
						if (slotIndex == NO_SLOT_INDEX) {
							this.addSlot(new ConfirmationSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, false, noConcrete));
						} else {
							this.addSlot(new FillerSlot(CONFIRMATION_INVENTORY, slotIndex, x, y, redPane.copy()));
						}
					}
				}
			}
		}

		public YNConfirmationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
			this(windowId, playerInventory, data.readItem(), readPokemonPoolFromBuffer(data), readWinnerFromBuffer(data), data.readUtf(), data.readInt());
		}

		private static List<Pokemon> readPokemonPoolFromBuffer(PacketBuffer buffer) {
			int size = buffer.readInt();
			List<Pokemon> pool = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				CompoundNBT nbt = buffer.readNbt();
				if (nbt != null) {
					Pokemon p = PokemonFactory.create(nbt);
					if (p != null) pool.add(p);
				}
			}
			return pool;
		}

		private static Pokemon readWinnerFromBuffer(PacketBuffer buffer) {
			if (buffer.readBoolean()) {
				CompoundNBT nbt = buffer.readNbt();
				if (nbt != null) {
					return PokemonFactory.create(nbt);
				}
			}
			return null;
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (clickTypeIn == ClickType.PICKUP && slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);
				if (slot instanceof ConfirmationSlot) {
					ConfirmationSlot confirmationSlot = (ConfirmationSlot) slot;
					if (confirmationSlot.mayPickup(player)) {
						if (!player.level.isClientSide && confirmationCallback != null) {
							this.confirmationHandled = true;
							boolean isYes = confirmationSlot.isYesOption();

							if (isYes && player instanceof ServerPlayerEntity) {
								ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
								BankAccount userAccount = (BankAccount) BankAccountProxy.getBankAccount(serverPlayer).orElseThrow(() -> new NullPointerException("bank account"));
								double balance = userAccount.getBalance().doubleValue();

								if (balance < (double) this.containerCost) {
									int needed = this.containerCost - (int) balance;
									player.closeContainer();
									serverPlayer.sendMessage(
										new StringTextComponent("You cannot afford this roll! You need ")
											.withStyle(TextFormatting.RED)
											.append(new StringTextComponent("₽" + needed).withStyle(TextFormatting.GOLD))
											.append(new StringTextComponent(" more.").withStyle(TextFormatting.RED)),
										serverPlayer.getUUID()
									);
									confirmationCallback = null;
									itemToDisplay = ItemStack.EMPTY;
									currentLevelSpec = "";
									currentCost = 0;
									return ItemStack.EMPTY;
								}

								userAccount.take(this.containerCost);
								serverPlayer.sendMessage(
									new StringTextComponent("Spent ")
										.withStyle(TextFormatting.GREEN)
										.append(new StringTextComponent("₽" + this.containerCost).withStyle(TextFormatting.GOLD))
										.append(new StringTextComponent(" on your Game Corner roll.").withStyle(TextFormatting.GREEN)),
									serverPlayer.getUUID()
								);
							}

							ConfirmationCallback callback = confirmationCallback;
							List<Pokemon> pool = new ArrayList<>(this.containerPool);
							Pokemon winner = this.containerWinner;
							String levelSpec = this.containerLevelSpec;

							confirmationCallback = null;
							itemToDisplay = ItemStack.EMPTY;
							currentLevelSpec = "";
							currentCost = 0;

							player.closeContainer();

							if (player instanceof ServerPlayerEntity) {
								ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
								serverPlayer
									.getServer()
									.execute(() -> {
										callback.accept(isYes, pool, winner, levelSpec);
									});
							} else {
								callback.accept(isYes, pool, winner, levelSpec);
							}
						} else if (player.level.isClientSide) {} else if (confirmationCallback == null) {}
						return ItemStack.EMPTY;
					}
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
		public void removed(PlayerEntity playerIn) {
			super.removed(playerIn);
			if (!playerIn.level.isClientSide && confirmationCallback != null && !confirmationHandled) {
				ConfirmationCallback callback = confirmationCallback;
				List<Pokemon> pool = new ArrayList<>(this.containerPool);
				Pokemon winner = this.containerWinner;
				String levelSpec = this.containerLevelSpec;

				confirmationCallback = null;
				itemToDisplay = ItemStack.EMPTY;
				currentLevelSpec = "";
				currentCost = 0;

				callback.accept(false, pool, winner, levelSpec);
			} else if (playerIn.level.isClientSide) {
				itemToDisplay = ItemStack.EMPTY;
				currentLevelSpec = "";
				currentCost = 0;
			} else {}
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

	public static class ConfirmationSlot extends Slot {

		private final boolean isYes;
		private final ItemStack displayStack;

		public ConfirmationSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean isYes, ItemStack displayStack) {
			super(inventoryIn, index, xPosition, yPosition);
			this.isYes = isYes;
			this.displayStack = displayStack;
		}

		public boolean isYesOption() {
			return isYes;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return true;
		}

		@Override
		public ItemStack getItem() {
			return displayStack;
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			return ItemStack.EMPTY;
		}
	}

	public static class FillerSlot extends Slot {

		private final ItemStack displayStack;

		public FillerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, ItemStack displayStack) {
			super(inventoryIn, index, xPosition, yPosition);
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

	private static class EmptyInventory implements IInventory {

		private final int size;

		public EmptyInventory(int size) {
			this.size = size;
		}

		@Override
		public int getContainerSize() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public ItemStack getItem(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int index, int count) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setItem(int index, ItemStack stack) {}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public void clearContent() {}
	}
}
