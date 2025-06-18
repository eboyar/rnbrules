package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.MistySurge;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.MistyTerrain;
import com.pixelmonmod.pixelmon.battles.status.Terrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MistySurge.class)
public class CustomMistySurge extends AbstractAbility {

	public CustomMistySurge() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		Terrain terrain = newPokemon.bc.globalStatusController.getTerrain();
		if (!(terrain instanceof MistyTerrain)) {
			if (terrain != null) {
				newPokemon.bc.globalStatusController.removeGlobalStatus(terrain);
			}

			MistyTerrain et = new MistyTerrain(200);
			newPokemon.bc.sendToAll(et.langStart, new Object[0]);
			newPokemon.addGlobalStatus(et);
		}
	}
}
