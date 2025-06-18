package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Attack.class)
public class CustomAttack {

	@ModifyVariable(method = "calcCriticalHit", at = @At("STORE"), ordinal = 0, remap = false)
	private static float modifyBaseCritChance(float percent) {
		if (Math.abs(percent - 0.0417F) < 0.0001F) {
			return 0.0625F;
		}
		return percent;
	}
}
