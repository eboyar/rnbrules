package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeLootEvent;
import com.pixelmonmod.pixelmon.blocks.PokeChestBlock;
import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import com.pixelmonmod.pixelmon.items.heldItems.BerryItem;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PokeLootListener {

	@SubscribeEvent
	public void onClaimPokeLoot(PokeLootEvent.Claim event) {
		ServerPlayerEntity claimer = event.player;
		PokeChestTileEntity chest = event.chest;
		ServerWorld world = (ServerWorld) claimer.level;

		BattleDependencyManager depManager = BattleDependencyManager.get(world);

		if (depManager.pokeChestHasDependencies(chest)) {
			Set<String> chestDeps = depManager.getPokeChestDependencies(chest);

			for (String depId : chestDeps) {
				if (!depManager.playerHasDependency(claimer.getUUID(), depId)) {
					String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

					claimer.sendMessage(new StringTextComponent("§7You cannot claim this loot until you §6" + depDescription + "§7."), claimer.getUUID());
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void onGetPokeLootDrops(PokeLootEvent.GetDrops event) {
		ServerPlayerEntity claimer = event.player;
		ItemStack[] drops = event.getDrops();
		List<ItemStack> newDrops = new ArrayList<>();

		for (ItemStack is : drops) {
			if (is.getItem() instanceof BerryItem) {
				int originalCount = is.getCount();
				int min = (int) Math.floor(originalCount * 0.5);
				int max = (int) Math.ceil(originalCount * 1.5);
				int randomizedCount = min + claimer.getRandom().nextInt(max - min + 1);

				while (randomizedCount > 64) {
					ItemStack stack = new ItemStack(is.getItem(), 64);
					newDrops.add(stack);
					randomizedCount -= 64;
				}

				if (randomizedCount > 0) {
					ItemStack stack = new ItemStack(is.getItem(), randomizedCount);
					newDrops.add(stack);
				}
			} else {
				newDrops.add(is);
			}
		}

		event.setDrops(newDrops.toArray(new ItemStack[0]));
	}
}
