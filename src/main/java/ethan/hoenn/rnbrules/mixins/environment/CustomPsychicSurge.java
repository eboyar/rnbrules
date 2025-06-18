package ethan.hoenn.rnbrules.mixins.environment;

import com.pixelmonmod.pixelmon.api.pokemon.ability.AbstractAbility;
import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.PsychicSurge;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.PsychicTerrain;
import com.pixelmonmod.pixelmon.battles.status.Terrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PsychicSurge.class)
public class CustomPsychicSurge extends AbstractAbility {

	public CustomPsychicSurge() {}

	/**
	 * @author ethan
	 * @reason permanent
	 */
	@Overwrite(remap = false)
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		Terrain terrain = newPokemon.bc.globalStatusController.getTerrain();
		if (!(terrain instanceof PsychicTerrain)) {
			if (terrain != null) {
				newPokemon.bc.globalStatusController.removeGlobalStatus(terrain);
			}

			PsychicTerrain et = new PsychicTerrain(200);
			newPokemon.bc.sendToAll(et.langStart, new Object[0]);
			newPokemon.addGlobalStatus(et);
		}
	}
}
