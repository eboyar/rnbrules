package ethan.hoenn.rnbrules.mixins.balls;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.logic.QuickBall;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallEntity;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallMode;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(QuickBall.class)
public class CustomQuickBall {

	/**
	 * @author ethan
	 * @reason 5 -> 6.5
	 */
	@Overwrite(remap = false)
	public double getCatchBonus(PokeBall type, PokeBallEntity entity, PlayerEntity thrower, Pokemon pokemon, PokeBallMode mode) {
		return (mode == PokeBallMode.BATTLE && pokemon.getPixelmonEntity().map(pe -> pe.battleController != null && pe.battleController.battleTurn == 0).orElse(false)) ? 6.5 : 1.0;
	}
}
