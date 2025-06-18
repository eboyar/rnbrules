package ethan.hoenn.rnbrules.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class MaxHealingSerum extends GenericUpgradeComponentItem {

	public MaxHealingSerum(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!world.isClientSide) {
			ItemStack stack = player.getItemInHand(hand);
			PlayerInventory inventory = player.inventory;
			boolean foundMaxPartyRestore = false;
			int refillCount = 0;

			for (ItemStack item : inventory.items) {
				if (item.getItem() instanceof MaxPartyRestore) {
					foundMaxPartyRestore = true;
					if (item.getDamageValue() > 0) {
						item.setDamageValue(0);
						refillCount++;
					}
				}
			}

			if (!foundMaxPartyRestore) {} else if (refillCount == 0) {
				player.sendMessage(new StringTextComponent("§7Your §5Max Party Restore§7 is already full."), player.getUUID());
			} else {
				player.sendMessage(new StringTextComponent("§aRefilled §5Max Party Restore§a."), player.getUUID());

				if (!player.isCreative()) {
					stack.shrink(1);
				}
			}
		}

		return ActionResult.success(player.getItemInHand(hand));
	}
}
