package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.RareCandyEvent;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.ExpCandyItem;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionCappedRareCandy implements IInteraction {

	@Override
	public boolean processInteract(PixelmonEntity pixelmon, PlayerEntity player, Hand hand, ItemStack itemStack) {
		if (player instanceof ServerPlayerEntity && pixelmon.getOwner() == player && itemStack.getItem() instanceof ExpCandyItem) {
			Pokemon pokemon = pixelmon.getPokemon();

			ServerWorld world = ((ServerPlayerEntity) player).getLevel();

			LevelCapManager manager = LevelCapManager.get(world);
			int lvlCap = manager.getLevelCap(player.getUUID());

			if (!pokemon.doesLevel()) {
				player.sendMessage(new TranslationTextComponent("pixelmon.interaction.rarecandy", new Object[] { pixelmon.getNickname() }), Util.NIL_UUID);
				return true;
			} else if (pokemon.getPokemonLevel() < lvlCap + 3 && !Pixelmon.EVENT_BUS.post(new RareCandyEvent((ServerPlayerEntity) player, pixelmon, itemStack, (ExpCandyItem) itemStack.getItem()))) {
				ExpCandyItem candy = (ExpCandyItem) itemStack.getItem();
				int expAward = candy.getAmount();
				if (expAward < 0) {
					while (expAward < 0) {
						pokemon.getPokemonLevelContainer().awardEXP(pokemon.getExperienceToLevelUp(), candy.getGainType());
						++expAward;
					}
				} else {
					pokemon.getPokemonLevelContainer().awardEXP(expAward, candy.getGainType());
				}

				if (!player.isCreative()) {
					player.getItemInHand(hand).shrink(1);
				}

				return true;
			} else {
				Species old = pokemon.getSpecies();
				pokemon.tryEvolution();
				if (pokemon.getSpecies() != old && !player.isCreative()) {
					player.getItemInHand(hand).shrink(1);
				}

				return true;
			}
		} else {
			return false;
		}
	}
}
