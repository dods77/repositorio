package game;

public abstract class CharacterEntity extends GameEntity {
    private final double moveSpeed;
    private double moveX;
    private double moveY;

    protected CharacterEntity(double x, double y, int size, double moveSpeed) {
        super(x, y, size, size);
        this.moveSpeed = moveSpeed;
    }

    public void setMovement(double horizontal, double vertical) {
        double length = Math.hypot(horizontal, vertical);
        if (length > 1) {
            horizontal /= length;
            vertical /= length;
        }
        moveX = horizontal;
        moveY = vertical;
    }

    protected void moveWithin(Room room, double deltaSeconds) {
        x += moveX * moveSpeed * deltaSeconds;
        y += moveY * moveSpeed * deltaSeconds;
        x = Math.max(width / 2.0, Math.min(room.getWidth() - width / 2.0, x));
        y = Math.max(height / 2.0, Math.min(room.getHeight() - height / 2.0, y));
    }
}
