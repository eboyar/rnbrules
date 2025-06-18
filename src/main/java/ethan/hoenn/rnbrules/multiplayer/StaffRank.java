package ethan.hoenn.rnbrules.multiplayer;

import java.util.Objects;

public class StaffRank {
    private final String id;
    private final String displayName;
    private final String nameColor;
    private final int priority;
    
    public StaffRank(String id, String displayName, String nameColor, int priority) {
        this.id = id;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.priority = priority;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getNameColor() {
        return nameColor;
    }
    
    public int getPriority() {
        return priority;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StaffRank staffRank = (StaffRank) obj;
        return Objects.equals(id, staffRank.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "StaffRank{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", nameColor='" + nameColor + '\'' +
                ", priority=" + priority +
                '}';
    }
    
    public static final StaffRank STAFF = new StaffRank("staff", "Staff", "&c", 10);
    public static final StaffRank ADMIN = new StaffRank("admin", "Admin", "&4", 20);
}
