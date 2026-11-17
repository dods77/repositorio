package game;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Room {
    public enum Exit {
        NONE,
        LEFT,
        RIGHT
    }

    static final int DOOR_HALF_HEIGHT = 48;
    static final int WALL_THICKNESS = 20;
    private static final int PATH_GRID_SIZE = 16;

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
    private final SchoolItem treasureItem;
    private final int treasureItemX;
    private final int treasureItemY;
    private boolean treasureItemCollected;

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
        treasureItem = type == RoomType.TREASURE
                ? SchoolItem.randomTreasureItem()
                : null;
        treasureItemX = width / 2;
        treasureItemY = height / 2 + 60;
        double[][] spawnPoints = {
            {width * 0.82, height * 0.32},
            {width * 0.18, height * 0.32},
            {width * 0.82, height * 0.68},
            {width * 0.18, height * 0.68},
            {width * 0.50, height * 0.18}
        };
        if (number > 1) {
            int gilbertoCount = ThreadLocalRandom.current().nextInt(1, 6);
            for (int index = 0; index < gilbertoCount; index++) {
                gilbertos.add(new Gilberto(spawnPoints[index][0], spawnPoints[index][1]));
            }
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

    public SchoolItem getTreasureItem() {
        return treasureItem;
    }

    public boolean isTreasureItemCollected() {
        return treasureItemCollected;
    }

    public void clearShots() {
        shots.clear();
    }

    Point2D.Double getNextWaypoint(Gilberto mover) {
        Point2D.Double directTarget = getPlayerContactPoint(mover.getX(), mover.getY());
        if (!isDirectPathBlocked(mover, directTarget)) {
            return directTarget;
        }

        int columns = width / PATH_GRID_SIZE;
        int rows = height / PATH_GRID_SIZE;
        int startColumn = clamp((int) Math.round(
                (mover.getX() - PATH_GRID_SIZE / 2.0) / PATH_GRID_SIZE),
                0, columns - 1);
        int startRow = clamp((int) Math.round(
                (mover.getY() - PATH_GRID_SIZE / 2.0) / PATH_GRID_SIZE),
                0, rows - 1);
        int start = startRow * columns + startColumn;
        int[] costs = new int[columns * rows];
        int[] parents = new int[columns * rows];
        boolean[] visited = new boolean[columns * rows];
        Arrays.fill(costs, Integer.MAX_VALUE);
        Arrays.fill(parents, -1);
        costs[start] = 0;

        PriorityQueue<PathNode> open = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.estimatedCost));
        open.add(new PathNode(startColumn, startRow,
                getPathHeuristic(startColumn, startRow, mover)));

        int goal = -1;
        int[] directions = {-1, 0, 1};
        while (!open.isEmpty()) {
            PathNode current = open.remove();
            int currentIndex = current.row * columns + current.column;
            if (visited[currentIndex]) {
                continue;
            }
            visited[currentIndex] = true;

            double currentX = gridCoordinate(current.column);
            double currentY = gridCoordinate(current.row);
            if (isPlayerApproachCell(currentX, currentY, mover)) {
                goal = currentIndex;
                break;
            }

            for (int deltaRow : directions) {
                for (int deltaColumn : directions) {
                    if (deltaColumn == 0 && deltaRow == 0) {
                        continue;
                    }
                    int nextColumn = current.column + deltaColumn;
                    int nextRow = current.row + deltaRow;
                    if (isBlockedPathCell(nextColumn, nextRow, columns, rows, mover)) {
                        continue;
                    }
                    if (deltaColumn != 0 && deltaRow != 0
                            && (isBlockedPathCell(current.column + deltaColumn,
                                    current.row, columns, rows, mover)
                                || isBlockedPathCell(current.column,
                                    current.row + deltaRow, columns, rows, mover))) {
                        continue;
                    }

                    int nextIndex = nextRow * columns + nextColumn;
                    int nextCost = costs[currentIndex]
                            + (deltaColumn != 0 && deltaRow != 0 ? 14 : 10);
                    if (nextCost >= costs[nextIndex]) {
                        continue;
                    }
                    costs[nextIndex] = nextCost;
                    parents[nextIndex] = currentIndex;
                    open.add(new PathNode(nextColumn, nextRow,
                            nextCost + getPathHeuristic(nextColumn, nextRow, mover)));
                }
            }
        }

        if (goal < 0) {
            return null;
        }
        if (goal == start) {
            return getPlayerContactPoint(mover.getX(), mover.getY());
        }

        List<Point2D.Double> path = new ArrayList<>();
        int pathIndex = goal;
        while (pathIndex != start) {
            int column = pathIndex % columns;
            int row = pathIndex / columns;
            path.add(new Point2D.Double(gridCoordinate(column), gridCoordinate(row)));
            pathIndex = parents[pathIndex];
            if (pathIndex < 0) {
                return null;
            }
        }

        Collections.reverse(path);
        Point2D.Double contactPoint = getPlayerContactPoint(
                gridCoordinate(goal % columns), gridCoordinate(goal / columns));
        if (!isDirectPathBlocked(mover, contactPoint)) {
            return contactPoint;
        }
        for (int index = path.size() - 1; index >= 0; index--) {
            Point2D.Double waypoint = path.get(index);
            if (!isDirectPathBlocked(mover, waypoint)) {
                return waypoint;
            }
        }
        return path.get(0);
    }

    private boolean isDirectPathBlocked(Gilberto mover, Point2D.Double target) {
        double horizontal = target.x - mover.getX();
        double vertical = target.y - mover.getY();
        double distance = Math.hypot(horizontal, vertical);
        int steps = Math.max(1, (int) Math.ceil(distance / (PATH_GRID_SIZE / 2.0)));
        for (int step = 1; step <= steps; step++) {
            double progress = (double) step / steps;
            double x = mover.getX() + horizontal * progress;
            double y = mover.getY() + vertical * progress;
            for (Gilberto other : gilbertos) {
                if (other != mover && overlapsAt(x, y, mover, other)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getPathHeuristic(int column, int row, Gilberto mover) {
        double halfWidth = (mover.getWidth() + player.getWidth()) / 2.0;
        double halfHeight = (mover.getHeight() + player.getHeight()) / 2.0;
        double horizontal = Math.max(0,
                Math.abs(gridCoordinate(column) - player.getX()) - halfWidth)
                / PATH_GRID_SIZE;
        double vertical = Math.max(0,
                Math.abs(gridCoordinate(row) - player.getY()) - halfHeight)
                / PATH_GRID_SIZE;
        double smaller = Math.min(horizontal, vertical);
        double larger = Math.max(horizontal, vertical);
        return (int) (14 * smaller + 10 * (larger - smaller));
    }

    private boolean isBlockedPathCell(int column, int row, int columns, int rows,
            Gilberto mover) {
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return true;
        }
        double x = gridCoordinate(column);
        double y = gridCoordinate(row);
        if (x < mover.getWidth() / 2.0 || x > width - mover.getWidth() / 2.0
                || y < mover.getHeight() / 2.0 || y > height - mover.getHeight() / 2.0) {
            return true;
        }
        for (Gilberto other : gilbertos) {
            if (other != mover && overlapsAt(x, y, mover, other)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerApproachCell(double x, double y, Gilberto mover) {
        double horizontal = Math.abs(x - player.getX());
        double vertical = Math.abs(y - player.getY());
        double halfWidth = (mover.getWidth() + player.getWidth()) / 2.0;
        double halfHeight = (mover.getHeight() + player.getHeight()) / 2.0;
        return horizontal <= halfWidth + PATH_GRID_SIZE / 2.0
                && vertical <= halfHeight + PATH_GRID_SIZE / 2.0
                && (horizontal >= halfWidth || vertical >= halfHeight);
    }

    private Point2D.Double getPlayerContactPoint(double x, double y) {
        double horizontal = x - player.getX();
        double vertical = y - player.getY();
        double halfWidth = 32;
        double halfHeight = 32;
        if (Math.abs(horizontal) >= Math.abs(vertical)) {
            return new Point2D.Double(player.getX()
                    + Math.copySign(halfWidth, horizontal),
                    Math.max(player.getY() - halfHeight,
                            Math.min(player.getY() + halfHeight, y)));
        }
        return new Point2D.Double(
                Math.max(player.getX() - halfWidth,
                        Math.min(player.getX() + halfWidth, x)),
                player.getY() + Math.copySign(halfHeight, vertical));
    }

    private boolean overlapsAt(double x, double y, GameEntity first, GameEntity second) {
        return Math.abs(x - second.getX())
                < (first.getWidth() + second.getWidth()) / 2.0
                && Math.abs(y - second.getY())
                < (first.getHeight() + second.getHeight()) / 2.0;
    }

    private double gridCoordinate(int cell) {
        return cell * PATH_GRID_SIZE + PATH_GRID_SIZE / 2.0;
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class PathNode {
        private final int column;
        private final int row;
        private final int estimatedCost;

        private PathNode(int column, int row, int estimatedCost) {
            this.column = column;
            this.row = row;
            this.estimatedCost = estimatedCost;
        }
    }

    public Exit update(double deltaSeconds) {
        player.update(deltaSeconds);
        Exit exit = player.move(this, deltaSeconds);
        if (exit != Exit.NONE) {
            return exit;
        }

        collectTreasureItem();

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
        if (treasureItem != null && !treasureItemCollected) {
            treasureItem.draw(graphics, treasureItemX, treasureItemY);
        }
        for (Shot shot : shots) {
            shot.draw(graphics);
        }
        player.draw(graphics);
    }

    private void collectTreasureItem() {
        if (treasureItem == null || treasureItemCollected
                || Math.abs(player.getX() - treasureItemX)
                        > player.getWidth() / 2.0 + 16
                || Math.abs(player.getY() - treasureItemY)
                        > player.getHeight() / 2.0 + 16) {
            return;
        }
        player.applyItem(treasureItem);
        treasureItemCollected = true;
    }

    private boolean overlaps(GameEntity first, GameEntity second) {
        return Math.abs(first.getX() - second.getX())
                < (first.getWidth() + second.getWidth()) / 2.0
                && Math.abs(first.getY() - second.getY())
                < (first.getHeight() + second.getHeight()) / 2.0;
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
