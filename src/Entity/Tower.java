import java.util.List;

public abstract class Tower {
    protected String name;
    protected int cost;
    protected int damage;
    protected double range; // tinh theo pixel
    protected int fireRate; // frame per attack
    protected int fireTimer;
    protected int row,col; // vi tri tren grid
    protected int level; // lvl de update

    //animation
    public static final int IDLE =0;
    public static final int ATTACK=1;
    protected int state = IDLE;
    protected int aniTick = 0;
    protected int aniIndex = 0;
    protected int aniSpeed = 10;
    protected int[] maxFrames = {4, 4};
    public Tower(String name, int cost, int damage, double range, int fireRate, int row, int col){
        this.name = name;
        this.cost = cost;
        this.damage = damage;
        this.range = range;
        this.fireRate = fireRate;
        this.fireTimer = 0;
        this.row = row;
        this.col = col;
        this.level = 0;
    }
    public void update(List<Enemy> Enemies,List<Projectile> Projectiles){
        Enemy target = findTarget(Enemies);
        if(target != null){
            state = ATTACK;
            fireTimer++;
            if(fireTimer >= fireRate){
                attack(target,Projectiles);
                fireTimer = 0;
                aniIndex=0;
            }
        }
        else{
            state= IDLE;
        }
        updateAnimation();
    }
    protected void updateAnimation(){
        aniTick++;
        if(aniTick >= aniSpeed){
            aniTick=0;
            aniIndex++;
            if(aniIndex >= maxFrames[state]){
                aniIndex=0;
            }
        }
    }
    protected Enemy findTarget(List<Enemy>enemies){
        // tim enemy gan nhat
        Enemy closet = null;
        double minDist = Double.MAX_VALUE;
        double tx =col*GameConstants.TILE_SIZE+GameConstants.TILE_SIZE/2;
        double ty = row*GameConstants.TILE_SIZE+GameConstants.TILE_SIZE/2;
        for(Enemy e :enemies){
            double dx = e.getX()-tx;
            double dy = e.getY()-ty;
            double dist = Math.sqrt(dx*dx+dy*dy);
            if(dist < minDist&&dist<=range){
                minDist = dist;
                closet = e;
            }
        }
        return closet;
    }
    protected abstract void attack(Enemy target,List<Projectile> Projectiles);
    public void upgrade(){
        if(level < GameConstants.TOWER_MAX_LEVEL) {
            level++;
            onUpgrade();
        }
    }
    protected abstract void onUpgrade();

    public int getCost(){return cost;}
    public int getDamage(){return damage;}
    public double getRange(){return range;}
    public int getLevel(){return level;}
    public String getName(){return name;}
    public int getRow(){return row;}
    public int getCol(){return col;}
    public double getX() { return col * GameConstants.TILE_SIZE; }
    public double getY() { return row * GameConstants.TILE_SIZE; }
    public int getUpgradeCost(){
        return cost*(level+1);
    }
    public int getState(){return state;}
    public int getAniIndex(){return aniIndex;}
}
class Archer extends Tower {
    public Archer(int row,int col){
        super("Archer",50,15,150,40,row,col);
        this.maxFrames=new int[]{6,8};
    }
    @Override
    protected void onUpgrade() {
        damage +=8;
        range +=20;
    }
    @Override
    protected void attack(Enemy target,List<Projectile> projectiles){
        projectiles.add(new Projectile(getX(), getY(), 6.0, damage, target, "archer"));
    }
}
class Cannon extends Tower {
    private int aoeRadius = 60;
    public Cannon(int row,int col){
        super("Cannon",100,40,120,90,row,col);
        this.maxFrames=new int[]{8,8};
    }
    @Override
    protected void attack(Enemy target,List<Projectile> projectiles){
        // Sinh cục pháo bự (Tốc độ bay: 4.0 -> bay chậm nổ to)
        projectiles.add(new Projectile(getX(), getY(), 4.0, damage, target, "cannon"));
    }
    @Override
    protected void onUpgrade() {
        damage +=20;
        range +=15;
    }
}
class Mage extends Tower {
    public Mage(int row,int col){
        super("Mage",125,25,130,50,row,col);
        this.maxFrames=new int[]{6,11};
    }
    @Override
    protected void onUpgrade() {
        damage +=5;
        range +=15;
        fireRate -=5;
    }
    @Override
    protected void attack(Enemy target,List<Projectile> projectiles){
        //xuyen giap bypass armor reduction
        if (target instanceof Boss){
            ((Boss)target).armorReduction = 0;
        }
        projectiles.add(new Projectile(getX(), getY(), 5.0, damage, target, "mage"));
        }
    }
class Sniper extends Tower {
    public Sniper(int row,int col){
        super("Sniper",150,80,300,120,row,col);
        this.maxFrames=new int[]{12,3};
    }
    @Override
    protected void onUpgrade() {
        damage +=40;
        range +=50;
    }
    @Override
    protected void attack(Enemy target,List<Projectile> projectiles){
        projectiles.add(new Projectile(getX(), getY(), 12.0, damage, target, "sniper"));
    }
}
class Freeze extends Tower {
    private int freezeDuration = 60;//frames
    public Freeze(int row,int col){
        super("Freeze",125,5,140,60,row,col);
        this.maxFrames=new int[]{8,3};
    }
    @Override
    protected void onUpgrade() {
        freezeDuration +=30;
        range +=20;
    }
    @Override
    protected void attack(Enemy target,List<Projectile> projectiles){
        target.applyFreeze(freezeDuration);
        projectiles.add(new Projectile(getX(), getY(), 5.0, damage, target, "freeze"));
    }
}
