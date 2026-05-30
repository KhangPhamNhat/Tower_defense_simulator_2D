import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;

public class SpriteManager {

    // Frame configs: how many frames each animation has
    private static final HashMap<String, Integer> FRAME_COUNT = new HashMap<>() {{
        // Enemies
        put("goblin",        1);
        put("goblin_idle",   1);
        put("goblin_run",    1);
        put("orc_idle",      18);
        put("orc_run",       24);
        put("orc_death",     30);

        put("troll_idle",    6);
        put("troll_run",     8);
        put("troll_death",   4);

        // Bosses
        put("boss1_idle",    8);
        put("boss1_run",     8);
        put("boss1_death",   8);

        put("boss2_idle",    6);
        put("boss2_run",     12);
        put("boss2_death",   22);

        put("boss3_idle",    8);
        put("boss3_run",     8);
        put("boss3_death",   10);

        // Towers (attack animation)
        put("archer",        8);
        put("cannon",        4);
        put("freeze",        5);
        put("mage",          11);
        put("sniper",        6);
    }};

    // Speed: frames per tick to advance (higher = slower animation)
    private static final int ANIM_SPEED = 6;

    private static int tick = 0;
    private static final HashMap<String, BufferedImage>   imgCache   = new HashMap<>();
    private static final HashMap<String, BufferedImage[]> frameCache = new HashMap<>();

    // ── Call once per game frame ──────────────────────────────────────────
    public static void tick() {
        tick++;
    }

    // ── Get animated frame (auto-cycles) ─────────────────────────────────
    public static BufferedImage getFrame(String name) {
        if (!FRAME_COUNT.containsKey(name)) {
            return get(name + ".png"); // fallback to static
        }
        int count = FRAME_COUNT.get(name);
        if (count <= 1) return get(name + ".png");

        // Load frames if not cached
        if (!frameCache.containsKey(name)) {
            BufferedImage[] frames = new BufferedImage[count];
            for (int i = 0; i < count; i++) {
                frames[i] = get(name + "_" + i + ".png");
            }
            frameCache.put(name, frames);
        }

        int frame = (tick / ANIM_SPEED) % count;
        BufferedImage[] frames = frameCache.get(name);
        return frames[frame] != null ? frames[frame] : frames[0];
    }

    // ── Get static image ──────────────────────────────────────────────────
    public static BufferedImage get(String filename) {
        if (imgCache.containsKey(filename)) return imgCache.get(filename);
        try {
            InputStream is = SpriteManager.class.getResourceAsStream("/sprites/" + filename);
            if (is == null) {
                imgCache.put(filename, null);
                return null;
            }
            BufferedImage img = ImageIO.read(is);
            imgCache.put(filename, img);
            return img;
        } catch (Exception e) {
            imgCache.put(filename, null);
            return null;
        }
    }

    // ── Helper: get enemy animation based on state ────────────────────────
    public static BufferedImage getEnemyFrame(Enemy enemy) {
        String base;
        if      (enemy instanceof BossThree) base = "boss3";
        else if (enemy instanceof BossTwo)   base = "boss2";
        else if (enemy instanceof BossOne)   base = "boss1";
        else if (enemy instanceof Boss)      base = "boss1";
        else if (enemy instanceof Troll)     base = "troll";
        else if (enemy instanceof Orc)       base = "orc";
        else                                  base = "goblin";

        // Pick animation state
        String anim;
        if (enemy.isDead())        anim = base + "_death";
        else if (enemy.getHp() < enemy.getMaxHp() * 0.3) anim = base + "_run"; // low hp = running fast feeling
        else                       anim = base + "_run";

        BufferedImage frame = getFrame(anim);
        if (frame == null) frame = getFrame(base + "_idle");
        if (frame == null) frame = get(base + ".png");
        return frame;
    }

    // ── Helper: get tower attack frame ────────────────────────────────────
    public static BufferedImage getTowerFrame(Tower tower) {
        String name = tower.getName().toLowerCase();
        BufferedImage frame = getFrame(name);
        if (frame == null) frame = get(name + ".png");
        return frame;
    }

    public static int getTick() { return tick; }
}

