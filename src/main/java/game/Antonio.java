package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class Antonio extends CharacterEntity {
    private static final double MAX_SHOT_DISTANCE = 350;
    private static final double SHOT_COOLDOWN = 1.25;
    private static final double SHOT_SPEED = 260;
    private static final int SHOT_SIZE = 8;

    private double shotCooldown;
    private Facing facing = Facing.DOWN;

    public Antonio(double x, double y) {
        super(x, y, 20, 31, 100, 90, 0, 1);
    }

    @Override
    public void update(double deltaSeconds) {
        shotCooldown = Math.max(0, shotCooldown - deltaSeconds);
    }

    public void approachIfOutOfRange(PlayerCharacter player, Room room,
            double deltaSeconds) {
        double horizontal = player.getX() - x;
        double vertical = player.getY() - y;
        double distance = Math.hypot(horizontal, vertical);
        if (distance <= MAX_SHOT_DISTANCE) {
            setMovement(0, 0);
            return;
        }

        setMovement(horizontal / distance, vertical / distance);
        double approachDistance = Math.min(distance - MAX_SHOT_DISTANCE,
                getMoveSpeed() * deltaSeconds);
        moveWithin(room, approachDistance / getMoveSpeed());
        updateFacing(horizontal, vertical);
    }

    public EnemyShot tryShoot(PlayerCharacter player) {
        double horizontal = player.getX() - x;
        double vertical = player.getY() - y;
        double distance = Math.hypot(horizontal, vertical);
        if (distance == 0) {
            return null;
        }

        updateFacing(horizontal, vertical);
        if (distance > MAX_SHOT_DISTANCE || shotCooldown > 0) {
            return null;
        }

        shotCooldown = SHOT_COOLDOWN;
        double directionX = horizontal / distance;
        double directionY = vertical / distance;
        double startX = x + directionX * (width / 2.0 + SHOT_SIZE / 2.0);
        double startY = y + directionY * (height / 2.0 + SHOT_SIZE / 2.0);
        return new EnemyShot(startX, startY,
                directionX * SHOT_SPEED, directionY * SHOT_SPEED, SHOT_SIZE, 1);
    }

    public void receiveShot(Shot shot, int damage) {
        takeDamage(shot.isSyringe() ? getHealth() : damage);
    }

    @Override
    public void draw(Graphics2D graphics) {
        BufferedImage sprite = Sprites.enemy(facing, 0);
        int left = (int) Math.round(x - sprite.getWidth() / 2.0);
        int top = (int) Math.round(y - sprite.getHeight() / 2.0);
        graphics.setColor(new Color(65, 205, 225));
        graphics.drawOval(left - 3, top - 3,
                sprite.getWidth() + 6, sprite.getHeight() + 6);
        graphics.drawImage(sprite, left, top, null);
    }

    private void updateFacing(double horizontal, double vertical) {
        if (Math.abs(horizontal) >= Math.abs(vertical)) {
            facing = horizontal < 0 ? Facing.LEFT : Facing.RIGHT;
        } else {
            facing = vertical < 0 ? Facing.UP : Facing.DOWN;
        }
    }
}