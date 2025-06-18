package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.status.*;
import ethan.hoenn.rnbrules.status.CustomAuroraVeil;
import ethan.hoenn.rnbrules.status.CustomMagmaStorm;
import ethan.hoenn.rnbrules.status.CustomTailwind;
import java.util.List;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnvironmentListener {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBattleStartedPost(BattleStartedEvent.Post event) {
		BattleController bc = event.getBattleController();
		List<BattleParticipant> bps = bc.participants;

		for (BattleParticipant bp : bps) {
			if (bp instanceof TrainerParticipant) {
				PixelmonWrapper user = bp.getActiveUnfaintedPokemon().get(0);
				if (user != null) {
					CompoundNBT entityData = ((TrainerParticipant) bp).trainer.getPersistentData();
					String environmentId = entityData.getString("Environment");

					if (!environmentId.isEmpty()) {
						applyEnvironmentEffect(bc, environmentId, user);
					}
				}
			}
		}
	}

	private void applyEnvironmentEffect(BattleController bc, String environmentId, PixelmonWrapper user) {
		switch (environmentId) {
			case "Sandstorm":
				if (!(bc.globalStatusController.getWeather() instanceof Sandstorm)) {
					Sandstorm sand = new Sandstorm(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) sand);
				}
				break;
			case "Hail":
				if (!(bc.globalStatusController.getWeather() instanceof Hail)) {
					Hail hail = new Hail(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) hail);
				}
				break;
			case "Rain":
				if (!(bc.globalStatusController.getWeather() instanceof Rainy)) {
					Rainy rain = new Rainy(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) rain);
				}
				break;
			case "Sun":
				if (!(bc.globalStatusController.getWeather() instanceof Sunny)) {
					Sunny sun = new Sunny(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) sun);
				}
				break;
			case "Thunderstorm":
				if (!(bc.globalStatusController.getWeather() instanceof Rainy) && !(bc.globalStatusController.getTerrain() instanceof ElectricTerrain)) {
					Rainy rain2 = new Rainy(200);
					ElectricTerrain electric = new ElectricTerrain(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) rain2);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) electric);
				}
				break;
			case "Tailwind":
				if (user.getStatuses().stream().noneMatch(status -> status instanceof CustomTailwind)) {
					CustomTailwind tailwind = new CustomTailwind();
					user.addTeamStatus(tailwind, null);
				}
				break;
			case "AuroraVeil":
				if (user.getStatuses().stream().noneMatch(status -> status instanceof CustomAuroraVeil)) {
					CustomAuroraVeil av = new CustomAuroraVeil();
					user.addTeamStatus(av, null);
				}
				break;
			case "MagmaStorm":
				if (bc.globalStatusController.getGlobalStatuses().stream().noneMatch(status -> status instanceof CustomMagmaStorm)) {
					CustomMagmaStorm mstorm = new CustomMagmaStorm();
					bc.globalStatusController.addGlobalStatus(null, mstorm);
				}
				break;
			case "PsychicTerrain":
				if (!(bc.globalStatusController.getTerrain() instanceof PsychicTerrain)) {
					PsychicTerrain psychic = new PsychicTerrain(200);
					bc.globalStatusController.addGlobalStatus(null, (GlobalStatusBase) psychic);
				}
				break;
		}
	}

	//depreciated
	public static int extractTeamSelectTime(String input) {
		char[] chars = input.toCharArray();
		int len = chars.length;
		int keyLen = "TeamSelectTime:".length();

		for (int i = 0; i < len - keyLen; i++) {
			if (chars[i] == 'T' && input.startsWith("TeamSelectTime:", i)) {
				int j = i + keyLen;

				while (j < len && chars[j] == ' ') j++;

				int num = 0;
				while (j < len && chars[j] >= '0' && chars[j] <= '9') {
					num = num * 10 + (chars[j] - '0');
					j++;
				}

				return num;
			}
		}
		return -1;
	}
}
