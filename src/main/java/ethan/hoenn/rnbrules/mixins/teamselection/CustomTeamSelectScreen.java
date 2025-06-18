package ethan.hoenn.rnbrules.mixins.teamselection;

import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import com.pixelmonmod.pixelmon.client.gui.battles.VersusScreen;
import com.pixelmonmod.pixelmon.client.gui.battles.rules.TeamSelectPokemonIcon;
import com.pixelmonmod.pixelmon.client.gui.battles.rules.TeamSelectScreen;
import com.pixelmonmod.pixelmon.client.gui.widgets.RoundButton;
import java.util.List;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeamSelectScreen.class)
public abstract class CustomTeamSelectScreen extends VersusScreen {

	@Shadow(remap = false)
	private List<TeamSelectPokemonIcon> icons;

	@Shadow(remap = false)
	@Mutable
	private int numSelected;

	@Shadow(remap = false)
	private RoundButton confirmButton;

	@Shadow(remap = false)
	private ClientBattleManager bm;

	@Shadow(remap = false)
	@Mutable
	public String rejectClause;

	protected CustomTeamSelectScreen() {
		super();
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void autoSelectInitialTeam(CallbackInfo ci) {
		if (this.numSelected == 0 && !this.icons.isEmpty()) {
			int maxPokemonToSelect = (Integer) this.bm.rules.getOrDefault(BattleRuleRegistry.NUM_POKEMON);

			for (int i = 0; i < Math.min(this.icons.size(), 6); i++) {
				if (this.numSelected >= maxPokemonToSelect) {
					break;
				}

				TeamSelectPokemonIcon icon = this.icons.get(i);

				if (icon.selectIndex == -1 && !icon.isDisabled()) {
					icon.selectIndex = this.numSelected;
					this.numSelected++;
				}
			}

			if (this.numSelected > 0) {
				this.confirmButton.setText(I18n.get("gui.battlerules.confirm"));
				this.rejectClause = "";
			} else {
				this.confirmButton.setText(I18n.get("gui.battlerules.selectteam"));
			}
		}
	}
}
