package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.stats.links.DelegateLink;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.medicine.MedicineItem;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionCustomPotion implements IInteraction {

	public InteractionCustomPotion() {}

	public boolean processInteract(PixelmonEntity entityPixelmon, PlayerEntity player, Hand hand, ItemStack itemstack) {
		if (player instanceof ServerPlayerEntity && entityPixelmon.getOwner() == player && itemstack.getItem() instanceof MedicineItem) {
			ServerWorld world = ((ServerPlayerEntity) player).getLevel();
			GauntletManager gm = GauntletManager.get(world);

			if (!gm.isPartOfAnyGauntlet(player.getUUID()) || (gm.isPartOfAnyGauntlet(player.getUUID()) && gm.isHealingAllowedForPlayer(player.getUUID()))) {
				if (((MedicineItem) itemstack.getItem()).useMedicine(new DelegateLink(entityPixelmon.getPokemon()), 0) && !player.isCreative()) {
					player.getItemInHand(hand).shrink(1);
				}

				return true;
			} else {
				player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to heal your Pokemon."), player.getUUID());
				return true;
			}
		} else {
			return false;
		}
	}
}
