public class WaveConfig {
    private int waveNumber;
    private String[] enenmyTypes;
    private int spawnDelay;
    private boolean hasBoss;

    public WaveConfig(int waveNumber, String[] enenmyTypes, int spawnDelay, boolean hasBoss) {
        this.waveNumber = waveNumber;
        this.enenmyTypes = enenmyTypes;
        this.spawnDelay = spawnDelay;
        this.hasBoss = hasBoss;
    }
    public int getWaveNumber() {return waveNumber;}
    public String[] getEnemyTypes() {return enenmyTypes;}
    public int getSpawnDelay() {return spawnDelay;}
    public boolean hasBoss() {return hasBoss;}
}
