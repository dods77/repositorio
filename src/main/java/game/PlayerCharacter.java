package game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerCharacter extends CharacterEntity {
    private static final String NAME = "Isaacaias";
    private static final int SHOT_SIZE = 10;
    private static final double SHOT_SPEED = 420;

    private double aimX = 1;
    private double aimY;
    private double shotCooldown;
    private boolean shooting;
    private boolean hasBackpack;
    private int projectileSize = SHOT_SIZE;
    private int sleepHitsPerEnemy;
    private double sleepDuration;
    private double syringeChance;
    private DroneFamiliar droneFamiliar;
    private CompanionCat companionCat;
    private boolean hasKatana;
    private boolean hasRootShots;
    private boolean hasCapoeira;
    private boolean hasPoisonTrail;
    private int capoeiraShotCount;
    private boolean capoeiraSpinReady;
    private double rootDuration;
    private double katanaAttackCooldown;
    private SchoolItem activeItem;
    private double activeItemCooldown;
    private double slashTimer;
    private boolean spinningSlash;
    private Facing facing = Facing.DOWN;
    private double walkTime;
    private int walkFrame;

    public PlayerCharacter(double x, double y) {
        super(x, y, 20, 29, 100, 220, 10, 3);
    }

    public void setAim(double horizontal, double vertical) {
        double length = Math.hypot(horizontal, vertical);
        if (length > 0) {
            aimX = horizontal / length;
            aimY = vertical / length;
        }
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public void applyItem(SchoolItem item) {
        applyItemBonuses(item);
        hasBackpack |= item.hasBackpackCosmetic();
        item.applySpecialEffect(this);
    }

    private void applyItemBonuses(SchoolItem item) {
        increaseDamage(item.getDamageBonus());
        multiplyDamage(item.getDamageMultiplier());
        multiplyMoveSpeed(item.getMoveSpeedMultiplier());
        increaseMoveSpeed(item.getMoveSpeedBonus());
        multiplyAttackSpeed(item.getAttackSpeedMultiplier());
        increaseAttackSpeed(item.getAttackSpeedBonus());
        if (item.getMaxHealthChange() > 0) {
            increaseMaxHealth(item.getMaxHealthChange());
        } else if (item.getMaxHealthChange() < 0) {
            decreaseMaxHealth(-item.getMaxHealthChange());
        }
    }

    @Override
    public void update(double deltaSeconds) {
        shotCooldown = Math.max(0, shotCooldown - deltaSeconds);
        katanaAttackCooldown = Math.max(0, katanaAttackCooldown - deltaSeconds);
        activeItemCooldown = Math.max(0, activeItemCooldown - deltaSeconds);
        slashTimer = Math.max(0, slashTimer - deltaSeconds);

        double facingX = shooting ? aimX : getMoveX();
        double facingY = shooting ? aimY : getMoveY();
        if (Math.abs(facingX) > 0.01 || Math.abs(facingY) > 0.01) {
            if (Math.abs(facingX) >= Math.abs(facingY)) {
                facing = facingX < 0 ? Facing.LEFT : Facing.RIGHT;
            } else {
                facing = facingY < 0 ? Facing.UP : Facing.DOWN;
            }
        }

        if (Math.abs(getMoveX()) > 0.01 || Math.abs(getMoveY()) > 0.01) {
            walkTime += deltaSeconds;
            walkFrame = (int) (walkTime * 8) % 2;
        } else {
            walkTime = 0;
            walkFrame = 0;
        }
    }

    public Room.Exit move(Room room, double deltaSeconds) {
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;
        double nextX = x + getMoveX() * getMoveSpeed() * deltaSeconds;
        double nextY = y + getMoveY() * getMoveSpeed() * deltaSeconds;
        double minX = Room.WALL_THICKNESS + halfWidth;
        double maxX = room.getWidth() - Room.WALL_THICKNESS - halfWidth;
        double minY = Room.WALL_THICKNESS + halfHeight;
        double maxY = room.getHeight() - Room.WALL_THICKNESS - halfHeight;

        nextY = Math.max(minY, Math.min(maxY, nextY));
        boolean inDoorway = Math.abs(nextY - room.getHeight() / 2.0)
                <= Room.DOOR_HALF_HEIGHT - halfHeight;

        if (nextX < minX && room.hasLeftDoor() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.LEFT;
        }
        if (nextX > maxX && room.hasRightDoor() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.RIGHT;
        }

        x = Math.max(minX, Math.min(maxX, nextX));
        y = nextY;
        return Room.Exit.NONE;
    }

    public Shot tryShoot() {
        if (hasKatana || !shooting || shotCooldown > 0) {
            return null;
        }

        shotCooldown = 1.0 / getAttackSpeed();
        double startX = x + aimX * (width / 2.0 + projectileSize);
        double startY = y + aimY * (height / 2.0 + projectileSize);
        boolean syringe = ThreadLocalRandom.current().nextDouble() < syringeChance;
        if (hasCapoeira && ++capoeiraShotCount == 3) {
            capoeiraShotCount = 0;
            capoeiraSpinReady = true;
        }
        return new Shot(startX, startY, aimX * SHOT_SPEED, aimY * SHOT_SPEED,
                projectileSize, syringe, sleepHitsPerEnemy > 0, hasRootShots);
    }

    public Shot tryShootWithDrone(List<Gilberto> enemies, double deltaSeconds) {
        if (droneFamiliar == null) {
            return null;
        }
        return droneFamiliar.tryShoot(this, enemies, deltaSeconds);
    }

    void increaseProjectileSize(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Projectile size multiplier must be positive");
        }
        projectileSize = Math.max(1, (int) Math.round(projectileSize * multiplier));
    }

    void enableSleepShots(int hitsPerEnemy, double duration) {
        if (hitsPerEnemy <= 0 || !Double.isFinite(duration) || duration <= 0) {
            throw new IllegalArgumentException("Sleep shots require positive hits and duration");
        }
        sleepHitsPerEnemy = hitsPerEnemy;
        sleepDuration = duration;
    }

    int getSleepHitsPerEnemy() {
        return sleepHitsPerEnemy;
    }

    double getSleepDuration() {
        return sleepDuration;
    }

    double getRootDuration() {
        return rootDuration;
    }

    void enableSyringeShots(double chance) {
        if (!Double.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Syringe chance must be between 0 and 1");
        }
        syringeChance = chance;
    }

    void enableDroneFamiliar() {
        if (droneFamiliar == null) {
            droneFamiliar = new DroneFamiliar();
        }
    }

    void enableKatana() {
        hasKatana = true;
    }

    void enableRootShots(double duration) {
        if (!Double.isFinite(duration) || duration <= 0) {
            throw new IllegalArgumentException("Root duration must be positive");
        }
        hasRootShots = true;
        rootDuration = duration;
    }

    void enableCapoeira() {
        hasCapoeira = true;
    }

    void enablePoisonTrail() {
        hasPoisonTrail = true;
    }

    void enableCatFamiliar() {
        if (companionCat == null) {
            companionCat = new CompanionCat(x, y);
        }
    }

    boolean hasPoisonTrail() {
        return hasPoisonTrail;
    }

    boolean consumeCapoeiraSpin() {
        boolean ready = capoeiraSpinReady;
        capoeiraSpinReady = false;
        return ready;
    }

    public void activateItems(List<Gilberto> enemies) {
        if (activeItem != null && activeItemCooldown <= 0) {
            activeItem.activate(this, enemies);
            activeItemCooldown = activeItem.getCooldownSeconds();
        }
    }

    void useRazorblade(List<Gilberto> enemies) {
        if (getHealth() > 1) {
            takeDamage(Math.min(5, getHealth() - 1));
        }
        attackForward(enemies, getDamage() * 3, 92, Math.PI / 2);
        showSlash(false);
    }

    void useShockwave(List<Gilberto> enemies) {
        for (Gilberto enemy : enemies) {
            if (Math.hypot(enemy.getX() - x, enemy.getY() - y) <= 140) {
                enemy.takeDamage(getDamage() * 2);
            }
        }
    }

    void useRandomItemPocket() {
        applyItem(SchoolItem.randomPocketItem());
    }

    public void updateCompanions(List<Gilberto> enemies, double deltaSeconds) {
        if (companionCat != null) {
            companionCat.update(this, enemies, deltaSeconds);
        }
    }

    public List<ItemCooldown> getActiveItemCooldowns() {
        List<ItemCooldown> cooldowns = new ArrayList<>();
        if (activeItem != null) {
            cooldowns.add(new ItemCooldown(activeItem.getDisplayName(),
                    cooldownProgress(activeItemCooldown,
                            activeItem.getCooldownSeconds())));
        }
        return cooldowns;
    }

    public SchoolItem getActiveItem() {
        return activeItem;
    }

    public SchoolItem collectItem(SchoolItem item) {
        if (!item.isActivatable()) {
            applyItem(item);
            return null;
        }
        if (activeItem != null && activeItem.getClass() == item.getClass()) {
            return null;
        }

        SchoolItem droppedItem = activeItem;
        if (droppedItem != null) {
            removeActiveItemBonuses(droppedItem);
        }
        activeItem = item;
        activeItemCooldown = 0;
        applyItemBonuses(item);
        item.applySpecialEffect(this);
        return droppedItem;
    }

    private void removeActiveItemBonuses(SchoolItem item) {
        decreaseAttackSpeed(item.getAttackSpeedBonus());
    }

    public boolean tryKatanaAttack(List<Gilberto> enemies) {
        if (!hasKatana || !shooting || katanaAttackCooldown > 0) {
            return false;
        }
        katanaAttackCooldown = 1.0 / getAttackSpeed();
        attackForward(enemies, getDamage(), 76, Math.PI / 3);
        showSlash(false);
        return true;
    }

    public void drawCompanions(Graphics2D graphics) {
        if (companionCat != null) {
            companionCat.draw(graphics);
        }
    }

    private double cooldownProgress(double remaining, double duration) {
        return Math.max(0, Math.min(1, 1 - remaining / duration));
    }

    private void showSlash(boolean spin) {
        slashTimer = 0.22;
        spinningSlash = spin;
    }

    void showSpinningSlash() {
        showSlash(true);
    }

    private void drawSlash(Graphics2D graphics) {
        if (slashTimer <= 0) {
            return;
        }
        Graphics2D slashGraphics = (Graphics2D) graphics.create();
        float progress = (float) (slashTimer / 0.22);
        slashGraphics.setComposite(AlphaComposite.SrcOver.derive(progress));
        slashGraphics.translate(x, y);
        if (spinningSlash) {
            slashGraphics.rotate((1 - progress) * Math.PI * 2);
            slashGraphics.drawImage(Sprites.MELEE_SLASH, 0, -48, 96, 96, null);
        } else {
            slashGraphics.rotate(Math.atan2(aimY, aimX));
            slashGraphics.drawImage(Sprites.MELEE_SLASH, 0, -48, 96, 96, null);
        }
        slashGraphics.dispose();
    }

    private void attackForward(List<Gilberto> enemies, int damage,
            double range, double halfAngle) {
        for (Gilberto enemy : enemies) {
            double offsetX = enemy.getX() - x;
            double offsetY = enemy.getY() - y;
            double distance = Math.hypot(offsetX, offsetY);
            if (distance <= range && distance > 0
                    && (aimX * offsetX + aimY * offsetY) / distance
                            >= Math.cos(halfAngle)) {
                enemy.takeDamage(damage);
            }
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        BufferedImage sprite = Sprites.player(facing, walkFrame);
        int left = (int) Math.round(x - sprite.getWidth() / 2.0);
        int top = (int) Math.round(y - sprite.getHeight() / 2.0);
        graphics.drawImage(sprite, left, top, null);
        drawSlash(graphics);
        if (droneFamiliar != null) {
            droneFamiliar.draw(graphics);
        }
        drawCompanions(graphics);
        if (hasBackpack) {
            graphics.setColor(new Color(115, 72, 39));
            graphics.fillRoundRect((int) Math.round(x - 14),
                    (int) Math.round(y - 4), 10, 12, 3, 3);
            graphics.setColor(new Color(220, 180, 90));
            graphics.drawRect((int) Math.round(x - 11),
                    (int) Math.round(y - 1), 4, 3);
        }

        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        graphics.setColor(Color.WHITE);
        int nameWidth = graphics.getFontMetrics().stringWidth(NAME);
        graphics.drawString(NAME, (int) Math.round(x) - nameWidth / 2, top - 5);
    }

    public record ItemCooldown(String name, double progress) {
    }
}
