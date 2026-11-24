package game;

public abstract class CharacterEntity extends GameEntity {
    private int maxHealth;
    private double moveSpeed;
    private int damage;
    private double attackSpeed;
    private int health;
    private double moveX;
    private double moveY;

    protected CharacterEntity(double x, double y, int width, int height, int maxHealth,
            double moveSpeed, int damage, double attackSpeed) {
        super(x, y, width, height);
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

    protected void increaseMoveSpeed(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Speed increase cannot be negative");
        }
        moveSpeed += amount;
    }

    protected void multiplyMoveSpeed(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Speed multiplier must be positive");
        }
        moveSpeed *= multiplier;
    }

    protected void increaseDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage increase cannot be negative");
        }
        damage += amount;
    }

    protected void multiplyDamage(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Damage multiplier must be positive");
        }
        damage = (int) Math.round(damage * multiplier);
    }

    protected void increaseAttackSpeed(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Attack speed increase cannot be negative");
        }
        attackSpeed += amount;
    }

    protected void decreaseAttackSpeed(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Attack speed decrease cannot be negative");
        }
        attackSpeed = Math.max(0.1, attackSpeed - amount);
    }

    protected void multiplyAttackSpeed(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Attack speed multiplier must be positive");
        }
        attackSpeed *= multiplier;
    }

    protected void increaseMaxHealth(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Health increase must be positive");
        }
        maxHealth = Math.addExact(maxHealth, amount);
        health = (int) Math.min(maxHealth, (long) health + amount);
    }

    protected void decreaseMaxHealth(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Health decrease must be positive");
        }
        maxHealth = Math.max(1, maxHealth - amount);
        health = Math.min(health, maxHealth);
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        health = amount >= health ? 0 : health - amount;
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal cannot be negative");
        }
        health = (int) Math.min(maxHealth, (long) health + amount);
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

    protected double getMoveX() {
        return moveX;
    }

    protected double getMoveY() {
        return moveY;
    }

    protected void moveWithin(Room room, double deltaSeconds) {
        double nextX = x + moveX * getMoveSpeed() * deltaSeconds;
        double nextY = y + moveY * getMoveSpeed() * deltaSeconds;
        nextX = Math.max(Room.WALL_THICKNESS + width / 2.0,
                Math.min(room.getWidth() - Room.WALL_THICKNESS - width / 2.0,
                        nextX));
        nextY = Math.max(Room.WALL_THICKNESS + height / 2.0,
                Math.min(room.getHeight() - Room.WALL_THICKNESS - height / 2.0,
                        nextY));
        room.moveEntityWithin(this, nextX, nextY);
    }
}
