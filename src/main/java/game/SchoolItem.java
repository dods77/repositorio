package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class SchoolItem {
    private final String displayName;
    private final Rarity rarity;
    private final String description;
    private final int damageBonus;
    private final double moveSpeedBonus;
    private final double attackSpeedBonus;
    private final double damageMultiplier;
    private final double moveSpeedMultiplier;
    private final double attackSpeedMultiplier;
    private final int maxHealthChange;
    private final boolean backpackCosmetic;

    protected SchoolItem(String displayName, Rarity rarity, String description,
            int damageBonus, double moveSpeedBonus, double attackSpeedBonus) {
        this(displayName, rarity, description, damageBonus, moveSpeedBonus,
                attackSpeedBonus, 1, 1, 1, 0, false);
    }

    protected SchoolItem(String displayName, Rarity rarity, String description,
            int damageBonus, double moveSpeedBonus, double attackSpeedBonus,
            double damageMultiplier, double moveSpeedMultiplier,
            double attackSpeedMultiplier, int maxHealthChange,
            boolean backpackCosmetic) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.description = description;
        this.damageBonus = damageBonus;
        this.moveSpeedBonus = moveSpeedBonus;
        this.attackSpeedBonus = attackSpeedBonus;
        this.damageMultiplier = damageMultiplier;
        this.moveSpeedMultiplier = moveSpeedMultiplier;
        this.attackSpeedMultiplier = attackSpeedMultiplier;
        this.maxHealthChange = maxHealthChange;
        this.backpackCosmetic = backpackCosmetic;
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

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public double getMoveSpeedMultiplier() {
        return moveSpeedMultiplier;
    }

    public double getAttackSpeedMultiplier() {
        return attackSpeedMultiplier;
    }

    public int getMaxHealthChange() {
        return maxHealthChange;
    }

    public boolean hasBackpackCosmetic() {
        return backpackCosmetic;
    }

    public Color getRarityColor() {
        return rarity.color;
    }

    public static List<SchoolItem> allItems() {
        return List.of(
                new CanetaItem(), new LapisItem(), new BorrachaItem(),
                new MalaItem(), new GorduraItem(), new JoelhoFudidoItem(),
                new OculosItem(), new PantufasItem(),
                new ReguaItem(), new CadernoItem(),
                new LasagnaPingodoceItem(), new HalteresItem(),
                new KatanaItem(), new CabeloItem(), new CapoeiraItem(),
                new MalagetasItem(), new FamiliarIaItem(),
                new EstojoItem(), new CalculadoraItem(),
                new PistolaDoDredItem(), new TokenPirataItem(),
                new SqlInjectionItem(), new FamiliarDroneItem(),
                new RazorbladeItem(), new FonesItem(), new BolsoItem(),
                new MochilaItem());
    }

    public static SchoolItem randomTreasureItem() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 50) {
            return randomOf(new CanetaItem(), new LapisItem(), new BorrachaItem(),
                    new MalaItem(), new GorduraItem(), new JoelhoFudidoItem(),
                    new OculosItem(), new PantufasItem());
        }
        if (roll < 80) {
            return randomOf(new ReguaItem(), new CadernoItem(),
                    new LasagnaPingodoceItem(), new HalteresItem(),
                    new KatanaItem(), new CabeloItem(), new CapoeiraItem(),
                    new MalagetasItem(), new FamiliarIaItem());
        }
        if (roll < 95) {
            return randomOf(new EstojoItem(), new CalculadoraItem(),
                    new PistolaDoDredItem(), new TokenPirataItem(),
                    new SqlInjectionItem(), new FamiliarDroneItem(),
                    new RazorbladeItem(), new FonesItem(), new BolsoItem());
        }
        return new MochilaItem();
    }

    public void draw(Graphics2D graphics, int centerX, int centerY) {
        draw(graphics, centerX, centerY, 31, 12);
    }

    public void draw(Graphics2D graphics, int centerX, int centerY,
            int labelBaselineOffset) {
        draw(graphics, centerX, centerY, labelBaselineOffset, 12);
    }

    public void draw(Graphics2D graphics, int centerX, int centerY,
            int labelBaselineOffset, int labelFontSize) {
        graphics.setColor(rarity.color);
        graphics.fillOval(centerX - 16, centerY - 16, 32, 32);
        graphics.setColor(Color.WHITE);
        graphics.drawOval(centerX - 16, centerY - 16, 32, 32);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        String initial = displayName.substring(0, 1);
        int initialWidth = graphics.getFontMetrics().stringWidth(initial);
        graphics.drawString(initial, centerX - initialWidth / 2, centerY + 5);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, labelFontSize));
        int nameWidth = graphics.getFontMetrics().stringWidth(displayName);
        graphics.drawString(displayName, centerX - nameWidth / 2,
                centerY + labelBaselineOffset);
    }

    public void applySpecialEffect(PlayerCharacter player) {
    }

    public boolean isActivatable() {
        return false;
    }

    public double getCooldownSeconds() {
        return 0;
    }

    public void activate(PlayerCharacter player, List<Gilberto> enemies) {
    }

    private static SchoolItem randomOf(SchoolItem... items) {
        return items[ThreadLocalRandom.current().nextInt(items.length)];
    }

    public static final class CanetaItem extends SchoolItem {
        public CanetaItem() {
            super("Caneta", Rarity.COMUM, "+2 dano", 2, 0, 0);
        }
    }

    public static final class LapisItem extends SchoolItem {
        public LapisItem() {
            super("Lápis", Rarity.COMUM, "+10 velocidade", 0, 10, 0);
        }
    }

    public static final class BorrachaItem extends SchoolItem {
        public BorrachaItem() {
            super("Borracha", Rarity.COMUM, "+0,5 velocidade de ataque", 0, 0, 0.5);
        }
    }

    public static final class ReguaItem extends SchoolItem {
        public ReguaItem() {
            super("Régua", Rarity.INCOMUM, "+3 dano", 3, 0, 0);
        }
    }

    public static final class CadernoItem extends SchoolItem {
        public CadernoItem() {
            super("Caderno", Rarity.INCOMUM, "+20 velocidade", 0, 20, 0);
        }
    }

    public static final class EstojoItem extends SchoolItem {
        public EstojoItem() {
            super("Estojo", Rarity.RARO, "+4 dano e +0,5 velocidade de ataque", 4, 0, 0.5);
        }
    }

    public static final class CalculadoraItem extends SchoolItem {
        public CalculadoraItem() {
            super("Calculadora", Rarity.RARO, "+1 dano e +1 velocidade de ataque", 1, 0, 1);
        }
    }

    public static final class MochilaItem extends SchoolItem {
        public MochilaItem() {
            super("Mochila", Rarity.LENDARIO, "+5 dano, +20 velocidade e +1 ataque",
                    5, 20, 1);
        }
    }

    public static final class MalaItem extends SchoolItem {
        public MalaItem() {
            super("Mala", Rarity.COMUM, "Cosmético: mochila decorativa",
                    0, 0, 0, 1, 1, 1, 0, true);
        }
    }

    public static final class GorduraItem extends SchoolItem {
        public GorduraItem() {
            super("Gordura", Rarity.COMUM, "+20 HP máximo, -10% velocidade",
                    0, 0, 0, 1, 0.9, 1, 20, false);
        }
    }

    public static final class JoelhoFudidoItem extends SchoolItem {
        public JoelhoFudidoItem() {
            super("Joelho fudido", Rarity.COMUM, "+25% dano, -10% velocidade",
                    0, 0, 0, 1.25, 0.9, 1, 0, false);
        }
    }

    public static final class LasagnaPingodoceItem extends SchoolItem {
        public LasagnaPingodoceItem() {
            super("Lasagna pingodoçe", Rarity.INCOMUM, "+50% dano, -20 HP máximo",
                    0, 0, 0, 1.5, 1, 1, -20, false);
        }
    }

    public static final class HalteresItem extends SchoolItem {
        public HalteresItem() {
            super("Halteres", Rarity.INCOMUM, "+50% dano",
                    0, 0, 0, 1.5, 1, 1, 0, false);
        }
    }

    public static final class PistolaDoDredItem extends SchoolItem {
        public PistolaDoDredItem() {
            super("Pistola do Dred", Rarity.RARO,
                    "2x dano, 0,6x velocidade de ataque",
                    0, 0, 0, 2, 1, 0.6, 0, false);
        }
    }

    public static final class TokenPirataItem extends SchoolItem {
        public TokenPirataItem() {
            super("Token pirata", Rarity.RARO, "Raro; sem efeito",
                    0, 0, 0, 1, 1, 1, 0, false);
        }
    }

    public static final class OculosItem extends SchoolItem {
        public OculosItem() {
            super("Óculos", Rarity.COMUM, "+50% tamanho da bala, -10% velocidade, -20 HP",
                    0, 0, 0, 1, 0.9, 1, -20, false);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.increaseProjectileSize(1.5);
        }
    }

    public static final class PantufasItem extends SchoolItem {
        public PantufasItem() {
            super("Pantufas", Rarity.COMUM, "3 tiros adormecem o inimigo por 2 segundos",
                    0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableSleepShots(3, 2);
        }
    }

    public static final class SqlInjectionItem extends SchoolItem {
        public SqlInjectionItem() {
            super("SQL Injection", Rarity.RARO,
                    "10% de chance de disparar uma seringa que derrota o inimigo",
                    0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableSyringeShots(0.10);
        }
    }

    public static final class FamiliarDroneItem extends SchoolItem {
        public FamiliarDroneItem() {
            super("Familiar drone", Rarity.RARO,
                    "Drone orbital que dispara contra inimigos", 0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableDroneFamiliar();
        }
    }

    public static final class KatanaItem extends SchoolItem {
        public KatanaItem() {
            super("Katana", Rarity.INCOMUM,
                    "Substitui tiros por golpes de katana; use as setas para atacar",
                    0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableKatana();
        }
    }

    public static final class RazorbladeItem extends SchoolItem {
        public RazorbladeItem() {
            super("Razorblade", Rarity.RARO,
                    "+1 velocidade de ataque; ative para cortar à sua frente com custo de 5 HP",
                    0, 0, 1);
        }

        @Override
        public boolean isActivatable() {
            return true;
        }

        @Override
        public double getCooldownSeconds() {
            return 5;
        }

        @Override
        public void activate(PlayerCharacter player, List<Gilberto> enemies) {
            player.useRazorblade(enemies);
        }
    }

    public static final class FonesItem extends SchoolItem {
        public FonesItem() {
            super("Fones", Rarity.RARO, "Ative para causar uma onda de choque ao redor",
                    0, 0, 0);
        }

        @Override
        public boolean isActivatable() {
            return true;
        }

        @Override
        public double getCooldownSeconds() {
            return 8;
        }

        @Override
        public void activate(PlayerCharacter player, List<Gilberto> enemies) {
            player.useShockwave(enemies);
        }
    }

    public static final class BolsoItem extends SchoolItem {
        public BolsoItem() {
            super("Bolso", Rarity.RARO,
                    "Ative para receber um item aleatório (recarga: 15 s)",
                    0, 0, 0);
        }

        @Override
        public boolean isActivatable() {
            return true;
        }

        @Override
        public double getCooldownSeconds() {
            return 15;
        }

        @Override
        public void activate(PlayerCharacter player, List<Gilberto> enemies) {
            player.useRandomItemPocket();
        }
    }

    public static final class CabeloItem extends SchoolItem {
        public CabeloItem() {
            super("Cabelo", Rarity.INCOMUM,
                    "Tiros prendem inimigos por 2 segundos", 0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableRootShots(2);
        }
    }

    public static final class CapoeiraItem extends SchoolItem {
        public CapoeiraItem() {
            super("Capoeira", Rarity.INCOMUM,
                    "Cada 3 tiros causa um ataque giratório corpo a corpo",
                    0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableCapoeira();
        }
    }

    public static final class MalagetasItem extends SchoolItem {
        public MalagetasItem() {
            super("Malagetas", Rarity.INCOMUM,
                    "Deixa nuvens de veneno ao andar", 0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enablePoisonTrail();
        }
    }

    public static final class FamiliarIaItem extends SchoolItem {
        public FamiliarIaItem() {
            super("Familiar de IA", Rarity.INCOMUM,
                    "Gato familiar que segue e ataca inimigos próximos",
                    0, 0, 0);
        }

        @Override
        public void applySpecialEffect(PlayerCharacter player) {
            player.enableCatFamiliar();
        }
    }

    public static SchoolItem randomPocketItem() {
        return randomOf(new CanetaItem(), new LapisItem(), new BorrachaItem(),
                new ReguaItem(), new CadernoItem(), new EstojoItem(),
                new CalculadoraItem(), new MochilaItem(), new GorduraItem(),
                new JoelhoFudidoItem(), new LasagnaPingodoceItem(),
                new HalteresItem(), new OculosItem(), new PantufasItem(),
                new SqlInjectionItem(), new FamiliarDroneItem());
    }

    protected enum Rarity {
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
