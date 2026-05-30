public class Projectile {
    private double x,y;
    private double speed;
    private int damage;
    private Enemy target;
    private boolean active = true;// Đạn còn bay hay đã chạm mục tiêu
    // Tùy chọn: Để vẽ ảnh đạn khác nhau (Mũi tên, Quả cầu lửa, Đạn pháo...)
    private String type;
    public Projectile(double x, double y, double speed, int damage, Enemy target,String type) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.target = target;
        this.type = type;
    }
    public void update(){
        // neu muc tieu chet truoc khi dan hit thi huy dan
        if(target == null||target.isDead()){
            active = false;
            return;
        }
        //dan dí theo muc tieu
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distance = Math.sqrt(dx*dx + dy*dy);
        //neu dan trung dich
        if(distance<speed){
            target.takeDamage(damage);
            active = false;
        }
        //neu chua trung
        else {
            x+=(dx/distance)*speed;
            y+=(dy/distance)*speed;
        }
    }
    public boolean isActive() {
        return active;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public String  getType() {
        return type;
    }
}
