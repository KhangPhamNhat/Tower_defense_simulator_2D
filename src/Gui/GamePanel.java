import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.io.InputStream;
public class GamePanel extends JPanel {
    private GameEngine gameEngine;
    private java.util.Map<String, BufferedImage> sprites = new HashMap<>();
    public GamePanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        loadSprites();
        setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMap(g);
        drawTowers(g);
        drawEnemies(g);
        drawProjectiles(g);
    }

    private void drawMap(Graphics g) {
        int[][] grid = gameEngine.getMap().getGrid();
        int size = GameConstants.TILE_SIZE;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                int tileType = grid[i][j];
                int x = j * size;
                int y = i * size;

                // 1. LAYER NỀN: LUÔN VẼ CỎ CHO MỌI Ô
                BufferedImage grassImg = sprites.get("grass");
                if (grassImg != null) {
                    g.drawImage(grassImg, x, y, size, size, null);
                } else {
                    g.setColor(new Color(163, 206, 39));
                    g.fillRect(x, y, size, size);
                }

                // 2. LAYER GIỮA: VẼ ĐƯỜNG ĐẤT CHO PATH, BASE VÀ SPAWN
                // Hợp nhất điều kiện: Cứ là 1 trong 3 loại này thì lót đất xuống trước
                if (tileType == GameConstants.PATH || tileType == GameConstants.BASE || tileType == GameConstants.SPAWN) {
                    BufferedImage pathImg = sprites.get("path");
                    if (pathImg != null) {
                        g.drawImage(pathImg, x, y, size, size, null);
                    } else {
                        // Fallback màu đất
                        g.setColor(new Color(238, 214, 175));
                        g.fillRect(x, y, size, size);
                    }
                }

                // 3. LAYER TRÊN CÙNG: VẼ ĐÈ LÂU ĐÀI VÀ LỀU SPAWN
                BufferedImage topImg = null;
                if (tileType == GameConstants.BASE) {
                    topImg = sprites.get("base");
                } else if (tileType == GameConstants.SPAWN) {
                    topImg = sprites.get("spawn");
                }

                // Nếu có ảnh nhà thì ốp đè lên trên lớp đường đất
                if (topImg != null) {
                    g.drawImage(topImg, x, y, size, size, null);
                }
            }
        }
    }

    private void drawTowers(Graphics g) {
        int size = GameConstants.TILE_SIZE;
        for(Tower tower : gameEngine.getTowers()) {
            BufferedImage baseImg = getSpriteOrDefault("tower_base","base");
            int x = (int) tower.getX();
            int y = (int) tower.getY();
            if(baseImg!=null){
                g.drawImage(baseImg, x, y, size, size, null);
            }
            else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x + 4, y + 4, size - 8, size - 8);
            }
            String stateName = (tower.getState()==Tower.IDLE)?"idle":"shoot";
            String towerName =tower.getName().toLowerCase();

            int frameNumber=tower.getAniIndex();
            String key = towerName+"_"+stateName+"_"+frameNumber;
            BufferedImage unitImg = sprites.get(key);
            if(unitImg!=null){
                int y_offset=15;
                if(tower instanceof Freeze){
                    y_offset=30;
                }
                g.drawImage(unitImg, x, y-y_offset, size, size, null);

            }
            else {

                g.setColor(getTowerColor(tower));
                g.fillRect(x + 16, y - 4, size - 32, size - 32);
            }
        }
    }

    private void drawEnemies(Graphics g) {
        int size = GameConstants.TILE_SIZE;
        for (Enemy enemy : gameEngine.getEnemies()) {
            int x = (int) enemy.getX();
            int y = (int) enemy.getY();

            int frameNumber=enemy.getAniIndex();
            String enemyName = enemy.getName().toLowerCase();
            String key = enemyName +"_run_"+frameNumber;
            BufferedImage img = sprites.get(key);
            if (img != null) {
                // LOGIC LẬT ẢNH: Kiểm tra đường đi tiếp theo để biết quái đang rẽ trái hay phải
                // Nếu dx âm (targetX < currentX) -> Đi sang trái -> Lật ảnh
                boolean isFacingLeft = false;
                if (enemy.pathIndex < enemy.path.size() - 1) {
                    int[] next = enemy.path.get(enemy.pathIndex + 1);
                    double targetX = next[1] * size;
                    if (targetX < x) isFacingLeft = true;
                }

                if (isFacingLeft) {
                    // Lật ngang (Flip Horizontal)
                    g.drawImage(img, x + size, y, x, y + size, 0, 0, img.getWidth(), img.getHeight(), null);
                } else {
                    g.drawImage(img, x, y, size, size, null);
                }
            } else {
                g.setColor(getEnemyColor(enemy));
                g.fillOval(x + 8, y + 8, size - 16, size - 16);
            }

            // Thanh máu (HP Bar) - Đẩy lên cao hơn mặt quái
            g.setColor(Color.RED);
            g.fillRect(x, y - 10, size, 5);
            g.setColor(Color.GREEN);
            int hpWidth = (int) ((double) enemy.getHp() / enemy.getMaxHp() * size);
            g.fillRect(x, y - 10, hpWidth, 5);
        }
    }
    private void loadSprites() {
        String[] names = {
                "grass", "grass2", "grass3",
                "path", "path_h", "path_v", "path_corner",
                "spawn", "base", "tower_base",
                "projectile_archer","projectile_mage","projectile_cannon","projectile_freeze","projectile_sniper",
        };
        for (String name : names) {
            try {
                InputStream is = getClass().getResourceAsStream("/sprites/" + name + ".png");
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    sprites.put(name, img);
                }
            } catch (Exception e) {
                System.out.println("Lỗi đọc file: " + name);
            }
        }
        Object[][] enemyData = {
                {"goblin", 6},
                {"orc", 7},
                {"troll", 8},
                {"boss1", 8},
                {"boss2", 11},
                {"boss3", 7}
        };
        for(Object[]data:enemyData){
            String type = (String)data[0];
            int maxFrame= (int)data[1];
            for(int i = 0; i < maxFrame; i++){
                String key = type+"_run_"+i;
                try{
                    InputStream is = getClass().getResourceAsStream("/sprites/" + key + ".png");
                    if(is != null) {
                        sprites.put(key,ImageIO.read(is));
                    }
                } catch (IOException e) {}
            }
        }
        Object[][] towerData = {
                {"archer", 6,8},
                {"cannon", 8,8},
                {"mage", 6,11},
                {"sniper", 12,3},
                {"freeze", 8,3}
        };
        for (Object[] data : towerData) {
            String type = (String) data[0];
            int idleFrames = (int) data[1];
            int shootFrames = (int) data[2];
            // Load ảnh IDLE
            for(int i = 0; i < idleFrames; i++){
                String key = type + "_idle_" + i;
                try {
                    InputStream is = getClass().getResourceAsStream("/sprites/" + key + ".png");
                    if(is != null) sprites.put(key, ImageIO.read(is));
                } catch (IOException e) {}
            }

            // Load ảnh SHOOT (Từ 0 đến < số_frame)
            for(int i = 0; i < shootFrames; i++){
                String key = type + "_shoot_" + i;
                try {
                    InputStream is = getClass().getResourceAsStream("/sprites/" + key + ".png");
                    if(is != null) sprites.put(key, ImageIO.read(is));
                } catch (IOException e) {}
            }
        }
    }
    private void drawProjectiles(Graphics g) {
        int size = GameConstants.TILE_SIZE;
        for(Projectile p :gameEngine.getProjectiles()){
            int x = (int) p.getX();
            int y = (int) p.getY();
            // Lấy ảnh đạn dựa theo loại (type)
            BufferedImage proImg = sprites.get("projectile_"+p.getType().toLowerCase());
            if (proImg != null) {
                g.drawImage(proImg, x+size/4, y+size/4, size/2, size/2, null);
            }
            else{
                g.setColor(Color.YELLOW);
                g.fillOval(x+size/2-4, y+size/2-4, 8,8);
            }
        }
    }
    // Hàm tiện ích: Nếu không tìm thấy ảnh nâng cao, tự động dùng ảnh mặc định
    private BufferedImage getSpriteOrDefault(String targetKey, String defaultKey) {
        if (sprites.containsKey(targetKey)) {
            return sprites.get(targetKey);
        }
        return sprites.get(defaultKey);
    }

    private Color getTowerColor(Tower tower) {
        if (tower instanceof Archer) return Color.ORANGE;
        if (tower instanceof Cannon) return Color.DARK_GRAY;
        if (tower instanceof Mage) return Color.MAGENTA;
        if (tower instanceof Sniper) return Color.GREEN;
        if (tower instanceof Freeze) return Color.CYAN;
        return Color.GRAY;
    }

    private Color getEnemyColor(Enemy enemy) {
        if (enemy instanceof Boss) return Color.RED;
        if (enemy instanceof Troll) return Color.GRAY;
        if (enemy instanceof Orc) return Color.ORANGE;
        return Color.GREEN;
    }

    public void refresh() {
        SpriteManager.tick();
        repaint();
    }
}