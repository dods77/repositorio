package game;

import java.awt.Color;
import java.awt.Graphics2D;

public final class EnemyShot extends GameEntity {
    private final double velocityX;
    private final double velocityY;
    private final int damage;

    public EnemyShot(double x, double y, double velocityX, double velocityY,
            int size, int damage) {
        super(x, y, size, size);
        if (size <= 0 || damage < 0) {
            throw new IllegalArgumentException(
                    "Enemy shot size must be positive and damage non-negative");
        }
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void update(double deltaSeconds) {
        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
    }

    public boolean isOutside(Room room) {
        return x + width < 0 || y + height < 0
                || x - width > room.getWidth() || y - height > room.getHeight();
    }

    @Override
    public void draw(Graphics2D graphics) {
        int left = (int) Math.round(x - width / 2.0);
        int top = (int) Math.round(y - height / 2.0);
        graphics.setColor(new Color(230, 70, 75));
        graphics.fillOval(left, top, width, height);
        graphics.setColor(new Color(255, 190, 110));
        graphics.drawOval(left, top, width, height);
    }
}