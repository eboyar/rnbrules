package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import ethan.hoenn.rnbrules.utils.accessors.CustomPlayerParticipantAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeathlessListener {

	private final Map<UUID, boolean[]> prebattleFaintedStatus = new HashMap<>();

	//same check as for gauntlets but general purpose instead
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBattleStartPost(BattleStartedEvent.Post event) {
		List<BattleParticipant> participants = event.getBattleController().participants;

		for (BattleParticipant participant : participants) {
			if (participant instanceof PlayerParticipant && participant.getEntity() instanceof ServerPlayerEntity) {
				PlayerParticipant playerParticipant = (PlayerParticipant) participant;
				ServerPlayerEntity player = (ServerPlayerEntity) participant.getEntity();
				UUID playerUUID = player.getUUID();

				PartyStorage storage = playerParticipant.getStorage();
				Pokemon[] partyPokemon = storage.getAll();
				boolean[] faintedStatus = new boolean[partyPokemon.length];

				for (int i = 0; i < partyPokemon.length; i++) {
					Pokemon pokemon = partyPokemon[i];

					faintedStatus[i] = (pokemon != null && pokemon.isFainted());
				}

				prebattleFaintedStatus.put(playerUUID, faintedStatus);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBattleEnd(BattleEndEvent event) {
		List<BattleParticipant> participants = event.getBattleController().participants;

		for (BattleParticipant participant : participants) {
			if (participant instanceof PlayerParticipant && participant.getEntity() instanceof ServerPlayerEntity) {
				PlayerParticipant playerParticipant = (PlayerParticipant) participant;
				ServerPlayerEntity player = (ServerPlayerEntity) participant.getEntity();
				UUID playerUUID = player.getUUID();

				boolean hadDeathlessBattle = false;

				if (prebattleFaintedStatus.containsKey(playerUUID)) {
					boolean[] preFainted = prebattleFaintedStatus.get(playerUUID);
					PartyStorage currentStorage = playerParticipant.getStorage();
					Pokemon[] currentParty = currentStorage.getAll();

					hadDeathlessBattle = true;

					if (preFainted.length == currentParty.length) {
						for (int i = 0; i < preFainted.length; i++) {
							Pokemon pokemon = currentParty[i];

							if (pokemon != null && !preFainted[i] && pokemon.isFainted()) {
								hadDeathlessBattle = false;
								break;
							}
						}
					} else {
						hadDeathlessBattle = false;
					}

					prebattleFaintedStatus.remove(playerUUID);
				}

				if (event.getResult(participant.getEntity()).orElse(BattleResults.DRAW).equals(BattleResults.VICTORY)) {
					try {
						CustomPlayerParticipantAccessor.setDeathless(playerParticipant, hadDeathlessBattle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					CustomPlayerParticipantAccessor.setDeathless(playerParticipant, false);
				}
			}
		}
	}
}
