package game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerCharacter extends CharacterEntity {
    private static final String NAME = "Isaacaias";
    private static final int MAGAZINE_SIZE = 64;
    private static final double MAGAZINE_RECHARGE_RATE = 16;
    private static final double OVERHEAT_DURATION = 1;
    private static final int SHOT_SIZE = 10;
    private static final double SHOT_SPEED = 420;
    private static final double SLASH_DURATION = 0.18;
    private static final double KATANA_SWEEP_ANGLE = Math.PI * 2 / 3;
    private static final double DASH_DURATION = 0.14;
    private static final double DASH_COOLDOWN = 0.75;
    private static final double DASH_SPEED = 550;
    private static final double MELEE_COOLDOWN = 0.22;
    private static final double MELEE_RANGE = 80;
    private static final double MELEE_HALF_ANGLE = Math.PI / 3;
    private static final int MELEE_DAMAGE_MULTIPLIER = 10;

    private double aimX = 1;
    private double aimY;
    private double shotCooldown;
    private double magazineAmmo = MAGAZINE_SIZE;
    private double overheatCooldown;
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
    private double dashTimer;
    private double dashCooldown;
    private double dashX;
    private double dashY;
    private double meleeCooldown;
    private double slashTimer;
    private double slashStartAngle;
    private boolean spinningSlash;
    private boolean whiteMeleeSlash;
    private Facing facing = Facing.DOWN;
    private double walkTime;
    private int walkFrame;

    public PlayerCharacter(double x, double y) {
        super(x, y, 20, 29, 100, 220, 5, 3);
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

    public int getMagazineAmmo() {
        return (int) Math.floor(magazineAmmo);
    }

    public int getMagazineSize() {
        return MAGAZINE_SIZE;
    }

    public double getOverheatCooldown() {
        return overheatCooldown;
    }

    public double getMagazineProgress() {
        return magazineAmmo / MAGAZINE_SIZE;
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
        if (overheatCooldown > 0) {
            overheatCooldown = Math.max(0, overheatCooldown - deltaSeconds);
            rechargeMagazine(deltaSeconds);
        } else if (!shooting || hasKatana) {
            rechargeMagazine(deltaSeconds);
        }
        katanaAttackCooldown = Math.max(0, katanaAttackCooldown - deltaSeconds);
        dashCooldown = Math.max(0, dashCooldown - deltaSeconds);
        meleeCooldown = Math.max(0, meleeCooldown - deltaSeconds);
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
        double dashSeconds = Math.min(deltaSeconds, dashTimer);
        double normalSeconds = deltaSeconds - dashSeconds;
        double nextX = x + dashX * DASH_SPEED * dashSeconds
                + getMoveX() * getMoveSpeed() * normalSeconds;
        double nextY = y + dashY * DASH_SPEED * dashSeconds
                + getMoveY() * getMoveSpeed() * normalSeconds;
        dashTimer = Math.max(0, dashTimer - deltaSeconds);
        double minX = Room.WALL_THICKNESS + halfWidth;
        double maxX = room.getWidth() - Room.WALL_THICKNESS - halfWidth;
        double minY = Room.WALL_THICKNESS + halfHeight;
        double maxY = room.getHeight() - Room.WALL_THICKNESS - halfHeight;

        nextY = Math.max(minY, Math.min(maxY, nextY));
        boolean inDoorway = Math.abs(nextY - room.getHeight() / 2.0)
                <= Room.DOOR_HALF_HEIGHT - halfHeight;

        if (nextX < minX && room.hasLeftDoor()
                && room.areEnemiesDefeated() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.LEFT;
        }
        if (nextX > maxX && room.hasRightDoor()
                && room.areEnemiesDefeated() && inDoorway) {
            x = nextX;
            y = nextY;
            return Room.Exit.RIGHT;
        }

        nextX = Math.max(minX, Math.min(maxX, nextX));
        room.moveEntityWithin(this, nextX, nextY);
        return Room.Exit.NONE;
    }

    public void dash() {
        if (dashCooldown > 0) {
            return;
        }

        double directionX = getMoveX();
        double directionY = getMoveY();
        if (Math.hypot(directionX, directionY) == 0) {
            switch (facing) {
                case DOWN:
                    directionY = 1;
                    break;
                case UP:
                    directionY = -1;
                    break;
                case LEFT:
                    directionX = -1;
                    break;
                case RIGHT:
                    directionX = 1;
                    break;
                default:
                    throw new IllegalStateException("Unknown facing direction");
            }
        }
        double length = Math.hypot(directionX, directionY);
        dashX = directionX / length;
        dashY = directionY / length;
        dashTimer = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;
    }

    public Shot tryShoot() {
        if (hasKatana || !shooting || shotCooldown > 0) {
            return null;
        }

        if (overheatCooldown > 0 || magazineAmmo < 1) {
            return null;
        }
        shotCooldown = 0.00002 / getAttackSpeed();
        magazineAmmo--;
        if (magazineAmmo == 0) {
            overheatCooldown = OVERHEAT_DURATION;
        }
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

    private void rechargeMagazine(double deltaSeconds) {
        magazineAmmo = Math.min(MAGAZINE_SIZE,
                magazineAmmo + MAGAZINE_RECHARGE_RATE * deltaSeconds);
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

    public void updateCompanions(List<Gilberto> enemies, double deltaSeconds) {
        if (companionCat != null) {
            companionCat.update(this, enemies, deltaSeconds);
        }
    }

    public void collectItem(SchoolItem item) {
        applyItem(item);
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

    public boolean tryMeleeAttack(double horizontal, double vertical,
            List<Gilberto> gilbertos, List<Antonio> antonios) {
        if (Math.hypot(horizontal, vertical) == 0 || meleeCooldown > 0) {
            return false;
        }
        setAim(horizontal, vertical);
        meleeCooldown = MELEE_COOLDOWN;
        int meleeDamage = getDamage() * MELEE_DAMAGE_MULTIPLIER;
        boolean killedEnemy = attackForward(gilbertos, meleeDamage,
                MELEE_RANGE, MELEE_HALF_ANGLE);
        killedEnemy |= attackForward(antonios, meleeDamage,
                MELEE_RANGE, MELEE_HALF_ANGLE);
        showMeleeSlash(horizontal, vertical);
        if (killedEnemy) {
            magazineAmmo = MAGAZINE_SIZE;
            overheatCooldown = 0;
            shotCooldown = 0;
        }
        return killedEnemy;
    }

    public void drawCompanions(Graphics2D graphics) {
        if (companionCat != null) {
            companionCat.draw(graphics);
        }
    }

    private void showSlash(boolean spin) {
        slashTimer = SLASH_DURATION;
        slashStartAngle = Math.atan2(aimY, aimX);
        spinningSlash = spin;
        whiteMeleeSlash = false;
    }

    private void showMeleeSlash(double horizontal, double vertical) {
        slashTimer = SLASH_DURATION;
        slashStartAngle = Math.atan2(vertical, horizontal);
        spinningSlash = false;
        whiteMeleeSlash = true;
    }

    void showSpinningSlash() {
        showSlash(true);
    }

    private void drawSlash(Graphics2D graphics) {
        if (slashTimer <= 0) {
            return;
        }
        Graphics2D slashGraphics = (Graphics2D) graphics.create();
        double progress = 1 - slashTimer / SLASH_DURATION;
        if (whiteMeleeSlash) {
            drawWhiteMeleeSlash(slashGraphics, progress);
            slashGraphics.dispose();
            return;
        }
        double easedProgress = 1 - Math.pow(1 - progress, 3);
        float opacity = (float) (1 - progress * 0.55);
        double scale = 0.8 + 0.2 * easedProgress;
        double rotation = slashStartAngle - Math.PI / 2;
        if (spinningSlash) {
            rotation += easedProgress * Math.PI * 2;
        } else {
            rotation += (easedProgress - 0.5) * KATANA_SWEEP_ANGLE;
        }
        slashGraphics.setComposite(AlphaComposite.SrcOver.derive(opacity));
        slashGraphics.translate(x, y);
        slashGraphics.rotate(rotation);
        slashGraphics.scale(scale, scale);
        slashGraphics.drawImage(Sprites.MELEE_SLASH, 0, -48, 96, 96, null);
        slashGraphics.dispose();
    }

    private void drawWhiteMeleeSlash(Graphics2D graphics, double progress) {
        int frameIndex = Math.min(Sprites.MELEE_SWING_FRAMES.length - 1,
                (int) (progress * Sprites.MELEE_SWING_FRAMES.length));
        BufferedImage frame = Sprites.MELEE_SWING_FRAMES[frameIndex];
        graphics.translate(x, y);
        graphics.rotate(slashStartAngle);
        double scale = 0.76 + progress * 0.08;
        graphics.scale(scale, scale);
        graphics.drawImage(frame, -40, -80, null);
    }

    private boolean attackForward(List<? extends CharacterEntity> enemies, int damage,
            double range, double halfAngle) {
        boolean killedEnemy = false;
        for (CharacterEntity enemy : enemies) {
            double offsetX = enemy.getX() - x;
            double offsetY = enemy.getY() - y;
            double distance = Math.hypot(offsetX, offsetY);
            if (distance <= range && distance > 0
                    && (aimX * offsetX + aimY * offsetY) / distance
                            >= Math.cos(halfAngle)) {
                boolean wasAlive = enemy.getHealth() > 0;
                enemy.takeDamage(damage);
                killedEnemy |= wasAlive && enemy.getHealth() == 0;
            }
        }
        return killedEnemy;
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

}
