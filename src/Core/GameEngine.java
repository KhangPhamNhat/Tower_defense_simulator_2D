import java.util.*;

public class GameEngine implements Runnable {
    private Map map;
    private List<Tower> towers;
    private List<Enemy> enemies;
    private int coins;
    private int baseHP;
    private int currentWave;
    private boolean running;
    private WaveManager waveManager;
    private int currentDifficulty;
    private List<Projectile> projectiles;
    private int mapID;
    public GameEngine(Map map, int difficulty,int mapID) {
        this.map = map;
        this.mapID = mapID;
        this.currentDifficulty = difficulty;
        this.enemies = new ArrayList<>();
        this.towers = new ArrayList<>();
        this.coins = GameConstants.START_COINS;
        this.baseHP = GameConstants.BASE_HP;
        this.currentWave = 0;
        this.running = false;
        this.projectiles = new ArrayList<>();
    }
    @Override
    public void run() {
        running = true;
        while (running) {
            long startTime = System.currentTimeMillis();
            update();
            long elapsed  = System.currentTimeMillis() - startTime;
            long sleep = GameConstants.FRAME_TIME - elapsed;
            if (sleep > 0){
                try {Thread.sleep(sleep);}
                catch (InterruptedException e){ Thread.currentThread().interrupt();}
            }
        }
    }
    private void update() {
        // tower attack enemy
        for(Tower t : towers){
            t.update(enemies,projectiles);
        }
        //dan bay
        Iterator<Projectile> it = projectiles.iterator();
        while(it.hasNext()){
            Projectile p = it.next();
            p.update();//cho dan bay 1 nhip
            //neu dan ko co(co nghia dinh quai hoac la quai da chet) xoa dan
            if(!p.isActive()){
                it.remove();
            }
        }
        // enemy di chuyen
        List<Enemy> toRemove = new ArrayList<>();
        for(Enemy e:enemies){
            e.update();
            if(e.isDead()){
                coins+=e.getReward();
                toRemove.add(e);
            }
            else if(e.isReachedBase()){
                baseHP -=e.getDamage();
                toRemove.add(e);
            }
        }
        enemies.removeAll(toRemove);
        if(enemies.isEmpty() &&waveManager!=null&&waveManager.getCountdown()>0){
            waveManager.skipCountdown();
        }
    }
    public void spawnEnemies(String type, boolean isBoss){
        double mapDiff=Math.pow(2,mapID-1);
        double difficulty = GameConstants.DIFF_MULTIPLIERS[currentDifficulty]*mapDiff;
        Enemy e = EnemyFactory.create(type, isBoss,map.getPath(),difficulty);
        enemies.add(e);
    }
    public boolean placeTower(Tower tower, int row, int col){
        if(towers.size()>=GameConstants.MAX_TOWERS){return false;}
        if(!map.canPlaceTower(row,col)){return false;}
        if(coins < tower.getCost())return false;
        for(Tower t : towers){
            if(t.getRow() == row && t.getCol() == col){return false;}
        }
        coins-=tower.getCost();
        towers.add(tower);
        return true;
    }
    public void start(){
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        if(waveManager != null){
            waveManager.startNextWave();
        }
    }
    public boolean sellTower(int row, int col){
        Tower toRemove = null;
        for(Tower t : towers){
            if(t.getRow() == row && t.getCol() == col){
                toRemove = t;
                break;
            }
        }
        if (toRemove !=null){
            coins+=toRemove.getCost()/2;
            towers.remove(toRemove);
            return true;
        }
        return false;
    }
    public void stop(){running = false;}
    public boolean isGameOver(){return baseHP <= 0;}
    public boolean isWaveCleared(){return enemies.isEmpty() && !waveManager.isRunning()&&currentWave>=GameConstants.WAVE_PER_STAGE;}
    public int getCoins(){ return coins;}
    public int getBaseHP(){return baseHP;}
    public List<Enemy> getEnemies(){return enemies;}
    public List<Tower> getTowers(){return towers;}
    public Map getMap(){return map;}
    public int getCurrentWave() { return currentWave; }
    public void setCurrentWave(int currentWave){this.currentWave = currentWave;}
    public void setWaveManager(WaveManager waveManager){this.waveManager= waveManager;}
    public void sendCoins(int amount){
        coins-=amount;
    }
    public int getTowerCount(){return towers.size();}

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public boolean upgradeTower(int row, int col) {
        for(Tower t : towers){
            if(t.getRow() == row && t.getCol() == col){
                int cost =  t.getUpgradeCost();
                if(t.getLevel()>= GameConstants.TOWER_MAX_LEVEL){return false;}
                if(coins<cost){return false;}
                coins = coins-cost;
                t.upgrade();
                return true;
            }
        }
        return false;
    }

    public Tower getTowerAt(int row, int col) {
        for(Tower t : towers){
            if(t.getRow() == row && t.getCol() == col){return t;}
        }
        return null;
    }
    public List<Projectile> getProjectiles(){return projectiles;}
}
