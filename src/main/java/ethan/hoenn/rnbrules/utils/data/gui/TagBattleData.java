package ethan.hoenn.rnbrules.utils.data.gui;

import java.util.HashMap;
import java.util.Map;

public class TagBattleData {

	private static final Map<Integer, PartnerInfo> partnerMap = new HashMap<>();

	public static void storePartnerData(int trainerEntityId, String partnerTrainerName) {
		partnerMap.put(trainerEntityId, new PartnerInfo(partnerTrainerName));
	}

	public static PartnerInfo getPartnerData(int trainerEntityId) {
		return partnerMap.get(trainerEntityId);
	}

	public static void clearAll() {
		partnerMap.clear();
	}

	public static class PartnerInfo {

		private final String partnerTrainerName;

		public PartnerInfo(String partnerTrainerName) {
			this.partnerTrainerName = partnerTrainerName;
		}

		public String getPartnerTrainerName() {
			return partnerTrainerName;
		}
	}
}
