import javax.swing.*;
import java.awt.*;
import java.util.List;
public class HudPanel extends JPanel {
    private GameEngine gameEngine;
    private String selectedTower = null;
    private int playerID;
    private boolean infinityMode = false;
    private GameClient gameClient;

    private static final String[] TOWERS={"ARCHER","CANNON","MAGE","SNIPER","FREEZE"};
    private static final int[] COSTS = {50,100,125,150,125};
    private static final Color[] COLORS ={Color.ORANGE,Color.DARK_GRAY,Color.MAGENTA,Color.GREEN,Color.CYAN};
    private static final String[] ENEMIES={"GOBLIN","ORC","TROLL"};
    private static final int[] ENEMY_COSTS = {20,40,80};
    private static final Color[] ENEMY_COLORS={Color.GREEN,Color.ORANGE,Color.GRAY};
    public HudPanel(GameEngine gameEngine,int playerID,GameClient gameClient) {
        this.gameEngine = gameEngine;
        this.playerID = playerID;
        this.gameClient = gameClient;
        setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH,60));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());
        //load tower da so huu
        List<String> ownedTowers = DBManager.getOwnedTowers(playerID);
        //ben trai:info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,15));
        infoPanel.setBackground(Color.DARK_GRAY);

        JLabel coinsLabel = new JLabel("Coins: 0");
        coinsLabel.setForeground(Color.YELLOW);
        coinsLabel.setFont(new Font("Arial",Font.BOLD,16));

        JLabel hpLabel = new JLabel("HP: 0");
        hpLabel.setForeground(Color.RED);
        hpLabel.setFont(new Font("Arial",Font.BOLD,16));

        JLabel waveLabel = new JLabel("Wave: 0/15");
        waveLabel.setForeground(Color.WHITE);
        waveLabel.setFont(new Font("Arial",Font.BOLD,16));

        JLabel towerCountLabel = new JLabel("Towers: 0");
        towerCountLabel.setForeground(Color.GREEN);
        towerCountLabel.setFont(new Font("Arial",Font.BOLD,16));

        JLabel countdownLabel = new JLabel("");
        countdownLabel.setForeground(Color.RED);
        countdownLabel.setFont(new Font("Arial",Font.BOLD,16));


        infoPanel.add(coinsLabel);
        infoPanel.add(hpLabel);
        infoPanel.add(waveLabel);
        infoPanel.add(towerCountLabel);
        infoPanel.add(countdownLabel);
        //ben phai tower button
        JPanel towerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,3,5));
        towerPanel.setBackground(Color.DARK_GRAY);

        for(int i=0;i<TOWERS.length;i++) {
            final String type = TOWERS[i];
            final int cost = COSTS[i];
            final Color color = COLORS[i];
            if(!ownedTowers.contains(type)) {continue;}
            JButton btn = new JButton("<html><center>"+type+"<br>"+cost+"</center></html>");
            btn.setPreferredSize(new Dimension(85,50));
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial",Font.BOLD,16));
            btn.addActionListener(e->selectedTower=type);
            towerPanel.add(btn);
        }
        //nut spawn enemy
        if(gameClient!=null){
            JPanel enemyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,3,5));
            enemyPanel.setBackground(Color.DARK_GRAY);

            JLabel spawnLabel = new JLabel("SPAWN:");
            spawnLabel.setForeground(Color.WHITE);
            spawnLabel.setFont(new Font("Arial",Font.BOLD,12));
            enemyPanel.add(spawnLabel);

            for(int i=0;i<ENEMIES.length;i++) {
                final String type = ENEMIES[i];
                final int cost = ENEMY_COSTS[i];
                final Color color = ENEMY_COLORS[i];
                JButton btn = new JButton("<html><center>"+type+"<br>"+cost+"</center></html>");
                btn.setPreferredSize(new Dimension(75,50));
                btn.setBackground(color);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Arial",Font.BOLD,10));
                btn.addActionListener(e->{
                    if(gameEngine.getCoins()>=cost){
                        gameEngine.sendCoins(cost);
                        gameClient.sendSpawnEnemy(type);
                    }
                    else{
                        System.out.println("Not enough coins!");
                    }
                });
                enemyPanel.add(btn);
            }
            towerPanel.add(enemyPanel);
        }
        //nut dau hang
        JButton surrender = new JButton("GG");
        surrender.setPreferredSize(new Dimension(70,50));
        surrender.setBackground(Color.RED);
        surrender.setForeground(Color.WHITE);
        surrender.setFont(new Font("Arial",Font.BOLD,16));
        surrender.addActionListener(e->{
            int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to surrender?","Surrender",JOptionPane.YES_NO_OPTION);
            if(confirm==JOptionPane.YES_OPTION) {
                gameEngine.stop();
                if(gameClient != null) {
                    gameClient.sendGameOver();//bao server minh thua
                }
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.getContentPane().removeAll();
                frame.setContentPane(new MenuPanel(frame,playerID));
                frame.revalidate();
                frame.repaint();
            }
        });
        towerPanel.add(surrender);
        //nut skip
        JButton skipBtn = new JButton("SKIP");
        skipBtn.setPreferredSize(new Dimension(70,50));
        skipBtn.setBackground(Color.DARK_GRAY);
        skipBtn.setForeground(Color.WHITE);
        skipBtn.setFont(new Font("Arial",Font.BOLD,16));
        skipBtn.setEnabled(false);
        skipBtn.addActionListener(e->{
            gameEngine.getWaveManager().skipCountdown();
            skipBtn.setEnabled(false);
        });
        if(gameEngine.getWaveManager()!=null) {
            gameEngine.getWaveManager().setOnSkipReady(() -> skipBtn.setEnabled(true));
            gameEngine.getWaveManager().setOnWaveEmpty(() -> skipBtn.setEnabled(false));
        }
        towerPanel.add(skipBtn);
        add(infoPanel,BorderLayout.WEST);
        add(towerPanel,BorderLayout.EAST);
        //timer update label
        new Timer(100,e->{
            coinsLabel.setText("Coins: "+gameEngine.getCoins());
            hpLabel.setText("HP: "+gameEngine.getBaseHP());
            waveLabel.setText(infinityMode?"Wave: "+gameEngine.getCurrentWave(): "Wave: "+gameEngine.getCurrentWave()+"/15");
            towerCountLabel.setText("Towers: " + gameEngine.getTowerCount() + "/" + GameConstants.MAX_TOWERS);
            int remaining = gameEngine.getWaveManager()!=null?gameEngine.getWaveManager().getCountdown():0;
            if(remaining>0){
                countdownLabel.setText("Countdown: "+remaining+"s");
            }
            else{
                countdownLabel.setText("");
            }
        }).start();
    }

    public String getSelectedTower() {return selectedTower;}
    public void clearSelectedTower(){
        selectedTower=null;
    }
    public void refresh(){
        repaint();
    }
    public void setInfinityMode(boolean inf) { this.infinityMode = inf; }
}
