package ethan.hoenn.rnbrules.gui.flight;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.network.FlyDestinationsPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.utils.enums.FlightDestination;
import ethan.hoenn.rnbrules.utils.managers.FlyManager;
import java.util.HashSet;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class FlyGui {

	private static Set<String> clientUnlockedDestinations = new HashSet<>();

	private static final Set<Integer> DESTINATION_SLOTS = IntStream.of(2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24).boxed().collect(Collectors.toSet());

	public static void setClientUnlockedDestinations(Set<String> destinations) {
		clientUnlockedDestinations = destinations;
	}

	public static boolean isDestinationUnlockedOnClient(String destinationName) {
		return clientUnlockedDestinations.contains(destinationName);
	}

	public static void openGui(ServerPlayerEntity player) {
		Set<String> unlockedDestinations = new HashSet<>();
		FlyManager flyManager = FlyManager.get((ServerWorld) player.level);

		for (FlightDestination dest : FlightDestination.values()) {
			if (flyManager.hasDestination(player.getUUID(), dest.name())) {
				unlockedDestinations.add(dest.name());
			}
		}

		PacketHandler.INSTANCE.sendTo(new FlyDestinationsPacket(unlockedDestinations), player.connection.getConnection(), net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT);

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Select Destination");
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new FlyContainer(windowId, inventory);
				}
			}
		);
	}

	public static class FlyContainer extends Container {

		private static final EmptyInventory EMPTY_INVENTORY = new EmptyInventory(27);
		final ServerPlayerEntity serverPlayer;

		public FlyContainer(int windowId, PlayerInventory playerInventory) {
			super(GuiRegistry.FLY_CONTAINER.get(), windowId);
			if (playerInventory.player instanceof ServerPlayerEntity) {
				this.serverPlayer = (ServerPlayerEntity) playerInventory.player;
			} else {
				this.serverPlayer = null;
			}

			FlightDestination[] destinations = FlightDestination.values();
			int destIndex = 0;
			ItemStack fillerPane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));

			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 9; col++) {
					int slotIndex = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (DESTINATION_SLOTS.contains(slotIndex) && destIndex < destinations.length) {
						FlyDestinationSlot slot = new FlyDestinationSlot(EMPTY_INVENTORY, slotIndex, x, y, destinations[destIndex++]);
						slot.setParentContainer(this);
						this.addSlot(slot);
					} else {
						this.addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, fillerPane.copy()));
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

				if (slot instanceof FlyDestinationSlot) {
					if (slot.mayPickup(player)) {
						slot.onTake(player, slot.getItem());
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
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
	}

	public static class FlyDestinationSlot extends Slot {

		final FlightDestination destination;
		private FlyContainer parentContainer;

		public FlyDestinationSlot(IInventory inventory, int slotIndex, int x, int y, @Nullable FlightDestination destination) {
			super(inventory, slotIndex, x, y);
			this.destination = destination;
		}

		public void setParentContainer(FlyContainer container) {
			this.parentContainer = container;
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
		public boolean mayPickup(PlayerEntity player) {
			if (destination == null) {
				return false;
			}

			if (player.level.isClientSide) {
				return FlyGui.isDestinationUnlockedOnClient(destination.name());
			}

			if (player instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				return (FlyManager.canFly(serverPlayer) && FlyManager.get((ServerWorld) serverPlayer.level).hasDestination(serverPlayer.getUUID(), destination.name()));
			}

			return false;
		}

		@Override
		public ItemStack getItem() {
			if (destination == null) {
				ItemStack stack = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
				stack.setHoverName(new StringTextComponent(" "));
				return stack;
			}

			boolean isUnlocked;

			if (parentContainer != null && parentContainer.serverPlayer != null) {
				ServerPlayerEntity serverPlayer = parentContainer.serverPlayer;
				isUnlocked = FlyManager.get((ServerWorld) serverPlayer.level).hasDestination(serverPlayer.getUUID(), destination.name());
			} else {
				isUnlocked = FlyGui.isDestinationUnlockedOnClient(destination.name());
			}

			ItemStack stack;
			ITextComponent name;

			if (isUnlocked) {
				stack = new ItemStack(destination.getRepresentativeItem());
				name = getTownFormattedName(destination);
			} else {
				stack = new ItemStack(Items.GRAY_DYE);
				name = new StringTextComponent(destination.getDisplayName()).withStyle(TextFormatting.GRAY);
			}

			stack.setHoverName(name);

			CompoundNBT display = stack.getOrCreateTagElement("display");
			ListNBT lore = new ListNBT();
			ITextComponent loreText;

			if (isUnlocked) {
				loreText = new StringTextComponent(destination.getDescription()).withStyle(TextFormatting.AQUA);
			} else {
				loreText = new StringTextComponent("You have not unlocked this flight destination yet.").withStyle(TextFormatting.RED);
			}

			String loreJson = ITextComponent.Serializer.toJson(loreText);
			lore.add(StringNBT.valueOf(loreJson));
			display.put("Lore", lore);

			return stack;
		}

		private ITextComponent getTownFormattedName(FlightDestination destination) {
			return new StringTextComponent(destination.getDisplayName()).withStyle(destination.getTextColor());
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			if (destination != null && thePlayer instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) thePlayer;

				player.closeContainer();

				FlyManager.setPlayerCooldown(player);

				RNBConfig.TeleportLocation location = RNBConfig.getTownLocation(destination.name());

				FlyCountdown.start(player, destination.getDisplayName(), location);
			}

			return ItemStack.EMPTY;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
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
		public ItemStack getItem(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int slot, int count) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setItem(int slot, ItemStack stack) {}

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
