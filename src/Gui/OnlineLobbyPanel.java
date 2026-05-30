import javax.swing.*;
import java.awt.*;
import java.util.List;
public class OnlineLobbyPanel extends JPanel {
    private static final Color BG_DARK = new Color(15,25,50);
    private static final Color GOLD = new Color(255,200,50);
    private static final Color BORDER_CLR = new Color(50,100,160);
    public OnlineLobbyPanel(JFrame frame,int playerID) {
        setLayout(new GridBagLayout());
        setBackground(BG_DARK);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ONLINE PvP",SwingConstants.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,35));
        title.setForeground(GOLD);
        c.gridy = 0; c.gridwidth=2;
        add(title,c);
        // chon map
        JLabel mapLabel = new JLabel("Select Map: ");
        mapLabel.setFont(new Font("Arial",Font.BOLD,16));
        mapLabel.setForeground(Color.WHITE);
        c.gridy = 1; c.gridwidth=1;c.gridx=0;
        add(mapLabel,c);

        String [] maps ={"Forest Map","Swamp Crossing","Dark Castle"};
        JComboBox<String> mapComboBox = new JComboBox<>(maps);
        mapComboBox.setFont(new Font("Arial",Font.PLAIN,16));
        mapComboBox.setForeground(Color.WHITE);
        mapComboBox.setBackground(new Color(10,20,40));
        c.gridx=1;
        add(mapComboBox,c);
        //nut host
        JButton hostBtn = new JButton("HOST");
        hostBtn.setFont(new Font("Arial",Font.BOLD,20));
        hostBtn.setBackground(new Color(50,150,50));
        hostBtn.setForeground(Color.WHITE);
        c.gridy = 2; c.gridwidth=2;
        hostBtn.addActionListener(e -> {
            //start server tren thread rieng
            int mapID = mapComboBox.getSelectedIndex()+1;
            new Thread(()-> new Network().start()).start();
            JOptionPane.showMessageDialog(frame,"Server started!\n Your IP: "+getLocalIP()+"\n Waiting for player....");
            startOnlineGame(frame,playerID,"localhost",mapID);
        });
        add(hostBtn,c);
        //nut join va ip field
        JLabel ipLabel = new JLabel("HOST IP");
        ipLabel.setForeground(Color.WHITE);
        ipLabel.setFont(new Font("Arial",Font.BOLD,16));
        c.gridy = 3; c.gridwidth=1;
        add(ipLabel,c);

        JTextField ipField = new JTextField("",15);
        ipField.setToolTipText("Enter Host IP");
        ipField.setFont(new Font("Arial",Font.PLAIN,16));
        ipField.setForeground(Color.WHITE);
        ipField.setBackground(new Color(10,20,40));
        ipField.setCaretColor(Color.WHITE);
        ipField.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        c.gridx=1;
        add(ipField,c);

        JButton joinBtn = new JButton("JOIN");
        joinBtn.setFont(new Font("Arial",Font.BOLD,20));
        joinBtn.setBackground(new Color(30,100,200));
        joinBtn.setForeground(Color.WHITE);
        c.gridy = 4; c.gridwidth=2;c.gridx=0;
        joinBtn.addActionListener(e -> {
            String ip = ipField.getText();
            if(ip.isEmpty()){
                JOptionPane.showMessageDialog(frame,"Please enter host IP");
                return;
            }
            int mapID = mapComboBox.getSelectedIndex()+1;
            startOnlineGame(frame,playerID,ip,mapID);
        });
        add(joinBtn,c);
        //back
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial",Font.BOLD,16));
        backBtn.setBackground(new Color(30,50,90));
        backBtn.setForeground(Color.WHITE);
        c.gridy = 5;
        backBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setContentPane(new MenuPanel(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        add(backBtn,c);
    }
    private void  startOnlineGame(JFrame frame,int playerID,String ip,int mapID) {
        int[][] grid = XMLLoader.loadMap("maps.xml",mapID);
        Map map = new Map(grid);
        GameEngine gameEngine = new GameEngine(map,GameConstants.DIFF_NORMAL,mapID);
        GameClient gameClient = new GameClient(gameEngine,ip,playerID);

        List<WaveConfig> waves = XMLLoader.loadWaves("waves.xml",mapID);
        WaveManager waveManager = new WaveManager(waves,gameEngine);
        gameEngine.setWaveManager(waveManager);
        gameClient.setWaveManager(waveManager);

        frame.dispose();
        new GameWindown(gameEngine,playerID,GameConstants.MODE_ONLINE,mapID,GameConstants.DIFF_NORMAL,gameClient);
        new Thread(()-> gameClient.connect()).start();
    }
    private String getLocalIP(){
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        }catch(Exception e){
            return "localhost";
        }
    }
}
