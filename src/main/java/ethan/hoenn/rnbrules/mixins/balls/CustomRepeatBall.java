package ethan.hoenn.rnbrules.mixins.balls;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.logic.RepeatBall;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallEntity;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RepeatBall.class)
public class CustomRepeatBall {

	/**
	 * @author ethan
	 * @reason 3.5 -> 4.5
	 */
	@Overwrite(remap = false)
	public double getCatchBonus(PokeBall type, PokeBallEntity entity, PlayerEntity thrower, Pokemon pokemon, PokeBallMode mode) {
		PlayerPartyStorage storage = StorageProxy.getParty((ServerPlayerEntity) thrower);
		return storage.playerPokedex.hasCaught(pokemon.getSpecies()) ? 4.5 : 1.0;
	}
}
