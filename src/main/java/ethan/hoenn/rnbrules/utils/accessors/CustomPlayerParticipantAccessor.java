package ethan.hoenn.rnbrules.utils.accessors;

import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;

public interface CustomPlayerParticipantAccessor {
	static void setDeathless(PlayerParticipant participant, boolean deathless) {
		((ICustomPlayerParticipant) participant).rnbrules$setDeathless(deathless);
	}

	static boolean isDeathless(PlayerParticipant participant) {
		return ((ICustomPlayerParticipant) participant).rnbrules$isDeathless();
	}

	static void setDeathlessMultiplier(PlayerParticipant participant, int multiplier) {
		((ICustomPlayerParticipant) participant).rnbrules$setDeathlessMultiplier(multiplier);
	}

	static int getDeathlessMultiplier(PlayerParticipant participant) {
		return ((ICustomPlayerParticipant) participant).rnbrules$getDeathlessMultiplier();
	}
}
