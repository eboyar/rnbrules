package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.SandStream;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Sandstorm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SandStream.class)
public class CustomSandStream extends AbstractAbility {

	public CustomSandStream() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		if (!(newPokemon.bc.globalStatusController.getWeatherIgnoreAbility() instanceof Sandstorm) && newPokemon.bc.globalStatusController.canWeatherChange(new Sandstorm())) {
			Sandstorm sandstorm = new Sandstorm(100);
			newPokemon.addGlobalStatus(sandstorm);
			newPokemon.bc.sendToAll("pixelmon.abilities.sandstream", new Object[] { newPokemon.getNickname() });
		}
	}
}
