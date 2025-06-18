package ethan.hoenn.rnbrules.items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BigBackpackHandler implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

	private final ItemStack stack;
	private final LazyOptional<IItemHandler> inventory;
	private final ItemStackHandler itemHandler;

	public BigBackpackHandler(ItemStack stack, int slots) {
		this.stack = stack;
		this.itemHandler = new ItemStackHandler(slots) {
			@Override
			public boolean isItemValid(int slot, @Nonnull ItemStack stackToCheck) {
				return !ItemStack.isSame(stackToCheck, BigBackpackHandler.this.stack);
			}

			@Override
			protected void onContentsChanged(int slot) {
				CompoundNBT tag = stack.getOrCreateTag();
				tag.put("Inventory", serializeNBT());
				tag.putInt("BackpackSize", getSlots());
			}
		};
		this.inventory = LazyOptional.of(() -> itemHandler);

		CompoundNBT tag = stack.getTag();
		if (tag != null && tag.contains("Inventory")) {
			itemHandler.deserializeNBT(tag.getCompound("Inventory"));
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, inventory.cast());
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.put("Inventory", itemHandler.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (nbt.contains("Inventory")) {
			itemHandler.deserializeNBT(nbt.getCompound("Inventory"));
		}
	}
}
