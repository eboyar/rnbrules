package ethan.hoenn.rnbrules.mixins.effects;

import com.pixelmonmod.pixelmon.api.pokemon.ability.abilities.QuickFeet;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Paralysis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Paralysis.class)
public class CustomParalysis {

	@Inject(method = "modifyStats", at = @At("HEAD"), cancellable = true, remap = false)
	private void modifyParalysisSpeedReduction(PixelmonWrapper user, int[] stats, CallbackInfoReturnable<int[]> cir) {
		if (!(user.getBattleAbility() instanceof QuickFeet)) {
			int speedIndex = BattleStatsType.SPEED.getStatIndex();
			stats[speedIndex] = (int) ((double) stats[speedIndex] * 0.25);
		}
		cir.setReturnValue(stats);
	}
}
