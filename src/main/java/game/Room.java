package game;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Room {
    public enum Exit {
        NONE,
        LEFT,
        RIGHT
    }

    static final int DOOR_HALF_HEIGHT = 48;
    static final int WALL_THICKNESS = 20;

    private final int width;
    private final int height;
    private final int number;
    private final RoomType type;
    private final boolean leftDoor;
    private final boolean rightDoor;
    private final PlayerCharacter player;
    private final List<Gilberto> gilbertos = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final BufferedImage background;

    public Room(int width, int height, int number, RoomType type,
            boolean leftDoor, boolean rightDoor, PlayerCharacter player) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        this.number = number;
        this.type = type;
        this.leftDoor = leftDoor;
        this.rightDoor = rightDoor;
        this.player = player;
        double[][] spawnPoints = {
            {width * 0.82, height * 0.32},
            {width * 0.18, height * 0.32},
            {width * 0.82, height * 0.68},
            {width * 0.18, height * 0.68},
            {width * 0.50, height * 0.18}
        };
        int gilbertoCount = ThreadLocalRandom.current().nextInt(1, 6);
        for (int index = 0; index < gilbertoCount; index++) {
            gilbertos.add(new Gilberto(spawnPoints[index][0], spawnPoints[index][1]));
        }
        background = createBackground();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    public boolean hasLeftDoor() {
        return leftDoor;
    }

    public boolean hasRightDoor() {
        return rightDoor;
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public int getShotCount() {
        return shots.size();
    }

    public void clearShots() {
        shots.clear();
    }

    public Exit update(double deltaSeconds) {
        player.update(deltaSeconds);
        Exit exit = player.move(this, deltaSeconds);
        if (exit != Exit.NONE) {
            return exit;
        }

        for (Gilberto gilberto : gilbertos) {
            double previousX = gilberto.getX();
            double previousY = gilberto.getY();
            gilberto.update(deltaSeconds);
            gilberto.moveToward(player, this, deltaSeconds);
            for (Gilberto other : gilbertos) {
                if (other != gilberto && overlaps(gilberto, other)) {
                    gilberto.setPosition(previousX, previousY);
                    break;
                }
            }
            gilberto.tryDamageOnContact(player);
        }

        Shot newShot = player.tryShoot();
        if (newShot != null) {
            shots.add(newShot);
        }

        Iterator<Shot> shotIterator = shots.iterator();
        while (shotIterator.hasNext()) {
            Shot shot = shotIterator.next();
            shot.update(deltaSeconds);
            if (shot.isOutside(this)) {
                shotIterator.remove();
                continue;
            }

            Iterator<Gilberto> gilbertoIterator = gilbertos.iterator();
            while (gilbertoIterator.hasNext()) {
                Gilberto gilberto = gilbertoIterator.next();
                if (overlaps(shot, gilberto)) {
                    gilberto.takeDamage(player.getDamage());
                    shotIterator.remove();
                    if (gilberto.getHealth() == 0) {
                        gilbertoIterator.remove();
                    }
                    break;
                }
            }
        }
        return Exit.NONE;
    }

    public void draw(Graphics2D graphics) {
        graphics.drawImage(background, 0, 0, null);

        for (Gilberto gilberto : gilbertos) {
            gilberto.draw(graphics);
        }
        for (Shot shot : shots) {
            shot.draw(graphics);
        }
        player.draw(graphics);
    }

    private boolean overlaps(GameEntity first, GameEntity second) {
        return Math.abs(first.getX() - second.getX())
                        <= (first.getWidth() + second.getWidth()) / 2.0
                && Math.abs(first.getY() - second.getY())
                        <= (first.getHeight() + second.getHeight()) / 2.0;
    }

    private BufferedImage createBackground() {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        drawFloor(graphics);
        drawWalls(graphics);
        drawDoors(graphics);
        if (type == RoomType.TREASURE) {
            drawTreasureDecor(graphics);
        }

        graphics.dispose();
        return image;
    }

    private void drawFloor(Graphics2D graphics) {
        BufferedImage floor = type == RoomType.TREASURE
                ? Sprites.FLOOR_TREASURE
                : Sprites.FLOOR_NORMAL;
        for (int y = 0; y < height; y += floor.getHeight()) {
            for (int x = 0; x < width; x += floor.getWidth()) {
                graphics.drawImage(floor, x, y, null);
            }
        }
    }

    private void drawWalls(Graphics2D graphics) {
        BufferedImage horizontal = Sprites.WALL_HORIZONTAL;
        for (int x = 0; x < width; x += horizontal.getWidth()) {
            graphics.drawImage(horizontal, x, 0, null);
            graphics.drawImage(horizontal, x, height - horizontal.getHeight(), null);
        }

        BufferedImage vertical = Sprites.WALL_VERTICAL;
        for (int y = 0; y < height; y += vertical.getHeight()) {
            graphics.drawImage(vertical, 0, y, null);
            graphics.drawImage(vertical, width - vertical.getWidth(), y, null);
        }
    }

    private void drawDoors(Graphics2D graphics) {
        int doorY = height / 2 - Sprites.DOOR.getHeight() / 2;
        if (leftDoor) {
            graphics.drawImage(Sprites.DOOR, 0, doorY, null);
        }
        if (rightDoor) {
            graphics.drawImage(Sprites.DOOR, width - Sprites.DOOR.getWidth(),
                    doorY, null);
        }
    }

    private void drawTreasureDecor(Graphics2D graphics) {
        int centerX = width / 2;
        int centerY = height / 2;
        graphics.drawImage(Sprites.TREASURE_RUG,
                centerX - Sprites.TREASURE_RUG.getWidth() / 2,
                centerY - Sprites.TREASURE_RUG.getHeight() / 2, null);
        graphics.drawImage(Sprites.PEDESTAL,
                centerX - Sprites.PEDESTAL.getWidth() / 2,
                centerY - Sprites.PEDESTAL.getHeight() / 2, null);
    }
}
