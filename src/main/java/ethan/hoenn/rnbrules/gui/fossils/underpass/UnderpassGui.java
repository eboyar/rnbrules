package ethan.hoenn.rnbrules.gui.fossils.underpass;

import com.pixelmonmod.pixelmon.enums.items.EnumFossils;
import ethan.hoenn.rnbrules.gui.fossils.FossilAssets;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class UnderpassGui {

	private static final int NUM_SLOTS = 11;
	private static final String UNDERPASS_OT = "underpass";

	static {
		if (!FossilAssets.isInitialized()) {
			FossilAssets.initializeAssets();
		}
	}

	public static void openGui(ServerPlayerEntity player) {
		if (!FossilAssets.isInitialized()) {
			FossilAssets.initializeAssets();
		}

		Inventory fossilInventory = new Inventory(NUM_SLOTS);

		for (int i = 0; i < NUM_SLOTS; i++) {
			EnumFossils fossil = EnumFossils.fromIndex(i);
			ItemStack previewItem = FossilAssets.getPreviewForFossil(fossil);
			if (!previewItem.isEmpty()) {
				fossilInventory.setItem(i, previewItem);
			}
		}

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Select a Fossil");
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new UnderpassContainer(windowId, inventory, fossilInventory);
				}
			}
		);
	}

	public static boolean giveFossilToPlayer(ServerPlayerEntity player, int fossilIndex) {
		if (fossilIndex < 0 || fossilIndex >= NUM_SLOTS) {
			return false;
		}

		EnumFossils fossil = EnumFossils.fromIndex(fossilIndex);
		if (fossil == EnumFossils.NULL) {
			return false;
		}

		String fossilName = getFossilItemName(fossil);
		ItemStack fossilItem = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("pixelmon", fossilName)));

		if (!player.inventory.add(fossilItem)) {
			player.drop(fossilItem, false);
			player.sendMessage(new StringTextComponent("Your inventory is full! The fossil has been dropped on the ground.").withStyle(TextFormatting.YELLOW), player.getUUID());
		}

		try {
			GlobalOTManager otManager = GlobalOTManager.get(player.getLevel());
			otManager.addGlobalOT(UNDERPASS_OT);
			otManager.addPlayerGlobalOT(player.getUUID(), UNDERPASS_OT);
		} catch (Exception e) {}

		player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);

		player.sendMessage(
			new StringTextComponent("You received a ")
				.withStyle(TextFormatting.GREEN)
				.append(new StringTextComponent(fossil.getPokemon().getName() + " Fossil").withStyle(TextFormatting.GOLD))
				.append(new StringTextComponent("!").withStyle(TextFormatting.GREEN)),
			player.getUUID()
		);

		return true;
	}

	private static String getFossilItemName(EnumFossils fossil) {
		switch (fossil) {
			case DOME:
				return "covered_fossil_1";
			case OLD_AMBER:
				return "covered_fossil_2";
			case ROOT:
				return "covered_fossil_3";
			case CLAW:
				return "covered_fossil_4";
			case SKULL:
				return "covered_fossil_5";
			case ARMOR:
				return "covered_fossil_6";
			case COVER:
				return "covered_fossil_7";
			case PLUME:
				return "covered_fossil_8";
			case JAW:
				return "covered_fossil_9";
			case SAIL:
				return "covered_fossil_10";
			default:
				return "covered_fossil_0";
		}
	}

	public static class FossilSelectionSlot extends Slot {

		public FossilSelectionSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return true;
		}
	}

	public static class UnderpassContainer extends Container {

		private final IInventory fossilSlotsInventory;

		public UnderpassContainer(int windowId, PlayerInventory playerInventory, IInventory fossilSlotsInventory) {
			super(GuiRegistry.UNDERPASS_CONTAINER.get(), windowId);
			checkContainerSize(fossilSlotsInventory, NUM_SLOTS);
			this.fossilSlotsInventory = fossilSlotsInventory;

			for (int i = 0; i < NUM_SLOTS; i++) {
				int posX = 8 + i * 18;
				int posY = 18;
				this.addSlot(new FossilSelectionSlot(fossilSlotsInventory, i, posX, posY));
			}
		}

		public UnderpassContainer(int windowId, PlayerInventory playerInventory) {
			this(windowId, playerInventory, new Inventory(NUM_SLOTS));
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId >= 0 && slotId < this.slots.size() && clickTypeIn == ClickType.PICKUP && player instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

				boolean success = giveFossilToPlayer(serverPlayer, slotId);

				if (success) {
					serverPlayer.closeContainer();
				}

				return ItemStack.EMPTY;
			}

			return super.clicked(slotId, dragType, clickTypeIn, player);
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public void removed(PlayerEntity player) {
			super.removed(player);

			if (!player.level.isClientSide && fossilSlotsInventory instanceof Inventory) {
				((Inventory) fossilSlotsInventory).clearContent();
			}
		}
	}
}
