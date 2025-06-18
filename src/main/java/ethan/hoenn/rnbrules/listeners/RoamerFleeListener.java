package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.TurnEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import java.util.Objects;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RoamerFleeListener {

	@SubscribeEvent
	public void onTurnEnd(TurnEndEvent event) {
		BattleController bc = event.getBattleController();
		PlayerParticipant pp = null;

		if (bc == null || bc.battleEnded) {
			return;
		}

		if (event.getTurnNumber() == 0) {
			for (BattleParticipant participant : bc.participants) {
				if (participant instanceof PlayerParticipant) {
					pp = (PlayerParticipant) participant;
					continue;
				}

				if (participant instanceof TrainerParticipant) {
					return;
				}

				if (participant.getActiveUnfaintedPokemon().isEmpty()) {
					continue;
				}

				PixelmonWrapper pw = participant.getActiveUnfaintedPokemon().get(0);
				if (pw == null || pw.pokemon == null) {
					continue;
				}

				Species species = pw.pokemon.getSpecies();
				boolean isRoamerSpecies =
					species.is(PixelmonSpecies.LATIAS) ||
					species.is(PixelmonSpecies.LATIOS) ||
					species.is(PixelmonSpecies.TORNADUS) ||
					species.is(PixelmonSpecies.ENAMORUS) ||
					species.is(PixelmonSpecies.LANDORUS) ||
					species.is(PixelmonSpecies.THUNDURUS) ||
					species.is(PixelmonSpecies.CRESSELIA);

				if (isRoamerSpecies) {
					boolean canFlee = BattleParticipant.canSwitch(pw)[1];

					if (canFlee) {
						Objects.requireNonNull(pp).player.sendMessage(new StringTextComponent("§7The roamer §4escaped§7 and vanished."), Objects.requireNonNull(pp).player.getUUID());
						bc.endBattle();
						return;
					}
				}
			}
		}
	}
}
