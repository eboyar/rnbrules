package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pokeballs.OccupiedPokeBallEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OccupiedPokeBallEntity.class)
public class CustomOccupiedPokeBallEntity {

	@Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/battles/api/rules/BattleRules;isDefault()Z"))
	private boolean alwaysUseTeamBuilder(BattleRules battleRules) {
		return false;
	}
}
