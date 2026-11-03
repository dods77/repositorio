package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class Gilberto extends CharacterEntity {
    private static final double CONTACT_COOLDOWN = 1.0;

    private double contactCooldown;
    private Facing facing = Facing.LEFT;
    private double walkTime;
    private int walkFrame;

    public Gilberto(double x, double y) {
        super(x, y, 32, 100, 165, 10, 3);
    }

    @Override
    public void update(double deltaSeconds) {
        contactCooldown = Math.max(0, contactCooldown - deltaSeconds);
    }

    public void moveToward(PlayerCharacter player, Room room, double deltaSeconds) {
        double horizontal = player.getX() - x;
        double vertical = player.getY() - y;
        double distance = Math.hypot(horizontal, vertical);
        if (distance > 0) {
            setMovement(horizontal / distance, vertical / distance);
            updateFacing(horizontal, vertical, deltaSeconds);
        } else {
            setMovement(0, 0);
            walkFrame = 0;
            return;
        }

        double halfWidth = (width + player.getWidth()) / 2.0;
        double halfHeight = (height + player.getHeight()) / 2.0;
        double travelToHorizontalContact = Math.abs(horizontal) > halfWidth
            ? (Math.abs(horizontal) - halfWidth) * distance / Math.abs(horizontal)
            : 0;
        double travelToVerticalContact = Math.abs(vertical) > halfHeight
            ? (Math.abs(vertical) - halfHeight) * distance / Math.abs(vertical)
            : 0;
        double travelToContact = Math.max(travelToHorizontalContact,
            travelToVerticalContact);
        double movementTime = Math.min(deltaSeconds,
            travelToContact / getMoveSpeed());
        moveWithin(room, movementTime);
    }

    public void tryDamageOnContact(PlayerCharacter player) {
        boolean touching = Math.abs(x - player.getX()) <= (width + player.getWidth()) / 2.0
                && Math.abs(y - player.getY()) <= (height + player.getHeight()) / 2.0;
        if (touching && contactCooldown <= 0) {
            player.takeDamage(1);
            contactCooldown = CONTACT_COOLDOWN;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        BufferedImage sprite = Sprites.enemy(facing, walkFrame);
        int left = (int) Math.round(x - sprite.getWidth() / 2.0);
        int top = (int) Math.round(y - sprite.getHeight() / 2.0);
        graphics.drawImage(sprite, left, top, null);
    }

    private void updateFacing(double horizontal, double vertical,
            double deltaSeconds) {
        if (Math.abs(horizontal) >= Math.abs(vertical)) {
            facing = horizontal < 0 ? Facing.LEFT : Facing.RIGHT;
        } else {
            facing = vertical < 0 ? Facing.UP : Facing.DOWN;
        }
        walkTime += deltaSeconds;
        walkFrame = (int) (walkTime * 7) % 2;
    }
}
