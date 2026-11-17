package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

public enum SchoolItem {
    CANETA("Caneta", Rarity.COMUM, "+2 dano", 2, 0, 0),
    LAPIS("Lápis", Rarity.COMUM, "+10 velocidade", 0, 10, 0),
    BORRACHA("Borracha", Rarity.COMUM, "+0,5 velocidade de ataque", 0, 0, 0.5),
    REGUA("Régua", Rarity.INCOMUM, "+3 dano", 3, 0, 0),
    CADERNO("Caderno", Rarity.INCOMUM, "+20 velocidade", 0, 20, 0),
    ESTOJO("Estojo", Rarity.RARO, "+4 dano e +0,5 velocidade de ataque", 4, 0, 0.5),
    CALCULADORA("Calculadora", Rarity.RARO, "+1 dano e +1 velocidade de ataque", 1, 0, 1),
    MOCHILA("Mochila", Rarity.LENDARIO, "+5 dano, +20 velocidade e +1 ataque", 5, 20, 1);

    private final String displayName;
    private final Rarity rarity;
    private final String description;
    private final int damageBonus;
    private final double moveSpeedBonus;
    private final double attackSpeedBonus;

    SchoolItem(String displayName, Rarity rarity, String description,
            int damageBonus, double moveSpeedBonus, double attackSpeedBonus) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.description = description;
        this.damageBonus = damageBonus;
        this.moveSpeedBonus = moveSpeedBonus;
        this.attackSpeedBonus = attackSpeedBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRarityName() {
        return rarity.displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getDamageBonus() {
        return damageBonus;
    }

    public double getMoveSpeedBonus() {
        return moveSpeedBonus;
    }

    public double getAttackSpeedBonus() {
        return attackSpeedBonus;
    }

    public Color getRarityColor() {
        return rarity.color;
    }

    public static SchoolItem randomTreasureItem() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 50) {
            return randomOf(CANETA, LAPIS, BORRACHA);
        }
        if (roll < 80) {
            return randomOf(REGUA, CADERNO);
        }
        if (roll < 95) {
            return randomOf(ESTOJO, CALCULADORA);
        }
        return MOCHILA;
    }

    public void draw(Graphics2D graphics, int centerX, int centerY) {
        graphics.setColor(rarity.color);
        graphics.fillOval(centerX - 16, centerY - 16, 32, 32);
        graphics.setColor(Color.WHITE);
        graphics.drawOval(centerX - 16, centerY - 16, 32, 32);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        String initial = displayName.substring(0, 1);
        int initialWidth = graphics.getFontMetrics().stringWidth(initial);
        graphics.drawString(initial, centerX - initialWidth / 2, centerY + 5);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        int nameWidth = graphics.getFontMetrics().stringWidth(displayName);
        graphics.drawString(displayName, centerX - nameWidth / 2, centerY + 31);
    }

    private static SchoolItem randomOf(SchoolItem... items) {
        return items[ThreadLocalRandom.current().nextInt(items.length)];
    }

    private enum Rarity {
        COMUM("Comum", new Color(165, 165, 165)),
        INCOMUM("Incomum", new Color(90, 190, 110)),
        RARO("Raro", new Color(85, 145, 235)),
        LENDARIO("Lendário", new Color(225, 175, 65));

        private final String displayName;
        private final Color color;

        Rarity(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }
    }
}
