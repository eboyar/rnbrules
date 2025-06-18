package ethan.hoenn.rnbrules.items;

import com.pixelmonmod.pixelmon.api.util.helpers.TextHelper;
import com.pixelmonmod.pixelmon.client.gui.ScreenHelper;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndlessCandy extends Item {

	public EndlessCandy(Properties properties) {
		super(properties);
		properties.stacksTo(1);
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
