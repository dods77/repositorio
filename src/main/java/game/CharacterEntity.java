package game;

public abstract class CharacterEntity extends GameEntity {
    private final int maxHealth;
    private final double moveSpeed;
    private final int damage;
    private final double attackSpeed;
    private int health;
    private double moveX;
    private double moveY;

    protected CharacterEntity(double x, double y, int size, int maxHealth,
            double moveSpeed, int damage, double attackSpeed) {
        super(x, y, size, size);
        if (maxHealth <= 0 || moveSpeed <= 0 || damage < 0 || attackSpeed <= 0) {
            throw new IllegalArgumentException("Character stats must be positive, except damage");
        }
        this.maxHealth = maxHealth;
        health = maxHealth;
        this.moveSpeed = moveSpeed;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public int getDamage() {
        return damage;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        health = amount >= health ? 0 : health - amount;
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
        x += moveX * getMoveSpeed() * deltaSeconds;
        y += moveY * getMoveSpeed() * deltaSeconds;
        x = Math.max(width / 2.0, Math.min(room.getWidth() - width / 2.0, x));
        y = Math.max(height / 2.0, Math.min(room.getHeight() - height / 2.0, y));
    }
}
