package ethan.hoenn.rnbrules.items;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class LocationBlockItem extends BlockItem {

	public LocationBlockItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);

		tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Used to mark and track location zones"));
		tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Players will be notified when entering a new zone"));

		if (stack.hasCustomHoverName()) {
			tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "Location: " + TextFormatting.GREEN + stack.getHoverName().getString()));
		} else {
			tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "Rename in an anvil to set location name"));
		}
	}
}
