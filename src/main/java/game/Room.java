package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Room {
    public enum Exit {
        NONE,
        LEFT,
        RIGHT
    }

    static final int DOOR_HALF_HEIGHT = 48;
    private static final int DOOR_DEPTH = 22;

    private final int width;
    private final int height;
    private final int number;
    private final RoomType type;
    private final boolean leftDoor;
    private final boolean rightDoor;
    private final PlayerCharacter player;
    private Gilberto gilberto;
    private final List<Shot> shots = new ArrayList<>();

    public Room(int width, int height, int number, RoomType type,
            boolean leftDoor, boolean rightDoor, PlayerCharacter player) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        this.number = number;
        this.type = type;
        this.leftDoor = leftDoor;
        this.rightDoor = rightDoor;
        this.player = player;
        if (type == RoomType.NORMAL) {
            gilberto = new Gilberto(width - 80, height / 2.0);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    public boolean hasLeftDoor() {
        return leftDoor;
    }

    public boolean hasRightDoor() {
        return rightDoor;
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public int getShotCount() {
        return shots.size();
    }

    public void clearShots() {
        shots.clear();
    }

    public Exit update(double deltaSeconds) {
        player.update(deltaSeconds);
        Exit exit = player.move(this, deltaSeconds);
        if (exit != Exit.NONE) {
            return exit;
        }

        if (gilberto != null) {
            gilberto.update(deltaSeconds);
            gilberto.moveToward(player, this, deltaSeconds);
            gilberto.tryDamageOnContact(player);
        }

        Shot newShot = player.tryShoot();
        if (newShot != null) {
            shots.add(newShot);
        }

        Iterator<Shot> iterator = shots.iterator();
        while (iterator.hasNext()) {
            Shot shot = iterator.next();
            shot.update(deltaSeconds);
            if (shot.isOutside(this)) {
                iterator.remove();
            } else if (gilberto != null && overlaps(shot, gilberto)) {
                gilberto.takeDamage(player.getDamage());
                iterator.remove();
                if (gilberto.getHealth() == 0) {
                    gilberto = null;
                }
            }
        }
        return Exit.NONE;
    }

    public void draw(Graphics2D graphics) {
        boolean treasureRoom = type == RoomType.TREASURE;

        graphics.setColor(treasureRoom
                ? new Color(62, 53, 48)
                : new Color(48, 48, 62));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(treasureRoom
                ? new Color(72, 62, 55)
                : new Color(56, 56, 70));
        for (int x = 40; x < width; x += 40) {
            graphics.drawLine(x, 0, x, height);
        }
        for (int y = 40; y < height; y += 40) {
            graphics.drawLine(0, y, width, y);
        }

        graphics.setColor(treasureRoom
                ? new Color(215, 188, 125)
                : new Color(190, 170, 145));
        graphics.drawRect(1, 1, width - 3, height - 3);

        if (leftDoor) {
            drawDoor(graphics, 0);
        }
        if (rightDoor) {
            drawDoor(graphics, width - DOOR_DEPTH);
        }

        if (treasureRoom) {
            drawPedestal(graphics);
        }

        for (Shot shot : shots) {
            shot.draw(graphics);
        }
        if (gilberto != null) {
            gilberto.draw(graphics);
        }
        player.draw(graphics);
    }

    private boolean overlaps(GameEntity first, GameEntity second) {
        return Math.abs(first.getX() - second.getX())
                        <= (first.getWidth() + second.getWidth()) / 2.0
                && Math.abs(first.getY() - second.getY())
                        <= (first.getHeight() + second.getHeight()) / 2.0;
    }

    private void drawDoor(Graphics2D graphics, int x) {
        int doorTop = height / 2 - DOOR_HALF_HEIGHT;
        int doorHeight = DOOR_HALF_HEIGHT * 2;

        graphics.setColor(new Color(24, 22, 30));
        graphics.fillRect(x, doorTop, DOOR_DEPTH, doorHeight);

        graphics.setColor(new Color(215, 188, 125));
        graphics.drawLine(x, doorTop, x + DOOR_DEPTH, doorTop);
        graphics.drawLine(x, doorTop + doorHeight, x + DOOR_DEPTH,
                doorTop + doorHeight);
    }

    private void drawPedestal(Graphics2D graphics) {
        int centerX = width / 2;
        int centerY = height / 2;

        graphics.setColor(new Color(28, 24, 24, 110));
        graphics.fillOval(centerX - 38, centerY + 16, 76, 24);

        graphics.setColor(new Color(112, 101, 89));
        graphics.fillRoundRect(centerX - 24, centerY - 8, 48, 34, 8, 8);

        graphics.setColor(new Color(154, 140, 121));
        graphics.fillOval(centerX - 30, centerY - 20, 60, 26);

        graphics.setColor(new Color(220, 200, 160));
        graphics.drawOval(centerX - 30, centerY - 20, 60, 26);
    }
}
