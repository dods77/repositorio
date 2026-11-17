package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public final class CompanionCat {
    private static final double FOLLOW_DISTANCE = 34;
    private static final double MOVE_SPEED = 260;
    private static final double ATTACK_RANGE = 38;
    private static final double ATTACK_COOLDOWN = 1;
    private static final int ATTACK_DAMAGE = 8;
    private static final int SIZE = 18;

    private double x;
    private double y;
    private double attackCooldown;

    public CompanionCat(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(PlayerCharacter player, List<Gilberto> enemies,
            double deltaSeconds) {
        attackCooldown = Math.max(0, attackCooldown - deltaSeconds);
        double targetX = player.getX() - FOLLOW_DISTANCE;
        double targetY = player.getY() + FOLLOW_DISTANCE / 2;
        double followX = targetX - x;
        double followY = targetY - y;
        double followDistance = Math.hypot(followX, followY);
        if (followDistance > 2) {
            double travel = Math.min(followDistance, MOVE_SPEED * deltaSeconds);
            x += followX / followDistance * travel;
            y += followY / followDistance * travel;
        }

        if (attackCooldown > 0) {
            return;
        }
        Gilberto target = findNearestEnemy(enemies);
        if (target != null
                && Math.hypot(target.getX() - x, target.getY() - y) <= ATTACK_RANGE) {
            target.takeDamage(ATTACK_DAMAGE);
            attackCooldown = ATTACK_COOLDOWN;
        }
    }

    public void draw(Graphics2D graphics) {
        graphics.setColor(new Color(220, 180, 130));
        graphics.fillOval((int) Math.round(x - SIZE / 2.0),
                (int) Math.round(y - SIZE / 2.0), SIZE, SIZE);
        graphics.setColor(Color.BLACK);
        graphics.fillOval((int) Math.round(x - 4), (int) Math.round(y - 2), 2, 2);
        graphics.fillOval((int) Math.round(x + 2), (int) Math.round(y - 2), 2, 2);
        graphics.drawArc((int) Math.round(x - 5), (int) Math.round(y - 1),
                10, 7, 200, 140);
    }

    private Gilberto findNearestEnemy(List<Gilberto> enemies) {
        Gilberto nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Gilberto enemy : enemies) {
            double distance = Math.hypot(enemy.getX() - x, enemy.getY() - y);
            if (distance < nearestDistance) {
                nearest = enemy;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
