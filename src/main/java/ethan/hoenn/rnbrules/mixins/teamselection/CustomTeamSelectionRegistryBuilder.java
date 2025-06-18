package ethan.hoenn.rnbrules.mixins.teamselection;

import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeamSelectionRegistry.Builder.class)
public class CustomTeamSelectionRegistryBuilder {

	@Shadow(remap = false)
	protected boolean showRules;

	@Inject(method = "showRules()Lcom/pixelmonmod/pixelmon/battles/api/rules/teamselection/TeamSelectionRegistry$Builder;", at = @At("RETURN"), cancellable = true, remap = false)
	private void overrideShowRules(CallbackInfoReturnable<TeamSelectionRegistry.Builder> cir) {
		TeamSelectionRegistry.Builder builder = cir.getReturnValue();
		builder.showRules(false);
		cir.setReturnValue(builder);
	}

	@Redirect(
		method = "showRules(Z)Lcom/pixelmonmod/pixelmon/battles/api/rules/teamselection/TeamSelectionRegistry$Builder;",
		at = @At(value = "FIELD", target = "Lcom/pixelmonmod/pixelmon/battles/api/rules/teamselection/TeamSelectionRegistry$Builder;showRules:Z"),
		remap = false
	)
	private void redirectShowRulesSetting(TeamSelectionRegistry.Builder instance, boolean value) {
		this.showRules = false;
	}

	@ModifyArg(
		method = "start()V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/battles/api/rules/teamselection/TeamSelection;<init>(ILcom/pixelmonmod/pixelmon/battles/api/rules/BattleRules;ZZZLjava/util/function/Consumer;Ljava/util/function/Consumer;[Lcom/pixelmonmod/pixelmon/api/storage/PartyStorage;)V"
		),
		index = 2,
		remap = false
	)
	private boolean forceShowRulesFalseInStart(boolean showRules) {
		return false;
	}
}
