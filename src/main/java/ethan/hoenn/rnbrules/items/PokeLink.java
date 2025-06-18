package ethan.hoenn.rnbrules.items;

import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.api.util.helpers.TextHelper;
import com.pixelmonmod.pixelmon.client.gui.ScreenHelper;
import com.pixelmonmod.pixelmon.comm.packetHandlers.OpenScreenPacket;
import com.pixelmonmod.pixelmon.comm.packetHandlers.clientStorage.newStorage.pc.ClientChangeOpenPCPacket;
import com.pixelmonmod.pixelmon.enums.EnumGuiScreen;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PokeLink extends Item {

	public PokeLink(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!world.isClientSide) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

			ServerWorld sWorld = ((ServerPlayerEntity) player).getLevel();
			GauntletManager gm = GauntletManager.get(sWorld);

			ItemStack stack = player.getItemInHand(hand);

			if (stack.getDamageValue() != 50) {
				if (!gm.isPartOfAnyGauntlet(player.getUUID())) {
					PCStorage pc = (PCStorage) StorageProxy.getPCForPlayer(serverPlayer);
					NetworkHelper.sendPacket(new ClientChangeOpenPCPacket(pc.uuid), serverPlayer);
					OpenScreenPacket.open(player, EnumGuiScreen.PC, new int[0]);

					if (!player.isCreative()) {
						stack.setDamageValue(stack.getDamageValue() + 1);
					}
				} else {
					player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to change your Pokémon."), player.getUUID());
				}
			} else {
				player.sendMessage(new StringTextComponent("§7You must recharge your §cPokéLink§7 with a Battery."), player.getUUID());
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
