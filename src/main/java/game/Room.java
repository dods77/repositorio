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
import java.util.Random;
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
    private static final double ANTONIO_SPAWN_CLEARANCE = 48;
    private static final int GALLERY_COLUMNS = 7;
    private static final int GALLERY_COLUMN_SPACING = 100;
    private static final int GALLERY_ROW_SPACING = 105;
    private static final int GALLERY_TOP_OFFSET = 48;
    private static final int GALLERY_PEDESTAL_SIZE = 64;
    private static final int GALLERY_ITEM_LIFT = 18;
    private static final int GALLERY_LABEL_OFFSET = 60;
    private static final int GALLERY_LABEL_FONT_SIZE = 10;
    private static final double CHURCH_PROP_CENTER_CHANCE = 0.10;
    private static final double CHURCH_PROP_BORDER_CHANCE = 0.10;
    private static final int CHURCH_PROP_SPACING = 28;

    private final int width;
    private final int height;
    private final int number;
    private final RoomType type;
    private final boolean leftDoor;
    private final boolean rightDoor;
    private final PlayerCharacter player;
    private final List<Gilberto> gilbertos = new ArrayList<>();
    private final List<Antonio> antonios = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<EnemyShot> enemyShots = new ArrayList<>();
    private final List<PoisonCloud> poisonClouds = new ArrayList<>();
    private final BufferedImage background;
    private final List<ItemDisplay> itemDisplays = new ArrayList<>();
    private final List<ChurchProp> churchProps = new ArrayList<>();
    private final Random visualRandom;
    private double poisonSpawnCooldown;

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
        visualRandom = new Random(ThreadLocalRandom.current().nextLong());
        if (type == RoomType.TREASURE) {
            itemDisplays.add(new ItemDisplay(SchoolItem.randomTreasureItem(),
                    width / 2, height / 2 + 60));
        } else if (type == RoomType.GALLERY) {
            addGalleryItems(width, height);
        }
        createChurchProps();
        double[][] spawnPoints = {
            {width * 0.82, height * 0.32},
            {width * 0.18, height * 0.32},
            {width * 0.82, height * 0.68},
            {width * 0.18, height * 0.68},
            {width * 0.50, height * 0.18}
        };
        if (number > 1 && type != RoomType.TREASURE) {
            int gilbertoCount = ThreadLocalRandom.current().nextInt(1, 6);
            for (int index = 0; index < gilbertoCount; index++) {
                gilbertos.add(new Gilberto(spawnPoints[index][0], spawnPoints[index][1]));
            }
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int antonioCount = random.nextInt(1, 4);
            for (int index = 0; index < antonioCount; index++) {
                antonios.add(createRandomAntonio(random));
            }
        }
        background = createBackground();
    }

    private Antonio createRandomAntonio(ThreadLocalRandom random) {
        double minimumX = WALL_THICKNESS + 10;
        double maximumX = width - WALL_THICKNESS - 10;
        double minimumY = WALL_THICKNESS + 16;
        double maximumY = height - WALL_THICKNESS - 16;
        if (maximumX <= minimumX) {
            minimumX = width / 2.0;
            maximumX = minimumX;
        }
        if (maximumY <= minimumY) {
            minimumY = height / 2.0;
            maximumY = minimumY;
        }

        Antonio bestCandidate = null;
        double bestClearance = -1;
        for (int attempt = 0; attempt < 100; attempt++) {
            double x = maximumX == minimumX
                    ? minimumX : random.nextDouble(minimumX, maximumX);
            double y = maximumY == minimumY
                    ? minimumY : random.nextDouble(minimumY, maximumY);
            Antonio candidate = new Antonio(x, y);
            if (isObstacleBlockedAt(x, y, candidate.getWidth(),
                    candidate.getHeight())) {
                continue;
            }
            double clearance = getAntonioSpawnClearance(candidate);
            if (clearance >= ANTONIO_SPAWN_CLEARANCE) {
                return candidate;
            }
            if (clearance > bestClearance) {
                bestCandidate = candidate;
                bestClearance = clearance;
            }
        }
        if (bestCandidate != null) {
            return bestCandidate;
        }
        return new Antonio(width / 2.0, height / 2.0 + 80);
    }

    private double getAntonioSpawnClearance(Antonio candidate) {
        double clearance = Math.hypot(candidate.getX() - player.getX(),
                candidate.getY() - player.getY());
        for (Gilberto gilberto : gilbertos) {
            clearance = Math.min(clearance,
                    Math.hypot(candidate.getX() - gilberto.getX(),
                            candidate.getY() - gilberto.getY()));
        }
        for (Antonio antonio : antonios) {
            clearance = Math.min(clearance,
                    Math.hypot(candidate.getX() - antonio.getX(),
                            candidate.getY() - antonio.getY()));
        }
        return clearance;
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
        return shots.size() + enemyShots.size();
    }

    public SchoolItem getTreasureItem() {
        if (type != RoomType.TREASURE || itemDisplays.isEmpty()) {
            return null;
        }
        return itemDisplays.get(0).getItem();
    }

    public boolean isTreasureItemCollected() {
        return type == RoomType.TREASURE && !itemDisplays.isEmpty()
                && itemDisplays.get(0).isCollected();
    }

    public ItemDisplay getNearestItem(double x, double y, double maxDistance) {
        ItemDisplay nearest = null;
        double nearestDistance = maxDistance;
        for (ItemDisplay display : itemDisplays) {
            if (display.isCollected()) {
                continue;
            }
            double distance = Math.hypot(display.getX() - x, display.getY() - y);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = display;
            }
        }
        return nearest;
    }

    public void clearShots() {
        shots.clear();
        enemyShots.clear();
        poisonClouds.clear();
    }

    Point2D.Double getNextWaypoint(Gilberto mover) {
        Point2D.Double directTarget = getPlayerContactPoint(
            mover.getX(), mover.getY(), mover);
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
            return getPlayerContactPoint(mover.getX(), mover.getY(), mover);
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
            gridCoordinate(goal % columns), gridCoordinate(goal / columns), mover);
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
            if (isObstacleBlockedAt(x, y, mover.getWidth(), mover.getHeight())) {
                return true;
            }
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
        if (isObstacleBlockedAt(x, y, mover.getWidth(), mover.getHeight())) {
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

    private Point2D.Double getPlayerContactPoint(double x, double y, Gilberto mover) {
        double horizontal = x - player.getX();
        double vertical = y - player.getY();
        double halfWidth = (mover.getWidth() + player.getWidth()) / 2.0;
        double halfHeight = (mover.getHeight() + player.getHeight()) / 2.0;
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

    boolean isObstacleBlockedAt(double x, double y, int entityWidth,
            int entityHeight) {
        for (ChurchProp prop : churchProps) {
            if (Math.abs(x - prop.x) < (entityWidth + prop.collisionWidth) / 2.0
                    && Math.abs(y - prop.y)
                            < (entityHeight + prop.collisionHeight) / 2.0) {
                return true;
            }
        }
        return false;
    }

    void moveEntityWithin(GameEntity entity, double nextX, double nextY) {
        double resolvedX = entity.getX();
        double resolvedY = entity.getY();
        if (!isObstacleBlockedAt(nextX, resolvedY,
                entity.getWidth(), entity.getHeight())) {
            resolvedX = nextX;
        }
        if (!isObstacleBlockedAt(resolvedX, nextY,
                entity.getWidth(), entity.getHeight())) {
            resolvedY = nextY;
        }
        entity.setPosition(resolvedX, resolvedY);
    }

    public static final class ItemDisplay {
        private final SchoolItem item;
        private final int x;
        private final int y;
        private boolean collected;

        private ItemDisplay(SchoolItem item, int x, int y) {
            this.item = item;
            this.x = x;
            this.y = y;
        }

        public SchoolItem getItem() {
            return item;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isCollected() {
            return collected;
        }
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
        double previousPlayerX = player.getX();
        double previousPlayerY = player.getY();
        player.update(deltaSeconds);
        Exit exit = player.move(this, deltaSeconds);
        if (exit != Exit.NONE) {
            return exit;
        }

        updatePoisonTrail(previousPlayerX, previousPlayerY, deltaSeconds);
        collectItems();

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

        for (Antonio antonio : antonios) {
            antonio.update(deltaSeconds);
            antonio.approachIfOutOfRange(player, this, deltaSeconds);
            EnemyShot enemyShot = antonio.tryShoot(player);
            if (enemyShot != null) {
                enemyShots.add(enemyShot);
            }
        }

        player.updateCompanions(gilbertos, deltaSeconds);
        gilbertos.removeIf(gilberto -> gilberto.getHealth() == 0);

        Iterator<PoisonCloud> cloudIterator = poisonClouds.iterator();
        while (cloudIterator.hasNext()) {
            if (cloudIterator.next().update(gilbertos, deltaSeconds)) {
                cloudIterator.remove();
            }
        }

        Shot newShot = player.tryShoot();
        if (newShot != null) {
            shots.add(newShot);
            if (player.consumeCapoeiraSpin()) {
                player.showSpinningSlash();
                damageInRadius(player.getX(), player.getY(), 64,
                        (int) Math.round(player.getDamage() * 1.5));
            }
        }
        player.tryKatanaAttack(gilbertos);
        gilbertos.removeIf(gilberto -> gilberto.getHealth() == 0);
        Shot familiarShot = player.tryShootWithDrone(gilbertos, deltaSeconds);
        if (familiarShot != null) {
            shots.add(familiarShot);
        }

        Iterator<Shot> shotIterator = shots.iterator();
        while (shotIterator.hasNext()) {
            Shot shot = shotIterator.next();
            shot.update(deltaSeconds);
            if (shot.isOutside(this)
                    || isObstacleBlockedAt(shot.getX(), shot.getY(),
                            shot.getWidth(), shot.getHeight())) {
                shotIterator.remove();
                continue;
            }

            Iterator<Gilberto> gilbertoIterator = gilbertos.iterator();
            boolean hitEnemy = false;
            while (gilbertoIterator.hasNext()) {
                Gilberto gilberto = gilbertoIterator.next();
                if (overlaps(shot, gilberto)) {
                    gilberto.receiveShot(shot, player.getDamage(),
                            player.getSleepHitsPerEnemy(), player.getSleepDuration(),
                            player.getRootDuration());
                    shotIterator.remove();
                    if (gilberto.getHealth() == 0) {
                        gilbertoIterator.remove();
                    }
                    hitEnemy = true;
                    break;
                }
            }
            if (hitEnemy) {
                continue;
            }

            Iterator<Antonio> antonioIterator = antonios.iterator();
            while (antonioIterator.hasNext()) {
                Antonio antonio = antonioIterator.next();
                if (overlaps(shot, antonio)) {
                    antonio.receiveShot(shot, player.getDamage());
                    shotIterator.remove();
                    if (antonio.getHealth() == 0) {
                        antonioIterator.remove();
                    }
                    break;
                }
            }
        }

        Iterator<EnemyShot> enemyShotIterator = enemyShots.iterator();
        while (enemyShotIterator.hasNext()) {
            EnemyShot enemyShot = enemyShotIterator.next();
            enemyShot.update(deltaSeconds);
            if (enemyShot.isOutside(this)
                    || isObstacleBlockedAt(enemyShot.getX(), enemyShot.getY(),
                            enemyShot.getWidth(), enemyShot.getHeight())) {
                enemyShotIterator.remove();
            } else if (overlaps(enemyShot, player)) {
                player.takeDamage(enemyShot.getDamage());
                enemyShotIterator.remove();
            }
        }
        return Exit.NONE;
    }

    public void draw(Graphics2D graphics) {
        graphics.drawImage(background, 0, 0, null);

        for (PoisonCloud cloud : poisonClouds) {
            cloud.draw(graphics);
        }
        for (Gilberto gilberto : gilbertos) {
            gilberto.draw(graphics);
        }
        for (Antonio antonio : antonios) {
            antonio.draw(graphics);
        }
        for (ItemDisplay display : itemDisplays) {
            if (display.isCollected()) {
                continue;
            }
            if (type == RoomType.GALLERY) {
                drawGalleryItem(graphics, display);
            } else {
                display.getItem().draw(graphics, display.getX(), display.getY());
            }
        }
        for (Shot shot : shots) {
            shot.draw(graphics);
        }
        for (EnemyShot enemyShot : enemyShots) {
            enemyShot.draw(graphics);
        }
        player.draw(graphics);
    }

    public void activateItems() {
        player.activateItems(gilbertos);
        gilbertos.removeIf(gilberto -> gilberto.getHealth() == 0);
    }

    private void updatePoisonTrail(double previousX, double previousY,
            double deltaSeconds) {
        poisonSpawnCooldown = Math.max(0, poisonSpawnCooldown - deltaSeconds);
        if (player.hasPoisonTrail() && poisonSpawnCooldown == 0
                && Math.hypot(player.getX() - previousX,
                        player.getY() - previousY) >= 2) {
            poisonClouds.add(new PoisonCloud(player.getX(), player.getY()));
            poisonSpawnCooldown = 0.3;
        }
    }

    private void damageInRadius(double centerX, double centerY, double radius,
            int damage) {
        Iterator<Gilberto> iterator = gilbertos.iterator();
        while (iterator.hasNext()) {
            Gilberto gilberto = iterator.next();
            if (Math.hypot(gilberto.getX() - centerX,
                    gilberto.getY() - centerY) <= radius) {
                gilberto.takeDamage(damage);
                if (gilberto.getHealth() == 0) {
                    iterator.remove();
                }
            }
        }
    }

    private void collectItems() {
        double reachX = player.getWidth() / 2.0 + 16;
        double reachY = player.getHeight() / 2.0 + 16;
        int existingItemCount = itemDisplays.size();
        for (int index = 0; index < existingItemCount; index++) {
            ItemDisplay display = itemDisplays.get(index);
            if (display.isCollected()
                    || Math.abs(player.getX() - display.getX()) > reachX
                    || Math.abs(player.getY() - display.getY()) > reachY) {
                continue;
            }
            SchoolItem droppedItem = player.collectItem(display.getItem());
            display.collected = true;
            if (droppedItem != null) {
                int direction = player.getX() < width / 2.0 ? 1 : -1;
                double dropX = Math.max(WALL_THICKNESS + player.getWidth() / 2.0,
                        Math.min(width - WALL_THICKNESS - player.getWidth() / 2.0,
                                player.getX() + direction * 48));
                itemDisplays.add(new ItemDisplay(droppedItem,
                        (int) Math.round(dropX), (int) Math.round(player.getY())));
            }
        }
    }

    private void addGalleryItems(int roomWidth, int roomHeight) {
        List<SchoolItem> items = SchoolItem.allItems();
        int startX = roomWidth / 2
                - (GALLERY_COLUMNS - 1) * GALLERY_COLUMN_SPACING / 2;
        int rows = (items.size() + GALLERY_COLUMNS - 1) / GALLERY_COLUMNS;
        int gridHeight = (rows - 1) * GALLERY_ROW_SPACING;
        int firstRowY = (roomHeight - gridHeight) / 2 + GALLERY_TOP_OFFSET;
        for (int index = 0; index < items.size(); index++) {
            int column = index % GALLERY_COLUMNS;
            int row = index / GALLERY_COLUMNS;
            int itemsInRow = Math.min(GALLERY_COLUMNS,
                    items.size() - row * GALLERY_COLUMNS);
            int rowOffset = (GALLERY_COLUMNS - itemsInRow)
                    * GALLERY_COLUMN_SPACING / 2;
            itemDisplays.add(new ItemDisplay(items.get(index),
                    startX + column * GALLERY_COLUMN_SPACING + rowOffset,
                    firstRowY + row * GALLERY_ROW_SPACING));
        }
    }

    private void drawGalleryItem(Graphics2D graphics, ItemDisplay display) {
        int size = GALLERY_PEDESTAL_SIZE;
        graphics.drawImage(Sprites.PEDESTAL,
                display.getX() - size / 2, display.getY() - size / 2,
                size, size, null);
        display.getItem().draw(graphics, display.getX(),
                display.getY() - GALLERY_ITEM_LIFT, GALLERY_LABEL_OFFSET,
                GALLERY_LABEL_FONT_SIZE);
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
        drawChurchProps(graphics);
        if (type == RoomType.TREASURE) {
            drawTreasureDecor(graphics);
        }

        graphics.dispose();
        return image;
    }

    private void drawFloor(Graphics2D graphics) {
        int tileSize = Sprites.churchFloor(0).getWidth();
        for (int y = 0; y < height; y += tileSize) {
            for (int x = 0; x < width; x += tileSize) {
                BufferedImage tile = Sprites.churchFloor(visualRandom.nextInt(4));
                graphics.drawImage(tile, x, y, null);
            }
        }
    }

    private void createChurchProps() {
        if (type != RoomType.NORMAL || width < 360 || height < 300) {
            return;
        }

        int pewCount = 4 + visualRandom.nextInt(3);
        for (int index = 0; index < pewCount; index++) {
            addRandomChurchProp(0, 58, 12);
        }

        addRandomChurchProp(1, 24, 24);
        addRandomChurchProp(1, 24, 24);
        int detailCount = 2 + visualRandom.nextInt(3);
        for (int index = 0; index < detailCount; index++) {
            int frame = visualRandom.nextBoolean() ? 2 : 3;
            addRandomChurchProp(frame, 14, 14);
        }
    }

    private void addRandomChurchProp(int frame, int collisionWidth,
            int collisionHeight) {
        for (int attempt = 0; attempt < 80; attempt++) {
            Point2D.Double position = randomChurchPropPosition();
            double x = position.x;
            double y = position.y;
            if (isNearDoorway(x, y, collisionHeight)
                    || overlapsChurchProp(x, y, collisionWidth, collisionHeight)) {
                continue;
            }
            addChurchProp(frame, x, y, collisionWidth, collisionHeight);
            return;
        }
    }

    private Point2D.Double randomChurchPropPosition() {
        double wallInset = WALL_THICKNESS + 32;
        double minimumX = Math.min(wallInset, width / 2.0);
        double maximumX = Math.max(minimumX, width - wallInset);
        double minimumY = Math.min(wallInset, height / 2.0);
        double maximumY = Math.max(minimumY, height - wallInset);
        double greenMinimumX = Math.max(minimumX, width * 0.08);
        double greenMaximumX = Math.min(maximumX, width * 0.92);
        double region = visualRandom.nextDouble();
        double x;
        double y;

        if (region < CHURCH_PROP_CENTER_CHANCE) {
            x = randomBetween(greenMinimumX, greenMaximumX);
            y = randomBetween(height * 0.40, height * 0.60);
        } else if (region < CHURCH_PROP_CENTER_CHANCE
                + CHURCH_PROP_BORDER_CHANCE) {
            int edge = visualRandom.nextInt(4);
            if (edge < 2) {
                x = randomBetween(minimumX, maximumX);
                y = edge == 0
                        ? randomBetween(minimumY, Math.min(height * 0.12, maximumY))
                        : randomBetween(Math.min(maximumY,
                                Math.max(height * 0.88, minimumY)),
                                maximumY);
            } else {
                x = edge == 2
                        ? randomBetween(minimumX, Math.min(width * 0.12, maximumX))
                        : randomBetween(Math.min(maximumX,
                                Math.max(width * 0.88, minimumX)), maximumX);
                y = randomBetween(minimumY, maximumY);
            }
        } else {
            x = randomBetween(greenMinimumX, greenMaximumX);
            double bandStart = visualRandom.nextBoolean() ? 0.10 : 0.60;
            y = randomBetween(Math.max(minimumY, height * bandStart),
                    Math.min(maximumY, height * (bandStart + 0.30)));
        }
        return new Point2D.Double(x, y);
    }

    private double randomBetween(double minimum, double maximum) {
        if (maximum <= minimum) {
            return minimum;
        }
        return minimum + visualRandom.nextDouble() * (maximum - minimum);
    }

    private boolean isNearDoorway(double x, double y, int propHeight) {
        boolean nearLeftDoor = leftDoor && x < width * 0.25;
        boolean nearRightDoor = rightDoor && x > width * 0.75;
        return (nearLeftDoor || nearRightDoor)
                && Math.abs(y - height / 2.0)
                        < DOOR_HALF_HEIGHT + propHeight / 2.0;
    }

    private boolean overlapsChurchProp(double x, double y, int propWidth,
            int propHeight) {
        for (ChurchProp prop : churchProps) {
            if (Math.abs(x - prop.x)
                            < (propWidth + prop.collisionWidth) / 2.0
                                    + CHURCH_PROP_SPACING
                    && Math.abs(y - prop.y)
                            < (propHeight + prop.collisionHeight) / 2.0
                                    + CHURCH_PROP_SPACING) {
                return true;
            }
        }
        return false;
    }

    private void addChurchProp(int frame, double x, double y,
            int collisionWidth, int collisionHeight) {
        churchProps.add(new ChurchProp(frame, x, y,
                collisionWidth, collisionHeight));
    }

    private void drawChurchProps(Graphics2D graphics) {
        for (ChurchProp prop : churchProps) {
            BufferedImage sprite = Sprites.churchProp(prop.frame);
            graphics.drawImage(sprite,
                    (int) Math.round(prop.x - sprite.getWidth() / 2.0),
                    (int) Math.round(prop.y - sprite.getHeight() / 2.0),
                    null);
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

    private static final class ChurchProp {
        private final int frame;
        private final double x;
        private final double y;
        private final int collisionWidth;
        private final int collisionHeight;

        private ChurchProp(int frame, double x, double y,
                int collisionWidth, int collisionHeight) {
            this.frame = frame;
            this.x = x;
            this.y = y;
            this.collisionWidth = collisionWidth;
            this.collisionHeight = collisionHeight;
        }
    }
}
