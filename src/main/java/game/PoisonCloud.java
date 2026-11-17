package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

public final class PoisonCloud {
    private static final double RADIUS = 30;
    private static final double LIFETIME = 3;
    private static final double DAMAGE_INTERVAL = 0.75;
    private static final int DAMAGE = 2;

    private final double x;
    private final double y;
    private double lifetime = LIFETIME;
    private double damageTimer = DAMAGE_INTERVAL;

    public PoisonCloud(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean update(List<Gilberto> enemies, double deltaSeconds) {
        lifetime -= deltaSeconds;
        damageTimer -= deltaSeconds;
        if (damageTimer <= 0) {
            damageTimer += DAMAGE_INTERVAL;
            Iterator<Gilberto> iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Gilberto enemy = iterator.next();
                if (Math.hypot(enemy.getX() - x, enemy.getY() - y) <= RADIUS) {
                    enemy.takeDamage(DAMAGE);
                    if (enemy.getHealth() == 0) {
                        iterator.remove();
                    }
                }
            }
        }
        return lifetime <= 0;
    }

    public void draw(Graphics2D graphics) {
        graphics.setColor(new Color(105, 190, 75, 110));
        graphics.fillOval((int) Math.round(x - RADIUS),
                (int) Math.round(y - RADIUS),
                (int) Math.round(RADIUS * 2), (int) Math.round(RADIUS * 2));
    }
}
