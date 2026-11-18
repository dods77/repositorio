package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

@SuppressWarnings("serial")
public final class GamePanel extends JPanel {
    private static final int ROOM_WIDTH = 800;
    private static final int ROOM_HEIGHT = 600;

    private final Dungeon dungeon = new Dungeon(ROOM_WIDTH, ROOM_HEIGHT);
    private final Set<String> pressedKeys = new HashSet<>();
    private final Timer timer;
    private boolean mouseShooting;
    private int mouseX;
    private int mouseY;
    private long previousFrameNanos;

    public GamePanel() {
        setPreferredSize(new Dimension(ROOM_WIDTH, ROOM_HEIGHT));
        setFocusable(true);
        installControls();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    mouseShooting = true;
                    updateMousePosition(event);
                    requestFocusInWindow();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    mouseShooting = false;
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                updateMousePosition(event);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                updateMousePosition(event);
            }
        });

        previousFrameNanos = System.nanoTime();
        timer = new Timer(16, event -> tick());
        timer.start();
    }

    private void tick() {
        long now = System.nanoTime();
        double deltaSeconds = Math.min((now - previousFrameNanos) / 1_000_000_000.0, 0.05);
        previousFrameNanos = now;

        PlayerCharacter player = dungeon.getPlayer();
        double horizontal = (isPressed("D") ? 1 : 0) - (isPressed("A") ? 1 : 0);
        double vertical = (isPressed("S") ? 1 : 0) - (isPressed("W") ? 1 : 0);
        player.setMovement(horizontal, vertical);

        double aimX;
        double aimY;
        if (mouseShooting) {
            aimX = mouseX - player.getX();
            aimY = mouseY - player.getY();
        } else {
            aimX = (isPressed("RIGHT") ? 1 : 0) - (isPressed("LEFT") ? 1 : 0);
            aimY = (isPressed("DOWN") ? 1 : 0) - (isPressed("UP") ? 1 : 0);
        }
        boolean shooting = mouseShooting || aimX != 0 || aimY != 0;
        if (shooting) {
            player.setAim(aimX, aimY);
        }
        player.setShooting(shooting);
        dungeon.update(deltaSeconds);
        repaint();
    }

    private boolean isPressed(String key) {
        return pressedKeys.contains(key);
    }

    private void updateMousePosition(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    private void installControls() {
        bindKey("pressed W", "W", true);
        bindKey("released W", "W", false);
        bindKey("pressed A", "A", true);
        bindKey("released A", "A", false);
        bindKey("pressed S", "S", true);
        bindKey("released S", "S", false);
        bindKey("pressed D", "D", true);
        bindKey("released D", "D", false);
        bindKey("pressed UP", "UP", true);
        bindKey("released UP", "UP", false);
        bindKey("pressed LEFT", "LEFT", true);
        bindKey("released LEFT", "LEFT", false);
        bindKey("pressed DOWN", "DOWN", true);
        bindKey("released DOWN", "DOWN", false);
        bindKey("pressed RIGHT", "RIGHT", true);
        bindKey("released RIGHT", "RIGHT", false);
        bindKey("pressed SPACE", "SPACE", true);
        bindKey("released SPACE", "SPACE", false);
    }

    private void bindKey(String keyStroke, String key, boolean pressed) {
        bind(keyStroke, keyStroke, () -> {
            if (pressed) {
                boolean newlyPressed = pressedKeys.add(key);
                if (newlyPressed && "SPACE".equals(key)) {
                    dungeon.activateItems();
                }
            } else {
                pressedKeys.remove(key);
            }
        });
    }

    private void bind(String keyStroke, String actionName, Runnable action) {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        dungeon.draw(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Little Room", 16, 26);

        Room currentRoom = dungeon.getCurrentRoom();
        String roomText = currentRoom.getType() == RoomType.GALLERY
                ? currentRoom.getType().getDisplayName()
                : "Room " + dungeon.getCurrentRoomNumber()
                        + " / " + dungeon.getNumberedRoomCount();
        int roomTextWidth = g.getFontMetrics().stringWidth(roomText);
        g.drawString(roomText, ROOM_WIDTH - 16 - roomTextWidth, 26);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString("Move: WASD    Shoot: arrows or hold left mouse    Active item: Space",
            16, 48);
        if (dungeon.getCurrentRoomType() == RoomType.TREASURE) {
            g.setColor(new Color(235, 205, 125));
            String treasureText = dungeon.getCurrentRoomType().getDisplayName();
            int treasureTextWidth = g.getFontMetrics().stringWidth(treasureText);
            g.drawString(treasureText, ROOM_WIDTH - 16 - treasureTextWidth, 48);
            g.setColor(Color.WHITE);
        }
        g.drawString("Shots in room: " + dungeon.getShotCount(), 16, 68);
        PlayerCharacter player = dungeon.getPlayer();
        SchoolItem highlightedItem = currentRoom.getTreasureItem();
        boolean itemCollected = currentRoom.isTreasureItemCollected();
        if (highlightedItem == null) {
            Room.ItemDisplay nearest = currentRoom.getNearestItem(
                    player.getX(), player.getY(), 100);
            if (nearest != null) {
                highlightedItem = nearest.getItem();
            }
        }
        if (highlightedItem != null) {
            g.setColor(highlightedItem.getRarityColor());
            String itemText = (itemCollected ? "Obtido: " : "Pegue: ")
                    + highlightedItem.getDisplayName() + " ("
                    + highlightedItem.getRarityName() + "): "
                    + highlightedItem.getDescription()
                    + (itemCollected ? "" : " — encoste para pegar");
            g.drawString(itemText, 16, 142);
            g.setColor(Color.WHITE);
        }
        int barX = 16;
        int barY = 82;
        int barWidth = Sprites.HUD_BAR_BACKGROUND.getWidth();
        int barHeight = Sprites.HUD_BAR_BACKGROUND.getHeight();
        g.drawImage(Sprites.HUD_BAR_BACKGROUND, barX, barY, null);
        int healthWidth = barWidth * player.getHealth() / player.getMaxHealth();
        if (healthWidth > 0) {
            g.drawImage(Sprites.HUD_BAR_FILL,
                    barX, barY, barX + healthWidth, barY + barHeight,
                    0, 0, healthWidth, barHeight, null);
        }
        g.setColor(Color.WHITE);
        g.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(),
                barX + barWidth + 8, barY + 12);
        g.drawString("Speed: " + player.getMoveSpeed()
                + "    Damage: " + player.getDamage()
                + "    Attack: " + player.getAttackSpeed() + " shots/s",
                16, 118);
        drawItemCooldowns(g, player);
        g.dispose();
    }

    private void drawItemCooldowns(Graphics2D graphics, PlayerCharacter player) {
        int x = 16;
        int y = 164;
        int barWidth = 140;
        int barHeight = 10;
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (PlayerCharacter.ItemCooldown cooldown : player.getActiveItemCooldowns()) {
            graphics.setColor(Color.WHITE);
            graphics.drawString(cooldown.name(), x, y + barHeight - 1);
            int currentBarX = x + 72;
            graphics.setColor(new Color(65, 65, 65));
            graphics.fillRect(currentBarX, y, barWidth, barHeight);
            graphics.setColor(cooldown.progress() >= 1
                    ? new Color(80, 205, 110)
                    : new Color(85, 155, 220));
            graphics.fillRect(currentBarX, y,
                    (int) Math.round(barWidth * cooldown.progress()), barHeight);
            graphics.setColor(Color.WHITE);
            graphics.drawRect(currentBarX, y, barWidth, barHeight);
            y += 17;
        }
    }
}
