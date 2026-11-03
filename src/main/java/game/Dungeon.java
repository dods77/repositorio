package game;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public final class Dungeon {
    public static final int DEFAULT_ROOM_COUNT = 15;
    public static final int TREASURE_ROOM_INTERVAL = 5;

    private final List<Room> rooms = new ArrayList<>();
    private final PlayerCharacter player;
    private int currentRoomIndex;

    public Dungeon(int roomWidth, int roomHeight) {
        this(roomWidth, roomHeight, DEFAULT_ROOM_COUNT);
    }

    public Dungeon(int roomWidth, int roomHeight, int roomCount) {
        if (roomCount < 1) {
            throw new IllegalArgumentException("Dungeon must have at least one room");
        }

        player = new PlayerCharacter(roomWidth / 2.0, roomHeight / 2.0);
        for (int number = 1; number <= roomCount; number++) {
            RoomType type = number % TREASURE_ROOM_INTERVAL == 0
                    ? RoomType.TREASURE
                    : RoomType.NORMAL;
            rooms.add(new Room(roomWidth, roomHeight, number, type,
                    number > 1, number < roomCount, player));
        }
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return rooms.get(currentRoomIndex);
    }

    public int getCurrentRoomNumber() {
        return getCurrentRoom().getNumber();
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public RoomType getCurrentRoomType() {
        return getCurrentRoom().getType();
    }

    public int getShotCount() {
        return getCurrentRoom().getShotCount();
    }

    public void update(double deltaSeconds) {
        Room.Exit exit = getCurrentRoom().update(deltaSeconds);
        if (exit != Room.Exit.NONE) {
            moveThroughDoor(exit);
        }
    }

    public void draw(Graphics2D graphics) {
        getCurrentRoom().draw(graphics);
    }

    private void moveThroughDoor(Room.Exit exit) {
        int direction = exit == Room.Exit.RIGHT ? 1 : -1;
        int nextIndex = currentRoomIndex + direction;
        if (nextIndex < 0 || nextIndex >= rooms.size()) {
            return;
        }

        getCurrentRoom().clearShots();
        currentRoomIndex = nextIndex;
        getCurrentRoom().clearShots();

        double entryInset = Room.WALL_THICKNESS + player.getWidth() / 2.0 + 8;
        double entryX = exit == Room.Exit.RIGHT
                ? entryInset
                : getCurrentRoom().getWidth() - entryInset;
        player.setPosition(entryX, getCurrentRoom().getHeight() / 2.0);
    }
}
