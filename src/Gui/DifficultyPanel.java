import javax.swing.*;
import java.awt.*;
import java.util.List;
public class DifficultyPanel extends JPanel {
    private int playerID;
    private int mapID;
    private int mode;
    public DifficultyPanel(JFrame frame,int mode,int mapID,int playerID) {
        this.playerID = playerID;
        this.mapID = mapID;
        this.mode = mode;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;
        //title
        JLabel title = new JLabel("DIFFICULTY SELECTION",SwingConstants.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,30));
        title.setForeground(Color.WHITE);
        c.gridy = 0;
        add(title,c);
        //select mode
        String[] diffs ={"EASY","NORMAL","HARD"};
        int[] diffConsts ={GameConstants.DIFF_EASY,GameConstants.DIFF_NORMAL,GameConstants.DIFF_HARD};
        Color[] colors ={Color.GREEN,Color.ORANGE,Color.RED};

        for(int i=0;i<diffs.length;i++){
            final int diff = diffConsts[i];
            final String diffStr = diffs[i];
            JButton button = new JButton(diffs[i]);
            boolean unlocked = DBManager.isMapUnlocked(playerID,mapID,mode == GameConstants.MODE_INF ? "INFINITY" :"STORY",diffStr);

            button.setFont(new Font("Arial",Font.BOLD,20));
            button.setBackground(unlocked ? colors[i] : Color.DARK_GRAY);
            button.setForeground(Color.WHITE);
            button.setEnabled(unlocked);
            if(!unlocked) button.setText(diffs[i]+"[LOCKED]");

            c.gridy=i+1;
            button.addActionListener(e ->startGame(frame,mode,diff,mapID,playerID));
            add(button,c);
        }
        //back
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial",Font.BOLD,20));
        backBtn.setBackground(Color.BLACK);
        backBtn.setForeground(Color.WHITE);
        c.gridy=4;
        backBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new MapSelectPanel(frame,mode,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(backBtn,c);
    }
    private void startGame(JFrame frame,int mode,int diff,int mapId,int playerID){
        int[][] grid=XMLLoader.loadMap("maps.xml",mapId);
        Map map = new Map(grid);
        GameEngine gameEngine = new GameEngine(map,diff,mapId);
        List<WaveConfig> waves =XMLLoader.loadWaves("waves.xml",mapId);
        WaveManager waveManager = new WaveManager(waves,gameEngine);
        if(mode == GameConstants.MODE_INF){
            waveManager.setInifinityMode(true);
        }
        gameEngine.setWaveManager(waveManager);
        frame.dispose();
        new GameWindown(gameEngine,playerID,mode,mapId,diff,null);
    }
}
