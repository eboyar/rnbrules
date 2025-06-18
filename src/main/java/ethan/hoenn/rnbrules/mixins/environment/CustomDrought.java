package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Drought;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Sunny;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Drought.class)
public class CustomDrought extends AbstractAbility {

	public CustomDrought() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		if (!(newPokemon.bc.globalStatusController.getWeatherIgnoreAbility() instanceof Sunny) && newPokemon.bc.globalStatusController.canWeatherChange(new Sunny())) {
			Sunny sunny = new Sunny(200);
			newPokemon.addGlobalStatus(sunny);
			newPokemon.bc.sendToAll("pixelmon.abilities.drought", new Object[] { newPokemon.getNickname() });
		}
	}
}
