package ethan.hoenn.rnbrules.mixins.battle;

import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BattleController.class)
public class CustomBattleController {

	@Shadow(remap = false)
	public List<PixelmonWrapper> turnList;

	// fixes an issue where a battle gets stuck when a trainer on a team has no more Pokémon but other team members still have available Pokémon
	@Inject(method = "checkDefeated", at = @At(value = "RETURN"), remap = false)
	private void onCheckDefeated(BattleParticipant p, PixelmonWrapper poke, CallbackInfo ci) {
		BattleController self = (BattleController) (Object) this;

		if (p.isDefeated && !self.isTeamDefeated(p)) {
			for (BattleParticipant teammate : self.getTeam(p)) {
				if (!teammate.isDefeated && teammate != p && teammate.countAblePokemon() > 0) {
					teammate.wait = false;

					if (teammate.controlledPokemon.isEmpty() && teammate.countAblePokemon() > 0) {
						self.reviveAfterDefeat(teammate);
					}

					p.wait = false;
					break;
				}
			}
		}
	}

	@Inject(method = "checkFaint", at = @At("TAIL"), remap = false)
	private void afterCheckFaint(CallbackInfo ci) {
		BattleController self = (BattleController) (Object) this;

		for (int team = 0; team <= 1; team++) {
			final int teamId = team;
			List<BattleParticipant> teamMembers = new ArrayList<>();

			for (BattleParticipant p : self.participants) {
				if (p.team == teamId) {
					teamMembers.add(p);
				}
			}

			if (teamMembers.isEmpty()) continue;

			boolean allWaiting = true;
			boolean allDefeated = true;

			for (BattleParticipant p : teamMembers) {
				allWaiting &= p.getWait();
				allDefeated &= p.isDefeated;
			}

			if (allWaiting && !allDefeated) {
				for (BattleParticipant p : teamMembers) {
					if (!p.isDefeated && p.countAblePokemon() > 0) {
						p.wait = false;

						if (p.controlledPokemon.isEmpty() && p.countAblePokemon() > 0) {
							self.reviveAfterDefeat(p);
						}
						break;
					}
				}
			}
		}
	}
}
