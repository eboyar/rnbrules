package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.api.events.battles.AttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AttackEvent.CriticalHit.class)
public class CustomCriticalHitMultiplier {

    /**
     * @author ethan
     * @reason 2.0x -> 1.5x
     */
    @Overwrite(remap = false)
    public void setCrit(boolean crit) {
        ((AttackEvent.CriticalHit) (Object) this).critMultiplier = crit ? (double)1.5F : (double)1.0F;
    }
}
