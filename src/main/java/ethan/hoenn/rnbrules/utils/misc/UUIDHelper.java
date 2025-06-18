package ethan.hoenn.rnbrules.utils.misc;

import java.util.UUID;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public final class UUIDHelper {

	private UUIDHelper() {}

	public static CompoundNBT removeEncounterUUID(CompoundNBT entityData, UUID playerUUID) {
		if (entityData.contains("Encounters", 9)) {
			ListNBT encounters = entityData.getList("Encounters", 10);
			boolean removed = false;

			for (int i = 0; i < encounters.size(); i++) {
				CompoundNBT encounter = encounters.getCompound(i);

				if (encounter.contains("UUID")) {
					UUID extractedUUID = encounter.getUUID("UUID");

					if (extractedUUID.equals(playerUUID)) {
						encounters.remove(i);
						removed = true;
						break;
					}
				}
			}

			if (removed) {
				entityData.put("Encounters", encounters);
			}
		}
		return entityData;
	}

	public static ListNBT addEncounterUUID(CompoundNBT entityData, UUID playerUUID) {
		ListNBT encounters = entityData.getList("Encounters", 10);
		boolean alreadyExists = false;

		for (int i = 0; i < encounters.size(); i++) {
			CompoundNBT encounter = encounters.getCompound(i);
			if (playerUUID.equals(encounter.getUUID("UUID"))) {
				alreadyExists = true;
				break;
			}
		}

		if (!alreadyExists) {
			CompoundNBT uuidNBT = new CompoundNBT();
			uuidNBT.putUUID("UUID", playerUUID);
			encounters.add(uuidNBT);
		}

		return encounters;
	}
}
