package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.battles.BattleMode;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.OpenBattleModePacket;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.EtherItem;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionCustomEther implements IInteraction {

	public InteractionCustomEther() {}

	public boolean processInteract(PixelmonEntity entityPixelmon, PlayerEntity player, Hand hand, ItemStack itemstack) {
		if (player instanceof ServerPlayerEntity && entityPixelmon.getOwner() == player) {
			Item item = itemstack.getItem();
			if (item instanceof EtherItem) {
				if (!entityPixelmon.getPokemon().getMoveset().hasFullPP()) {
					ServerWorld world = ((ServerPlayerEntity) player).getLevel();
					GauntletManager gm = GauntletManager.get(world);

					if (!gm.isPartOfAnyGauntlet(player.getUUID()) || (gm.isPartOfAnyGauntlet(player.getUUID()) && gm.isHealingAllowedForPlayer(player.getUUID()))) {
						NetworkHelper.sendPacket(new OpenBattleModePacket(BattleMode.CHOOSE_ETHER, entityPixelmon.getPartyPosition()), (ServerPlayerEntity) player);
						return true;
					} else {
						player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to heal your Pokemon."), player.getUUID());
						return true;
					}
				} else {
					ChatHandler.sendChat(player, "pixelmon.interaction.ppfail", new Object[] { entityPixelmon.getNickname() });
				}
			}
		}

		return false;
	}
}
