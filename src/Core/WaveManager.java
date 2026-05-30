import java.util.List;

public class WaveManager implements Runnable {
    private List<WaveConfig> waves;
    private int currentWave;
    private boolean running;
    private GameEngine engine; // call enemy
    private boolean infinityMode = false;
    public int infinityWave = 0;
    private boolean skipNext = false;
    private int countdown = 0;
    private Runnable onSkipReady;
    private Runnable onSkipReadyTemplate;
    private Runnable onWaveEmpty;
    public WaveManager(List<WaveConfig> waves, GameEngine engine) {
        this.waves = waves;
        this.engine = engine;
        this.currentWave = 0;
        this.running = false;
    }

    public void setInifinityMode(boolean inifinityMode) {
        this.infinityMode = inifinityMode;
    }

    @Override
    public void run(){
        running = true;
        while(running){
            WaveConfig config=null;
            //inf mode
            if (infinityMode) {
                config = generateInfinityWave(infinityWave);
                infinityWave++;
            }
            else{
                if(currentWave>= waves.size()) {break;}
                config=waves.get(currentWave);

            }
            engine.setCurrentWave(infinityMode?infinityWave:currentWave+1);
            //spawn enemy trong tung wave
            for(String enemyType : config.getEnemyTypes()){
                if (!running){break;}
                boolean isBoss = config.hasBoss()&&enemyType.startsWith("BOSS");
                engine.spawnEnemies(enemyType,isBoss);
                try{
                    Thread.sleep(config.getSpawnDelay());
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            currentWave++;
            // dem nguoc toi wave ke tiep
            if(running){
                onSkipReady=onSkipReadyTemplate;
                long waitTime = GameConstants.WAVE_COUNTDOWN*1000L;
                long startTime = System.currentTimeMillis();
                while(running&&!skipNext&&(System.currentTimeMillis()-startTime)<waitTime){
                    countdown = (int)((waitTime - (System.currentTimeMillis()-startTime))/1000)+1;
                    //enable skip sau 5s
                    if(onSkipReady!=null&&System.currentTimeMillis()-startTime>=5000){
                        javax.swing.SwingUtilities.invokeLater(onSkipReady);
                        onSkipReady = null;
                    }
                    try{Thread.sleep(100);}
                    catch (InterruptedException e){Thread.currentThread().interrupt();break;}
                }
                countdown=0;
                skipNext = false;
                if(onWaveEmpty!=null){
                    javax.swing.SwingUtilities.invokeLater(onWaveEmpty);
                }
            }
        }
        running = false;
    }
    private WaveConfig generateInfinityWave(int waveNum){
        //do kho tang dan
        String[] pool;
        int delay;
        boolean hasBoss = waveNum >0 && waveNum%15==0;//boss moi 15 wave
        if(waveNum<5){
            pool = new String[]{"GOBLIN","GOBLIN","ORC"};
            delay = 1000;
        }
        else if (waveNum < 15) {
            pool = new String[]{"ORC", "ORC", "TROLL"};
            delay = 800;
        } else if (waveNum < 30) {
            pool = new String[]{"TROLL", "TROLL", "TROLL", "ORC"};
            delay = 600;
        } else {
            pool = new String[]{"TROLL", "TROLL", "TROLL", "TROLL", "TROLL"};
            delay = 400;
        }
        if(hasBoss){
            int bossNum = (waveNum/15)%3+1;
            pool = new String[]{"BOSS_ONE","BOSS_TWO","BOSS_THREE"};
            pool = new String[]{pool[bossNum-1]};
            delay = 0;
        }
        return new WaveConfig(waveNum+1,pool,delay,hasBoss);
    }
    public void startNextWave(){
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }
    public void stop(){running = false;}
    public int getCurrentWave() {return currentWave;}
    public boolean isRunning(){return running;}
    public void skipCountdown(){skipNext = true;}
    public int getCountdown(){return countdown;}
    public void setOnSkipReady(Runnable r){onSkipReady = r; onSkipReadyTemplate = r;}
    public  void setOnWaveEmpty(Runnable r){onWaveEmpty = r;}
}
