package game;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Shot extends GameEntity {
    private final double velocityX;
    private final double velocityY;

    public Shot(double x, double y, double velocityX, double velocityY) {
        super(x, y, 10, 10);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
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
        graphics.setColor(new Color(255, 225, 125));
        graphics.fillOval((int) Math.round(x - width / 2.0),
                (int) Math.round(y - height / 2.0), width, height);
    }
}
