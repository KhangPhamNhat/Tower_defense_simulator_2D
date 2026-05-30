import java.util.List;

public class EnemyFactory {
    public static Enemy create(String type, boolean isBoss, List<int[]> path,double difficulty){
        if (isBoss) {
            switch (type) {
                case "BOSS_ONE": return new BossOne(path,difficulty);
                case "BOSS_TWO": return new BossTwo(path,difficulty);
                case "BOSS_THREE": return new BossThree(path,difficulty);
                default: throw new IllegalArgumentException("Invalid boss type");
            }
        }
        switch (type) {
            case "GOBLIN": return new Goblin(path,difficulty);
            case "ORC":return new Orc(path,difficulty);
            case "TROLL":return new Troll(path,difficulty);
            default: throw new IllegalArgumentException("Invalid mob type");
        }
    }
}
