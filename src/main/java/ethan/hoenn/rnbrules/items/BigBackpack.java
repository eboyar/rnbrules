package ethan.hoenn.rnbrules.items;

import com.pixelmonmod.pixelmon.api.util.helpers.TextHelper;
import com.pixelmonmod.pixelmon.client.gui.ScreenHelper;
import ethan.hoenn.rnbrules.gui.backpack.BigBackpackContainer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class BigBackpack extends Item {

	public static final int SLOTS = 184;

	public BigBackpack(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!world.isClientSide) {
			NetworkHooks.openGui(
				(ServerPlayerEntity) player,
				new INamedContainerProvider() {
					@Override
					public ITextComponent getDisplayName() {
						return new StringTextComponent("BIG Poké Bag");
					}

					@Override
					public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
						return new BigBackpackContainer(windowId, playerInventory, stack, hand);
					}
				},
				buffer -> {
					buffer.writeBoolean(hand == Hand.MAIN_HAND);
				}
			);
		}

		return ActionResult.sidedSuccess(stack, world.isClientSide);
	}

	@Override
	@Nullable
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new BigBackpackHandler(stack, SLOTS);
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		String tt = this.getTooltipText(stack);
		if (!tt.isEmpty()) {
			if (ScreenHelper.isKeyDown(340)) {
				for (String s : tt.split("\n")) {
					tooltip.add(TextHelper.colour(TextFormatting.GRAY + s));
				}
			} else {
				tooltip.add(new StringTextComponent(TextFormatting.GRAY + I18n.get("gui.tooltip.collapsed", new Object[0])));
			}
		}

		super.appendHoverText(stack, world, tooltip, flagIn);
	}

	public String getTooltipText(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && nbt.contains("tooltip") ? nbt.getString("tooltip") : this.getTooltipText();
	}

	public String getTooltipText() {
		return I18n.exists(this.getDescriptionId() + ".tooltip") ? I18n.get(this.getDescriptionId() + ".tooltip", new Object[0]) : "";
	}
}
