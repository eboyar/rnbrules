package ethan.hoenn.rnbrules.utils.data.gui;

import java.util.HashMap;
import java.util.Map;

public class LinkedTrainerData {

	private static final Map<Integer, TrainerLinkInfo> linkedTrainerMap = new HashMap<>();

	public static void storeLinkedTrainerData(int trainerEntityId, String linkedTrainerName, int linkedTeamSize, boolean hasLinked) {
		linkedTrainerMap.put(trainerEntityId, new TrainerLinkInfo(linkedTrainerName, linkedTeamSize, hasLinked));
	}

	public static TrainerLinkInfo getLinkedTrainerData(int trainerEntityId) {
		return linkedTrainerMap.get(trainerEntityId);
	}

	public static void clearAll() {
		linkedTrainerMap.clear();
	}

	public static class TrainerLinkInfo {

		private final String linkedTrainerName;
		private final int linkedTeamSize;
		private final boolean hasLinked;

		public TrainerLinkInfo(String linkedTrainerName, int linkedTeamSize, boolean hasLinked) {
			this.linkedTrainerName = linkedTrainerName;
			this.linkedTeamSize = linkedTeamSize;
			this.hasLinked = hasLinked;
		}

		public String getLinkedTrainerName() {
			return linkedTrainerName;
		}

		public int getLinkedTeamSize() {
			return linkedTeamSize;
		}

		public boolean hasLinked() {
			return hasLinked;
		}
	}
}
