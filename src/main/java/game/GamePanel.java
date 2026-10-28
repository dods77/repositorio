package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
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

    private final Room room = new Room(ROOM_WIDTH, ROOM_HEIGHT);
    private final Set<String> pressedKeys = new HashSet<>();
    private final Timer timer;
    private long previousFrameNanos;

    public GamePanel() {
        setPreferredSize(new Dimension(ROOM_WIDTH, ROOM_HEIGHT));
        setFocusable(true);
        installControls();

        previousFrameNanos = System.nanoTime();
        timer = new Timer(16, event -> tick());
        timer.start();
    }

    private void tick() {
        long now = System.nanoTime();
        double deltaSeconds = Math.min((now - previousFrameNanos) / 1_000_000_000.0, 0.05);
        previousFrameNanos = now;

        PlayerCharacter player = room.getPlayer();
        double horizontal = (isPressed("D") ? 1 : 0) - (isPressed("A") ? 1 : 0);
        double vertical = (isPressed("S") ? 1 : 0) - (isPressed("W") ? 1 : 0);
        player.setMovement(horizontal, vertical);

        double aimX = (isPressed("RIGHT") ? 1 : 0) - (isPressed("LEFT") ? 1 : 0);
        double aimY = (isPressed("DOWN") ? 1 : 0) - (isPressed("UP") ? 1 : 0);
        boolean shooting = aimX != 0 || aimY != 0;
        if (shooting) {
            player.setAim(aimX, aimY);
        }
        player.setShooting(shooting);
        room.update(deltaSeconds);
        repaint();
    }

    private boolean isPressed(String key) {
        return pressedKeys.contains(key);
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
    }

    private void bindKey(String keyStroke, String key, boolean pressed) {
        bind(keyStroke, keyStroke, () -> {
            if (pressed) {
                pressedKeys.add(key);
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

        room.draw(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Little Room", 16, 26);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString("Move: WASD    Hold arrow keys to shoot", 16, 48);
        g.drawString("Shots in room: " + room.getShotCount(), 16, 68);
        PlayerCharacter player = room.getPlayer();
        int barX = 16;
        int barY = 82;
        int barWidth = 160;
        int barHeight = 14;
        g.setColor(new Color(55, 35, 40));
        g.fillRect(barX, barY, barWidth, barHeight);
        g.setColor(new Color(205, 65, 75));
        g.fillRect(barX, barY,
                barWidth * player.getHealth() / player.getMaxHealth(), barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
        g.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(),
                barX + barWidth + 8, barY + 12);
        g.drawString("Speed: " + player.getMoveSpeed()
                + "    Damage: " + player.getDamage()
                + "    Attack: " + player.getAttackSpeed() + " shots/s",
                16, 118);
        g.dispose();
    }
}
