package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.api.battles.BattleAIMode;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.controller.ai.BattleAIBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import ethan.hoenn.rnbrules.ai.RNBAI;
import ethan.hoenn.rnbrules.ai.RoamerAI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BattleAIMode.class)
public class CustomBattleAIMode {

	@Inject(method = "createAI", at = @At("HEAD"), cancellable = true, remap = false)
	private void onCreateAI(BattleParticipant participant, CallbackInfoReturnable<BattleAIBase> cir) {
		BattleAIMode mode = (BattleAIMode) (Object) this;
		//this needs to be made configurable asap
		if (mode == BattleAIMode.TACTICAL) {
			//LOGGER.info("Injecting RNB AI!");
			try {
				cir.setReturnValue(new RNBAI(participant));
				cir.cancel();
				//LOGGER.info("RNB AI injection successful!");
			} catch (Exception e) {
				//LOGGER.error("Failed to inject RNB AI", e);
			}
		} else if (mode == BattleAIMode.RANDOM && participant instanceof WildPixelmonParticipant) {
			PixelmonWrapper target = participant.getActiveUnfaintedPokemon().get(0);
			Species s = target.pokemon.getSpecies();

			if (
				s.is(PixelmonSpecies.LATIAS) ||
				s.is(PixelmonSpecies.LATIOS) ||
				s.is(PixelmonSpecies.TORNADUS) ||
				s.is(PixelmonSpecies.ENAMORUS) ||
				s.is(PixelmonSpecies.LANDORUS) ||
				s.is(PixelmonSpecies.THUNDURUS) ||
				s.is(PixelmonSpecies.CRESSELIA)
			) {
				try {
					cir.setReturnValue(new RoamerAI(participant));
					cir.cancel();
					//LOGGER.info("ROAMER AI injection successful!");
				} catch (Exception e) {
					//LOGGER.error("Failed to inject ROAMER AI", e);
				}
			}
		}
	}
}
