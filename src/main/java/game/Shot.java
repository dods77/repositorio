package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class Shot extends GameEntity {
    private final double velocityX;
    private final double velocityY;
    private final boolean syringe;
    private final boolean sleepShot;
    private final boolean rootShot;

    public Shot(double x, double y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, 10, false, false);
    }

    public Shot(double x, double y, double velocityX, double velocityY,
            int size, boolean syringe, boolean sleepShot) {
        this(x, y, velocityX, velocityY, size, syringe, sleepShot, false);
    }

    public Shot(double x, double y, double velocityX, double velocityY,
            int size, boolean syringe, boolean sleepShot, boolean rootShot) {
        super(x, y, size, size);
        if (size <= 0) {
            throw new IllegalArgumentException("Shot size must be positive");
        }
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.syringe = syringe;
        this.sleepShot = sleepShot;
        this.rootShot = rootShot;
    }

    public boolean isSyringe() {
        return syringe;
    }

    public boolean isSleepShot() {
        return sleepShot;
    }

    public boolean isRootShot() {
        return rootShot;
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
        if (syringe) {
            graphics.setColor(Color.PINK);
            graphics.fillOval((int) Math.round(x - width / 2.0),
                    (int) Math.round(y - height / 2.0), width, height);
            graphics.setColor(Color.WHITE);
            graphics.drawLine((int) Math.round(x), (int) Math.round(y - height / 2.0),
                    (int) Math.round(x), (int) Math.round(y + height / 2.0));
            return;
        }
        BufferedImage sprite = Sprites.SHOT;
        int left = (int) Math.round(x - width / 2.0);
        int top = (int) Math.round(y - height / 2.0);
        graphics.drawImage(sprite, left, top, width, height, null);
    }
}
