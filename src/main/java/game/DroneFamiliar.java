package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public final class DroneFamiliar {
    private static final double ORBIT_RADIUS = 42;
    private static final double ORBIT_SPEED = 2.2;
    private static final double SHOT_COOLDOWN_SECONDS = 0.8;
    private static final double SHOT_SPEED = 320;
    private static final int SIZE = 18;
    private static final int SHOT_SIZE = 8;

    private double angle;
    private double shotCooldown;
    private double x;
    private double y;

    public Shot tryShoot(PlayerCharacter player, List<Gilberto> enemies,
            double deltaSeconds) {
        angle += ORBIT_SPEED * deltaSeconds;
        x = player.getX() + Math.cos(angle) * ORBIT_RADIUS;
        y = player.getY() + Math.sin(angle) * ORBIT_RADIUS;
        shotCooldown = Math.max(0, shotCooldown - deltaSeconds);
        if (shotCooldown > 0) {
            return null;
        }

        Gilberto target = findNearestEnemy(enemies);
        if (target == null) {
            return null;
        }

        shotCooldown = SHOT_COOLDOWN_SECONDS;
        double directionX = target.getX() - x;
        double directionY = target.getY() - y;
        double distance = Math.hypot(directionX, directionY);
        if (distance == 0) {
            return null;
        }
        directionX /= distance;
        directionY /= distance;
        return new Shot(x, y, directionX * SHOT_SPEED, directionY * SHOT_SPEED,
                SHOT_SIZE, false, false);
    }

    public void draw(Graphics2D graphics) {
        graphics.setColor(new Color(80, 175, 235));
        graphics.fillOval((int) Math.round(x - SIZE / 2.0),
                (int) Math.round(y - SIZE / 2.0), SIZE, SIZE);
        graphics.setColor(Color.WHITE);
        graphics.drawOval((int) Math.round(x - SIZE / 2.0),
                (int) Math.round(y - SIZE / 2.0), SIZE, SIZE);
        graphics.drawLine((int) Math.round(x - 5), (int) Math.round(y),
                (int) Math.round(x + 5), (int) Math.round(y));
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
