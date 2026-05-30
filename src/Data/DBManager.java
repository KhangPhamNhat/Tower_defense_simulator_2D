import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String URL = "jdbc:sqlserver://localhost:1433;" +
            "databaseName=TowerDefense;" +
            "trustServerCertificate=true;" +
            "integratedSecurity=true";
    private static final String MASTER_URL = "jdbc:sqlserver://localhost:1433;" +
            "databaseName=master;" +
            "trustServerCertificate=true;" +
            "integratedSecurity=true";
    private static Connection conn;

    public static void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            createDatabase();
            conn = DriverManager.getConnection(URL);
            createTables();
            System.out.println("DB connected!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void unlockTower(int playerId, String towerType) {
        try (PreparedStatement ps = conn.prepareStatement(
                "IF NOT EXISTS (SELECT * FROM tower_owned WHERE player_id=? AND tower_type=?) " +
                        "INSERT INTO tower_owned(player_id, tower_type) VALUES(?,?)")) {
            ps.setInt(1, playerId);
            ps.setString(2, towerType);
            ps.setInt(3, playerId);
            ps.setString(4, towerType);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createDatabase() throws SQLException {
        try (Connection c = DriverManager.getConnection(MASTER_URL);
             Statement st = c.createStatement()) {
            st.execute("IF NOT EXISTS (SELECT * FROM sys.databases WHERE name='TowerDefense') " +
                    "CREATE DATABASE TowerDefense");
        }
    }

    private static void createTables() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='players' AND xtype='U') " +
                    "CREATE TABLE players (id INT PRIMARY KEY IDENTITY(1,1)," +
                    "username NVARCHAR(50) UNIQUE NOT NULL," +
                    "password NVARCHAR(255) NOT NULL," +
                    "gems INT DEFAULT 0," +
                    "created_at DATETIME DEFAULT GETDATE())");
            st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='match_history' AND xtype='U') " +
                    "CREATE TABLE match_history (id INT PRIMARY KEY IDENTITY(1,1)," +
                    "player_id INT, mode NVARCHAR(20), map_id INT, score INT, result NVARCHAR(10)," +
                    "played_at DATETIME DEFAULT GETDATE()," +
                    "FOREIGN KEY(player_id) REFERENCES players(id))");
            st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='leaderboard' AND xtype='U') " +
                    "CREATE TABLE leaderboard (id INT PRIMARY KEY IDENTITY(1,1)," +
                    "player_id INT, mode NVARCHAR(20), map_id INT, best_score INT," +
                    "FOREIGN KEY(player_id) REFERENCES players(id))");
            st.execute(
                    "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tower_owned' AND xtype='U') " +
                            "CREATE TABLE tower_owned (" +
                            "player_id INT," +
                            "tower_type NVARCHAR(20)," +
                            "PRIMARY KEY(player_id, tower_type)," +
                            "FOREIGN KEY(player_id) REFERENCES players(id))"
            );
            st.execute(
                    "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='map_progress' AND xtype='U') " +
                            "CREATE TABLE map_progress (" +
                            "player_id INT," +
                            "map_id INT," +
                            "mode NVARCHAR(20)," +
                            "difficulty NVARCHAR(10)," +
                            "completed BIT DEFAULT 0," +
                            "PRIMARY KEY(player_id, map_id, mode, difficulty)," +
                            "FOREIGN KEY(player_id) REFERENCES players(id))"
            );
        }
    }

    public static boolean registerPlayer(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO players(username, password) VALUES(?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static int loginPlayer(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM players WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public static void saveMatch(int playerId, String mode, int mapId, int score, String result,String difficulty) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO match_history(player_id, mode, map_id, score, result) VALUES(?,?,?,?,?)")) {
            ps.setInt(1, playerId); ps.setString(2, mode);
            ps.setInt(3, mapId); ps.setInt(4, score); ps.setString(5, result);
            ps.executeUpdate();
            updateLeaderboard(playerId, mode, mapId, score,difficulty);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void updateLeaderboard(int playerId, String mode, int mapId, int score, String difficulty) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT best_score FROM leaderboard WHERE player_id=? AND mode=? AND map_id=? AND difficulty=?");
        ps.setInt(1, playerId); ps.setString(2, mode);
        ps.setInt(3, mapId); ps.setString(4, difficulty);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int current = rs.getInt("best_score");
            boolean shouldUpdate = mode.equals("STORY") ? score < current : score > current;
            if (shouldUpdate) {
                PreparedStatement up = conn.prepareStatement(
                        "UPDATE leaderboard SET best_score=? WHERE player_id=? AND mode=? AND map_id=? AND difficulty=?");
                up.setInt(1, score); up.setInt(2, playerId);
                up.setString(3, mode); up.setInt(4, mapId);
                up.setString(5, difficulty);
                up.executeUpdate();
            }
        } else {
            PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO leaderboard(player_id, mode, map_id, best_score, difficulty) VALUES(?,?,?,?,?)");
            ins.setInt(1, playerId); ins.setString(2, mode);
            ins.setInt(3, mapId); ins.setInt(4, score);
            ins.setString(5, difficulty);
            ins.executeUpdate();
        }
    }

    public static ResultSet getLeaderboard(String mode, int mapId,String difficulty) throws SQLException {
        try {
            String order = mode.equals("STORY") ? "ASC" : "DESC";
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 50 p.username, l.best_score " +
                            "FROM leaderboard l JOIN players p ON l.player_id = p.id " +
                            "WHERE l.mode=? AND l.map_id=? AND l.difficulty=? " +
                            "ORDER BY l.best_score " + order);
            ps.setString(1, mode); ps.setInt(2, mapId); ps.setString(3, difficulty);
            return ps.executeQuery();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void disconnect() {
        try { if (conn != null) conn.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
    // Lấy danh sách tower đã sở hữu
    public static List<String> getOwnedTowers(int playerId) {
        List<String> owned = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT tower_type FROM tower_owned WHERE player_id=?")) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) owned.add(rs.getString("tower_type"));
        } catch (SQLException e) { e.printStackTrace(); }
        return owned;
    }

    // Mua tower
    public static boolean buyTower(int playerId, String towerType, int gemCost) {
        try {
            // Kiểm tra gems
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT gems FROM players WHERE id=?");
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("gems") >= gemCost) {
                // Trừ gems
                PreparedStatement up = conn.prepareStatement(
                        "UPDATE players SET gems=gems-? WHERE id=?");
                up.setInt(1, gemCost);
                up.setInt(2, playerId);
                up.executeUpdate();
                // Thêm tower
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO tower_owned(player_id, tower_type) VALUES(?,?)");
                ins.setInt(1, playerId);
                ins.setString(2, towerType);
                ins.executeUpdate();
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Thêm gems cho player khi clear ải
    public static void addGems(int playerId, int amount) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE players SET gems=gems+? WHERE id=?")) {
            ps.setInt(1, amount);
            ps.setInt(2, playerId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Lấy gems hiện có
    public static int getGems(int playerId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT gems FROM players WHERE id=?")) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("gems");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    // Đánh dấu hoàn thành
    public static void completeMap(int playerId, int mapId, String mode, String difficulty) {
        try (PreparedStatement ps = conn.prepareStatement(
                "IF NOT EXISTS (SELECT * FROM map_progress WHERE player_id=? AND map_id=? AND mode=? AND difficulty=?) " +
                        "INSERT INTO map_progress(player_id, map_id, mode, difficulty, completed) VALUES(?,?,?,?,1) " +
                        "ELSE UPDATE map_progress SET completed=1 WHERE player_id=? AND map_id=? AND mode=? AND difficulty=?")) {
            ps.setInt(1, playerId); ps.setInt(2, mapId); ps.setString(3, mode); ps.setString(4, difficulty);
            ps.setInt(5, playerId); ps.setInt(6, mapId); ps.setString(7, mode); ps.setString(8, difficulty);
            ps.setInt(9, playerId); ps.setInt(10, mapId); ps.setString(11, mode); ps.setString(12, difficulty);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Check đã hoàn thành chưa
    public static boolean isCompleted(int playerId, int mapId, String mode, String difficulty) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT completed FROM map_progress WHERE player_id=? AND map_id=? AND mode=? AND difficulty=?")) {
            ps.setInt(1, playerId); ps.setInt(2, mapId);
            ps.setString(3, mode); ps.setString(4, difficulty);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("completed");
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Check map có được unlock không
    public static boolean isMapUnlocked(int playerId, int mapId, String mode, String difficulty) {
        // Map 1 Easy Story/Inf luôn unlock
        if (mapId == 1 && difficulty.equals("EASY")) return true;

        if (mode.equals("STORY")) {
            if (difficulty.equals("EASY")) {
                // Map 2 Easy cần clear Map 1 Easy
                // Map 3 Easy cần clear Map 2 Easy
                return isCompleted(playerId, mapId-1, "STORY", "EASY");
            } else if (difficulty.equals("NORMAL")) {
                // Normal cần clear Easy cùng map
                return isCompleted(playerId, mapId, "STORY", "EASY");
            } else { // HARD
                // Hard cần clear Normal cùng map
                return isCompleted(playerId, mapId, "STORY", "NORMAL");
            }
        } else if (mode.equals("INFINITY")) {
            // Inf cần clear Story Easy cùng map
            return isCompleted(playerId, mapId, "STORY", "EASY");
        }
        return true;
    }
}