import java.util.List;

public abstract class Enemy {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected double speed;
    protected int damage;
    protected int reward;
    protected List<int[]> path;
    protected int pathIndex;// dang ở ô thứ mấy
    protected double x, y;//pixel
    protected boolean dead;
    protected boolean reachedBase;
    protected int freezeTimer;

    //animation
    protected int aniTick = 0;
    protected int aniSpeed = 10;
    protected int aniIndex = 0;
    protected int maxFrame = 6;
    public Enemy(String name, int hp, double speed, int damage, int reward ,List<int[]> path) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
        this.damage = damage;
        this.reward = reward;
        this.path = path;
        this.pathIndex = 0;
        this.dead = false;
        this.reachedBase = false;
        // bat dau tai o spawn
        this.x = path.get(0)[1]*GameConstants.TILE_SIZE;
        this.y = path.get(0)[0]*GameConstants.TILE_SIZE;
    }
    public void update(){
        if(dead||reachedBase){return;}
        if(freezeTimer > 0){freezeTimer--; return;}
        if(pathIndex >= path.size()-1){reachedBase=true;return;}
        // di chuyen den o tiep theo
        int[]next = path.get(pathIndex+1);
        double targetX = next[1]*GameConstants.TILE_SIZE;
        double targetY = next[0]*GameConstants.TILE_SIZE;
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);
        if(distance <= speed){
            x = targetX;
            y = targetY;
            pathIndex++;
        }
        else{
            x += dx/distance*speed;
            y += dy/distance*speed;
        }
        updateAnimation();
    }
    public void updateAnimation(){
        aniTick++;
        if(aniTick >= aniSpeed){
            aniTick = 0;
            aniIndex++;
            if(aniIndex >= maxFrame){
                aniIndex = 0;
            }
        }
    }
    public void applyFreeze(int duration){
        freezeTimer = duration;
    }
    public void takeDamage(int dmg){
        hp -= dmg;
        if(hp<=0){dead = true;}
    }
    public boolean isDead(){return dead;}
    public boolean isReachedBase(){return reachedBase;}
    public int getReward(){return reward;}
    public String getName(){return name;}
    public double getX(){return x;}
    public double getY(){return y;}
    public int getHp(){return hp;}
    public int getMaxHp(){return maxHp;}
    public int getDamage(){return damage;}
    public int getAniIndex(){return aniIndex;}
}
class Goblin extends Enemy{
    public Goblin(List<int[]> path, double difficulty){
        super("Goblin",(int)(50*difficulty),2,2,15,path);
        this.maxFrame = 6;
    }
}
class Orc extends Enemy{
    public Orc(List<int[]> path, double difficulty){
        super("Orc",(int)(100*difficulty),1,5,30,path);
        this.maxFrame = 7;
    }
}
class Troll extends Enemy{
    public Troll(List<int[]> path, double difficulty){
        super("Troll",(int)(200*difficulty),0.5,10,50,path);
        this.maxFrame = 8;
    }
}
class Boss extends Enemy{
    protected String ability;
    protected int abilityTimer;
    private static final int ABILITY_COOLDOWN = 300;
    protected int armorReduction = 0;
    protected boolean speedBooster = false;

    public Boss(String name, int hp,double speed, int damage, int reward,String ability,List<int[]> path) {
        super(name,hp,speed,damage,reward,path);
        this.ability = ability;
        this.abilityTimer = 0;
    }
    @Override
    public void takeDamage(int dmg){
        super.takeDamage(Math.max(1, dmg-armorReduction));
    }
    protected void useAbility(){}
        public String getAbility(){
            return ability;
        }
    @Override
    public void update() {
        super.update();
        abilityTimer++;
        if(abilityTimer>=ABILITY_COOLDOWN){
            useAbility();
            abilityTimer = 0;
        }
    }
    }
class BossOne extends Boss{
    private int armorTimer = 0;
    private boolean armorActive = false;
    public BossOne(List<int[]> path, double difficulty){
        super("Boss1",(int)(500*difficulty),0.5,20,150,"ARMOR",path);
        this.maxFrame = 8;
    }
    @Override
    public void useAbility(){
        armorReduction =15;
        armorActive = true;
        armorTimer = 0;
    }
    @Override
    public void update() {
        super.update();
        if(armorActive){
            armorTimer++;
            if(armorTimer>=300){ // 5s thi het armor active 300 frame / 60 fps
                armorActive = false;
                armorTimer = 0;
            }
        }
    }
}
class BossTwo extends Boss{
    private double baseSpeed;
    private int speedTimer = 0;
    private boolean speedActive = false;

    public BossTwo(List<int[]> path, double difficulty) {
        super("Boss2", (int)(400 * difficulty), 0.8, 15, 200, "SPEED_BURST", path);
        this.baseSpeed = 0.8;
        this.maxFrame = 11;
    }
    @Override
    public void useAbility(){
        speed = baseSpeed*2;
        speedActive = true;
        speedTimer = 0;
    }
    @Override
    public void update() {
        super.update();
        if(speedActive){
            speedTimer++;
            if(speedTimer>=180){speed = baseSpeed; speedActive = false;}
        }
    }
}
class BossThree extends Boss{
    public BossThree(List<int[]> path, double difficulty){
        super("Boss3",(int)(800*difficulty),0.8,30,300,"HEAL",path);
        this.maxFrame = 7;
    }
    @Override
    public void useAbility(){
        hp = Math.min(maxHp,hp+(int)(maxHp*0.1));
    }
}
