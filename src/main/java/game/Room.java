package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Room {
    private final int width;
    private final int height;
    private final PlayerCharacter player;
    private final List<Shot> shots = new ArrayList<>();

    public Room(int width, int height) {
        this.width = width;
        this.height = height;
        player = new PlayerCharacter(width / 2.0, height / 2.0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public int getShotCount() {
        return shots.size();
    }

    public void update(double deltaSeconds) {
        player.update(deltaSeconds);
        player.move(this, deltaSeconds);

        Shot newShot = player.tryShoot();
        if (newShot != null) {
            shots.add(newShot);
        }

        Iterator<Shot> iterator = shots.iterator();
        while (iterator.hasNext()) {
            Shot shot = iterator.next();
            shot.update(deltaSeconds);
            if (shot.isOutside(this)) {
                iterator.remove();
            }
        }
    }

    public void draw(Graphics2D graphics) {
        graphics.setColor(new Color(48, 48, 62));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(new Color(56, 56, 70));
        for (int x = 40; x < width; x += 40) {
            graphics.drawLine(x, 0, x, height);
        }
        for (int y = 40; y < height; y += 40) {
            graphics.drawLine(0, y, width, y);
        }

        graphics.setColor(new Color(190, 170, 145));
        graphics.drawRect(1, 1, width - 3, height - 3);

        for (Shot shot : shots) {
            shot.draw(graphics);
        }
        player.draw(graphics);
    }
}
