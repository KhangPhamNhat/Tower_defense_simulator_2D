import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindown extends JFrame {
    private GameEngine gameEngine;
    private GamePanel gamePanel;
    private HudPanel hudPanel;
    private boolean gameOverShown = false;
    private GameClient gameClient;
    private Timer timer;
    private int playerID;
    private int mapID;
    private int mode;
    private int difficulty;
    private long startTime;
    public GameWindown(GameEngine gameEngine,int playerID,int mode, int mapID,int difficulty, GameClient gameClient) {
        this.gameEngine = gameEngine;
        this.gameClient = gameClient;
        this.playerID=playerID;
        this.mode=mode;
        this.mapID=mapID;
        this.difficulty=difficulty;

        setTitle("Tower defense");;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        //layout
        setLayout(new BorderLayout());
        gamePanel = new GamePanel(gameEngine);
        hudPanel = new HudPanel(gameEngine,playerID,gameClient);
        if(mode ==GameConstants.MODE_INF){
            hudPanel.setInfinityMode(true);
        }
        add(hudPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        //click dat tower
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                int col = e.getX()/GameConstants.TILE_SIZE;
                int row = e.getY()/GameConstants.TILE_SIZE;
                if(e.getButton() == MouseEvent.BUTTON3){
                    //chuot phai => sell
                    if(gameEngine.sellTower(row,col)){
                        System.out.println("TOWER SOLD!");
                    }
                    return;
                }
                //chuot trai check neu co tower thi hoi co muon update ko
                Tower exited = gameEngine.getTowerAt(row,col);
                if(exited != null){
                    //co tower-> update
                    if(exited.getLevel()>=GameConstants.TOWER_MAX_LEVEL){
                        JOptionPane.showMessageDialog(gamePanel,"TOWER IS MAX LEVEL!");
                        return;
                    }
                    int cost = exited.getUpgradeCost();
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "Upgrade " + exited.getName() + " to level " + (exited.getLevel()+1) + "?\nCost: " + cost + " coins",
                            "Upgrade Tower", JOptionPane.YES_NO_OPTION);
                    if(confirm == JOptionPane.YES_OPTION){
                        if(!gameEngine.upgradeTower(row,col)){
                            JOptionPane.showMessageDialog(null,"NOT ENOUGH COINS");
                        }
                    }
                    return;
                }
                //chuot trai place tower
                String selected = hudPanel.getSelectedTower();
                if(selected==null) return;
                Tower tower = TowerFactory.create(selected,row,col);
                if(gameEngine.placeTower(tower,row,col)){
                    hudPanel.clearSelectedTower();
                }
            }
        });
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        //refresh ui 60fps
         timer = new Timer(1000/GameConstants.FPS,e->{
            gamePanel.refresh();
            hudPanel.refresh();
            if(gameEngine.isGameOver()&&!gameOverShown){
                gameOverShown=true;
                timer.stop();
                showGameOver();
            }
            //check win
             if(gameEngine.isWaveCleared()&&!gameOverShown){
                 gameOverShown=true;
                 timer.stop();
                 showWin();
             }
        });
        //overlay start
        JPanel startOverLay=new JPanel(new GridLayout());
        startOverLay.setOpaque(false);
        startOverLay.setBounds(0,0,GameConstants.SCREEN_WIDTH,GameConstants.SCREEN_HEIGHT);

        JButton startBtn = new JButton("START GAME");
        startBtn.setFont(new Font("Arial", Font.BOLD, 30));
        startBtn.setBackground(new Color(50, 150, 50));
        startBtn.setForeground(Color.WHITE);
        startBtn.setPreferredSize(new Dimension(250, 80));
        startBtn.addActionListener(e -> {
            gamePanel.remove(startOverLay);
            gamePanel.revalidate();
            gamePanel.repaint();
            timer.start();
            if(mode != GameConstants.MODE_ONLINE){
                gameEngine.start();
                startTime = System.currentTimeMillis();
            }
        });
        startOverLay.add(startBtn);
        gamePanel.setLayout(null);
        gamePanel.add(startOverLay);
        gamePanel.revalidate();
    }
    private void showGameOver(){
        //hien overlay game over
        String diffStr = difficulty == GameConstants.DIFF_HARD ? "HARD" : difficulty == GameConstants.DIFF_NORMAL ? "NORMAL" : "EASY";
        DBManager.saveMatch(playerID,getModeString(),mapID,gameEngine.getCurrentWave(),"LOSE",diffStr);
        int gemsReward = 0;
        if(mode==GameConstants.MODE_INF){gemsReward=gameEngine.getCurrentWave()*3;}
        else if(mode== GameConstants.MODE_ONLINE){gemsReward = 25;}

        DBManager.addGems(playerID,gemsReward);
        int choice = JOptionPane.showOptionDialog(
                this,
                "GAME OVER!\nWave: " + gameEngine.getCurrentWave() + "/15",
                "GAME OVER",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Back to Menu"},
                "Back to Menu"
        );
        if(choice==0){
            gameEngine.stop();
            dispose();
            JFrame frame = new JFrame("Tower Defense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MenuPanel(frame,playerID));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
    private String getModeString(){
        switch(mode){
            case GameConstants.MODE_STORY: return "STORY";
            case GameConstants.MODE_ONLINE: return "ONLINE";
            case GameConstants.MODE_INF: return "INFINITY";
            default:return "STORY";
        }
    }
    private void showWin(){
        int timeScore = (int)((System.currentTimeMillis()-startTime)/1000);
        String diffStr = difficulty == GameConstants.DIFF_HARD ? "HARD" : difficulty == GameConstants.DIFF_NORMAL ? "NORMAL" : "EASY";
        DBManager.completeMap(playerID, mapID, getModeString(), diffStr);
        DBManager.saveMatch(playerID,getModeString(),mapID,timeScore,"WIN",diffStr);
        //them gems khi thang
        int gemsReward;
        if(mode== GameConstants.MODE_ONLINE){gemsReward = 100;}
        else{
            if(difficulty == GameConstants.DIFF_HARD){gemsReward = 70;}
            else if(difficulty == GameConstants.DIFF_NORMAL){gemsReward = 60;}
            else{gemsReward = 50;}
        }
        DBManager.addGems(playerID,gemsReward);
        int choice = JOptionPane.showOptionDialog(
                this,
                "YOU WIN!\nWave: " + gameEngine.getCurrentWave() + "/15\n",
                "VICTORY",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Back to Menu"},"Back to Menu"
        );
        if(choice==0){
            gameEngine.stop();
            dispose();
            JFrame frame = new JFrame("Tower Defense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MenuPanel(frame,playerID));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
}
