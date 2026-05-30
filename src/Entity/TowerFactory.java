public class TowerFactory {
    public static Tower create(String type, int row, int col){
        switch(type){
            case "ARCHER" : return new Archer(row,col);
            case "CANNON" : return new Cannon(row,col);
            case "FREEZE" : return new Freeze(row,col);
            case "MAGE" : return new Mage(row,col);
            case "SNIPER" : return new Sniper(row,col);
            default: throw new IllegalArgumentException("Invalid Tower Type");
        }
    }
}
