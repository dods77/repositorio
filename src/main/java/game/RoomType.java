package game;

public enum RoomType {
    NORMAL("Normal Room"),
    TREASURE("Treasure Room");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
