package game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

final class Sprites {
    private static final Path SOURCE_SPRITES =
            Path.of("src", "main", "resources", "sprites");

    static final BufferedImage FLOOR_NORMAL = load("floor_normal.png");
    static final BufferedImage FLOOR_TREASURE = load("floor_treasure.png");
    static final BufferedImage WALL_HORIZONTAL = load("wall_horizontal.png");
    static final BufferedImage WALL_VERTICAL = load("wall_vertical.png");
    static final BufferedImage DOOR = load("door.png");
    static final BufferedImage PEDESTAL = load("pedestal.png");
    static final BufferedImage TREASURE_RUG = load("treasure_rug.png");
    static final BufferedImage SHOT = load("shot.png");
    static final BufferedImage HUD_BAR_BACKGROUND = load("hud_bar_background.png");
    static final BufferedImage HUD_BAR_FILL = load("hud_bar_fill.png");

    private static final Map<Facing, BufferedImage[]> PLAYER_SPRITES =
            new EnumMap<>(Facing.class);
    private static final Map<Facing, BufferedImage[]> ENEMY_SPRITES =
            new EnumMap<>(Facing.class);

    static {
        PLAYER_SPRITES.put(Facing.DOWN, new BufferedImage[] {
                load("player_down_0.png"),
                load("player_down_1.png")
        });
        PLAYER_SPRITES.put(Facing.UP, new BufferedImage[] {
                load("player_up_0.png"),
                load("player_up_1.png")
        });
        PLAYER_SPRITES.put(Facing.LEFT, new BufferedImage[] {
                load("player_left_0.png"),
                load("player_left_1.png")
        });
        PLAYER_SPRITES.put(Facing.RIGHT, new BufferedImage[] {
                load("player_right_0.png"),
                load("player_right_1.png")
        });

        ENEMY_SPRITES.put(Facing.DOWN, new BufferedImage[] {
                load("enemy_down_0.png"),
                load("enemy_down_1.png")
        });
        ENEMY_SPRITES.put(Facing.UP, new BufferedImage[] {
                load("enemy_up_0.png"),
                load("enemy_up_1.png")
        });
        ENEMY_SPRITES.put(Facing.LEFT, new BufferedImage[] {
                load("enemy_left_0.png"),
                load("enemy_left_1.png")
        });
        ENEMY_SPRITES.put(Facing.RIGHT, new BufferedImage[] {
                load("enemy_right_0.png"),
                load("enemy_right_1.png")
        });
    }

    private Sprites() {
    }

    static BufferedImage player(Facing facing, int frame) {
        BufferedImage[] frames = PLAYER_SPRITES.get(facing);
        return frames[Math.floorMod(frame, frames.length)];
    }

    static BufferedImage enemy(Facing facing, int frame) {
        BufferedImage[] frames = ENEMY_SPRITES.get(facing);
        return frames[Math.floorMod(frame, frames.length)];
    }

    private static BufferedImage load(String name) {
        String path = "/sprites/" + name;
        try (InputStream stream = open(name)) {
            if (stream == null) {
                throw new IllegalStateException("Missing sprite " + path
                        + ". Add it to src/main/resources/sprites.");
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Unsupported sprite format: " + path);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load sprite " + path, exception);
        }
    }

    private static InputStream open(String name) throws IOException {
        InputStream stream = Sprites.class.getResourceAsStream("/sprites/" + name);
        if (stream != null) {
            return stream;
        }

        Path sourceFile = SOURCE_SPRITES.resolve(name);
        if (Files.isRegularFile(sourceFile)) {
            return Files.newInputStream(sourceFile);
        }
        return null;
    }
}
