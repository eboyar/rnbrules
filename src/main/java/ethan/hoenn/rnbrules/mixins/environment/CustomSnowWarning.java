package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.SnowWarning;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Snow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SnowWarning.class)
public class CustomSnowWarning extends AbstractAbility {

	public CustomSnowWarning() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		if (!(newPokemon.bc.globalStatusController.getWeatherIgnoreAbility() instanceof Snow) && newPokemon.bc.globalStatusController.canWeatherChange(new Snow())) {
			Snow snow = new Snow(200);
			newPokemon.addGlobalStatus(snow);
			newPokemon.bc.sendToAll("pixelmon.abilities.snowwarning", new Object[] { newPokemon.getNickname() });
		}
	}
}
