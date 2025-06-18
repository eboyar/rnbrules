package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.Drizzle;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Rainy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Drizzle.class)
public class CustomDrizzle extends AbstractAbility {

	public CustomDrizzle() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		if (!(newPokemon.bc.globalStatusController.getWeatherIgnoreAbility() instanceof Rainy) && newPokemon.bc.globalStatusController.canWeatherChange(new Rainy())) {
			Rainy rainy = new Rainy(200);
			newPokemon.addGlobalStatus(rainy);
			newPokemon.bc.sendToAll("pixelmon.abilities.drizzle", new Object[] { newPokemon.getNickname() });
		}
	}
}
