package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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
        BufferedImage sprite = Sprites.SHOT;
        int left = (int) Math.round(x - sprite.getWidth() / 2.0);
        int top = (int) Math.round(y - sprite.getHeight() / 2.0);
        graphics.drawImage(sprite, left, top, null);
    }
}
