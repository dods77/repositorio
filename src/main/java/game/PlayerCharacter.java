package game;

import java.awt.Color;
import java.awt.Graphics2D;

public class PlayerCharacter extends CharacterEntity {
    private static final int SHOT_SIZE = 10;
    private static final double SHOT_SPEED = 420;
    private static final double SHOT_COOLDOWN = 0.22;

    private double aimX = 1;
    private double aimY;
    private double shotCooldown;
    private boolean shooting;

    public PlayerCharacter(double x, double y) {
        super(x, y, 32, 220);
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
    }

    public void move(Room room, double deltaSeconds) {
        moveWithin(room, deltaSeconds);
    }

    public Shot tryShoot() {
        if (!shooting || shotCooldown > 0) {
            return null;
        }

        shotCooldown = SHOT_COOLDOWN;
        double startX = x + aimX * (width / 2.0 + SHOT_SIZE);
        double startY = y + aimY * (height / 2.0 + SHOT_SIZE);
        return new Shot(startX, startY, aimX * SHOT_SPEED, aimY * SHOT_SPEED);
    }

    @Override
    public void draw(Graphics2D graphics) {
        int left = (int) Math.round(x - width / 2.0);
        int top = (int) Math.round(y - height / 2.0);
        graphics.setColor(new Color(235, 190, 160));
        graphics.fillOval(left, top, width, height);
        graphics.setColor(new Color(90, 45, 50));
        graphics.fillOval(left + 8, top + 10, 5, 5);
        graphics.fillOval(left + 19, top + 10, 5, 5);
    }
}
