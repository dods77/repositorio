package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class PlayerCharacter extends CharacterEntity {
    private static final String NAME = "Isaacaias";
    private static final int SHOT_SIZE = 10;
    private static final double SHOT_SPEED = 420;

    private double aimX = 1;
    private double aimY;
    private double shotCooldown;
    private boolean shooting;
    private Facing facing = Facing.DOWN;
    private double walkTime;
    private int walkFrame;

    public PlayerCharacter(double x, double y) {
        super(x, y, 32, 100, 220, 10, 3);
    }

    public void setAim(double horizontal, double vertical) {
        double length = Math.hypot(horizontal, vertical);
        if (length > 0) {
            aimX = horizontal / length;
            aimY = vertical / length;
        }
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    @Override
    public void update(double deltaSeconds) {
        shotCooldown = Math.max(0, shotCooldown - deltaSeconds);

        double facingX = shooting ? aimX : getMoveX();
        double facingY = shooting ? aimY : getMoveY();
        if (Math.abs(facingX) > 0.01 || Math.abs(facingY) > 0.01) {
            if (Math.abs(facingX) >= Math.abs(facingY)) {
                facing = facingX < 0 ? Facing.LEFT : Facing.RIGHT;
            } else {
                facing = facingY < 0 ? Facing.UP : Facing.DOWN;
            }
        }

        if (Math.abs(getMoveX()) > 0.01 || Math.abs(getMoveY()) > 0.01) {
            walkTime += deltaSeconds;
            walkFrame = (int) (walkTime * 8) % 2;
        } else {
            walkTime = 0;
            walkFrame = 0;
        }
    }

    public Room.Exit move(Room room, double deltaSeconds) {
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;
        double nextX = x + getMoveX() * getMoveSpeed() * deltaSeconds;
        double nextY = y + getMoveY() * getMoveSpeed() * deltaSeconds;
        double minX = Room.WALL_THICKNESS + halfWidth;
        double maxX = room.getWidth() - Room.WALL_THICKNESS - halfWidth;
        double minY = Room.WALL_THICKNESS + halfHeight;
        double maxY = room.getHeight() - Room.WALL_THICKNESS - halfHeight;

        nextY = Math.max(minY, Math.min(maxY, nextY));
        boolean inDoorway = Math.abs(nextY - room.getHeight() / 2.0)
                <= Room.DOOR_HALF_HEIGHT - halfHeight;

        if (nextX < minX && room.hasLeftDoor() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.LEFT;
        }
        if (nextX > maxX && room.hasRightDoor() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.RIGHT;
        }

        x = Math.max(minX, Math.min(maxX, nextX));
        y = nextY;
        return Room.Exit.NONE;
    }

    public Shot tryShoot() {
        if (!shooting || shotCooldown > 0) {
            return null;
        }

        shotCooldown = 1.0 / getAttackSpeed();
        double startX = x + aimX * (width / 2.0 + SHOT_SIZE);
        double startY = y + aimY * (height / 2.0 + SHOT_SIZE);
        return new Shot(startX, startY, aimX * SHOT_SPEED, aimY * SHOT_SPEED);
    }

    @Override
    public void draw(Graphics2D graphics) {
        BufferedImage sprite = Sprites.player(facing, walkFrame);
        int left = (int) Math.round(x - sprite.getWidth() / 2.0);
        int top = (int) Math.round(y - sprite.getHeight() / 2.0);
        graphics.drawImage(sprite, left, top, null);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        graphics.setColor(Color.WHITE);
        int nameWidth = graphics.getFontMetrics().stringWidth(NAME);
        graphics.drawString(NAME, (int) Math.round(x) - nameWidth / 2, top - 5);
    }
}
