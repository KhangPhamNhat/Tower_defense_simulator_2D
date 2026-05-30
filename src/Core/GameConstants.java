public class GameConstants {
    //==Man Hinh==
    public static final int TILE_SIZE = 64;
    public static final int GRID_COLS = 18;
    public static final int GRID_ROWS = 10;
    public static final int SCREEN_WIDTH = TILE_SIZE * GRID_COLS;
    public static final int SCREEN_HEIGHT = TILE_SIZE * GRID_ROWS;
    //==Game loop==
    public static final int FPS = 60;
    public static final long FRAME_TIME = 1000/FPS;
    //==tile type==
    public static final int GRASS = 0;
    public static final int PATH = 1;
    public static final int BASE = 2;
    public static final int SPAWN = 3;
    //==WAVE==
    public static final int WAVE_PER_STAGE = 15;
    public static final int STAGE_PER_MAP = 3;
    public static final int WAVE_COUNTDOWN = 20;
    //==currency==
    public static final int START_COINS = 200;
    public static final int START_GEMS = 0;
    public static final int BASE_HP = 50;
    //==game mode==
    public static final int MODE_STORY = 0;
    public static final int MODE_INF = 1;
    public static final int MODE_ONLINE = 2;
    //==dificulty==
    public static final int DIFF_EASY = 0;
    public static final int DIFF_NORMAL = 1;
    public static final int DIFF_HARD = 2;
    public static final double[] DIFF_MULTIPLIERS = {0.5,1.0,1.5};
    //=== tower ===
    public static final int TOWER_MAX_LEVEL = 2;
    public static final int MAX_TOWERS = 10;
    //==boss==
    public static final int BOSS_COUNT = 3;

}
