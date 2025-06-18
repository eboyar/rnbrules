package ethan.hoenn.rnbrules.mixins.teamselection;

import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TeamSelectionRegistry.class)
public class CustomTeamSelectionRegistry {

	@ModifyArg(
		method = "addTeamSelection(Lcom/pixelmonmod/pixelmon/battles/api/rules/BattleRules;Z[Lcom/pixelmonmod/pixelmon/api/storage/PartyStorage;)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/battles/api/rules/teamselection/TeamSelection;<init>(ILcom/pixelmonmod/pixelmon/battles/api/rules/BattleRules;ZZZ[Lcom/pixelmonmod/pixelmon/api/storage/PartyStorage;)V"
		),
		index = 2,
		remap = false
	)
	private static boolean forceShowRulesFalse(boolean showRules) {
		return false;
	}
}
