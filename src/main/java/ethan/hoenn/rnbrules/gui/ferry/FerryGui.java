package ethan.hoenn.rnbrules.gui.ferry;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.network.FerryDestinationsPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.utils.enums.FerryDestination;
import ethan.hoenn.rnbrules.utils.enums.FerryRoute;
import ethan.hoenn.rnbrules.utils.managers.FerryManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class FerryGui {

	private static Set<String> clientUnlockedDestinations = new HashSet<>();
	private static String clientCurrentLocation = null;

	public static void setClientUnlockedDestinations(Set<String> destinations) {
		clientUnlockedDestinations = destinations;
	}

	public static void setClientCurrentLocation(String location) {
		clientCurrentLocation = location;
	}

	public static boolean isDestinationUnlockedOnClient(String destinationName) {
		return clientUnlockedDestinations.contains(destinationName);
	}

	public static boolean isCurrentLocationOnClient(String destinationName) {
		return clientCurrentLocation != null && clientCurrentLocation.equals(destinationName);
	}

	public static void openGui(ServerPlayerEntity player, FerryRoute route, FerryDestination currentLocation) {
		Set<String> unlockedDestinations = new HashSet<>();
		String currentLocationName = currentLocation != null ? currentLocation.name() : null;

		FerryManager ferryManager = FerryManager.get((ServerWorld) player.level);

		for (FerryDestination dest : FerryDestination.values()) {
			if (ferryManager.hasDestination(player.getUUID(), dest.name())) {
				unlockedDestinations.add(dest.name());
			}
		}

		PacketHandler.INSTANCE.sendTo(
			new FerryDestinationsPacket(unlockedDestinations, currentLocationName),
			player.connection.getConnection(),
			net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT
		);

		String routeName = route == FerryRoute.EASTERN_HOENN ? "Eastern" : "Western";
		String title = routeName + " Ferry Route";

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent(title);
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new FerryContainer(windowId, inventory, route, currentLocation);
				}
			},
			buffer -> {
				buffer.writeUtf(route.name());
				buffer.writeBoolean(currentLocation != null);
				if (currentLocation != null) {
					buffer.writeUtf(currentLocation.name());
				}
			}
		);
	}

	public static class FerryContainer extends Container {

		private static final EmptyInventory EMPTY_INVENTORY = new EmptyInventory();
		final ServerPlayerEntity serverPlayer;
		final FerryRoute route;
		final FerryDestination currentLocation;

		public FerryContainer(int windowId, PlayerInventory playerInventory, FerryRoute route, FerryDestination currentLocation) {
			super(GuiRegistry.FERRY_CONTAINER.get(), windowId);
			this.route = route;
			this.currentLocation = currentLocation;

			if (playerInventory.player instanceof ServerPlayerEntity) {
				this.serverPlayer = (ServerPlayerEntity) playerInventory.player;
			} else {
				this.serverPlayer = null;
			}

			List<FerryDestination> routeDestinations = route.getDestinations();

			for (int col = 0; col < 9; col++) {
				FerryDestinationSlot slot = new FerryDestinationSlot(EMPTY_INVENTORY, col, 8 + col * 18, 18, null);
				slot.setParentContainer(this);
				this.addSlot(slot);
			}

			int destIndex = 0;
			for (int col = 0; col < 9; col++) {
				int slotIndex = 9 + col;
				FerryDestination dest = null;
				boolean isCompass = false;

				if (route == FerryRoute.WESTERN_HOENN) {
					if (col == 2 || col == 3 || col == 5 || col == 6) {
						if (destIndex < routeDestinations.size()) {
							dest = routeDestinations.get(destIndex++);
						}
					} else if (col == 4) {
						isCompass = true;
					}
				} else {
					if (col == 1 || col == 2 || col == 3 || col == 5 || col == 6 || col == 7) {
						if (destIndex < routeDestinations.size()) {
							dest = routeDestinations.get(destIndex++);
						}
					} else if (col == 4) {
						isCompass = true;
					}
				}

				FerryDestinationSlot slot = new FerryDestinationSlot(EMPTY_INVENTORY, slotIndex, 8 + col * 18, 18 + 18, dest, isCompass);
				slot.setParentContainer(this);
				this.addSlot(slot);
			}

			for (int col = 0; col < 9; col++) {
				int slotIndex = 18 + col;
				FerryDestinationSlot slot = new FerryDestinationSlot(EMPTY_INVENTORY, slotIndex, 8 + col * 18, 18 + 2 * 18, null);
				slot.setParentContainer(this);
				this.addSlot(slot);
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

				if (slot instanceof FerryDestinationSlot) {
					if (slot.mayPickup(player)) {
						slot.onTake(player, slot.getItem());
						return ItemStack.EMPTY;
					}
				}
			}

			player.inventory.setCarried(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
	}

	public static class FerryDestinationSlot extends Slot {

		final FerryDestination destination;
		private final boolean isCompass;
		private FerryContainer parentContainer;

		public FerryDestinationSlot(IInventory inventory, int slotIndex, int x, int y, @Nullable FerryDestination destination) {
			this(inventory, slotIndex, x, y, destination, false);
		}

		public FerryDestinationSlot(IInventory inventory, int slotIndex, int x, int y, @Nullable FerryDestination destination, boolean isCompass) {
			super(inventory, slotIndex, x, y);
			this.destination = destination;
			this.isCompass = isCompass;
		}

		public void setParentContainer(FerryContainer container) {
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
			if (destination == null || isCompass) {
				return false;
			}

			if (parentContainer != null && parentContainer.currentLocation != null && parentContainer.currentLocation == destination) {
				return false;
			}

			if (player.level.isClientSide) {
				return FerryGui.isDestinationUnlockedOnClient(destination.name());
			}

			if (player instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				return (FerryManager.canUseFerry(serverPlayer) && FerryManager.get((ServerWorld) serverPlayer.level).hasDestination(serverPlayer.getUUID(), destination.name()));
			}

			return false;
		}

		@Override
		public ItemStack getItem() {
			if (destination == null) {
				ItemStack stack = new ItemStack(Items.BLUE_STAINED_GLASS_PANE);
				stack.setHoverName(new StringTextComponent(" "));
				return stack;
			}

			boolean isUnlocked;
			boolean isCurrentLocation = false;

			if (parentContainer != null) {
				isCurrentLocation = parentContainer.currentLocation != null && parentContainer.currentLocation == destination;

				if (parentContainer.serverPlayer != null) {
					ServerPlayerEntity serverPlayer = parentContainer.serverPlayer;
					isUnlocked = FerryManager.get((ServerWorld) serverPlayer.level).hasDestination(serverPlayer.getUUID(), destination.name());
				} else {
					isUnlocked = FerryGui.isDestinationUnlockedOnClient(destination.name());
				}
			} else {
				isUnlocked = FerryGui.isDestinationUnlockedOnClient(destination.name());
			}

			ItemStack stack;
			ITextComponent name;

			if (isCurrentLocation) {
				stack = new ItemStack(Items.LIME_DYE);

				StringTextComponent prefix = new StringTextComponent("Current Port: ");
				prefix.setStyle(Style.EMPTY.withColor(TextFormatting.GREEN).withItalic(false));

				StringTextComponent destinationName = new StringTextComponent(destination.getDisplayName());
				destinationName.setStyle(Style.EMPTY.withColor(destination.getTextColor()).withItalic(false));

				name = new StringTextComponent("").append(prefix).append(destinationName);
			} else if (isUnlocked) {
				stack = new ItemStack(destination.getRepresentativeItem());
				name = getDestinationFormattedName(destination);
			} else {
				stack = new ItemStack(Items.GRAY_DYE);
				name = new StringTextComponent(destination.getDisplayName()).setStyle(Style.EMPTY.withColor(TextFormatting.GRAY).withItalic(false));
			}

			stack.setHoverName(name);

			CompoundNBT display = stack.getOrCreateTagElement("display");
			ListNBT lore = new ListNBT();
			ITextComponent loreText;

			if (isCurrentLocation) {
				loreText = new StringTextComponent("You are currently at this location.").setStyle(Style.EMPTY.withColor(TextFormatting.GREEN).withItalic(false));
			} else if (isUnlocked) {
				loreText = new StringTextComponent(destination.getDescription()).setStyle(Style.EMPTY.withColor(TextFormatting.AQUA).withItalic(false));
			} else {
				loreText = new StringTextComponent("You have not discovered this ferry destination yet.").setStyle(Style.EMPTY.withColor(TextFormatting.RED).withItalic(false));
			}

			String loreJson = ITextComponent.Serializer.toJson(loreText);
			lore.add(StringNBT.valueOf(loreJson));
			display.put("Lore", lore);

			return stack;
		}

		private ITextComponent getDestinationFormattedName(FerryDestination destination) {
			StringTextComponent name = new StringTextComponent(destination.getDisplayName());
			Style style = Style.EMPTY.withItalic(false).withColor(destination.getTextColor());
			return name.setStyle(style);
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			if (destination != null && thePlayer instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) thePlayer;

				player.closeContainer();

				FerryManager.setPlayerCooldown(player);

				RNBConfig.TeleportLocation location = RNBConfig.getFerryLocation(destination.name());

				FerryCountdown.start(player, destination.getDisplayName(), location);
			}

			return ItemStack.EMPTY;
		}
	}

	private static class EmptyInventory implements IInventory {

		@Override
		public int getContainerSize() {
			return 27;
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
