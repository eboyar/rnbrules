package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.GrassySurge;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.GrassyTerrain;
import com.pixelmonmod.pixelmon.battles.status.Terrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GrassySurge.class)
public class CustomGrassySurge extends AbstractAbility {

	public CustomGrassySurge() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		Terrain terrain = newPokemon.bc.globalStatusController.getTerrain();
		if (!(terrain instanceof GrassyTerrain)) {
			if (terrain != null) {
				newPokemon.bc.globalStatusController.removeGlobalStatus(terrain);
			}

			GrassyTerrain et = new GrassyTerrain(200);
			newPokemon.bc.sendToAll(et.langStart, new Object[0]);
			newPokemon.addGlobalStatus(et);
		}
	}
}
