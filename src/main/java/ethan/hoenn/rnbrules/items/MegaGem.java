package ethan.hoenn.rnbrules.items;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.TextHelper;
import com.pixelmonmod.pixelmon.blocks.PokeChestBlock;
import com.pixelmonmod.pixelmon.client.gui.ScreenHelper;
import com.pixelmonmod.pixelmon.enums.EnumMegaItem;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MegaGem extends Item {

	public MegaGem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!world.isClientSide) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			PlayerPartyStorage party = StorageProxy.getParty(serverPlayer);

			if (!party.getMegaItemsUnlocked().canMega()) {
				party.setMegaItem(EnumMegaItem.BraceletORAS, false);
				party.unlockMega(false);
				ItemStack stack = player.getItemInHand(hand);
				stack.shrink(1);
				player.sendMessage(new StringTextComponent("§7You completed a §9Key Stone§7! You can now Mega Evolve your Pokémon."), player.getUUID());
				player.playNotifySound(SoundRegistration.POKELOOT_OBTAINED.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
			} else {
				player.sendMessage(new StringTextComponent("§7You already have a completed §6Key Stone§7."), player.getUUID());
			}
		}
		return ActionResult.success(player.getItemInHand(hand));
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
