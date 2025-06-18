package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.ElectricSurge;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.ElectricTerrain;
import com.pixelmonmod.pixelmon.battles.status.Terrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ElectricSurge.class)
public class CustomElectricSurge extends AbstractAbility {

	public CustomElectricSurge() {}

	/**
	 * @author ethan
	 * @reason permanent terrain
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		Terrain terrain = newPokemon.bc.globalStatusController.getTerrain();
		if (!(terrain instanceof ElectricTerrain)) {
			if (terrain != null) {
				newPokemon.bc.globalStatusController.removeGlobalStatus(terrain);
			}

			ElectricTerrain et = new ElectricTerrain(200);
			newPokemon.bc.sendToAll(et.langStart, new Object[0]);
			newPokemon.addGlobalStatus(et);
		}
	}
}
