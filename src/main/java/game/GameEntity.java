package game;

import java.awt.Graphics2D;

public abstract class GameEntity {
    protected double x;
    protected double y;
    protected final int width;
    protected final int height;

    protected GameEntity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract void update(double deltaSeconds);

    public abstract void draw(Graphics2D graphics);
}
