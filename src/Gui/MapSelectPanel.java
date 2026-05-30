import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapSelectPanel extends JPanel {
    private int playerID;
    public MapSelectPanel(JFrame frame, int mode,int playerID) {
        this.playerID = playerID;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill=GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("MAP SELECTION",SwingConstants.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,30));
        title.setForeground(Color.WHITE);
        c.gridy=0;
        add(title,c);
        String[] maps = {"FOREST PATH", "SWAMP CROSSING", "DARK CASTLE"};
        String[] difficulty = {"EASY", "NORMAL", "HARD"};
        for(int i=0;i<maps.length;i++) {
            final int mapId=i+1;
            JButton btn = new JButton("Map "+mapId+ "-"+maps[i]);
            btn.setFont(new Font("Arial",Font.BOLD,20));
            btn.setForeground(Color.WHITE);
            //check unlock
            boolean unlocked = DBManager.isMapUnlocked(playerID,mapId,mode==GameConstants.MODE_INF ? "INFINITY":"STORY","EASY");
            if(unlocked) {
                btn.setBackground(Color.BLACK);
                btn.addActionListener(e -> {
                    frame.getContentPane().removeAll();
                    frame.setContentPane(new DifficultyPanel(frame,mode,mapId,playerID));
                    frame.revalidate();
                    frame.repaint();
                });
            }
            else{
                btn.setBackground(Color.DARK_GRAY);
                btn.setText("Map "+mapId+ "-"+maps[i]+"[LOCKED]");
                btn.setEnabled(false);
            }
            c.gridy=i+1;
            add(btn,c);
        }
        //back
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial",Font.BOLD,20));
        backBtn.setBackground(Color.BLACK);
        backBtn.setForeground(Color.WHITE);
        c.gridy=4;
        backBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new MenuPanel(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(backBtn,c);
    }

}
