package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.config.PixelmonServerConfig;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.items.EndlessCandy;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionEndlessCandy implements IInteraction {

	@Override
	public boolean processInteract(PixelmonEntity pixelmon, PlayerEntity player, Hand hand, ItemStack itemStack) {
		if (player instanceof ServerPlayerEntity && pixelmon.getOwner() == player && itemStack.getItem() instanceof EndlessCandy) {
			Pokemon pokemon = pixelmon.getPokemon();
			ServerWorld world = ((ServerPlayerEntity) player).getLevel();

			LevelCapManager manager = LevelCapManager.get(world);
			int lvlCap = manager.getLevelCap(player.getUUID());

			int lvl = pokemon.getPokemonLevel();

			if (!pokemon.doesLevel()) {
				player.sendMessage(new TranslationTextComponent("pixelmon.interaction.rarecandy", pixelmon.getNickname()), Util.NIL_UUID);
				return true;
			} else if (lvl < PixelmonServerConfig.maxLevel && lvl < lvlCap) {
				pokemon.getPokemonLevelContainer().awardEXP(pokemon.getExperienceToLevelUp());
				return true;
			} else {
				pokemon.tryEvolution();
			}

			return true;
		}

		return false;
	}
}
