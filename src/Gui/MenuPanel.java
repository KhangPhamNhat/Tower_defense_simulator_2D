import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private int playerID;
    public MenuPanel(JFrame frame,int playerID) {
        this.playerID = playerID;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        //title
        JLabel title = new JLabel("TOWER DEFENSE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.LIGHT_GRAY);
        c.gridx = 0;c.gridy = 0;
        add(title,c);
        //Story mode
        JButton storyBtn = new JButton("STORY MODE");
        storyBtn.setFont(new Font("Arial", Font.BOLD, 20));
        storyBtn.setForeground(Color.WHITE);
        storyBtn.setBackground(Color.DARK_GRAY);
        c.gridy=1;
        storyBtn.addActionListener(e -> startGame(frame,GameConstants.MODE_STORY));
        add(storyBtn,c);
        //Inf mode
        JButton infBtn = new JButton("INFINITY MODE");
        infBtn.setFont(new Font("Arial", Font.BOLD, 20));
        infBtn.setForeground(Color.WHITE);
        infBtn.setBackground(Color.DARK_GRAY);
        c.gridy=2;
        infBtn.addActionListener(e -> startGame(frame,GameConstants.MODE_INF));
        add(infBtn,c);
        //Online mode
        JButton onlineBtn = new JButton("ONLINE MODE");
        onlineBtn.setFont(new Font("Arial", Font.BOLD, 20));
        onlineBtn.setForeground(Color.WHITE);
        onlineBtn.setBackground(Color.DARK_GRAY);
        c.gridy=3;
        onlineBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setContentPane(new OnlineLobbyPanel(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(onlineBtn,c);
        //shop
        JButton shopBtn = new JButton("SHOP");
        shopBtn.setFont(new Font("Arial", Font.BOLD, 20));
        shopBtn.setForeground(Color.WHITE);
        shopBtn.setBackground(Color.DARK_GRAY);
        c.gridy=4;
        shopBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setContentPane(new ShopScreen(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(shopBtn,c);
        //leaderboard
        JButton leaderboardBtn = new JButton("LEADERBOARD");
        leaderboardBtn.setFont(new Font("Arial", Font.BOLD, 20));
        leaderboardBtn.setForeground(Color.WHITE);
        leaderboardBtn.setBackground(Color.DARK_GRAY);
        c.gridy=5;
        leaderboardBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setContentPane(new LeaderboardScreen(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(leaderboardBtn,c);
        //exit
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 20));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(Color.DARK_GRAY);
        c.gridy=6;
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn,c);
    }
    private void startGame(JFrame frame,int mode){
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new MapSelectPanel(frame, mode,playerID));
        frame.revalidate();
        frame.repaint();
    }
}
