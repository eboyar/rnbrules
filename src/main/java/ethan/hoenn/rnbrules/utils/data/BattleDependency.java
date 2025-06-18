package ethan.hoenn.rnbrules.utils.data;

public class BattleDependency {

	private final String id;
	private String description;

	public BattleDependency(String id, String desc) {
		this.id = id;
		this.description = desc;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
